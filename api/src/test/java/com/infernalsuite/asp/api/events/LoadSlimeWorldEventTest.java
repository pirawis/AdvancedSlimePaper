package com.infernalsuite.asp.api.events;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("LoadSlimeWorldEvent")
class LoadSlimeWorldEventTest {

    @Test
    @DisplayName("should expose the provided slime world and static handlers")
    void shouldExposeTheProvidedSlimeWorldAndStaticHandlers() {
        SlimeWorldInstance slimeWorld = mock(SlimeWorldInstance.class);

        LoadSlimeWorldEvent event = new LoadSlimeWorldEvent(slimeWorld);

        assertSame(slimeWorld, event.getSlimeWorld());
        assertSame(LoadSlimeWorldEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("should reject null slime worlds")
    void shouldRejectNullSlimeWorlds() {
        assertThrows(NullPointerException.class, () -> new LoadSlimeWorldEvent(null));
    }

    @Test
    @DisplayName("getHandlerList should return the shared handler list")
    void getHandlerListShouldReturnTheSharedHandlerList() {
        HandlerList handlers = LoadSlimeWorldEvent.getHandlerList();

        assertSame(handlers, LoadSlimeWorldEvent.getHandlerList());
    }
}
