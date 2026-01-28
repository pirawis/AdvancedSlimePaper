package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyString")
class SlimePropertyStringTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyString property = SlimePropertyString.create("world.name", "default");

            assertEquals("world.name", property.getKey());
            assertEquals("default", property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with empty default")
        void shouldCreateWithEmptyDefault() {
            SlimePropertyString property = SlimePropertyString.create("empty", "");

            assertEquals("", property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyString property = SlimePropertyString.create("difficulty", "normal",
                value -> value.equals("easy") || value.equals("normal") || value.equals("hard"));

            assertEquals("difficulty", property.getKey());
            assertEquals("normal", property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyString.create(null, "value"));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation when no validator")
        void shouldPassValidationWhenNoValidator() {
            SlimePropertyString property = SlimePropertyString.create("any", "");

            assertTrue(property.applyValidator("anything"));
            assertTrue(property.applyValidator(""));
            assertTrue(property.applyValidator("12345"));
        }

        @Test
        @DisplayName("should apply difficulty validator")
        void shouldApplyDifficultyValidator() {
            SlimePropertyString property = SlimePropertyString.create("difficulty", "normal",
                value -> value.equalsIgnoreCase("peaceful") ||
                        value.equalsIgnoreCase("easy") ||
                        value.equalsIgnoreCase("normal") ||
                        value.equalsIgnoreCase("hard"));

            assertTrue(property.applyValidator("peaceful"));
            assertTrue(property.applyValidator("EASY"));
            assertTrue(property.applyValidator("Normal"));
            assertTrue(property.applyValidator("hard"));
            assertFalse(property.applyValidator("impossible"));
            assertFalse(property.applyValidator(""));
        }

        @Test
        @DisplayName("should apply environment validator")
        void shouldApplyEnvironmentValidator() {
            SlimePropertyString property = SlimePropertyString.create("environment", "normal",
                value -> value.equalsIgnoreCase("normal") ||
                        value.equalsIgnoreCase("nether") ||
                        value.equalsIgnoreCase("the_end"));

            assertTrue(property.applyValidator("normal"));
            assertTrue(property.applyValidator("nether"));
            assertTrue(property.applyValidator("the_end"));
            assertFalse(property.applyValidator("void"));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyString property = SlimePropertyString.create("myKey", "value");
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
