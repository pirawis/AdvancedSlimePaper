package com.infernalsuite.asp.loaders.redis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringByteCodec")
class StringByteCodecTest {

    private StringByteCodec codec;

    @BeforeEach
    void setUp() {
        codec = StringByteCodec.INSTANCE;
    }

    @Nested
    @DisplayName("singleton instance")
    class SingletonTests {

        @Test
        @DisplayName("should have static instance")
        void shouldHaveStaticInstance() {
            assertNotNull(StringByteCodec.INSTANCE);
        }

        @Test
        @DisplayName("should return same instance")
        void shouldReturnSameInstance() {
            StringByteCodec codec1 = StringByteCodec.INSTANCE;
            StringByteCodec codec2 = StringByteCodec.INSTANCE;
            assertSame(codec1, codec2);
        }
    }

    @Nested
    @DisplayName("decodeKey")
    class DecodeKeyTests {

        @Test
        @DisplayName("should decode simple string key")
        void shouldDecodeSimpleStringKey() {
            String originalKey = "test-key";
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(originalKey);
            
            String decodedKey = codec.decodeKey(buffer);
            
            assertEquals(originalKey, decodedKey);
        }

        @Test
        @DisplayName("should decode key with special characters")
        void shouldDecodeKeyWithSpecialCharacters() {
            String originalKey = "key-with-特殊-chars";
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(originalKey);
            
            String decodedKey = codec.decodeKey(buffer);
            
            assertEquals(originalKey, decodedKey);
        }

        @Test
        @DisplayName("should decode empty key")
        void shouldDecodeEmptyKey() {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode("");
            
            String decodedKey = codec.decodeKey(buffer);
            
            assertEquals("", decodedKey);
        }

        @Test
        @DisplayName("should decode key with numbers")
        void shouldDecodeKeyWithNumbers() {
            String originalKey = "key123456";
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(originalKey);
            
            String decodedKey = codec.decodeKey(buffer);
            
            assertEquals(originalKey, decodedKey);
        }
    }

    @Nested
    @DisplayName("decodeValue")
    class DecodeValueTests {

        @Test
        @DisplayName("should decode byte array value")
        void shouldDecodeByteArrayValue() {
            byte[] originalValue = {1, 2, 3, 4, 5};
            ByteBuffer buffer = ByteBuffer.wrap(originalValue);
            
            byte[] decodedValue = codec.decodeValue(buffer);
            
            assertArrayEquals(originalValue, decodedValue);
        }

        @Test
        @DisplayName("should decode empty byte array")
        void shouldDecodeEmptyByteArray() {
            byte[] originalValue = {};
            ByteBuffer buffer = ByteBuffer.wrap(originalValue);
            
            byte[] decodedValue = codec.decodeValue(buffer);
            
            assertArrayEquals(originalValue, decodedValue);
        }

        @Test
        @DisplayName("should decode large byte array")
        void shouldDecodeLargeByteArray() {
            byte[] originalValue = new byte[10000];
            for (int i = 0; i < originalValue.length; i++) {
                originalValue[i] = (byte) (i % 256);
            }
            ByteBuffer buffer = ByteBuffer.wrap(originalValue);
            
            byte[] decodedValue = codec.decodeValue(buffer);
            
            assertArrayEquals(originalValue, decodedValue);
        }

        @Test
        @DisplayName("should decode partial buffer")
        void shouldDecodePartialBuffer() {
            byte[] originalValue = {1, 2, 3, 4, 5};
            ByteBuffer buffer = ByteBuffer.wrap(originalValue);
            buffer.position(1);
            buffer.limit(4);
            
            byte[] decodedValue = codec.decodeValue(buffer);
            
            byte[] expected = {2, 3, 4};
            assertArrayEquals(expected, decodedValue);
        }
    }

    @Nested
    @DisplayName("encodeKey")
    class EncodeKeyTests {

        @Test
        @DisplayName("should encode simple string key")
        void shouldEncodeSimpleStringKey() {
            String key = "test-key";
            
            ByteBuffer encoded = codec.encodeKey(key);
            
            assertNotNull(encoded);
            assertEquals(key, StandardCharsets.UTF_8.decode(encoded).toString());
        }

        @Test
        @DisplayName("should encode key with special characters")
        void shouldEncodeKeyWithSpecialCharacters() {
            String key = "key-with-特殊-chars";
            
            ByteBuffer encoded = codec.encodeKey(key);
            
            assertNotNull(encoded);
            assertEquals(key, StandardCharsets.UTF_8.decode(encoded).toString());
        }

        @Test
        @DisplayName("should encode empty key")
        void shouldEncodeEmptyKey() {
            String key = "";
            
            ByteBuffer encoded = codec.encodeKey(key);
            
            assertNotNull(encoded);
            assertEquals(0, encoded.remaining());
        }
    }

    @Nested
    @DisplayName("encodeValue")
    class EncodeValueTests {

        @Test
        @DisplayName("should encode byte array value")
        void shouldEncodeByteArrayValue() {
            byte[] value = {1, 2, 3, 4, 5};
            
            ByteBuffer encoded = codec.encodeValue(value);
            
            assertNotNull(encoded);
            byte[] result = new byte[encoded.remaining()];
            encoded.get(result);
            assertArrayEquals(value, result);
        }

        @Test
        @DisplayName("should encode null value as empty bytes")
        void shouldEncodeNullValueAsEmptyBytes() {
            ByteBuffer encoded = codec.encodeValue(null);
            
            assertNotNull(encoded);
            assertEquals(0, encoded.remaining());
        }

        @Test
        @DisplayName("should encode empty byte array")
        void shouldEncodeEmptyByteArray() {
            byte[] value = {};
            
            ByteBuffer encoded = codec.encodeValue(value);
            
            assertNotNull(encoded);
            assertEquals(0, encoded.remaining());
        }

        @Test
        @DisplayName("should encode large byte array")
        void shouldEncodeLargeByteArray() {
            byte[] value = new byte[10000];
            for (int i = 0; i < value.length; i++) {
                value[i] = (byte) (i % 256);
            }
            
            ByteBuffer encoded = codec.encodeValue(value);
            
            assertNotNull(encoded);
            byte[] result = new byte[encoded.remaining()];
            encoded.get(result);
            assertArrayEquals(value, result);
        }
    }

    @Nested
    @DisplayName("round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("should encode and decode key correctly")
        void shouldEncodeAndDecodeKeyCorrectly() {
            String originalKey = "test-key-123";
            
            ByteBuffer encoded = codec.encodeKey(originalKey);
            String decoded = codec.decodeKey(encoded);
            
            assertEquals(originalKey, decoded);
        }

        @Test
        @DisplayName("should encode and decode value correctly")
        void shouldEncodeAndDecodeValueCorrectly() {
            byte[] originalValue = {10, 20, 30, 40, 50};
            
            ByteBuffer encoded = codec.encodeValue(originalValue);
            byte[] decoded = codec.decodeValue(encoded);
            
            assertArrayEquals(originalValue, decoded);
        }

        @Test
        @DisplayName("should handle key and value together")
        void shouldHandleKeyAndValueTogether() {
            String key = "my-key";
            byte[] value = {1, 2, 3};
            
            ByteBuffer encodedKey = codec.encodeKey(key);
            ByteBuffer encodedValue = codec.encodeValue(value);
            
            String decodedKey = codec.decodeKey(encodedKey);
            byte[] decodedValue = codec.decodeValue(encodedValue);
            
            assertEquals(key, decodedKey);
            assertArrayEquals(value, decodedValue);
        }
    }
}
