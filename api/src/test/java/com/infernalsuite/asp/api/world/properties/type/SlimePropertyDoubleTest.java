package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyDouble")
class SlimePropertyDoubleTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test.key", 3.14159);

            assertEquals("test.key", property.getKey());
            assertEquals(3.14159, property.getDefaultValue(), 0.00001);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyDouble property = SlimePropertyDouble.create("percentage", 0.5,
                value -> value >= 0.0 && value <= 1.0);

            assertEquals("percentage", property.getKey());
            assertEquals(0.5, property.getDefaultValue(), 0.001);
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyDouble.create(null, 0.0));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyDouble property = SlimePropertyDouble.create("ratio", 0.5,
                value -> value >= 0.0 && value <= 1.0);

            assertTrue(property.applyValidator(0.75));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyDouble property = SlimePropertyDouble.create("ratio", 0.5,
                value -> value >= 0.0 && value <= 1.0);

            assertFalse(property.applyValidator(-0.1));
            assertFalse(property.applyValidator(1.1));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyDouble property = SlimePropertyDouble.create("any", 0.0);

            assertTrue(property.applyValidator(Double.MIN_VALUE));
            assertTrue(property.applyValidator(Double.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyDouble property = SlimePropertyDouble.create("myKey", 1.5);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
