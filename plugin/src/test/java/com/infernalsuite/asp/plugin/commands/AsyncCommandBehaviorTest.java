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
        @DisplayName("should reject worlds that are already loaded")
        void shouldRejectWorldsThatAreAlreadyLoaded() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(mock(World.class));
                LoadWorldCmd command = new LoadWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.onCommand(source(sender), new NamedWorldData("arena", worldData))
                );

                assertTrue(plainText(exception.getComponent()).contains("already loaded"));
            }
        }

        @Test
        @DisplayName("should reject worlds that are already being processed")
        void shouldRejectWorldsThatAreAlreadyBeingProcessed() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadWorldCmd command = new LoadWorldCmd(commandManager);
                worldsInUse.add("arena");

                try {
                    MessageCommandException exception = assertThrows(
                            MessageCommandException.class,
                            () -> command.onCommand(source(sender), new NamedWorldData("arena", worldData))
                    );

                    assertTrue(plainText(exception.getComponent()).contains("already being used on another command"));
                } finally {
                    worldsInUse.remove("arena");
                }
            }
        }

        @Test
        @DisplayName("should wrap world generation failures")
        void shouldWrapWorldGenerationFailures() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(loader);
            when(asp.readWorld(eq(loader), eq("arena"), eq(false), any(SlimePropertyMap.class))).thenReturn(slimeWorld);
            doThrow(new IllegalArgumentException("boom")).when(asp).loadWorld(slimeWorld, true);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadWorldCmd command = new LoadWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.onCommand(source(sender), new NamedWorldData("arena", worldData))
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("Failed to generate world arena: boom"));
            }
        }

        @Test
        @DisplayName("should surface unknown world errors")
        void shouldSurfaceUnknownWorldErrors() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader loader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(loader);
            when(asp.readWorld(eq(loader), eq("arena"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.UnknownWorldException("arena"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadWorldCmd command = new LoadWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.onCommand(source(sender), new NamedWorldData("arena", worldData))
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("world could not be found"));
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
        @DisplayName("should reject template and destination names that match")
        void shouldRejectTemplateAndDestinationNamesThatMatch() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadTemplateWorldCmd command = new LoadTemplateWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.onCommand(source(sender), new NamedWorldData("arena", worldData), "arena")
                );

                assertTrue(plainText(exception.getComponent()).contains("cannot be the same as the cloned world"));
            }
        }

        @Test
        @DisplayName("should send generation failures back to the sender")
        void shouldSendGenerationFailuresBackToTheSender() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld templateWorld = mock(SlimeWorld.class);
            SlimeWorld clonedWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(loader);
            when(templateWorld.clone("arena")).thenReturn(clonedWorld);
            when(asp.readWorld(eq(loader), eq("template"), eq(false), any(SlimePropertyMap.class))).thenReturn(templateWorld);
            doThrow(new IllegalArgumentException("boom")).when(asp).loadWorld(clonedWorld, true);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadTemplateWorldCmd command = new LoadTemplateWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(command.onCommand(
                        source(sender),
                        new NamedWorldData("template", worldData),
                        "arena"
                ));

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("Failed to generate world arena: boom"));
            }
        }

        @Test
        @DisplayName("should report corrupted template worlds to the sender")
        void shouldReportCorruptedTemplateWorldsToTheSender() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader loader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(loader);
            when(asp.readWorld(eq(loader), eq("template"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.CorruptedWorldException("template"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                LoadTemplateWorldCmd command = new LoadTemplateWorldCmd(commandManager);

                assertDoesNotThrow(() -> command.onCommand(
                        source(sender),
                        new NamedWorldData("template", worldData),
                        "arena"
                ).join());

                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "world seems to be corrupted");
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

        @Test
        @DisplayName("should clone using the source loader when no target loader is provided")
        void shouldCloneUsingTheSourceLoaderWhenNoTargetLoaderIsProvided() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            SlimeWorld templateWorld = mock(SlimeWorld.class);
            SlimeWorld clonedWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(templateWorld.clone("arena-copy", sourceLoader)).thenReturn(clonedWorld);

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
                        null
                ).join());

                verify(asp).loadWorld(clonedWorld, true);
                verify(worldsConfig).save();
                assertEquals(worldData, worlds.get("arena-copy"));
                assertFalse(worldsInUse.contains("arena-copy"));
            }
        }

        @Test
        @DisplayName("should reject worlds that are already being processed")
        void shouldRejectWorldsThatAreAlreadyBeingProcessed() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                CloneWorldCmd command = new CloneWorldCmd(commandManager);
                worldsInUse.add("arena-copy");

                try {
                    MessageCommandException exception = assertThrows(
                            MessageCommandException.class,
                            () -> command.cloneWorld(
                                    source(sender),
                                    new NamedWorldData("template", worldData),
                                    "arena-copy",
                                    null
                            )
                    );

                    assertTrue(plainText(exception.getComponent()).contains("already being used on another command"));
                } finally {
                    worldsInUse.remove("arena-copy");
                }
            }
        }

        @Test
        @DisplayName("should reject already loaded destination worlds")
        void shouldRejectAlreadyLoadedDestinationWorlds() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit()) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(mock(World.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("already loaded"));
            }
        }

        @Test
        @DisplayName("should reject template and destination names that match")
        void shouldRejectTemplateAndDestinationNamesThatMatch() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.cloneWorld(
                                source(sender),
                                new NamedWorldData("arena-copy", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("cannot be the same as the cloned world"));
            }
        }

        @Test
        @DisplayName("should surface duplicate stored world errors")
        void shouldSurfaceDuplicateStoredWorldErrors() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            SlimeWorld templateWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class))).thenReturn(templateWorld);
            when(templateWorld.clone("arena-copy", targetLoader))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException("arena-copy"));

            WorldsConfig worldsConfig = mock(WorldsConfig.class);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                new NamedSlimeLoader("mysql", targetLoader)
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("already a world called arena-copy stored"));
            }
        }

        @Test
        @DisplayName("should report corrupted template worlds")
        void shouldReportCorruptedTemplateWorlds() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.CorruptedWorldException("template", new IOException("broken")));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("world seems to be corrupted"));
            }
        }

        @Test
        @DisplayName("should report newer slime format errors while cloning")
        void shouldReportNewerSlimeFormatErrorsWhileCloning() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.NewerFormatException((byte) 99));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("newer version of the Slime Format"));
            }
        }

        @Test
        @DisplayName("should report generic loader io failures while cloning")
        void shouldReportGenericLoaderIoFailuresWhileCloning() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new IOException("disk error"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("Take a look at the server console"));
            }
        }

        @Test
        @DisplayName("should report missing template worlds while cloning")
        void shouldReportMissingTemplateWorldsWhileCloning() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.UnknownWorldException("template"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("world could not be found"));
            }
        }

        @Test
        @DisplayName("should report generic argument errors while cloning")
        void shouldReportGenericArgumentErrorsWhileCloning() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class)))
                    .thenThrow(new IllegalArgumentException("broken properties"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(mock(WorldsConfig.class));
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                null
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("Failed to load world template: broken properties"));
            }
        }

        @Test
        @DisplayName("should wrap generation failures")
        void shouldWrapGenerationFailures() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader sourceLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            SlimeWorld templateWorld = mock(SlimeWorld.class);
            SlimeWorld clonedWorld = mock(SlimeWorld.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(sourceLoader);
            when(asp.readWorld(eq(sourceLoader), eq("template"), eq(false), any(SlimePropertyMap.class))).thenReturn(templateWorld);
            when(templateWorld.clone("arena-copy", targetLoader)).thenReturn(clonedWorld);
            doThrow(new IllegalArgumentException("boom")).when(asp).loadWorld(clonedWorld, true);

            WorldsConfig worldsConfig = mock(WorldsConfig.class);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena-copy")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CloneWorldCmd command = new CloneWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.cloneWorld(
                                source(sender),
                                new NamedWorldData("template", worldData),
                                "arena-copy",
                                new NamedSlimeLoader("mysql", targetLoader)
                        )
                );

                assertFalse(worldsInUse.contains("arena-copy"));
                assertTrue(plainText(exception.getComponent()).contains("Failed to generate world arena-copy: boom"));
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

        @Test
        @DisplayName("should reject invalid world paths before touching the cache")
        void shouldRejectInvalidWorldPathsBeforeTouchingTheCache(@TempDir final Path tempDir) {
            Path filePath = assertDoesNotThrow(() -> Files.writeString(tempDir.resolve("not-a-dir.txt"), "data"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.importWorld(
                                source(sender),
                                new String[]{filePath.toString(), "file"},
                                filePath.toString(),
                                new NamedSlimeLoader("file", mock(SlimeLoader.class)),
                                null
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("does not point out to a valid world directory"));
            }
        }

        @Test
        @DisplayName("should reject missing world paths before touching the cache")
        void shouldRejectMissingWorldPathsBeforeTouchingTheCache(@TempDir final Path tempDir) {
            Path missingPath = tempDir.resolve("missing-world");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.importWorld(
                                source(sender),
                                new String[]{missingPath.toString(), "file"},
                                missingPath.toString(),
                                new NamedSlimeLoader("file", mock(SlimeLoader.class)),
                                null
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("does not point out to a valid world directory"));
            }
        }

        @Test
        @DisplayName("should do nothing when the confirmation command arguments differ")
        void shouldDoNothingWhenTheConfirmationCommandArgumentsDiffer(@TempDir final Path tempDir) {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = assertDoesNotThrow(() -> Files.createDirectory(tempDir.resolve("source-world")));
            String[] firstArgs = {worldPath.toString(), "file", "arena-copy"};
            String[] differentArgs = {worldPath.toString(), "file", "other-copy"};

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), firstArgs, worldPath.toString(), new NamedSlimeLoader("file", loader), "arena-copy").join();
                clearInvocations(sender);

                assertDoesNotThrow(() -> command.importWorld(
                        source(sender),
                        differentArgs,
                        worldPath.toString(),
                        new NamedSlimeLoader("file", loader),
                        "other-copy"
                ).join());

                assertFalse(worldsInUse.contains("other-copy"));
            }
        }

        @Test
        @DisplayName("should reject confirmed imports when config already contains the target world")
        void shouldRejectConfirmedImportsWhenConfigAlreadyContainsTheTargetWorld(@TempDir final Path tempDir) {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = assertDoesNotThrow(() -> Files.createDirectory(tempDir.resolve("source-world")));
            String[] args = {worldPath.toString(), "file", "arena-copy"};
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new HashMap<>();
            worlds.put("arena-copy", new WorldData());
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), "arena-copy").join();

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.importWorld(
                                source(sender),
                                args,
                                worldPath.toString(),
                                new NamedSlimeLoader("file", loader),
                                "arena-copy"
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("inside the worlds config file"));
            }
        }

        @Test
        @DisplayName("should use the source directory name and surface duplicate stored world errors")
        void shouldUseTheSourceDirectoryNameAndSurfaceDuplicateStoredWorldErrors(@TempDir final Path tempDir) throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = Files.createDirectory(tempDir.resolve("source-world"));
            String[] args = {worldPath.toString(), "file"};
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(asp.readVanillaWorld(worldPath.toFile(), "source-world", loader))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException("source-world"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null).join();
                clearInvocations(sender);

                MessageCommandException exception = joinMessageException(
                        command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null)
                );

                assertTrue(plainText(exception.getComponent()).contains("already contains a world called source-world"));
            }
        }

        @Test
        @DisplayName("should surface invalid vanilla world errors during import")
        void shouldSurfaceInvalidVanillaWorldErrorsDuringImport(@TempDir final Path tempDir) throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = Files.createDirectory(tempDir.resolve("broken-world"));
            String[] args = {worldPath.toString(), "file"};
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(asp.readVanillaWorld(worldPath.toFile(), "broken-world", loader))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.InvalidWorldException(worldPath, "bad level.dat"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null).join();
                clearInvocations(sender);

                MessageCommandException exception = joinMessageException(
                        command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null)
                );

                assertTrue(plainText(exception.getComponent()).contains("does not contain a valid Minecraft world"));
            }
        }

        @Test
        @DisplayName("should surface oversized world errors during import")
        void shouldSurfaceOversizedWorldErrorsDuringImport(@TempDir final Path tempDir) throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = Files.createDirectory(tempDir.resolve("huge-world"));
            String[] args = {worldPath.toString(), "file"};
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(asp.readVanillaWorld(worldPath.toFile(), "huge-world", loader))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.WorldTooBigException("huge-world"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null).join();
                clearInvocations(sender);

                MessageCommandException exception = joinMessageException(
                        command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null)
                );

                assertTrue(plainText(exception.getComponent()).contains("The Slime Format isn't meant for big worlds"));
            }
        }

        @Test
        @DisplayName("should surface loaded world errors during import")
        void shouldSurfaceLoadedWorldErrorsDuringImport(@TempDir final Path tempDir) throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = Files.createDirectory(tempDir.resolve("loaded-world"));
            String[] args = {worldPath.toString(), "file"};
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(asp.readVanillaWorld(worldPath.toFile(), "loaded-world", loader))
                    .thenThrow(new com.infernalsuite.asp.api.exceptions.WorldLoadedException("loaded-world"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null).join();
                clearInvocations(sender);

                MessageCommandException exception = joinMessageException(
                        command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null)
                );

                assertTrue(plainText(exception.getComponent()).contains("Please unload it before importing it"));
            }
        }

        @Test
        @DisplayName("should surface io failures during import")
        void shouldSurfaceIoFailuresDuringImport(@TempDir final Path tempDir) throws Exception {
            when(sender.getName()).thenReturn("console");
            SlimeLoader loader = mock(SlimeLoader.class);
            Path worldPath = Files.createDirectory(tempDir.resolve("io-world"));
            String[] args = {worldPath.toString(), "file"};
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(asp.readVanillaWorld(worldPath.toFile(), "io-world", loader))
                    .thenThrow(new IOException("disk error"));

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                ImportWorldCmd command = new ImportWorldCmd(commandManager);

                command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null).join();
                clearInvocations(sender);

                MessageCommandException exception = joinMessageException(
                        command.importWorld(source(sender), args, worldPath.toString(), new NamedSlimeLoader("file", loader), null)
                );

                assertTrue(plainText(exception.getComponent()).contains("Take a look at the server console"));
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
