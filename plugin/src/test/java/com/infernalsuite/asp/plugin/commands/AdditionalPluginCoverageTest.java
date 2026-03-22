package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.commands.parser.NamedSlimeLoader;
import com.infernalsuite.asp.plugin.commands.parser.NamedWorldData;
import com.infernalsuite.asp.plugin.commands.parser.suggestion.KnownSlimeWorldSuggestionProvider;
import com.infernalsuite.asp.plugin.commands.sub.CreateWorldCmd;
import com.infernalsuite.asp.plugin.commands.sub.HelpCmd;
import com.infernalsuite.asp.plugin.commands.sub.MigrateWorldCmd;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import com.infernalsuite.asp.plugin.loader.LoaderManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.suggestion.Suggestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Additional plugin coverage")
class AdditionalPluginCoverageTest extends AbstractCommandTest {

    @Mock
    private CommandContext<Source> commandContext;

    @Nested
    @DisplayName("CreateWorldCmd")
    class CreateWorldCmdTests {

        @Test
        @DisplayName("should reject worlds that are already being processed")
        void shouldRejectWorldsThatAreAlreadyBeingProcessed() {
            SlimeLoader loader = mock(SlimeLoader.class);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                CreateWorldCmd command = new CreateWorldCmd(commandManager);
                worldsInUse.add("arena");

                try {
                    MessageCommandException exception = assertThrows(
                            MessageCommandException.class,
                            () -> command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), null, null)
                    );

                    assertTrue(plainText(exception.getComponent()).contains("already being used on another command"));
                } finally {
                    worldsInUse.remove("arena");
                }
            }
        }

        @Test
        @DisplayName("should reject already loaded worlds before doing any work")
        void shouldRejectAlreadyLoadedWorldsBeforeDoingAnyWork() {
            SlimeLoader loader = mock(SlimeLoader.class);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(mock(World.class));
                CreateWorldCmd command = new CreateWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), null, null)
                );

                assertTrue(plainText(exception.getComponent()).contains("already exists"));
            }
        }

        @Test
        @DisplayName("should reject worlds already declared in config")
        void shouldRejectWorldsAlreadyDeclaredInConfig() {
            SlimeLoader loader = mock(SlimeLoader.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new HashMap<>();
            worlds.put("arena", new WorldData());
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = new CreateWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), null, null)
                );

                assertTrue(plainText(exception.getComponent()).contains("inside the worlds config file"));
            }
        }

        @Test
        @DisplayName("should reject unknown biomes before scheduling work")
        void shouldRejectUnknownBiomesBeforeSchedulingWork() {
            SlimeLoader loader = mock(SlimeLoader.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            NamespacedKey biomeKey = NamespacedKey.minecraft("missing_biome");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd(null, () -> {
                });

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), biomeKey, null)
                );

                assertTrue(plainText(exception.getComponent()).contains("Biome minecraft:missing_biomedoes not exist"));
            }
        }

        @Test
        @DisplayName("should reject invalid environments before creating worlds")
        void shouldRejectInvalidEnvironmentsBeforeCreatingWorlds() {
            SlimeLoader loader = mock(SlimeLoader.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd("minecraft:plains", () -> {
                });

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.createWorld(
                                source(sender),
                                "arena",
                                new NamedSlimeLoader("file", loader),
                                null,
                                "space"
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("is not a valid environment"));
            }
        }

        @Test
        @DisplayName("should create, save and load empty worlds using the default biome")
        void shouldCreateSaveAndLoadEmptyWorldsUsingTheDefaultBiome() throws Exception {
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new HashMap<>();
            when(worldsConfig.getWorlds()).thenReturn(worlds);
            when(loader.worldExists("arena")).thenReturn(false);
            when(asp.createEmptyWorld(eq("arena"), eq(false), any(SlimePropertyMap.class), eq(loader))).thenReturn(slimeWorld);
            boolean[] bedrockPlaced = {false};

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd("minecraft:plains", () -> bedrockPlaced[0] = true);

                assertDoesNotThrow(() -> command.createWorld(
                        source(sender),
                        "arena",
                        new NamedSlimeLoader("file", loader),
                        null,
                        null
                ).join());

                verify(asp).createEmptyWorld(eq("arena"), eq(false), any(SlimePropertyMap.class), eq(loader));
                verify(asp).saveWorld(slimeWorld);
                verify(asp).loadWorld(slimeWorld, true);
                verify(worldsConfig).save();
                assertTrue(bedrockPlaced[0]);
                assertEquals("file", worlds.get("arena").getDataSource());
                assertEquals("minecraft:plains", worlds.get("arena").getDefaultBiome());
                assertEquals("0, 64, 0", worlds.get("arena").getSpawn());
                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "Creating empty world arena");
                assertMessageSent(sender, "created in");
            }
        }

        @Test
        @DisplayName("should persist explicit biome and environment when creating worlds")
        void shouldPersistExplicitBiomeAndEnvironmentWhenCreatingWorlds() throws Exception {
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new HashMap<>();
            when(worldsConfig.getWorlds()).thenReturn(worlds);
            when(loader.worldExists("end-arena")).thenReturn(false);
            when(asp.createEmptyWorld(eq("end-arena"), eq(false), any(SlimePropertyMap.class), eq(loader))).thenReturn(slimeWorld);
            NamespacedKey biomeKey = NamespacedKey.minecraft("desert");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("end-arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd("minecraft:desert", () -> {
                });

                assertDoesNotThrow(() -> command.createWorld(
                        source(sender),
                        "end-arena",
                        new NamedSlimeLoader("file", loader),
                        biomeKey,
                        "the_end"
                ).join());

                assertEquals("minecraft:desert", worlds.get("end-arena").getDefaultBiome());
                assertEquals("the_end", worlds.get("end-arena").getEnvironment());
                assertFalse(worldsInUse.contains("end-arena"));
            }
        }

        @Test
        @DisplayName("should reject worlds that already exist in the loader")
        void shouldRejectWorldsThatAlreadyExistInTheLoader() throws Exception {
            SlimeLoader loader = mock(SlimeLoader.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(loader.worldExists("arena")).thenReturn(true);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd("minecraft:plains", () -> {
                });

                MessageCommandException exception = joinMessageException(
                        command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), null, null)
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("world already exists"));
            }
        }

        @Test
        @DisplayName("should surface world generation failures while creating worlds")
        void shouldSurfaceWorldGenerationFailuresWhileCreatingWorlds() throws Exception {
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(loader.worldExists("arena")).thenReturn(false);
            when(asp.createEmptyWorld(eq("arena"), eq(false), any(SlimePropertyMap.class), eq(loader))).thenReturn(slimeWorld);
            doThrow(new IllegalArgumentException("boom")).when(asp).loadWorld(slimeWorld, true);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd("minecraft:plains", () -> {
                });

                MessageCommandException exception = joinMessageException(
                        command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), null, null)
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("Failed to create world arena: boom."));
            }
        }

        @Test
        @DisplayName("should wrap io failures while creating worlds")
        void shouldWrapIoFailuresWhileCreatingWorlds() throws Exception {
            SlimeLoader loader = mock(SlimeLoader.class);
            SlimeWorld slimeWorld = mock(SlimeWorld.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());
            when(loader.worldExists("arena")).thenReturn(false);
            when(asp.createEmptyWorld(eq("arena"), eq(false), any(SlimePropertyMap.class), eq(loader))).thenReturn(slimeWorld);
            doThrow(new IOException("disk error")).when(asp).saveWorld(slimeWorld);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<Bukkit> bukkit = mockPrimaryThreadBukkit();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                CreateWorldCmd command = testableCreateWorldCmd("minecraft:plains", () -> {
                });

                MessageCommandException exception = joinMessageException(
                        command.createWorld(source(sender), "arena", new NamedSlimeLoader("file", loader), null, null)
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("Take a look at the server console"));
            }
        }
    }

    @Nested
    @DisplayName("MigrateWorldCmd")
    class MigrateWorldCmdTests {

        @Test
        @DisplayName("should migrate a world and update its configured data source")
        void shouldMigrateAWorldAndUpdateItsConfiguredDataSource() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader currentLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(currentLoader);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                assertDoesNotThrow(() -> command.onCommand(
                        source(sender),
                        new NamedWorldData("arena", worldData),
                        new NamedSlimeLoader("mysql", targetLoader)
                ).join());

                verify(asp).migrateWorld("arena", currentLoader, targetLoader);
                verify(worldsConfig).save();
                assertEquals("mysql", worldData.getDataSource());
                assertFalse(worldsInUse.contains("arena"));
                assertMessageSent(sender, "migrated in");
            }
        }

        @Test
        @DisplayName("should reject migrations to the same data source")
        void shouldRejectMigrationsToTheSameDataSource() {
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.onCommand(
                                source(sender),
                                new NamedWorldData("arena", worldData),
                                new NamedSlimeLoader("FILE", mock(SlimeLoader.class))
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("already stored using data source file"));
            }
        }

        @Test
        @DisplayName("should reject migrations when the current loader is missing")
        void shouldRejectMigrationsWhenTheCurrentLoaderIsMissing() {
            LoaderManager loaderManager = mockLoaderManager();
            WorldData worldData = new WorldData();
            worldData.setDataSource("ghost");
            when(loaderManager.getLoader("ghost")).thenReturn(null);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.onCommand(
                                source(sender),
                                new NamedWorldData("arena", worldData),
                                new NamedSlimeLoader("mysql", mock(SlimeLoader.class))
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("Unknown data source ghost"));
            }
        }

        @Test
        @DisplayName("should surface missing world errors during migration")
        void shouldSurfaceMissingWorldErrorsDuringMigration() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader currentLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(currentLoader);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline()) {
                doThrow(new UnknownWorldException("arena")).when(asp).migrateWorld("arena", currentLoader, targetLoader);
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.onCommand(
                                source(sender),
                                new NamedWorldData("arena", worldData),
                                new NamedSlimeLoader("mysql", targetLoader)
                        )
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("Can't find world arena in data source file"));
            }
        }

        @Test
        @DisplayName("should surface duplicate world errors in the target data source")
        void shouldSurfaceDuplicateWorldErrorsInTheTargetDataSource() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader currentLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(currentLoader);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline()) {
                doThrow(new WorldAlreadyExistsException("arena")).when(asp).migrateWorld("arena", currentLoader, targetLoader);
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.onCommand(
                                source(sender),
                                new NamedWorldData("arena", worldData),
                                new NamedSlimeLoader("mysql", targetLoader)
                        )
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("already contains a world named arena"));
            }
        }

        @Test
        @DisplayName("should reject migrations while the world is already being processed")
        void shouldRejectMigrationsWhileTheWorldIsAlreadyBeingProcessed() {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader currentLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(currentLoader);
            worldsInUse.add("arena");

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance()) {
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                MessageCommandException exception = assertThrows(
                        MessageCommandException.class,
                        () -> command.onCommand(
                                source(sender),
                                new NamedWorldData("arena", worldData),
                                new NamedSlimeLoader("mysql", mock(SlimeLoader.class))
                        )
                );

                assertTrue(plainText(exception.getComponent()).contains("already being used on another command"));
            } finally {
                worldsInUse.remove("arena");
            }
        }

        @Test
        @DisplayName("should wrap io failures during migration")
        void shouldWrapIoFailuresDuringMigration() throws Exception {
            LoaderManager loaderManager = mockLoaderManager();
            SlimeLoader currentLoader = mock(SlimeLoader.class);
            SlimeLoader targetLoader = mock(SlimeLoader.class);
            WorldData worldData = new WorldData();
            worldData.setDataSource("file");
            when(loaderManager.getLoader("file")).thenReturn(currentLoader);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<CompletableFuture> async = mockRunAsyncInline()) {
                doThrow(new IOException("disk error")).when(asp).migrateWorld("arena", currentLoader, targetLoader);
                MigrateWorldCmd command = new MigrateWorldCmd(commandManager);

                MessageCommandException exception = joinMessageException(
                        command.onCommand(
                                source(sender),
                                new NamedWorldData("arena", worldData),
                                new NamedSlimeLoader("mysql", targetLoader)
                        )
                );

                assertFalse(worldsInUse.contains("arena"));
                assertTrue(plainText(exception.getComponent()).contains("Take a look at the server console"));
            }
        }
    }

    @Nested
    @DisplayName("KnownSlimeWorldSuggestionProvider")
    class KnownSlimeWorldSuggestionProviderTests {

        @Test
        @DisplayName("should suggest all configured world names")
        void shouldSuggestAllConfiguredWorldNames() {
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            Map<String, WorldData> worlds = new LinkedHashMap<>();
            worlds.put("arena", new WorldData());
            worlds.put("lobby", new WorldData());
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<CompletableFuture> async = mockRunAsyncInline();
                 MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
                KnownSlimeWorldSuggestionProvider provider = new KnownSlimeWorldSuggestionProvider();

                List<String> suggestions = suggestions(provider.suggestionsFuture(commandContext, CommandInput.empty()));

                assertEquals(List.of("arena", "lobby"), suggestions);
            }
        }
    }

    @Nested
    @DisplayName("HelpCmd")
    class HelpCmdTests {

        @Test
        @DisplayName("should forward the parsed query to MinecraftHelp")
        void shouldForwardTheParsedQueryToMinecraftHelp() {
            @SuppressWarnings("unchecked")
            PaperCommandManager<Source> paperCommandManager = mock(PaperCommandManager.class);
            @SuppressWarnings("unchecked")
            MinecraftHelp<Source> help = mock(MinecraftHelp.class);
            Source source = source(sender);

            try (MockedStatic<com.infernalsuite.asp.api.AdvancedSlimePaperAPI> ignored = mockApiInstance();
                 MockedStatic<MinecraftHelp> minecraftHelpStatic = mockStatic(MinecraftHelp.class)) {
                minecraftHelpStatic.when(() -> MinecraftHelp.create(
                        eq("/swp help"),
                        eq(paperCommandManager),
                        any(AudienceProvider.class)
                )).thenReturn(help);

                HelpCmd command = new HelpCmd(commandManager, paperCommandManager);

                command.help(source, new String[]{"clone-world", "template"});

                verify(help).queryCommands("clone-world template", source);
            }
        }
    }

    private LoaderManager mockLoaderManager() {
        LoaderManager loaderManager = mock(LoaderManager.class);
        when(plugin.getLoaderManager()).thenReturn(loaderManager);
        return loaderManager;
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
        completableFuture.when(() -> CompletableFuture.supplyAsync(any(Supplier.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Supplier<Object> supplier = invocation.getArgument(0);
            try {
                return CompletableFuture.completedFuture(supplier.get());
            } catch (Throwable throwable) {
                return CompletableFuture.failedFuture(throwable);
            }
        });
        return completableFuture;
    }

    private CreateWorldCmd testableCreateWorldCmd(final String biomeKey, final Runnable bedrockPlacer) {
        return new CreateWorldCmd(commandManager) {
            @Override
            protected String resolveBiomeKey(final NamespacedKey key) {
                return biomeKey;
            }

            @Override
            protected void placeBedrock(final String worldName) {
                bedrockPlacer.run();
            }
        };
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

    private static List<String> suggestions(final CompletableFuture<? extends Iterable<? extends Suggestion>> future) {
        Iterable<? extends Suggestion> iterable = future.join();
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(Suggestion::suggestion)
                .toList();
    }
}
