package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyByteArray")
class SlimePropertyByteArrayTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            byte[] defaultValue = new byte[]{1, 2, 3};
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test.key", defaultValue);

            assertEquals("test.key", property.getKey());
            assertArrayEquals(defaultValue, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with empty array")
        void shouldCreateWithEmptyArray() {
            byte[] defaultValue = new byte[0];
            SlimePropertyByteArray property = SlimePropertyByteArray.create("empty", defaultValue);

            assertEquals("empty", property.getKey());
            assertEquals(0, property.getDefaultValue().length);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            byte[] defaultValue = new byte[]{1, 2, 3};
            SlimePropertyByteArray property = SlimePropertyByteArray.create("sized", defaultValue,
                value -> value.length <= 10);

            assertEquals("sized", property.getKey());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyByteArray.create(null, new byte[0]));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("limited", new byte[0],
                value -> value.length <= 5);

            assertTrue(property.applyValidator(new byte[]{1, 2, 3}));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("limited", new byte[0],
                value -> value.length <= 5);

            assertFalse(property.applyValidator(new byte[]{1, 2, 3, 4, 5, 6}));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("any", new byte[0]);

            assertTrue(property.applyValidator(new byte[0]));
            assertTrue(property.applyValidator(new byte[100]));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("myKey", new byte[0]);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
