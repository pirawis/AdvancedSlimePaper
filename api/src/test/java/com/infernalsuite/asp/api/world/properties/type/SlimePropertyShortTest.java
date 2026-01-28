package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyShort")
class SlimePropertyShortTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyShort property = SlimePropertyShort.create("test.key", (short) 1234);

            assertEquals("test.key", property.getKey());
            assertEquals((short) 1234, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyShort property = SlimePropertyShort.create("level", (short) 50,
                value -> value >= 1 && value <= 100);

            assertEquals("level", property.getKey());
            assertEquals((short) 50, property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyShort.create(null, (short) 0));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyShort property = SlimePropertyShort.create("level", (short) 1,
                value -> value >= 1 && value <= 100);

            assertTrue(property.applyValidator((short) 50));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyShort property = SlimePropertyShort.create("level", (short) 1,
                value -> value >= 1 && value <= 100);

            assertFalse(property.applyValidator((short) 0));
            assertFalse(property.applyValidator((short) 101));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyShort property = SlimePropertyShort.create("any", (short) 0);

            assertTrue(property.applyValidator(Short.MIN_VALUE));
            assertTrue(property.applyValidator(Short.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyShort property = SlimePropertyShort.create("myKey", (short) 100);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
