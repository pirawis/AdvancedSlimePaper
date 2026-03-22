package com.infernalsuite.asp.plugin.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@DisplayName("WorldsConfig")
class WorldsConfigTest {

    private WorldsConfig config;

    @BeforeEach
    void setUp() {
        config = new WorldsConfig();
    }

    @Nested
    @DisplayName("getWorlds")
    class GetWorldsTests {

        @Test
        @DisplayName("should return empty map by default")
        void shouldReturnEmptyMapByDefault() {
            Map<String, WorldData> worlds = config.getWorlds();

            assertNotNull(worlds);
            assertTrue(worlds.isEmpty());
        }

        @Test
        @DisplayName("should allow adding world data")
        void shouldAllowAddingWorldData() {
            WorldData worldData = new WorldData();
            config.getWorlds().put("lobby", worldData);

            assertEquals(1, config.getWorlds().size());
            assertSame(worldData, config.getWorlds().get("lobby"));
        }

        @Test
        @DisplayName("should allow adding multiple worlds")
        void shouldAllowAddingMultipleWorlds() {
            config.getWorlds().put("world1", new WorldData());
            config.getWorlds().put("world2", new WorldData());
            config.getWorlds().put("world3", new WorldData());

            assertEquals(3, config.getWorlds().size());
        }

        @Test
        @DisplayName("should allow removing world data")
        void shouldAllowRemovingWorldData() {
            WorldData worldData = new WorldData();
            config.getWorlds().put("temp", worldData);
            config.getWorlds().remove("temp");

            assertFalse(config.getWorlds().containsKey("temp"));
        }

        @Test
        @DisplayName("should preserve world data modifications")
        void shouldPreserveWorldDataModifications() {
            WorldData worldData = new WorldData();
            worldData.setDifficulty("hard");
            worldData.setSpawn("100, 64, 200");
            config.getWorlds().put("arena", worldData);

            WorldData retrieved = config.getWorlds().get("arena");
            assertEquals("hard", retrieved.getDifficulty());
            assertEquals("100, 64, 200", retrieved.getSpawn());
        }

        @Test
        @DisplayName("should return same map instance")
        void shouldReturnSameMapInstance() {
            Map<String, WorldData> map1 = config.getWorlds();
            Map<String, WorldData> map2 = config.getWorlds();

            assertSame(map1, map2);
        }

        @Test
        @DisplayName("should handle world names with special characters")
        void shouldHandleWorldNamesWithSpecialCharacters() {
            config.getWorlds().put("my-world_v2", new WorldData());
            config.getWorlds().put("world.test", new WorldData());
            config.getWorlds().put("world:123", new WorldData());

            assertEquals(3, config.getWorlds().size());
            assertTrue(config.getWorlds().containsKey("my-world_v2"));
            assertTrue(config.getWorlds().containsKey("world.test"));
            assertTrue(config.getWorlds().containsKey("world:123"));
        }

        @Test
        @DisplayName("should allow overwriting existing world")
        void shouldAllowOverwritingExistingWorld() {
            WorldData original = new WorldData();
            original.setDifficulty("easy");
            config.getWorlds().put("test", original);

            WorldData replacement = new WorldData();
            replacement.setDifficulty("hard");
            config.getWorlds().put("test", replacement);

            assertEquals(1, config.getWorlds().size());
            assertEquals("hard", config.getWorlds().get("test").getDifficulty());
        }
    }

    @Nested
    @DisplayName("iteration")
    class IterationTests {

        @Test
        @DisplayName("should allow iteration over worlds")
        void shouldAllowIterationOverWorlds() {
            config.getWorlds().put("world1", new WorldData());
            config.getWorlds().put("world2", new WorldData());

            int count = 0;
            for (Map.Entry<String, WorldData> entry : config.getWorlds().entrySet()) {
                assertNotNull(entry.getKey());
                assertNotNull(entry.getValue());
                count++;
            }

            assertEquals(2, count);
        }

        @Test
        @DisplayName("should allow clearing all worlds")
        void shouldAllowClearingAllWorlds() {
            config.getWorlds().put("world1", new WorldData());
            config.getWorlds().put("world2", new WorldData());
            config.getWorlds().clear();

            assertTrue(config.getWorlds().isEmpty());
        }
    }

    @Test
    @DisplayName("save should swallow IO failures from the config loader")
    void saveShouldSwallowIoFailuresFromTheConfigLoader() throws Exception {
        YamlConfigurationLoader loader = mock(YamlConfigurationLoader.class);
        CommentedConfigurationNode node = mock(CommentedConfigurationNode.class);
        when(loader.createNode()).thenReturn(node);
        when(node.set(any(io.leangen.geantyref.TypeToken.class), any())).thenReturn(node);
        doThrow(new ConfigurateException("disk error")).when(loader).save(node);

        try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
            configManager.when(ConfigManager::getWorldConfigLoader).thenReturn(loader);

            assertDoesNotThrow(() -> config.save());
        }
    }
}
