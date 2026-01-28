package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.LongArrayBinaryTag;
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
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create LongArrayBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            LongArrayBinaryTag tag = property.createTag(new long[]{100000000000L, 200000000000L, 300000000000L});

            assertArrayEquals(new long[]{100000000000L, 200000000000L, 300000000000L}, tag.value());
        }

        @Test
        @DisplayName("should handle empty array")
        void shouldHandleEmptyArray() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            LongArrayBinaryTag tag = property.createTag(new long[0]);

            assertEquals(0, tag.value().length);
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            LongArrayBinaryTag tag = property.createTag(new long[]{Long.MIN_VALUE, -999999999999L, 0L, 999999999999L, Long.MAX_VALUE});

            assertArrayEquals(new long[]{Long.MIN_VALUE, -999999999999L, 0L, 999999999999L, Long.MAX_VALUE}, tag.value());
        }

        @Test
        @DisplayName("should handle large array")
        void shouldHandleLargeArray() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            long[] largeArray = new long[1000];
            for (int i = 0; i < largeArray.length; i++) {
                largeArray[i] = (long) i * 1000000000L;
            }
            LongArrayBinaryTag tag = property.createTag(largeArray);

            assertEquals(1000, tag.value().length);
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            LongArrayBinaryTag tag = LongArrayBinaryTag.longArrayBinaryTag(new long[]{5000000000L, 6000000000L, 7000000000L});

            assertArrayEquals(new long[]{5000000000L, 6000000000L, 7000000000L}, property.readValue(tag));
        }

        @Test
        @DisplayName("should read empty array")
        void shouldReadEmptyArray() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            LongArrayBinaryTag tag = LongArrayBinaryTag.longArrayBinaryTag(new long[0]);

            assertEquals(0, property.readValue(tag).length);
        }

        @Test
        @DisplayName("should read min and max values")
        void shouldReadMinMaxValues() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            LongArrayBinaryTag tag = LongArrayBinaryTag.longArrayBinaryTag(new long[]{Long.MIN_VALUE, Long.MAX_VALUE});

            long[] result = property.readValue(tag);
            assertEquals(Long.MIN_VALUE, result[0]);
            assertEquals(Long.MAX_VALUE, result[1]);
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            long[] original = {1000000000000L, 2000000000000L, 3000000000000L};

            LongArrayBinaryTag tag = property.createTag(original);
            long[] result = property.readValue(tag);

            assertArrayEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip empty array correctly")
        void shouldRoundtripEmptyArray() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            long[] original = new long[0];

            LongArrayBinaryTag tag = property.createTag(original);
            long[] result = property.readValue(tag);

            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should roundtrip extreme values correctly")
        void shouldRoundtripExtremeValues() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("test", new long[0]);
            long[] original = {Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE};

            LongArrayBinaryTag tag = property.createTag(original);
            long[] result = property.readValue(tag);

            assertArrayEquals(original, result);
        }
    }

    @Nested
    @DisplayName("getKey")
    class GetKeyTests {

        @Test
        @DisplayName("should return key")
        void shouldReturnKey() {
            SlimePropertyLongArray property = SlimePropertyLongArray.create("uuidArray", new long[0]);
            assertEquals("uuidArray", property.getKey());
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
