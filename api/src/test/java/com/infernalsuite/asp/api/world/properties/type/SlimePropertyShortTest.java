package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.ShortBinaryTag;
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

        @Test
        @DisplayName("should handle zero default value")
        void shouldHandleZeroDefault() {
            SlimePropertyShort property = SlimePropertyShort.create("zero", (short) 0);
            assertEquals((short) 0, property.getDefaultValue());
        }

        @Test
        @DisplayName("should handle negative default value")
        void shouldHandleNegativeDefault() {
            SlimePropertyShort property = SlimePropertyShort.create("negative", (short) -500);
            assertEquals((short) -500, property.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create ShortBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            ShortBinaryTag tag = property.createTag((short) 999);

            assertEquals((short) 999, tag.value());
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            ShortBinaryTag tag = property.createTag((short) -1000);

            assertEquals((short) -1000, tag.value());
        }

        @Test
        @DisplayName("should handle max short value")
        void shouldHandleMaxValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            ShortBinaryTag tag = property.createTag(Short.MAX_VALUE);

            assertEquals(Short.MAX_VALUE, tag.value());
        }

        @Test
        @DisplayName("should handle min short value")
        void shouldHandleMinValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            ShortBinaryTag tag = property.createTag(Short.MIN_VALUE);

            assertEquals(Short.MIN_VALUE, tag.value());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            ShortBinaryTag tag = ShortBinaryTag.shortBinaryTag((short) 777);

            assertEquals((short) 777, property.readValue(tag));
        }

        @Test
        @DisplayName("should read zero value")
        void shouldReadZeroValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 1);
            ShortBinaryTag tag = ShortBinaryTag.shortBinaryTag((short) 0);

            assertEquals((short) 0, property.readValue(tag));
        }

        @Test
        @DisplayName("should read negative value")
        void shouldReadNegativeValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            ShortBinaryTag tag = ShortBinaryTag.shortBinaryTag((short) -12345);

            assertEquals((short) -12345, property.readValue(tag));
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

        @Test
        @DisplayName("should validate boundary values")
        void shouldValidateBoundaryValues() {
            SlimePropertyShort property = SlimePropertyShort.create("port", (short) 8080,
                value -> value >= 1 && value <= 32767);

            assertTrue(property.applyValidator((short) 1));
            assertTrue(property.applyValidator((short) 32767));
            assertFalse(property.applyValidator((short) 0));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyShort property = SlimePropertyShort.create("dataVersion", (short) 0);
            assertEquals("dataVersion", property.getKey());
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

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            short original = (short) 12345;

            ShortBinaryTag tag = property.createTag(original);
            Short result = property.readValue(tag);

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip negative value correctly")
        void shouldRoundtripNegativeValue() {
            SlimePropertyShort property = SlimePropertyShort.create("test", (short) 0);
            short original = (short) -9876;

            ShortBinaryTag tag = property.createTag(original);
            Short result = property.readValue(tag);

            assertEquals(original, result);
        }
    }
}
