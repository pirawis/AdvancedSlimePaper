package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyIntArray")
class SlimePropertyIntArrayTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            int[] defaultValue = new int[]{100, 200, 300};
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test.key", defaultValue);

            assertEquals("test.key", property.getKey());
            assertArrayEquals(defaultValue, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with empty array")
        void shouldCreateWithEmptyArray() {
            int[] defaultValue = new int[0];
            SlimePropertyIntArray property = SlimePropertyIntArray.create("empty", defaultValue);

            assertEquals("empty", property.getKey());
            assertEquals(0, property.getDefaultValue().length);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            int[] defaultValue = new int[]{1, 2, 3};
            SlimePropertyIntArray property = SlimePropertyIntArray.create("sized", defaultValue,
                value -> value.length <= 10);

            assertEquals("sized", property.getKey());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyIntArray.create(null, new int[0]));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("limited", new int[0],
                value -> value.length <= 5);

            assertTrue(property.applyValidator(new int[]{1, 2, 3}));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("limited", new int[0],
                value -> value.length <= 5);

            assertFalse(property.applyValidator(new int[]{1, 2, 3, 4, 5, 6}));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("any", new int[0]);

            assertTrue(property.applyValidator(new int[0]));
            assertTrue(property.applyValidator(new int[100]));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("myKey", new int[0]);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
