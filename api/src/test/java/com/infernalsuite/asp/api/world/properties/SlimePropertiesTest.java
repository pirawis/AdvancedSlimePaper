package com.infernalsuite.asp.api.world.properties;

import com.infernalsuite.asp.api.world.properties.type.SlimePropertyBoolean;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyFloat;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyInt;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeProperties")
class SlimePropertiesTest {

    @Nested
    @DisplayName("spawn coordinates")
    class SpawnCoordinateTests {

        @Test
        @DisplayName("SPAWN_X should have default value 0")
        void spawnXShouldHaveDefaultValue0() {
            assertEquals(0, SlimeProperties.SPAWN_X.getDefaultValue());
        }

        @Test
        @DisplayName("SPAWN_Y should have default value 255")
        void spawnYShouldHaveDefaultValue255() {
            assertEquals(255, SlimeProperties.SPAWN_Y.getDefaultValue());
        }

        @Test
        @DisplayName("SPAWN_Z should have default value 0")
        void spawnZShouldHaveDefaultValue0() {
            assertEquals(0, SlimeProperties.SPAWN_Z.getDefaultValue());
        }

        @Test
        @DisplayName("SPAWN_YAW should have default value 0.0")
        void spawnYawShouldHaveDefaultValue0() {
            assertEquals(0.0f, SlimeProperties.SPAWN_YAW.getDefaultValue());
        }

        @Test
        @DisplayName("SPAWN_X should have correct key")
        void spawnXShouldHaveCorrectKey() {
            assertEquals("spawnX", SlimeProperties.SPAWN_X.getKey());
        }

        @Test
        @DisplayName("SPAWN_Y should have correct key")
        void spawnYShouldHaveCorrectKey() {
            assertEquals("spawnY", SlimeProperties.SPAWN_Y.getKey());
        }

        @Test
        @DisplayName("SPAWN_Z should have correct key")
        void spawnZShouldHaveCorrectKey() {
            assertEquals("spawnZ", SlimeProperties.SPAWN_Z.getKey());
        }
    }

    @Nested
    @DisplayName("difficulty property")
    class DifficultyTests {

        @Test
        @DisplayName("should have default value peaceful")
        void shouldHaveDefaultValuePeaceful() {
            assertEquals("peaceful", SlimeProperties.DIFFICULTY.getDefaultValue());
        }

        @Test
        @DisplayName("should validate peaceful")
        void shouldValidatePeaceful() {
            assertTrue(SlimeProperties.DIFFICULTY.getValidator().apply("peaceful"));
        }

        @Test
        @DisplayName("should validate easy")
        void shouldValidateEasy() {
            assertTrue(SlimeProperties.DIFFICULTY.getValidator().apply("easy"));
        }

        @Test
        @DisplayName("should validate normal")
        void shouldValidateNormal() {
            assertTrue(SlimeProperties.DIFFICULTY.getValidator().apply("normal"));
        }

        @Test
        @DisplayName("should validate hard")
        void shouldValidateHard() {
            assertTrue(SlimeProperties.DIFFICULTY.getValidator().apply("hard"));
        }

        @Test
        @DisplayName("should validate case insensitive")
        void shouldValidateCaseInsensitive() {
            assertTrue(SlimeProperties.DIFFICULTY.getValidator().apply("PEACEFUL"));
            assertTrue(SlimeProperties.DIFFICULTY.getValidator().apply("Normal"));
        }

        @Test
        @DisplayName("should reject invalid difficulty")
        void shouldRejectInvalidDifficulty() {
            assertFalse(SlimeProperties.DIFFICULTY.getValidator().apply("impossible"));
        }
    }

    @Nested
    @DisplayName("boolean properties")
    class BooleanPropertyTests {

        @Test
        @DisplayName("ALLOW_MONSTERS should default to true")
        void allowMonstersShouldDefaultToTrue() {
            assertTrue(SlimeProperties.ALLOW_MONSTERS.getDefaultValue());
        }

        @Test
        @DisplayName("ALLOW_ANIMALS should default to true")
        void allowAnimalsShouldDefaultToTrue() {
            assertTrue(SlimeProperties.ALLOW_ANIMALS.getDefaultValue());
        }

        @Test
        @DisplayName("DRAGON_BATTLE should default to false")
        void dragonBattleShouldDefaultToFalse() {
            assertFalse(SlimeProperties.DRAGON_BATTLE.getDefaultValue());
        }

        @Test
        @DisplayName("PVP should default to true")
        void pvpShouldDefaultToTrue() {
            assertTrue(SlimeProperties.PVP.getDefaultValue());
        }

        @Test
        @DisplayName("SAVE_POI should default to false")
        void savePoiShouldDefaultToFalse() {
            assertFalse(SlimeProperties.SAVE_POI.getDefaultValue());
        }

        @Test
        @DisplayName("SAVE_BLOCK_TICKS should default to false")
        void saveBlockTicksShouldDefaultToFalse() {
            assertFalse(SlimeProperties.SAVE_BLOCK_TICKS.getDefaultValue());
        }

        @Test
        @DisplayName("SAVE_FLUID_TICKS should default to false")
        void saveFluidTicksShouldDefaultToFalse() {
            assertFalse(SlimeProperties.SAVE_FLUID_TICKS.getDefaultValue());
        }

        @Test
        @DisplayName("SHOULD_LIMIT_SAVE should default to false")
        void shouldLimitSaveShouldDefaultToFalse() {
            assertFalse(SlimeProperties.SHOULD_LIMIT_SAVE.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("environment property")
    class EnvironmentTests {

        @Test
        @DisplayName("should have default value normal")
        void shouldHaveDefaultValueNormal() {
            assertEquals("normal", SlimeProperties.ENVIRONMENT.getDefaultValue());
        }

        @Test
        @DisplayName("should validate normal")
        void shouldValidateNormal() {
            assertTrue(SlimeProperties.ENVIRONMENT.getValidator().apply("normal"));
        }

        @Test
        @DisplayName("should validate nether")
        void shouldValidateNether() {
            assertTrue(SlimeProperties.ENVIRONMENT.getValidator().apply("nether"));
        }

        @Test
        @DisplayName("should validate the_end")
        void shouldValidateTheEnd() {
            assertTrue(SlimeProperties.ENVIRONMENT.getValidator().apply("the_end"));
        }

        @Test
        @DisplayName("should reject invalid environment")
        void shouldRejectInvalidEnvironment() {
            assertFalse(SlimeProperties.ENVIRONMENT.getValidator().apply("custom"));
        }
    }

    @Nested
    @DisplayName("world type property")
    class WorldTypeTests {

        @Test
        @DisplayName("should have default value default")
        void shouldHaveDefaultValueDefault() {
            assertEquals("default", SlimeProperties.WORLD_TYPE.getDefaultValue());
        }

        @Test
        @DisplayName("should validate all world types")
        void shouldValidateAllWorldTypes() {
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("default"));
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("flat"));
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("large_biomes"));
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("amplified"));
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("customized"));
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("debug_all_block_states"));
            assertTrue(SlimeProperties.WORLD_TYPE.getValidator().apply("default_1_1"));
        }

        @Test
        @DisplayName("should reject invalid world type")
        void shouldRejectInvalidWorldType() {
            assertFalse(SlimeProperties.WORLD_TYPE.getValidator().apply("invalid"));
        }
    }

    @Nested
    @DisplayName("chunk pruning property")
    class ChunkPruningTests {

        @Test
        @DisplayName("should have default value aggressive")
        void shouldHaveDefaultValueAggressive() {
            assertEquals("aggressive", SlimeProperties.CHUNK_PRUNING.getDefaultValue());
        }

        @Test
        @DisplayName("should validate aggressive")
        void shouldValidateAggressive() {
            assertTrue(SlimeProperties.CHUNK_PRUNING.getValidator().apply("aggressive"));
        }

        @Test
        @DisplayName("should validate never")
        void shouldValidateNever() {
            assertTrue(SlimeProperties.CHUNK_PRUNING.getValidator().apply("never"));
        }

        @Test
        @DisplayName("should reject invalid pruning value")
        void shouldRejectInvalidPruningValue() {
            assertFalse(SlimeProperties.CHUNK_PRUNING.getValidator().apply("sometimes"));
        }
    }

    @Nested
    @DisplayName("save bounds properties")
    class SaveBoundsTests {

        @Test
        @DisplayName("SAVE_MIN_X should default to 0")
        void saveMinXShouldDefaultTo0() {
            assertEquals(0, SlimeProperties.SAVE_MIN_X.getDefaultValue());
        }

        @Test
        @DisplayName("SAVE_MIN_Z should default to 0")
        void saveMinZShouldDefaultTo0() {
            assertEquals(0, SlimeProperties.SAVE_MIN_Z.getDefaultValue());
        }

        @Test
        @DisplayName("SAVE_MAX_X should default to 0")
        void saveMaxXShouldDefaultTo0() {
            assertEquals(0, SlimeProperties.SAVE_MAX_X.getDefaultValue());
        }

        @Test
        @DisplayName("SAVE_MAX_Z should default to 0")
        void saveMaxZShouldDefaultTo0() {
            assertEquals(0, SlimeProperties.SAVE_MAX_Z.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("chunk section properties")
    class ChunkSectionTests {

        @Test
        @DisplayName("CHUNK_SECTION_MIN should default to -4")
        void chunkSectionMinShouldDefaultToMinus4() {
            assertEquals(-4, SlimeProperties.CHUNK_SECTION_MIN.getDefaultValue());
        }

        @Test
        @DisplayName("CHUNK_SECTION_MAX should default to 19")
        void chunkSectionMaxShouldDefaultTo19() {
            assertEquals(19, SlimeProperties.CHUNK_SECTION_MAX.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("sea level property")
    class SeaLevelTests {

        @Test
        @DisplayName("should default to -63")
        void shouldDefaultToMinus63() {
            assertEquals(-63, SlimeProperties.SEA_LEVEL.getDefaultValue());
        }

        @Test
        @DisplayName("should have correct key")
        void shouldHaveCorrectKey() {
            assertEquals("seaLevel", SlimeProperties.SEA_LEVEL.getKey());
        }
    }

    @Nested
    @DisplayName("default biome property")
    class DefaultBiomeTests {

        @Test
        @DisplayName("should default to minecraft:plains")
        void shouldDefaultToMinecraftPlains() {
            assertEquals("minecraft:plains", SlimeProperties.DEFAULT_BIOME.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("property types")
    class PropertyTypeTests {

        @Test
        @DisplayName("spawn coordinates should be SlimePropertyInt")
        void spawnCoordinatesShouldBeSlimePropertyInt() {
            assertInstanceOf(SlimePropertyInt.class, SlimeProperties.SPAWN_X);
            assertInstanceOf(SlimePropertyInt.class, SlimeProperties.SPAWN_Y);
            assertInstanceOf(SlimePropertyInt.class, SlimeProperties.SPAWN_Z);
        }

        @Test
        @DisplayName("spawn yaw should be SlimePropertyFloat")
        void spawnYawShouldBeSlimePropertyFloat() {
            assertInstanceOf(SlimePropertyFloat.class, SlimeProperties.SPAWN_YAW);
        }

        @Test
        @DisplayName("boolean properties should be SlimePropertyBoolean")
        void booleanPropertiesShouldBeSlimePropertyBoolean() {
            assertInstanceOf(SlimePropertyBoolean.class, SlimeProperties.ALLOW_MONSTERS);
            assertInstanceOf(SlimePropertyBoolean.class, SlimeProperties.ALLOW_ANIMALS);
            assertInstanceOf(SlimePropertyBoolean.class, SlimeProperties.PVP);
        }

        @Test
        @DisplayName("string properties should be SlimePropertyString")
        void stringPropertiesShouldBeSlimePropertyString() {
            assertInstanceOf(SlimePropertyString.class, SlimeProperties.DIFFICULTY);
            assertInstanceOf(SlimePropertyString.class, SlimeProperties.ENVIRONMENT);
            assertInstanceOf(SlimePropertyString.class, SlimeProperties.WORLD_TYPE);
        }
    }
}
