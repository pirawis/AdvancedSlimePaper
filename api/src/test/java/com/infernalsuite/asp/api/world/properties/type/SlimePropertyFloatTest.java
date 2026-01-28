package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyFloat")
class SlimePropertyFloatTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test.key", 3.14f);

            assertEquals("test.key", property.getKey());
            assertEquals(3.14f, property.getDefaultValue(), 0.001f);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyFloat property = SlimePropertyFloat.create("percentage", 0.5f,
                value -> value >= 0.0f && value <= 1.0f);

            assertEquals("percentage", property.getKey());
            assertEquals(0.5f, property.getDefaultValue(), 0.001f);
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyFloat.create(null, 0.0f));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyFloat property = SlimePropertyFloat.create("ratio", 0.5f,
                value -> value >= 0.0f && value <= 1.0f);

            assertTrue(property.applyValidator(0.75f));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyFloat property = SlimePropertyFloat.create("ratio", 0.5f,
                value -> value >= 0.0f && value <= 1.0f);

            assertFalse(property.applyValidator(-0.1f));
            assertFalse(property.applyValidator(1.1f));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyFloat property = SlimePropertyFloat.create("any", 0.0f);

            assertTrue(property.applyValidator(Float.MIN_VALUE));
            assertTrue(property.applyValidator(Float.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyFloat property = SlimePropertyFloat.create("myKey", 1.5f);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
