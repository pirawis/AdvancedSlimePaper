package com.infernalsuite.asp.plugin;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.SlimeNMSBridge;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.plugin.commands.CommandManager;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import com.infernalsuite.asp.plugin.loader.LoaderManager;
import com.infernalsuite.asp.plugin.testutil.TestAdvancedSlimePaperAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SWPlugin behavior")
class SWPluginBehaviorTest {

    @Mock
    private AdvancedSlimePaperAPI asp;

    @Mock
    private WorldsConfig worldsConfig;

    @Mock
    private SlimeWorldInstance writableWorld;

    @Mock
    private SlimeWorldInstance readOnlyWorld;

    @Mock
    private Logger logger;

    @Mock
    private org.slf4j.Logger slf4jLogger;

    @Mock
    private LoaderManager loaderManager;

    @Mock
    private SlimeLoader fileLoader;

    @Mock
    private SlimeWorld startupWorld;

    @Mock
    private SlimeWorld enableWorld;

    @AfterEach
    void tearDown() {
        TestAdvancedSlimePaperAPI.setDelegate(null);
    }

    @Test
    @DisplayName("should save writable worlds and unload all loaded worlds on disable")
    void shouldSaveWritableWorldsAndUnloadAllLoadedWorldsOnDisable() throws IOException {
        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("arena", new WorldData());
        configuredWorlds.put("lobby", new WorldData());
        configuredWorlds.put("ghost", new WorldData());

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(asp.getLoadedWorld("arena")).thenReturn(writableWorld);
        when(asp.getLoadedWorld("lobby")).thenReturn(readOnlyWorld);
        when(asp.getLoadedWorld("ghost")).thenReturn(null);
        when(writableWorld.isReadOnly()).thenReturn(false);
        when(writableWorld.getName()).thenReturn("arena");
        when(readOnlyWorld.isReadOnly()).thenReturn(true);
        when(readOnlyWorld.getName()).thenReturn("lobby");

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

            SWPlugin plugin = plugin();
            plugin.onDisable();

            verify(asp).saveWorld(writableWorld);
            verify(asp, never()).saveWorld(readOnlyWorld);
            bukkit.verify(() -> Bukkit.unloadWorld("arena", false));
            bukkit.verify(() -> Bukkit.unloadWorld("lobby", false));
        }
    }

    @Test
    @DisplayName("should log and still unload a world when saving fails on disable")
    void shouldLogAndStillUnloadAWorldWhenSavingFailsOnDisable() throws IOException {
        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("arena", new WorldData());
        IOException failure = new IOException("boom");

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(asp.getLoadedWorld("arena")).thenReturn(writableWorld);
        when(writableWorld.isReadOnly()).thenReturn(false);
        when(writableWorld.getName()).thenReturn("arena");
        doThrow(failure).when(asp).saveWorld(writableWorld);

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

            SWPlugin plugin = plugin();
            plugin.onDisable();

            verify(logger).log(Level.SEVERE, "Failed to save world arena", failure);
            bukkit.verify(() -> Bukkit.unloadWorld("arena", false));
        }
    }

    @Test
    @DisplayName("should load startup worlds, collect failures and persist config")
    void shouldLoadStartupWorldsCollectFailuresAndPersistConfig() throws Exception {
        WorldData success = new WorldData();
        success.setDataSource("file");
        success.setLoadOnStartup(true);

        WorldData skipped = new WorldData();
        skipped.setDataSource("file");
        skipped.setLoadOnStartup(false);

        WorldData missingLoader = new WorldData();
        missingLoader.setDataSource("ghost");
        missingLoader.setLoadOnStartup(true);

        WorldData unknown = new WorldData();
        unknown.setDataSource("file");
        unknown.setLoadOnStartup(true);

        WorldData newer = new WorldData();
        newer.setDataSource("file");
        newer.setLoadOnStartup(true);

        WorldData corrupted = new WorldData();
        corrupted.setDataSource("file");
        corrupted.setLoadOnStartup(true);

        WorldData ioFailure = new WorldData();
        ioFailure.setDataSource("file");
        ioFailure.setLoadOnStartup(true);

        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("arena", success);
        configuredWorlds.put("skip-me", skipped);
        configuredWorlds.put("ghost", missingLoader);
        configuredWorlds.put("unknown", unknown);
        configuredWorlds.put("future", newer);
        configuredWorlds.put("broken", corrupted);
        configuredWorlds.put("io", ioFailure);

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(loaderManager.getLoader("file")).thenReturn(fileLoader);
        when(loaderManager.getLoader("ghost")).thenReturn(null);
        when(asp.readWorld(eq(fileLoader), eq("arena"), eq(false), any())).thenReturn(startupWorld);
        when(asp.readWorld(eq(fileLoader), eq("unknown"), eq(false), any())).thenThrow(new UnknownWorldException("unknown"));
        when(asp.readWorld(eq(fileLoader), eq("future"), eq(false), any())).thenThrow(new NewerFormatException((byte) 14));
        when(asp.readWorld(eq(fileLoader), eq("broken"), eq(false), any())).thenThrow(new CorruptedWorldException("broken"));
        when(asp.readWorld(eq(fileLoader), eq("io"), eq(false), any())).thenThrow(new IOException("disk"));

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

            SWPlugin plugin = plugin();
            setField(plugin, "loaderManager", loaderManager);

            List<String> erroredWorlds = invokeLoadWorlds(plugin);
            Map<String, SlimeWorld> worldsToLoad = worldsToLoad(plugin);

            Assertions.assertEquals(List.of("ghost", "unknown", "future", "broken", "io"), erroredWorlds);
            Assertions.assertSame(startupWorld, worldsToLoad.get("arena"));
            Assertions.assertFalse(worldsToLoad.containsKey("skip-me"));
            verify(worldsConfig).save();
            verify(slf4jLogger, times(5)).error(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }
    }

    @Test
    @DisplayName("should stop loading when config initialization fails")
    void shouldStopLoadingWhenConfigInitializationFails() {
        IOException failure = new IOException("boom");

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedConstruction<LoaderManager> ignoredLoaders = mockConstruction(LoaderManager.class)) {
            configManager.when(ConfigManager::initialize).thenThrow(failure);

            SWPlugin plugin = plugin();
            plugin.onLoad();

            verify(slf4jLogger).error("Failed to load config files", failure);
            Assertions.assertNull(plugin.getLoaderManager());
            Assertions.assertTrue(ignoredLoaders.constructed().isEmpty());
        }
    }

    @Test
    @DisplayName("should load default world overrides during onLoad")
    void shouldLoadDefaultWorldOverridesDuringOnLoad() throws Exception {
        writeServerProperties("world");

        WorldData overworld = loadOnStartupWorld();
        WorldData nether = loadOnStartupWorld();
        WorldData end = loadOnStartupWorld();

        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("world", overworld);
        configuredWorlds.put("world_nether", nether);
        configuredWorlds.put("world_the_end", end);

        SlimeWorld netherWorld = mock(SlimeWorld.class);
        SlimeWorld endWorld = mock(SlimeWorld.class);
        SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
        Server server = mock(Server.class);

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(asp.readWorld(eq(fileLoader), eq("world"), eq(false), any())).thenReturn(startupWorld);
        when(asp.readWorld(eq(fileLoader), eq("world_nether"), eq(false), any())).thenReturn(netherWorld);
        when(asp.readWorld(eq(fileLoader), eq("world_the_end"), eq(false), any())).thenReturn(endWorld);
        when(server.getAllowNether()).thenReturn(true);
        when(server.getAllowEnd()).thenReturn(true);

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedConstruction<LoaderManager> loaders = mockConstruction(LoaderManager.class, (mock, context) ->
                     when(mock.getLoader("file")).thenReturn(fileLoader))) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
            bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
            bukkit.when(Bukkit::getServer).thenReturn(server);

            SWPlugin plugin = plugin();
            when(plugin.getServer()).thenReturn(server);

            plugin.onLoad();

            verify(worldsConfig).save();
            verify(bridge).setDefaultWorlds(startupWorld, netherWorld, endWorld);
            Assertions.assertSame(loaders.constructed().getFirst(), plugin.getLoaderManager());
            Assertions.assertEquals(3, worldsToLoad(plugin).size());
        }
    }

    @Test
    @DisplayName("should shut down when the default world fails to load on startup")
    void shouldShutDownWhenTheDefaultWorldFailsToLoadOnStartup() throws Exception {
        writeServerProperties("world");

        WorldData overworld = loadOnStartupWorld();
        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("world", overworld);

        SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
        Server server = mock(Server.class);

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(asp.readWorld(eq(fileLoader), eq("world"), eq(false), any())).thenThrow(new UnknownWorldException("world"));
        when(server.getAllowNether()).thenReturn(false);
        when(server.getAllowEnd()).thenReturn(false);

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedConstruction<LoaderManager> loaders = mockConstruction(LoaderManager.class, (mock, context) ->
                     when(mock.getLoader("file")).thenReturn(fileLoader))) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
            bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
            bukkit.when(Bukkit::getServer).thenReturn(server);

            SWPlugin plugin = plugin();
            when(plugin.getServer()).thenReturn(server);

            plugin.onLoad();

            verify(server).shutdown();
            verify(slf4jLogger).error("Shutting down server, as the default world could not be loaded.");
            verify(bridge).setDefaultWorlds(null, null, null);
            Assertions.assertSame(loaders.constructed().getFirst(), plugin.getLoaderManager());
        }
    }

    @Test
    @DisplayName("should shut down when the default nether fails to load on startup")
    void shouldShutDownWhenTheDefaultNetherFailsToLoadOnStartup() throws Exception {
        writeServerProperties("world");

        WorldData overworld = loadOnStartupWorld();
        WorldData nether = loadOnStartupWorld();
        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("world", overworld);
        configuredWorlds.put("world_nether", nether);

        SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
        Server server = mock(Server.class);

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(asp.readWorld(eq(fileLoader), eq("world"), eq(false), any())).thenReturn(startupWorld);
        when(asp.readWorld(eq(fileLoader), eq("world_nether"), eq(false), any())).thenThrow(new UnknownWorldException("world_nether"));
        when(server.getAllowNether()).thenReturn(true);
        when(server.getAllowEnd()).thenReturn(false);

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedConstruction<LoaderManager> loaders = mockConstruction(LoaderManager.class, (mock, context) ->
                     when(mock.getLoader("file")).thenReturn(fileLoader))) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
            bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
            bukkit.when(Bukkit::getServer).thenReturn(server);

            SWPlugin plugin = plugin();
            when(plugin.getServer()).thenReturn(server);

            plugin.onLoad();

            verify(server).shutdown();
            verify(slf4jLogger).error("Shutting down server, as the default nether world could not be loaded.");
            verify(bridge).setDefaultWorlds(startupWorld, null, null);
            Assertions.assertSame(loaders.constructed().getFirst(), plugin.getLoaderManager());
        }
    }

    @Test
    @DisplayName("should shut down when the default end fails to load on startup")
    void shouldShutDownWhenTheDefaultEndFailsToLoadOnStartup() throws Exception {
        writeServerProperties("world");

        WorldData overworld = loadOnStartupWorld();
        WorldData end = loadOnStartupWorld();
        Map<String, WorldData> configuredWorlds = new LinkedHashMap<>();
        configuredWorlds.put("world", overworld);
        configuredWorlds.put("world_the_end", end);

        SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
        Server server = mock(Server.class);

        when(worldsConfig.getWorlds()).thenReturn(configuredWorlds);
        when(asp.readWorld(eq(fileLoader), eq("world"), eq(false), any())).thenReturn(startupWorld);
        when(asp.readWorld(eq(fileLoader), eq("world_the_end"), eq(false), any())).thenThrow(new UnknownWorldException("world_the_end"));
        when(server.getAllowNether()).thenReturn(false);
        when(server.getAllowEnd()).thenReturn(true);

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class);
             MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedConstruction<LoaderManager> loaders = mockConstruction(LoaderManager.class, (mock, context) ->
                     when(mock.getLoader("file")).thenReturn(fileLoader))) {
            configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);
            bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
            bukkit.when(Bukkit::getServer).thenReturn(server);

            SWPlugin plugin = plugin();
            when(plugin.getServer()).thenReturn(server);

            plugin.onLoad();

            verify(server).shutdown();
            verify(slf4jLogger).error("Shutting down server, as the default end world could not be loaded.");
            verify(bridge).setDefaultWorlds(startupWorld, null, null);
            Assertions.assertSame(loaders.constructed().getFirst(), plugin.getLoaderManager());
        }
    }

    @Test
    @DisplayName("should load only missing worlds on enable and clear the startup cache")
    void shouldLoadOnlyMissingWorldsOnEnableAndClearTheStartupCache() throws Exception {
        SlimeWorld failingWorld = mock(SlimeWorld.class);
        when(enableWorld.getName()).thenReturn("arena");
        when(failingWorld.getName()).thenReturn("broken");

        TestAdvancedSlimePaperAPI.setDelegate(asp);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
             MockedConstruction<Metrics> ignoredMetrics = mockConstruction(Metrics.class);
             MockedConstruction<CommandManager> ignoredCommands = mockConstruction(CommandManager.class)) {
            bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(null);
            bukkit.when(() -> Bukkit.getWorld("broken")).thenReturn(null);
            when(asp.loadWorld(enableWorld, true)).thenReturn(mock(SlimeWorldInstance.class));
            when(asp.loadWorld(failingWorld, true)).thenThrow(new IllegalArgumentException("boom"));

            SWPlugin plugin = plugin();
            Map<String, SlimeWorld> worldsToLoad = worldsToLoad(plugin);
            worldsToLoad.put("arena", enableWorld);
            worldsToLoad.put("broken", failingWorld);

            plugin.onEnable();

            verify(asp).loadWorld(enableWorld, true);
            verify(asp).loadWorld(failingWorld, true);
            verify(slf4jLogger).error(eq("Failed to load world: {}"), eq("broken"), any(IllegalArgumentException.class));
            Assertions.assertTrue(worldsToLoad.isEmpty());
        }
    }

    private SWPlugin plugin() {
        SWPlugin plugin = mock(SWPlugin.class, CALLS_REAL_METHODS);
        lenient().when(plugin.getLogger()).thenReturn(logger);
        lenient().when(plugin.getSLF4JLogger()).thenReturn(slf4jLogger);
        try {
            setField(plugin, "worldsToLoad", new HashMap<String, SlimeWorld>());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return plugin;
    }

    private static WorldData loadOnStartupWorld() {
        WorldData worldData = new WorldData();
        worldData.setDataSource("file");
        worldData.setLoadOnStartup(true);
        return worldData;
    }

    private static Path serverPropertiesPath() {
        return Path.of("server.properties");
    }

    private static void writeServerProperties(final String levelName) throws IOException {
        Files.writeString(serverPropertiesPath(), "level-name=" + levelName + System.lineSeparator());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, SlimeWorld> worldsToLoad(final SWPlugin plugin) throws Exception {
        Field field = SWPlugin.class.getDeclaredField("worldsToLoad");
        field.setAccessible(true);
        return (Map<String, SlimeWorld>) field.get(plugin);
    }

    @SuppressWarnings("unchecked")
    private static List<String> invokeLoadWorlds(final SWPlugin plugin) throws Exception {
        Method method = SWPlugin.class.getDeclaredMethod("loadWorlds");
        method.setAccessible(true);
        return new ArrayList<>((List<String>) method.invoke(plugin));
    }

    private static void setField(final SWPlugin plugin, final String name, final Object value) throws Exception {
        Field field = SWPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }
}
