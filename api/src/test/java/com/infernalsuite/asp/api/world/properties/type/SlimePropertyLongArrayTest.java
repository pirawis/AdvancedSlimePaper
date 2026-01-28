package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyLongArray")
class SlimePropertyLongArrayTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            long[] defaultValue = new long[]{100L, 200L, 300L};
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test.key", defaultValue);

            assertEquals("test.key", property.getKey());
            assertArrayEquals(defaultValue, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with empty array")
        void shouldCreateWithEmptyArray() {
            long[] defaultValue = new long[0];
            SlimePropertyLongArray property = SlimePropertyLongArray.create("empty", defaultValue);

            assertEquals("empty", property.getKey());
            assertEquals(0, property.getDefaultValue().length);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            long[] defaultValue = new long[]{1L, 2L, 3L};
            SlimePropertyLongArray property = SlimePropertyLongArray.create("sized", defaultValue,
                value -> value.length <= 10);

            assertEquals("sized", property.getKey());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyLongArray.create(null, new long[0]));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("limited", new long[0],
                value -> value.length <= 5);

            assertTrue(property.applyValidator(new long[]{1L, 2L, 3L}));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("limited", new long[0],
                value -> value.length <= 5);

            assertFalse(property.applyValidator(new long[]{1L, 2L, 3L, 4L, 5L, 6L}));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("any", new long[0]);

            assertTrue(property.applyValidator(new long[0]));
            assertTrue(property.applyValidator(new long[100]));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("myKey", new long[0]);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
