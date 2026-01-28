package com.infernalsuite.asp.serialization.slime;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeSerializer")
class SlimeSerializerTest {

    @Nested
    @DisplayName("serializeCompoundTag")
    class SerializeCompoundTagTests {

        @Test
        @DisplayName("should return empty array for null tag")
        void shouldReturnEmptyArrayForNullTag() throws IOException {
            byte[] result = SlimeSerializer.serializeCompoundTag(null);
            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should return empty array for empty tag")
        void shouldReturnEmptyArrayForEmptyTag() throws IOException {
            CompoundBinaryTag emptyTag = CompoundBinaryTag.empty();
            byte[] result = SlimeSerializer.serializeCompoundTag(emptyTag);
            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should serialize simple compound tag")
        void shouldSerializeSimpleCompoundTag() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putInt("testInt", 42)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);
        }

        @Test
        @DisplayName("should serialize tag with string value")
        void shouldSerializeTagWithStringValue() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putString("name", "testWorld")
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);
        }

        @Test
        @DisplayName("should produce deserializable output")
        void shouldProduceDeserializableOutput() throws IOException {
            CompoundBinaryTag original = CompoundBinaryTag.builder()
                    .putInt("value", 123)
                    .putString("key", "test")
                    .build();

            byte[] serialized = SlimeSerializer.serializeCompoundTag(original);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(serialized));

            assertEquals(123, deserialized.getInt("value"));
            assertEquals("test", deserialized.getString("key"));
        }

        @Test
        @DisplayName("should serialize nested compound tag")
        void shouldSerializeNestedCompoundTag() throws IOException {
            CompoundBinaryTag innerTag = CompoundBinaryTag.builder()
                    .putInt("innerValue", 99)
                    .build();

            CompoundBinaryTag outerTag = CompoundBinaryTag.builder()
                    .put("nested", innerTag)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(outerTag);

            assertNotNull(result);
            assertTrue(result.length > 0);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertEquals(99, deserialized.getCompound("nested").getInt("innerValue"));
        }

        @Test
        @DisplayName("should serialize tag with array values")
        void shouldSerializeTagWithArrayValues() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putIntArray("intArray", new int[]{1, 2, 3, 4, 5})
                    .putByteArray("byteArray", new byte[]{10, 20, 30})
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, deserialized.getIntArray("intArray"));
            assertArrayEquals(new byte[]{10, 20, 30}, deserialized.getByteArray("byteArray"));
        }

        @Test
        @DisplayName("should serialize tag with multiple fields")
        void shouldSerializeTagWithMultipleFields() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putInt("int", 1)
                    .putLong("long", 2L)
                    .putFloat("float", 3.0f)
                    .putDouble("double", 4.0)
                    .putString("string", "five")
                    .putByte("byte", (byte) 6)
                    .putShort("short", (short) 7)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertEquals(1, deserialized.getInt("int"));
            assertEquals(2L, deserialized.getLong("long"));
            assertEquals(3.0f, deserialized.getFloat("float"), 0.001f);
            assertEquals(4.0, deserialized.getDouble("double"), 0.001);
            assertEquals("five", deserialized.getString("string"));
            assertEquals((byte) 6, deserialized.getByte("byte"));
            assertEquals((short) 7, deserialized.getShort("short"));
        }

        @Test
        @DisplayName("should handle tag with boolean as byte")
        void shouldHandleTagWithBooleanAsByte() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putBoolean("enabled", true)
                    .putBoolean("disabled", false)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertTrue(deserialized.getBoolean("enabled"));
            assertFalse(deserialized.getBoolean("disabled"));
        }
    }
}
