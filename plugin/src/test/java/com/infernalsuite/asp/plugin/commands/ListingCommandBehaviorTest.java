package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.api.SlimeNMSBridge;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.commands.parser.NamedSlimeLoader;
import com.infernalsuite.asp.plugin.commands.sub.DSListCmd;
import com.infernalsuite.asp.plugin.commands.sub.WorldListCmd;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Listing command behavior")
class ListingCommandBehaviorTest extends AbstractCommandTest {

    @Nested
    @DisplayName("WorldListCmd")
    class WorldListCmdTests {

        @Test
        @DisplayName("should reject non-numeric pages")
        void shouldRejectNonNumericPages() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                WorldListCmd command = new WorldListCmd(commandManager);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                org.mockito.Mockito.lenient().when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of());
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.listWorlds(source(sender), new String[]{"swm", "list", "oops"}, null, null)
                );

                assertTrue(plainText(exception.getComponent()).contains("'oops' is not a valid number."));
            }
        }

        @Test
        @DisplayName("should fail when no worlds are configured or loaded")
        void shouldFailWhenNoWorldsAreConfiguredOrLoaded() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                WorldListCmd command = new WorldListCmd(commandManager);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of());
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.listWorlds(source(sender), new String[]{"swm", "list"}, null, null)
                );

                assertTrue(plainText(exception.getComponent()).contains("There are no worlds configured."));
            }
        }

        @Test
        @DisplayName("should render loaded and configured worlds")
        void shouldRenderLoadedAndConfiguredWorlds() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
                 MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
                WorldListCmd command = new WorldListCmd(commandManager);
                World loadedWorld = mock(World.class);
                when(loadedWorld.getName()).thenReturn("loaded-srf");
                World nonSlimeWorld = mock(World.class);
                when(nonSlimeWorld.getName()).thenReturn("loaded-vanilla");
                SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);

                WorldData configuredOnly = new WorldData();
                Map<String, WorldData> worlds = new HashMap<>();
                worlds.put("configured-only", configuredOnly);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(worlds);
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(loadedWorld, nonSlimeWorld));
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
                when(bridge.getInstance(loadedWorld)).thenReturn(mock(SlimeWorldInstance.class));
                when(bridge.getInstance(nonSlimeWorld)).thenReturn(null);

                command.listWorlds(source(sender), new String[]{"swm", "list"}, null, null);

                ArgumentCaptor<Component> headerCaptor = ArgumentCaptor.forClass(Component.class);
                verify(sender).sendMessage(headerCaptor.capture());
                assertTrue(plainText(headerCaptor.getValue()).contains("World list [1/1]:"));

                ArgumentCaptor<String> lineCaptor = ArgumentCaptor.forClass(String.class);
                verify(sender, org.mockito.Mockito.times(3)).sendMessage(lineCaptor.capture());
                List<String> lines = lineCaptor.getAllValues();
                assertTrue(lines.stream().anyMatch(line -> line.contains("configured-only")));
                assertTrue(lines.stream().anyMatch(line -> line.contains("loaded-srf")));
                assertTrue(lines.stream().anyMatch(line -> line.contains("loaded-vanilla")));
            }
        }

        @Test
        @DisplayName("should list only slime worlds when requested")
        void shouldListOnlySlimeWorldsWhenRequested() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
                 MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
                WorldListCmd command = new WorldListCmd(commandManager);
                World slimeWorld = mock(World.class);
                when(slimeWorld.getName()).thenReturn("loaded-srf");
                World vanillaWorld = mock(World.class);
                when(vanillaWorld.getName()).thenReturn("loaded-vanilla");
                SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(slimeWorld, vanillaWorld));
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
                when(bridge.getInstance(slimeWorld)).thenReturn(mock(SlimeWorldInstance.class));
                when(bridge.getInstance(vanillaWorld)).thenReturn(null);

                command.listWorlds(source(sender), new String[]{"swm", "list", "slime"}, "slime", null);

                ArgumentCaptor<String> lineCaptor = ArgumentCaptor.forClass(String.class);
                verify(sender).sendMessage(any(Component.class));
                verify(sender, org.mockito.Mockito.times(1)).sendMessage(lineCaptor.capture());
                assertTrue(lineCaptor.getValue().contains("loaded-srf"));
                assertTrue(!lineCaptor.getValue().contains("loaded-vanilla"));
            }
        }

        @Test
        @DisplayName("should reject pages below one")
        void shouldRejectPagesBelowOne() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                WorldListCmd command = new WorldListCmd(commandManager);
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of());

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.listWorlds(source(sender), new String[]{"swm", "list", "0"}, null, "0")
                );

                assertTrue(plainText(exception.getComponent()).contains("'0' is not a valid number."));
            }
        }

        @Test
        @DisplayName("should reject pages beyond the available list")
        void shouldRejectPagesBeyondTheAvailableList() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
                 MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
                WorldListCmd command = new WorldListCmd(commandManager);
                World loadedWorld = mock(World.class);
                when(loadedWorld.getName()).thenReturn("loaded-srf");
                SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
                WorldsConfig worldsConfig = mock(WorldsConfig.class);
                when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(loadedWorld));
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
                when(bridge.getInstance(loadedWorld)).thenReturn(mock(SlimeWorldInstance.class));

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.listWorlds(source(sender), new String[]{"swm", "list", "2"}, null, "2")
                );

                assertTrue(plainText(exception.getComponent()).contains("There is only 1 page"));
            }
        }
    }

    @Nested
    @DisplayName("DSListCmd")
    class DSListCmdTests {

        @Test
        @DisplayName("should reject pages below one")
        void shouldRejectPagesBelowOne() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                DSListCmd command = new DSListCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.listWorlds(source(sender), new NamedSlimeLoader("file", mock(SlimeLoader.class)), 0)
                );

                assertTrue(plainText(exception.getComponent()).contains("Page number must be greater than 0"));
            }
        }

        @Test
        @DisplayName("should fail when the loader has no stored worlds")
        void shouldFailWhenTheLoaderHasNoStoredWorlds() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                DSListCmd command = new DSListCmd(commandManager);
                SlimeLoader loader = mock(SlimeLoader.class);
                when(loader.listWorlds()).thenReturn(List.of());

                CompletionException exception = assertThrows(
                        CompletionException.class,
                        () -> command.listWorlds(source(sender), new NamedSlimeLoader("file", loader), 1).join()
                );

                MessageCommandException messageException = assertMessageException(exception);
                assertTrue(plainText(messageException.getComponent()).contains("There are no worlds stored in data source file."));
            }
        }

        @Test
        @DisplayName("should wrap IO failures while listing worlds")
        void shouldWrapIoFailuresWhileListingWorlds() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                DSListCmd command = new DSListCmd(commandManager);
                SlimeLoader loader = mock(SlimeLoader.class);
                when(loader.listWorlds()).thenThrow(new IOException("broken"));

                CompletionException exception = assertThrows(
                        CompletionException.class,
                        () -> command.listWorlds(source(sender), new NamedSlimeLoader("file", loader), 1).join()
                );

                MessageCommandException messageException = assertMessageException(exception);
                assertTrue(plainText(messageException.getComponent()).contains("Failed to load world list"));
            }
        }

        @Test
        @DisplayName("should reject pages beyond the available world list")
        void shouldRejectPagesBeyondTheAvailableWorldList() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline()) {
                DSListCmd command = new DSListCmd(commandManager);
                SlimeLoader loader = mock(SlimeLoader.class);
                when(loader.listWorlds()).thenReturn(List.of("one", "two", "three"));

                CompletionException exception = assertThrows(
                        CompletionException.class,
                        () -> command.listWorlds(source(sender), new NamedSlimeLoader("file", loader), 2).join()
                );

                MessageCommandException messageException = assertMessageException(exception);
                assertTrue(plainText(messageException.getComponent()).contains("There is only 1 page"));
            }
        }

        @Test
        @DisplayName("should render a paginated sorted world list and mark loaded worlds")
        void shouldRenderAPaginatedSortedWorldListAndMarkLoadedWorlds() throws Exception {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
                DSListCmd command = new DSListCmd(commandManager);
                SlimeLoader loader = mock(SlimeLoader.class);
                World loadedWorld = mock(World.class);
                SlimeWorldInstance slimeWorld = mock(SlimeWorldInstance.class);
                SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
                when(loader.listWorlds()).thenReturn(new ArrayList<>(List.of("zeta", "alpha", "epsilon", "beta", "delta", "gamma")));
                when(slimeWorld.getLoader()).thenReturn(loader);

                bukkit.when(() -> Bukkit.getWorld("alpha")).thenReturn(null);
                bukkit.when(() -> Bukkit.getWorld("beta")).thenReturn(null);
                bukkit.when(() -> Bukkit.getWorld("delta")).thenReturn(null);
                bukkit.when(() -> Bukkit.getWorld("epsilon")).thenReturn(null);
                bukkit.when(() -> Bukkit.getWorld("gamma")).thenReturn(loadedWorld);
                bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
                when(bridge.getInstance(loadedWorld)).thenReturn(slimeWorld);

                command.listWorlds(source(sender), new NamedSlimeLoader("file", loader), 1).join();

                ArgumentCaptor<Component> componentCaptor = ArgumentCaptor.forClass(Component.class);
                verify(sender, org.mockito.Mockito.times(6)).sendMessage(componentCaptor.capture());
                List<String> messages = componentCaptor.getAllValues().stream().map(ListingCommandBehaviorTest::plainText).toList();

                assertTrue(messages.get(0).contains("World list [1/2]:"));
                assertTrue(messages.stream().anyMatch(message -> message.contains("alpha")));
                assertTrue(messages.stream().anyMatch(message -> message.contains("beta")));
                assertTrue(messages.stream().anyMatch(message -> message.contains("delta")));
                assertTrue(messages.stream().anyMatch(message -> message.contains("epsilon")));
                assertTrue(messages.stream().anyMatch(message -> message.contains("gamma")));
            }
        }

    }

    private static MockedStatic<CompletableFuture> mockRunAsyncInline() {
        MockedStatic<CompletableFuture> completableFuture = mockStatic(CompletableFuture.class, CALLS_REAL_METHODS);
        completableFuture.when(() -> CompletableFuture.runAsync(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            try {
                runnable.run();
                return CompletableFuture.completedFuture(null);
            } catch (Throwable throwable) {
                return CompletableFuture.failedFuture(throwable);
            }
        });
        return completableFuture;
    }
}
