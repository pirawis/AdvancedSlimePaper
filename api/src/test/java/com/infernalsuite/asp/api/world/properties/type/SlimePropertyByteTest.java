package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyByte")
class SlimePropertyByteTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyByte property = SlimePropertyByte.create("test.key", (byte) 42);

            assertEquals("test.key", property.getKey());
            assertEquals((byte) 42, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyByte property = SlimePropertyByte.create("range", (byte) 50,
                value -> value >= 0 && value <= 100);

            assertEquals("range", property.getKey());
            assertEquals((byte) 50, property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyByte.create(null, (byte) 0));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyByte property = SlimePropertyByte.create("level", (byte) 1,
                value -> value >= 1 && value <= 10);

            assertTrue(property.applyValidator((byte) 5));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyByte property = SlimePropertyByte.create("level", (byte) 1,
                value -> value >= 1 && value <= 10);

            assertFalse(property.applyValidator((byte) 0));
            assertFalse(property.applyValidator((byte) 11));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyByte property = SlimePropertyByte.create("any", (byte) 0);

            assertTrue(property.applyValidator(Byte.MIN_VALUE));
            assertTrue(property.applyValidator(Byte.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyByte property = SlimePropertyByte.create("myKey", (byte) 10);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
