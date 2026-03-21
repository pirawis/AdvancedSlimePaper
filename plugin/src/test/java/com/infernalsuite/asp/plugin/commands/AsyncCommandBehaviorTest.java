package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.plugin.SWPlugin;
import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.commands.parser.NamedSlimeLoader;
import com.infernalsuite.asp.plugin.commands.parser.NamedWorldData;
import com.infernalsuite.asp.plugin.commands.sub.CloneWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.DeleteWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.ImportWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.LoadTemplateWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.LoadWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.ReloadConfigCmd;
import com.infernalsuite.asp.plugin.commands.sub.UnloadWorldCmd;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import com.infernalsuite.asp.plugin.loader.LoaderManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Async plugin command behavior")
class AsyncCommandBehaviorTest extends AbstractCommandTest {

    @Nested
    @DisplayName("ReloadConfigCmd")
    class ReloadConfigCmdTests {

        @Test
        @DisplayName("should reload config and acknowledge success")
        void shouldReloadConfigAndAcknowledgeSuccess() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                ReloadConfigCmd command = new ReloadConfigCmd(commandManager);

                assertDoesNotThrow(() -> command.reloadConfig(source(sender)).join());

                configManager.verify(ConfigManager::initialize);
                assertMessageSent(sender, "Config reloaded.");
            }
        }

        @Test
        @DisplayName("should wrap reload failures in a message command exception")
        void shouldWrapReloadFailuresInAMessageCommandException() {
            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::initialize).thenThrow(new IOException("boom"));
                ReloadConfigCmd command = new ReloadConfigCmd(commandManager);

                MessageCommandException exception = joinMessageException(command.reloadConfig(source(sender)));

                assertTrue(plainText(exception.getComponent()).contains("Failed to reload the config file"));
            }
        }
    }

    @Nested
    @DisplayName("DeleteWorldCmd")
    class DeleteWorldCmdTests {

        @Test
        @DisplayName("should require confirmation before deleting a world")
        void shouldRequireConfirmationBeforeDeletingAWorld() {
            when(sender.getName()).thenReturn("console");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                DeleteWorldCmd command = new DeleteWorldCmd(commandManager);

                command.deleteWorld(source(sender), new String[]{"arena", "file"}, "arena", namedLoader("file")).join();

                assertMessageSent(sender, "If you are sure you want to continue");
            }
        }

        @Test
        @DisplayName("should delete the world after the same command is repeated")
        void shouldDeleteTheWorldAfterTheSameCommandIsRepeated() throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Map<String, WorldData> worlds = new HashMap<>();
            worlds.put("arena", new WorldData());
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                DeleteWorldCmd command = new DeleteWorldCmd(commandManager);
                String[] args = {"arena", "file"};

                command.deleteWorld(source(sender), args, "arena", new NamedSlimeLoader("file", loader)).join();
                clearInvocations(sender);

                assertDoesNotThrow(() -> command.deleteWorld(source(sender), args, "arena", new NamedSlimeLoader("file", loader)).join());

                verify(loader).deleteWorld("arena");
                verify(worldsConfig).save();
                assertFalse(worlds.containsKey("arena"));
                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "deleted in");
            }
        }

        @Test
        @DisplayName("should surface unknown world errors from the loader")
        void shouldSurfaceUnknownWorldErrorsFromTheLoader() throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);

            doThrow(new com.infernalsuite.asp.api.exceptions.UnknownWorldException("arena"))
                    .when(loader).deleteWorld("arena");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                DeleteWorldCmd command = new DeleteWorldCmd(commandManager);
                String[] args = {"arena", "file"};

                command.deleteWorld(source(sender), args, "arena", new NamedSlimeLoader("file", loader)).join();
                MessageCommandException exception = joinMessageException(
                        command.deleteWorld(source(sender), args, "arena", new NamedSlimeLoader("file", loader))
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("does not contain any world called arena"));
            }
        }
    }

    @Nested
    @DisplayName("LoadWorldCmd")
    class LoadWorldCmdTests {

        @Test
        @DisplayName("should load a configured world and report success")
        void shouldLoadAConfiguredWorldAndReportSuccess() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(loader);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                when(asp.readWorld(eq(loader), eq("arena"), eq(false), any(SlimePropertyMap.class))).thenReturn(slimeWorld);

                LoadWorldCmd command = new LoadWorldCmd(commandManager);

                assertDoesNotThrow(() -> command.onCommand(source(sender), new NamedWorldData("arena", worldData)).join());

                verify(asp).loadWorld(slimeWorld, true);
                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "Loading world arena");
                assertMessageSent(sender, "loaded and generated");
            }
        }

        @Test
        @DisplayName("should report invalid data source errors")
        void shouldReportInvalidDataSourceErrors() {
            LoaderManager loaderManager = mockLoaderManager();
            WorldData worldData = new WorldData();
            worldData.setDataSource("broken");
            when(loaderManager.getLoader("broken")).thenReturn(null);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadWorldCmd command = new LoadWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.onCommand(source(sender), new NamedWorldData("arena", worldData))
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("invalid data source broken"));
            }
        }
    }

    @Nested
    @DisplayName("LoadTemplateWorldCmd")
    class LoadTemplateWorldCmdTests {

        @Test
        @DisplayName("should create a temporary world from a template")
        void shouldCreateATemporaryWorldFromATemplate() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld templateWorld = mock(SlimeWorld.class);
            SlimeWorld clonedWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(loader);
            when(templateWorld.clone("arena")).thenReturn(clonedWorld);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                when(asp.readWorld(eq(loader), eq("template"), eq(false), any(SlimePropertyMap.class))).thenReturn(templateWorld);

                LoadTemplateWorldCmd command = new LoadTemplateWorldCmd(commandManager);

                assertDoesNotThrow(() -> command.onCommand(source(sender), new NamedWorldData("template", worldData), "arena").join());

                verify(asp).loadWorld(clonedWorld, true);
                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "Creating world arena using template");
                assertMessageSent(sender, "loaded and generated");
            }
        }

        @Test
        @DisplayName("should send an error message when the configured loader is invalid")
        void shouldSendAnErrorMessageWhenTheConfiguredLoaderIsInvalid() {
            LoaderManager loaderManager = mockLoaderManager();
            WorldData worldData = new WorldData();
            worldData.setDataSource("broken");
            when(loaderManager.getLoader("broken")).thenReturn(null);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadTemplateWorldCmd command = new LoadTemplateWorldCmd(commandManager);

                assertDoesNotThrow(() -> command.onCommand(source(sender), new NamedWorldData("template", worldData), "arena").join());

                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "invalid data source broken");
            }
        }
    }

    @Nested
    @DisplayName("CloneWorldCmd")
    class CloneWorldCmdTests {

        @Test
        @DisplayName("should clone a template world into another data source")
        void shouldCloneATemplateWorldIntoAnotherDataSource() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            SlimeWorld templateWorld = mock(SlimeWorld.class);
            SlimeWorld clonedWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(templateWorld.clone("arena-copy", targetLoader)).thenReturn(clonedWorld);

            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new HashMap<>();
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class))).thenReturn(templateWorld);

                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                assertDoesNotThrow(() -> command.cloneWorld(
                        source(sender),
                        new NamedWorldData("template", worldData),
                        "arena-copy",
                        new NamedSlimeLoader("mysql", targetLoader)
                ).join());

                verify(asp).loadWorld(clonedWorld, true);
                verify(worldsConfig).save();
                assertEquals(worldData, worlds.get("arena-copy"));
                assertFalse(worldsInUse.contains("arena-copy"));
                assertMessageSent(sender, "Creating world arena-copy using template");
                assertMessageSent(sender, "loaded and generated");
            }
        }
    }

    @Nested
    @DisplayName("ImportWorldCmd")
    class ImportWorldCmdTests {

        @Test
        @DisplayName("should warn first and then import the world on confirmation")
        void shouldWarnFirstAndThenImportTheWorldOnConfirmation(@TempDir final Path tempDir) throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);

            Path worldPath = Files.createDirectory(tempDir.resolve("source-world"));
            String[] args = {worldPath.toString(), "file", "arena-copy"};

            SlimePropertyMap propertyMap = new SlimePropertyMap();
            propertyMap.setValue(SlimeProperties.ENVIRONMENT, "the_end");
            propertyMap.setValue(SlimeProperties.DIFFICULTY, "hard");
            propertyMap.setValue(SlimeProperties.ALLOW_MONSTERS, false);
            propertyMap.setValue(SlimeProperties.DRAGON_BATTLE, true);
            propertyMap.setValue(SlimeProperties.PVP, false);
            propertyMap.setValue(SlimeProperties.WORLD_TYPE, "flat");
            propertyMap.setValue(SlimeProperties.DEFAULT_BIOME, "minecraft:desert");
            propertyMap.setValue(SlimeProperties.SPAWN_X, 12);
            propertyMap.setValue(SlimeProperties.SPAWN_Y, 64);
            propertyMap.setValue(SlimeProperties.SPAWN_Z, -7);

            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            when(slimeWorld.getPropertyMap()).thenReturn(propertyMap);

            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new HashMap<>();
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                when(asp.readVanillaWorld(worldPath.toFile(), "arena-copy", loader)).thenReturn(slimeWorld);

                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), "arena-copy").join();
                assertMessageSent(sender, "If you are sure you want to continue");
                clearInvocations(sender);

                assertDoesNotThrow(() -> command.importWorld(
                        source(sender),
                        args,
                        worldPath.toString(),
                        new NamedSlimeLoader("file", loader),
                        "arena-copy"
                ).join());

                verify(asp).saveWorld(slimeWorld);
                verify(asp).loadWorld(slimeWorld, true);
                verify(worldsConfig).save();
                assertEquals("file", worlds.get("arena-copy").getDataSource());
                assertEquals("12, 64, -7", worlds.get("arena-copy").getSpawn());
                assertEquals("the_end", worlds.get("arena-copy").getEnvironment());
                assertEquals("hard", worlds.get("arena-copy").getDifficulty());
                assertEquals("flat", worlds.get("arena-copy").getWorldType());
                assertEquals("minecraft:desert", worlds.get("arena-copy").getDefaultBiome());
                assertTrue(worlds.get("arena-copy").isDragonBattle());
                assertFalse(worlds.get("arena-copy").isPvp());
                assertFalse(worlds.get("arena-copy").isAllowMonsters());
                assertMessageSent(sender, "imported successfully");
            }
        }
    }

    @Nested
    @DisplayName("UnloadWorldCmd")
    class UnloadWorldCmdTests {

        @Test
        @DisplayName("should unload worlds without players immediately")
        void shouldUnloadWorldsWithoutPlayersImmediately() {
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            World bukkitWorld = mock(World.class);
            when(slimeWorld.getName()).thenReturn("arena");
            when(bukkitWorld.getPlayers()).thenReturn(Collections.emptyList());

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(bukkitWorld);
                bukkit.when(() -> Bukkit.unloadWorld(bukkitWorld, true)).thenReturn(true);
                UnloadWorldCmd command = new UnloadWorldCmd(commandManager);

                command.unloadWorld(source(sender), slimeWorld);

                bukkit.verify(() -> Bukkit.unloadWorld(bukkitWorld, true));
                assertMessageSent(sender, "unloaded correctly");
            }
        }

        @Test
        @DisplayName("should move players away before attempting unload")
        void shouldMovePlayersAwayBeforeAttemptingUnload() {
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            World bukkitWorld = mock(World.class);
            World defaultWorld = mock(World.class);
            Player player = mock(Player.class);
            Block spawnBlock = mock(Block.class);
            Block aboveSpawnBlock = mock(Block.class);
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            SWPlugin pluginInstance = mock(SWPlugin.class);

            when(slimeWorld.getName()).thenReturn("arena");
            when(bukkitWorld.getPlayers()).thenReturn(List.of(player));
            when(player.teleportAsync(any(Location.class))).thenReturn(CompletableFuture.completedFuture(true));

            Location spawn = new Location(defaultWorld, 0, 60, 0);
            when(defaultWorld.getSpawnLocation()).thenReturn(spawn);
            when(defaultWorld.getBlockAt(any(Location.class))).thenReturn(spawnBlock);
            when(spawnBlock.getType()).thenReturn(Material.AIR);
            when(spawnBlock.getRelative(BlockFace.UP)).thenReturn(aboveSpawnBlock);
            when(aboveSpawnBlock.getType()).thenReturn(Material.AIR);

            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
            }).when(scheduler).runTask(any(), any(Runnable.class));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<SWPlugin> pluginStatic = mockStatic(SWPlugin.class)) {
                pluginStatic.when(SWPlugin::getInstance).thenReturn(pluginInstance);
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(bukkitWorld);
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(defaultWorld));
                bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
                bukkit.when(() -> Bukkit.unloadWorld(bukkitWorld, true)).thenReturn(false);
                UnloadWorldCmd command = new UnloadWorldCmd(commandManager);

                command.unloadWorld(source(sender), slimeWorld);

                verify(player).teleportAsync(any(Location.class));
                assertMessageSent(sender, "Failed to unload world arena");
            }
        }
    }

    private LoaderManager mockLoaderManager() {
        LoaderManager loaderManager = mock(LoaderManager.class);
        when(plugin.getLoaderManager()).thenReturn(loaderManager);
        return loaderManager;
    }

    private NamedSlimeLoader namedLoader(final String name) {
        return new NamedSlimeLoader(name, mock(SlimeLoader.class));
    }

    private MockedStatic<Bukkit> mockPrimaryThreadBukkit() {
        MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
        return bukkit;
    }

    private MockedStatic<CompletableFuture> mockRunAsyncInline() {
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

    private static MessageCommandException joinMessageException(final CompletableFuture<?> future) {
        return assertMessageException(assertThrows(Throwable.class, future::join));
    }

    private static void assertMessageSent(final CommandSender sender, final String expectedText) {
        ArgumentCaptor<Component> messageCaptor = ArgumentCaptor.forClass(Component.class);
        verify(sender, atLeastOnce()).sendMessage(messageCaptor.capture());
        assertTrue(
                messageCaptor.getAllValues().stream()
                        .map(AbstractCommandTest::plainText)
                        .anyMatch(message -> message.contains(expectedText)),
                () -> "Expected any message to contain '" + expectedText + "' but got " + messageCaptor.getAllValues()
        );
    }
}
