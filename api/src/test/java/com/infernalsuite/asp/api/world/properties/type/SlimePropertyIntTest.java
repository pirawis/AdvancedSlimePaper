package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.IntBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyInt")
class SlimePropertyIntTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyInt property = SlimePropertyInt.create("test.key", 42);

            assertEquals("test.key", property.getKey());
            assertEquals(42, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyInt property = SlimePropertyInt.create("range", 50,
                value -> value >= 0 && value <= 100);

            assertEquals("range", property.getKey());
            assertEquals(50, property.getDefaultValue());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyInt.create(null, 0));
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create IntBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyInt property = SlimePropertyInt.create("test", 0);
            IntBinaryTag tag = property.createTag(123);

            assertEquals(123, tag.value());
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyInt property = SlimePropertyInt.create("test", 0);
            IntBinaryTag tag = property.createTag(-500);

            assertEquals(-500, tag.value());
        }

        @Test
        @DisplayName("should handle max int value")
        void shouldHandleMaxValue() {
            SlimePropertyInt property = SlimePropertyInt.create("test", 0);
            IntBinaryTag tag = property.createTag(Integer.MAX_VALUE);

            assertEquals(Integer.MAX_VALUE, tag.value());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyInt property = SlimePropertyInt.create("test", 0);
            IntBinaryTag tag = IntBinaryTag.intBinaryTag(999);

            assertEquals(999, property.readValue(tag));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyInt property = SlimePropertyInt.create("level", 1,
                value -> value >= 1 && value <= 10);

            assertTrue(property.applyValidator(5));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyInt property = SlimePropertyInt.create("level", 1,
                value -> value >= 1 && value <= 10);

            assertFalse(property.applyValidator(0));
            assertFalse(property.applyValidator(11));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyInt property = SlimePropertyInt.create("any", 0);

            assertTrue(property.applyValidator(Integer.MIN_VALUE));
            assertTrue(property.applyValidator(Integer.MAX_VALUE));
        }
    }
}
