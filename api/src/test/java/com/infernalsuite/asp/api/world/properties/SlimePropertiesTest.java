package com.infernalsuite.asp.api.world.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SlimeProperties constants and SlimePropertyMap functionality.
 */
@DisplayName("SlimeProperties Tests")
class SlimePropertiesTest {

    @Nested
    @DisplayName("SlimePropertyMap")
    class SlimePropertyMapTests {

        @Test
        @DisplayName("should create empty property map")
        void shouldCreateEmptyPropertyMap() {
            SlimePropertyMap map = new SlimePropertyMap();
            assertNotNull(map);
        }

        @Test
        @DisplayName("should set and get spawn coordinates")
        void shouldSetAndGetSpawnCoordinates() {
            SlimePropertyMap map = new SlimePropertyMap();

            map.setValue(SlimeProperties.SPAWN_X, 100);
            map.setValue(SlimeProperties.SPAWN_Y, 64);
            map.setValue(SlimeProperties.SPAWN_Z, -100);

            assertEquals(100, map.getValue(SlimeProperties.SPAWN_X));
            assertEquals(64, map.getValue(SlimeProperties.SPAWN_Y));
            assertEquals(-100, map.getValue(SlimeProperties.SPAWN_Z));
        }

        @Test
        @DisplayName("should set and get difficulty")
        void shouldSetAndGetDifficulty() {
            SlimePropertyMap map = new SlimePropertyMap();

            map.setValue(SlimeProperties.DIFFICULTY, "hard");

            assertEquals("hard", map.getValue(SlimeProperties.DIFFICULTY));
        }

        @Test
        @DisplayName("should set and get boolean properties")
        void shouldSetAndGetBooleanProperties() {
            SlimePropertyMap map = new SlimePropertyMap();

            map.setValue(SlimeProperties.ALLOW_MONSTERS, false);
            map.setValue(SlimeProperties.ALLOW_ANIMALS, true);
            map.setValue(SlimeProperties.PVP, true);

            assertFalse(map.getValue(SlimeProperties.ALLOW_MONSTERS));
            assertTrue(map.getValue(SlimeProperties.ALLOW_ANIMALS));
            assertTrue(map.getValue(SlimeProperties.PVP));
        }

        @Test
        @DisplayName("should return default values for unset properties")
        void shouldReturnDefaultValues() {
            SlimePropertyMap map = new SlimePropertyMap();

            // Default spawn coordinates
            assertEquals(0, map.getValue(SlimeProperties.SPAWN_X));
            assertEquals(255, map.getValue(SlimeProperties.SPAWN_Y));
            assertEquals(0, map.getValue(SlimeProperties.SPAWN_Z));

            // Default difficulty
            assertEquals("peaceful", map.getValue(SlimeProperties.DIFFICULTY));

            // Default boolean values
            assertTrue(map.getValue(SlimeProperties.ALLOW_MONSTERS));
            assertTrue(map.getValue(SlimeProperties.ALLOW_ANIMALS));
            assertTrue(map.getValue(SlimeProperties.PVP));
        }

        @Test
        @DisplayName("should copy properties from another map")
        void shouldCopyPropertiesFromAnotherMap() {
            SlimePropertyMap original = new SlimePropertyMap();
            original.setValue(SlimeProperties.SPAWN_X, 500);
            original.setValue(SlimeProperties.DIFFICULTY, "normal");

            SlimePropertyMap copy = original.clone();

            assertEquals(500, copy.getValue(SlimeProperties.SPAWN_X));
            assertEquals("normal", copy.getValue(SlimeProperties.DIFFICULTY));
        }

        @Test
        @DisplayName("should merge properties from another map")
        void shouldMergePropertiesFromAnotherMap() {
            SlimePropertyMap base = new SlimePropertyMap();
            base.setValue(SlimeProperties.SPAWN_X, 100);
            base.setValue(SlimeProperties.DIFFICULTY, "easy");

            SlimePropertyMap override = new SlimePropertyMap();
            override.setValue(SlimeProperties.SPAWN_X, 200);
            override.setValue(SlimeProperties.PVP, false);

            base.merge(override);

            assertEquals(200, base.getValue(SlimeProperties.SPAWN_X));
            assertEquals("easy", base.getValue(SlimeProperties.DIFFICULTY));
            assertFalse(base.getValue(SlimeProperties.PVP));
        }
    }

    @Nested
    @DisplayName("SlimeProperties Constants")
    class SlimePropertiesConstantsTests {

        @Test
        @DisplayName("SPAWN_X should have correct default value")
        void spawnXShouldHaveCorrectDefaultValue() {
            assertEquals(0, SlimeProperties.SPAWN_X.getDefaultValue());
        }

        @Test
        @DisplayName("SPAWN_Y should have correct default value")
        void spawnYShouldHaveCorrectDefaultValue() {
            assertEquals(255, SlimeProperties.SPAWN_Y.getDefaultValue());
        }

        @Test
        @DisplayName("DIFFICULTY should have correct default value")
        void difficultyShouldHaveCorrectDefaultValue() {
            assertEquals("peaceful", SlimeProperties.DIFFICULTY.getDefaultValue());
        }

        @Test
        @DisplayName("ENVIRONMENT should have correct default value")
        void environmentShouldHaveCorrectDefaultValue() {
            assertEquals("NORMAL", SlimeProperties.ENVIRONMENT.getDefaultValue());
        }
    }
}
