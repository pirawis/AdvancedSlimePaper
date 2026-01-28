package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.IntArrayBinaryTag;
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
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create IntArrayBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            IntArrayBinaryTag tag = property.createTag(new int[]{100, 200, 300});

            assertArrayEquals(new int[]{100, 200, 300}, tag.value());
        }

        @Test
        @DisplayName("should handle empty array")
        void shouldHandleEmptyArray() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            IntArrayBinaryTag tag = property.createTag(new int[0]);

            assertEquals(0, tag.value().length);
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            IntArrayBinaryTag tag = property.createTag(new int[]{Integer.MIN_VALUE, -1000, 0, 1000, Integer.MAX_VALUE});

            assertArrayEquals(new int[]{Integer.MIN_VALUE, -1000, 0, 1000, Integer.MAX_VALUE}, tag.value());
        }

        @Test
        @DisplayName("should handle large array")
        void shouldHandleLargeArray() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            int[] largeArray = new int[1000];
            for (int i = 0; i < largeArray.length; i++) {
                largeArray[i] = i * 100;
            }
            IntArrayBinaryTag tag = property.createTag(largeArray);

            assertEquals(1000, tag.value().length);
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            IntArrayBinaryTag tag = IntArrayBinaryTag.intArrayBinaryTag(new int[]{500, 600, 700});

            assertArrayEquals(new int[]{500, 600, 700}, property.readValue(tag));
        }

        @Test
        @DisplayName("should read empty array")
        void shouldReadEmptyArray() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            IntArrayBinaryTag tag = IntArrayBinaryTag.intArrayBinaryTag(new int[0]);

            assertEquals(0, property.readValue(tag).length);
        }

        @Test
        @DisplayName("should read min and max values")
        void shouldReadMinMaxValues() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            IntArrayBinaryTag tag = IntArrayBinaryTag.intArrayBinaryTag(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE});

            int[] result = property.readValue(tag);
            assertEquals(Integer.MIN_VALUE, result[0]);
            assertEquals(Integer.MAX_VALUE, result[1]);
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            int[] original = {1000, 2000, 3000, 4000, 5000};

            IntArrayBinaryTag tag = property.createTag(original);
            int[] result = property.readValue(tag);

            assertArrayEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip empty array correctly")
        void shouldRoundtripEmptyArray() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            int[] original = new int[0];

            IntArrayBinaryTag tag = property.createTag(original);
            int[] result = property.readValue(tag);

            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should roundtrip negative values correctly")
        void shouldRoundtripNegativeValues() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("test", new int[0]);
            int[] original = {-100000, -50000, 0, 50000, 100000};

            IntArrayBinaryTag tag = property.createTag(original);
            int[] result = property.readValue(tag);

            assertArrayEquals(original, result);
        }
    }

    @Nested
    @DisplayName("getKey")
    class GetKeyTests {

        @Test
        @DisplayName("should return key")
        void shouldReturnKey() {
            SlimePropertyIntArray property = SlimePropertyIntArray.create("heightMap", new int[0]);
            assertEquals("heightMap", property.getKey());
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
