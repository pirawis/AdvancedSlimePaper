package com.infernalsuite.asp.plugin.config;

import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorldData")
class WorldDataTest {

    private WorldData worldData;

    @BeforeEach
    void setUp() {
        worldData = new WorldData();
    }

    @Nested
    @DisplayName("default values")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have default data source as file")
        void shouldHaveDefaultDataSource() {
            assertEquals("file", worldData.getDataSource());
        }

        @Test
        @DisplayName("should have default spawn location")
        void shouldHaveDefaultSpawn() {
            assertEquals("0.5, 255, 0.5", worldData.getSpawn());
        }

        @Test
        @DisplayName("should have default difficulty as peaceful")
        void shouldHaveDefaultDifficulty() {
            assertEquals("peaceful", worldData.getDifficulty());
        }

        @Test
        @DisplayName("should have default environment as NORMAL")
        void shouldHaveDefaultEnvironment() {
            assertEquals("NORMAL", worldData.getEnvironment());
        }

        @Test
        @DisplayName("should have default world type as DEFAULT")
        void shouldHaveDefaultWorldType() {
            assertEquals("DEFAULT", worldData.getWorldType());
        }

        @Test
        @DisplayName("should have default biome as plains")
        void shouldHaveDefaultBiome() {
            assertEquals("minecraft:plains", worldData.getDefaultBiome());
        }

        @Test
        @DisplayName("should allow monsters by default")
        void shouldAllowMonstersByDefault() {
            assertTrue(worldData.isAllowMonsters());
        }

        @Test
        @DisplayName("should allow animals by default")
        void shouldAllowAnimalsByDefault() {
            assertTrue(worldData.isAllowAnimals());
        }

        @Test
        @DisplayName("should disable dragon battle by default")
        void shouldDisableDragonBattleByDefault() {
            assertFalse(worldData.isDragonBattle());
        }

        @Test
        @DisplayName("should enable pvp by default")
        void shouldEnablePvpByDefault() {
            assertTrue(worldData.isPvp());
        }

        @Test
        @DisplayName("should load on startup by default")
        void shouldLoadOnStartupByDefault() {
            assertTrue(worldData.isLoadOnStartup());
        }

        @Test
        @DisplayName("should not be read only by default")
        void shouldNotBeReadOnlyByDefault() {
            assertFalse(worldData.isReadOnly());
        }
    }

    @Nested
    @DisplayName("setters and getters")
    class SettersAndGettersTests {

        @Test
        @DisplayName("should set and get data source")
        void shouldSetAndGetDataSource() {
            worldData.setDataSource("mysql");
            assertEquals("mysql", worldData.getDataSource());
        }

        @Test
        @DisplayName("should set and get spawn")
        void shouldSetAndGetSpawn() {
            worldData.setSpawn("100, 64, 200");
            assertEquals("100, 64, 200", worldData.getSpawn());
        }

        @Test
        @DisplayName("should set and get difficulty")
        void shouldSetAndGetDifficulty() {
            worldData.setDifficulty("hard");
            assertEquals("hard", worldData.getDifficulty());
        }

        @Test
        @DisplayName("should set and get environment")
        void shouldSetAndGetEnvironment() {
            worldData.setEnvironment("NETHER");
            assertEquals("NETHER", worldData.getEnvironment());
        }

        @Test
        @DisplayName("should set and get world type")
        void shouldSetAndGetWorldType() {
            worldData.setWorldType("FLAT");
            assertEquals("FLAT", worldData.getWorldType());
        }

        @Test
        @DisplayName("should set and get default biome")
        void shouldSetAndGetDefaultBiome() {
            worldData.setDefaultBiome("minecraft:desert");
            assertEquals("minecraft:desert", worldData.getDefaultBiome());
        }

        @Test
        @DisplayName("should set and get allow monsters")
        void shouldSetAndGetAllowMonsters() {
            worldData.setAllowMonsters(false);
            assertFalse(worldData.isAllowMonsters());
        }

        @Test
        @DisplayName("should set and get allow animals")
        void shouldSetAndGetAllowAnimals() {
            worldData.setAllowAnimals(false);
            assertFalse(worldData.isAllowAnimals());
        }

        @Test
        @DisplayName("should set and get dragon battle")
        void shouldSetAndGetDragonBattle() {
            worldData.setDragonBattle(true);
            assertTrue(worldData.isDragonBattle());
        }

        @Test
        @DisplayName("should set and get pvp")
        void shouldSetAndGetPvp() {
            worldData.setPvp(false);
            assertFalse(worldData.isPvp());
        }

        @Test
        @DisplayName("should set and get load on startup")
        void shouldSetAndGetLoadOnStartup() {
            worldData.setLoadOnStartup(false);
            assertFalse(worldData.isLoadOnStartup());
        }

        @Test
        @DisplayName("should set and get read only")
        void shouldSetAndGetReadOnly() {
            worldData.setReadOnly(true);
            assertTrue(worldData.isReadOnly());
        }
    }

    @Nested
    @DisplayName("toPropertyMap")
    class ToPropertyMapTests {

        @Test
        @DisplayName("should convert default values to property map")
        void shouldConvertDefaultValuesToPropertyMap() {
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertNotNull(propertyMap);
            assertEquals(0, propertyMap.getValue(SlimeProperties.SPAWN_X));
            assertEquals(255, propertyMap.getValue(SlimeProperties.SPAWN_Y));
            assertEquals(0, propertyMap.getValue(SlimeProperties.SPAWN_Z));
        }

        @Test
        @DisplayName("should convert difficulty to property map")
        void shouldConvertDifficultyToPropertyMap() {
            worldData.setDifficulty("hard");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("hard", propertyMap.getValue(SlimeProperties.DIFFICULTY));
        }

        @Test
        @DisplayName("should convert boolean flags to property map")
        void shouldConvertBooleanFlagsToPropertyMap() {
            worldData.setAllowMonsters(false);
            worldData.setAllowAnimals(false);
            worldData.setDragonBattle(true);
            worldData.setPvp(false);

            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertFalse(propertyMap.getValue(SlimeProperties.ALLOW_MONSTERS));
            assertFalse(propertyMap.getValue(SlimeProperties.ALLOW_ANIMALS));
            assertTrue(propertyMap.getValue(SlimeProperties.DRAGON_BATTLE));
            assertFalse(propertyMap.getValue(SlimeProperties.PVP));
        }

        @Test
        @DisplayName("should throw for invalid difficulty")
        void shouldThrowForInvalidDifficulty() {
            worldData.setDifficulty("invalid_difficulty");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> worldData.toPropertyMap());
            assertTrue(exception.getMessage().contains("unknown difficulty"));
        }

        @Test
        @DisplayName("should throw for invalid spawn format")
        void shouldThrowForInvalidSpawnFormat() {
            worldData.setSpawn("invalid spawn");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> worldData.toPropertyMap());
            assertTrue(exception.getMessage().contains("invalid spawn location"));
        }

        @Test
        @DisplayName("should throw for non-numeric spawn")
        void shouldThrowForNonNumericSpawn() {
            worldData.setSpawn("abc, def, ghi");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> worldData.toPropertyMap());
            assertTrue(exception.getMessage().contains("invalid spawn location"));
        }

        @Test
        @DisplayName("should throw for incomplete spawn coordinates")
        void shouldThrowForIncompleteSpawnCoordinates() {
            worldData.setSpawn("100, 64");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> worldData.toPropertyMap());
            assertTrue(exception.getMessage().contains("invalid spawn location"));
        }

        @Test
        @DisplayName("should throw for invalid environment")
        void shouldThrowForInvalidEnvironment() {
            worldData.setEnvironment("invalid_environment");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> worldData.toPropertyMap());
            assertTrue(exception.getMessage().contains("unknown environment"));
        }

        @Test
        @DisplayName("should accept environment as numeric id 0")
        void shouldAcceptEnvironmentAsNumericIdZero() {
            worldData.setEnvironment("0");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("NORMAL", propertyMap.getValue(SlimeProperties.ENVIRONMENT));
        }

        @Test
        @DisplayName("should accept environment as numeric id -1")
        void shouldAcceptEnvironmentAsNumericIdMinusOne() {
            worldData.setEnvironment("-1");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("NETHER", propertyMap.getValue(SlimeProperties.ENVIRONMENT));
        }

        @Test
        @DisplayName("should accept environment as numeric id 1")
        void shouldAcceptEnvironmentAsNumericIdOne() {
            worldData.setEnvironment("1");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("THE_END", propertyMap.getValue(SlimeProperties.ENVIRONMENT));
        }

        @Test
        @DisplayName("should throw for invalid numeric environment")
        void shouldThrowForInvalidNumericEnvironment() {
            worldData.setEnvironment("5");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> worldData.toPropertyMap());
            assertTrue(exception.getMessage().contains("unknown environment"));
        }

        @Test
        @DisplayName("should accept uppercase difficulty")
        void shouldAcceptUppercaseDifficulty() {
            worldData.setDifficulty("HARD");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("HARD", propertyMap.getValue(SlimeProperties.DIFFICULTY));
        }

        @Test
        @DisplayName("should accept lowercase environment and preserve case")
        void shouldAcceptLowercaseEnvironment() {
            worldData.setEnvironment("nether");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            // Implementation validates but preserves original case
            assertEquals("nether", propertyMap.getValue(SlimeProperties.ENVIRONMENT));
        }

        @Test
        @DisplayName("should convert custom spawn to integers")
        void shouldConvertCustomSpawnToIntegers() {
            worldData.setSpawn("100.7, 64.9, -200.3");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals(100, propertyMap.getValue(SlimeProperties.SPAWN_X));
            assertEquals(64, propertyMap.getValue(SlimeProperties.SPAWN_Y));
            assertEquals(-200, propertyMap.getValue(SlimeProperties.SPAWN_Z));
        }

        @Test
        @DisplayName("should convert world type to property map")
        void shouldConvertWorldTypeToPropertyMap() {
            worldData.setWorldType("AMPLIFIED");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("AMPLIFIED", propertyMap.getValue(SlimeProperties.WORLD_TYPE));
        }

        @Test
        @DisplayName("should convert default biome to property map")
        void shouldConvertDefaultBiomeToPropertyMap() {
            worldData.setDefaultBiome("minecraft:forest");
            SlimePropertyMap propertyMap = worldData.toPropertyMap();

            assertEquals("minecraft:forest", propertyMap.getValue(SlimeProperties.DEFAULT_BIOME));
        }
    }
}
