package com.infernalsuite.asp.plugin;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import com.infernalsuite.asp.plugin.testutil.TestAdvancedSlimePaperAPI;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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

    private SWPlugin plugin() {
        SWPlugin plugin = mock(SWPlugin.class, CALLS_REAL_METHODS);
        lenient().when(plugin.getLogger()).thenReturn(logger);
        return plugin;
    }
}
