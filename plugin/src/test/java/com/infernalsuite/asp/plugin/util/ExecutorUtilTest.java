package com.infernalsuite.asp.plugin.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@DisplayName("ExecutorUtil")
class ExecutorUtilTest {

    @Test
    @DisplayName("should run immediately on the primary thread")
    void shouldRunImmediatelyOnThePrimaryThread() {
        AtomicBoolean executed = new AtomicBoolean(false);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);

            ExecutorUtil.runSyncAndWait(mock(Plugin.class), () -> executed.set(true));

            assertTrue(executed.get());
        }
    }

    @Test
    @DisplayName("should schedule and wait off the primary thread")
    void shouldScheduleAndWaitOffThePrimaryThread() {
        Plugin plugin = mock(Plugin.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        AtomicBoolean executed = new AtomicBoolean(false);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTask(org.mockito.ArgumentMatchers.eq(plugin), org.mockito.ArgumentMatchers.any(Runnable.class));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(false);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            ExecutorUtil.runSyncAndWait(plugin, () -> executed.set(true));

            assertTrue(executed.get());
        }
    }

    @Test
    @DisplayName("should propagate runtime exceptions from scheduled work")
    void shouldPropagateRuntimeExceptionsFromScheduledWork() {
        Plugin plugin = mock(Plugin.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        RuntimeException expected = new RuntimeException("boom");

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(scheduler).runTask(org.mockito.ArgumentMatchers.eq(plugin), org.mockito.ArgumentMatchers.any(Runnable.class));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(false);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> ExecutorUtil.runSyncAndWait(plugin, () -> {
                        throw expected;
                    })
            );

            assertSame(expected, exception);
        }
    }
}
