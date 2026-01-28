package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.ByteArrayBinaryTag;
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
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create ByteArrayBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            ByteArrayBinaryTag tag = property.createTag(new byte[]{1, 2, 3, 4, 5});

            assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, tag.value());
        }

        @Test
        @DisplayName("should handle empty array")
        void shouldHandleEmptyArray() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            ByteArrayBinaryTag tag = property.createTag(new byte[0]);

            assertEquals(0, tag.value().length);
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            ByteArrayBinaryTag tag = property.createTag(new byte[]{-128, -1, 0, 1, 127});

            assertArrayEquals(new byte[]{-128, -1, 0, 1, 127}, tag.value());
        }

        @Test
        @DisplayName("should handle large array")
        void shouldHandleLargeArray() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            byte[] largeArray = new byte[1000];
            for (int i = 0; i < largeArray.length; i++) {
                largeArray[i] = (byte) (i % 256);
            }
            ByteArrayBinaryTag tag = property.createTag(largeArray);

            assertEquals(1000, tag.value().length);
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            ByteArrayBinaryTag tag = ByteArrayBinaryTag.byteArrayBinaryTag(new byte[]{10, 20, 30});

            assertArrayEquals(new byte[]{10, 20, 30}, property.readValue(tag));
        }

        @Test
        @DisplayName("should read empty array")
        void shouldReadEmptyArray() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            ByteArrayBinaryTag tag = ByteArrayBinaryTag.byteArrayBinaryTag(new byte[0]);

            assertEquals(0, property.readValue(tag).length);
        }

        @Test
        @DisplayName("should read negative values")
        void shouldReadNegativeValues() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            ByteArrayBinaryTag tag = ByteArrayBinaryTag.byteArrayBinaryTag(new byte[]{Byte.MIN_VALUE, -50, 0, 50, Byte.MAX_VALUE});

            assertArrayEquals(new byte[]{Byte.MIN_VALUE, -50, 0, 50, Byte.MAX_VALUE}, property.readValue(tag));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            byte[] original = {1, 2, 3, 4, 5};

            ByteArrayBinaryTag tag = property.createTag(original);
            byte[] result = property.readValue(tag);

            assertArrayEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip empty array correctly")
        void shouldRoundtripEmptyArray() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            byte[] original = new byte[0];

            ByteArrayBinaryTag tag = property.createTag(original);
            byte[] result = property.readValue(tag);

            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should roundtrip full range values correctly")
        void shouldRoundtripFullRangeValues() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("test", new byte[0]);
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) {
                original[i] = (byte) (i - 128);
            }

            ByteArrayBinaryTag tag = property.createTag(original);
            byte[] result = property.readValue(tag);

            assertArrayEquals(original, result);
        }
    }

    @Nested
    @DisplayName("getKey")
    class GetKeyTests {

        @Test
        @DisplayName("should return key")
        void shouldReturnKey() {
            SlimePropertyByteArray property = SlimePropertyByteArray.create("lightData", new byte[0]);
            assertEquals("lightData", property.getKey());
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
