package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.LongBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyLong")
class SlimePropertyLongTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyLong property = SlimePropertyLong.create("test.key", 9876543210L);

            assertEquals("test.key", property.getKey());
            assertEquals(9876543210L, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyLong property = SlimePropertyLong.create("timestamp", 0L,
                value -> value >= 0);

            assertEquals("timestamp", property.getKey());
            assertEquals(0L, property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyLong.create(null, 0L));
        }

        @Test
        @DisplayName("should handle zero default value")
        void shouldHandleZeroDefault() {
            SlimePropertyLong property = SlimePropertyLong.create("zero", 0L);
            assertEquals(0L, property.getDefaultValue());
        }

        @Test
        @DisplayName("should handle negative default value")
        void shouldHandleNegativeDefault() {
            SlimePropertyLong property = SlimePropertyLong.create("negative", -999999999999L);
            assertEquals(-999999999999L, property.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create LongBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            LongBinaryTag tag = property.createTag(1234567890123L);

            assertEquals(1234567890123L, tag.value());
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            LongBinaryTag tag = property.createTag(-9876543210L);

            assertEquals(-9876543210L, tag.value());
        }

        @Test
        @DisplayName("should handle max long value")
        void shouldHandleMaxValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            LongBinaryTag tag = property.createTag(Long.MAX_VALUE);

            assertEquals(Long.MAX_VALUE, tag.value());
        }

        @Test
        @DisplayName("should handle min long value")
        void shouldHandleMinValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            LongBinaryTag tag = property.createTag(Long.MIN_VALUE);

            assertEquals(Long.MIN_VALUE, tag.value());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            LongBinaryTag tag = LongBinaryTag.longBinaryTag(5555555555L);

            assertEquals(5555555555L, property.readValue(tag));
        }

        @Test
        @DisplayName("should read zero value")
        void shouldReadZeroValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 1L);
            LongBinaryTag tag = LongBinaryTag.longBinaryTag(0L);

            assertEquals(0L, property.readValue(tag));
        }

        @Test
        @DisplayName("should read negative value")
        void shouldReadNegativeValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            LongBinaryTag tag = LongBinaryTag.longBinaryTag(-1000000000000L);

            assertEquals(-1000000000000L, property.readValue(tag));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyLong property = SlimePropertyLong.create("positiveOnly", 1L,
                value -> value > 0);

            assertTrue(property.applyValidator(100L));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyLong property = SlimePropertyLong.create("positiveOnly", 1L,
                value -> value > 0);

            assertFalse(property.applyValidator(0L));
            assertFalse(property.applyValidator(-1L));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyLong property = SlimePropertyLong.create("any", 0L);

            assertTrue(property.applyValidator(Long.MIN_VALUE));
            assertTrue(property.applyValidator(Long.MAX_VALUE));
        }

        @Test
        @DisplayName("should validate timestamp range")
        void shouldValidateTimestampRange() {
            SlimePropertyLong property = SlimePropertyLong.create("timestamp", 0L,
                value -> value >= 0 && value <= System.currentTimeMillis() + 86400000L);

            assertTrue(property.applyValidator(System.currentTimeMillis()));
            assertFalse(property.applyValidator(-1L));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyLong property = SlimePropertyLong.create("worldSeed", 0L);
            assertEquals("worldSeed", property.getKey());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyLong property = SlimePropertyLong.create("myKey", 100L);
            assertTrue(property.toString().contains("myKey"));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            long original = 9999999999999L;

            LongBinaryTag tag = property.createTag(original);
            Long result = property.readValue(tag);

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip max value correctly")
        void shouldRoundtripMaxValue() {
            SlimePropertyLong property = SlimePropertyLong.create("test", 0L);
            long original = Long.MAX_VALUE;

            LongBinaryTag tag = property.createTag(original);
            Long result = property.readValue(tag);

            assertEquals(original, result);
        }
    }
}
