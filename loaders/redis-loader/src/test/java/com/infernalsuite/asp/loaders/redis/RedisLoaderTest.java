package com.infernalsuite.asp.loaders.redis;

import com.infernalsuite.asp.loaders.redis.util.StringByteCodec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Redis Loader Components")
class RedisLoaderTest {

    @Nested
    @DisplayName("StringByteCodec")
    class StringByteCodecTests {

        private final StringByteCodec codec = StringByteCodec.INSTANCE;

        @Test
        @DisplayName("should encode key to ByteBuffer")
        void shouldEncodeKeyToByteBuffer() {
            ByteBuffer buffer = codec.encodeKey("testKey");

            assertNotNull(buffer);
            assertTrue(buffer.hasRemaining());
        }

        @Test
        @DisplayName("should decode key from ByteBuffer")
        void shouldDecodeKeyFromByteBuffer() {
            String original = "myKey";
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(original);

            String decoded = codec.decodeKey(buffer);

            assertEquals(original, decoded);
        }

        @Test
        @DisplayName("should encode value to ByteBuffer")
        void shouldEncodeValueToByteBuffer() {
            byte[] value = {1, 2, 3, 4, 5};

            ByteBuffer buffer = codec.encodeValue(value);

            assertNotNull(buffer);
            assertEquals(5, buffer.remaining());
        }

        @Test
        @DisplayName("should encode null value to empty ByteBuffer")
        void shouldEncodeNullValueToEmptyByteBuffer() {
            ByteBuffer buffer = codec.encodeValue(null);

            assertNotNull(buffer);
            assertEquals(0, buffer.remaining());
        }

        @Test
        @DisplayName("should decode value from ByteBuffer")
        void shouldDecodeValueFromByteBuffer() {
            byte[] original = {10, 20, 30};
            ByteBuffer buffer = ByteBuffer.wrap(original);

            byte[] decoded = codec.decodeValue(buffer);

            assertArrayEquals(original, decoded);
        }

        @Test
        @DisplayName("should roundtrip key correctly")
        void shouldRoundtripKey() {
            String original = "world:lobby";

            ByteBuffer encoded = codec.encodeKey(original);
            String decoded = codec.decodeKey(encoded);

            assertEquals(original, decoded);
        }

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            byte[] original = {0, 127, -128, 1, -1};

            ByteBuffer encoded = codec.encodeValue(original);
            byte[] decoded = codec.decodeValue(encoded);

            assertArrayEquals(original, decoded);
        }

        @Test
        @DisplayName("should handle empty key")
        void shouldHandleEmptyKey() {
            String empty = "";

            ByteBuffer encoded = codec.encodeKey(empty);
            String decoded = codec.decodeKey(encoded);

            assertEquals(empty, decoded);
        }

        @Test
        @DisplayName("should handle UTF-8 characters in key")
        void shouldHandleUtf8Characters() {
            String utf8Key = "dünya_世界_мир";

            ByteBuffer encoded = codec.encodeKey(utf8Key);
            String decoded = codec.decodeKey(encoded);

            assertEquals(utf8Key, decoded);
        }

        @Test
        @DisplayName("INSTANCE should not be null")
        void instanceShouldNotBeNull() {
            assertNotNull(StringByteCodec.INSTANCE);
        }
    }
}
