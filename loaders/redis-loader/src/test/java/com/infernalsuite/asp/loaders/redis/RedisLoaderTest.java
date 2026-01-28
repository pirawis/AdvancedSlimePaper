package com.infernalsuite.asp.loaders.redis;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.loaders.redis.util.StringByteCodec;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Redis Loader Components")
@Testcontainers
class RedisLoaderTest {

    @Container
    static RedisContainer redisContainer = new RedisContainer("redis:7.0");

    @Nested
    @DisplayName("RedisLoader Integration Tests")
    class RedisLoaderIntegrationTests {

        private RedisLoader loader;

        @BeforeEach
        void setUp() {
            String uri = redisContainer.getRedisURI();
            loader = new RedisLoader(uri);
        }

        @Test
        @DisplayName("should return false for non-existent world")
        void shouldReturnFalseForNonExistentWorld() throws IOException {
            assertFalse(loader.worldExists("nonexistent_" + System.currentTimeMillis()));
        }

        @Test
        @DisplayName("should return true after saving world")
        void shouldReturnTrueAfterSavingWorld() throws IOException {
            String worldName = "testworld_" + System.currentTimeMillis();
            byte[] worldData = new byte[]{1, 2, 3, 4, 5};
            loader.saveWorld(worldName, worldData);

            assertTrue(loader.worldExists(worldName));
        }

        @Test
        @DisplayName("should save and read world data")
        void shouldSaveAndReadWorldData() throws IOException, UnknownWorldException {
            String worldName = "save_read_" + System.currentTimeMillis();
            byte[] originalData = new byte[]{10, 20, 30, 40, 50};
            loader.saveWorld(worldName, originalData);

            byte[] readData = loader.readWorld(worldName);

            assertArrayEquals(originalData, readData);
        }

        @Test
        @DisplayName("should throw UnknownWorldException for non-existent world")
        void shouldThrowUnknownWorldExceptionForNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                loader.readWorld("does_not_exist_" + System.currentTimeMillis())
            );
        }

        @Test
        @DisplayName("should list saved worlds")
        void shouldListSavedWorlds() throws IOException {
            String uniqueName = "list_world_" + System.currentTimeMillis();
            loader.saveWorld(uniqueName, new byte[]{1, 2, 3});

            List<String> worlds = loader.listWorlds();

            assertTrue(worlds.contains(uniqueName));
        }

        @Test
        @DisplayName("should delete existing world")
        void shouldDeleteExistingWorld() throws IOException, UnknownWorldException {
            String worldName = "to_delete_" + System.currentTimeMillis();
            loader.saveWorld(worldName, new byte[]{1, 2, 3});
            assertTrue(loader.worldExists(worldName));

            loader.deleteWorld(worldName);

            assertFalse(loader.worldExists(worldName));
        }

        @Test
        @DisplayName("should throw UnknownWorldException when deleting non-existent world")
        void shouldThrowUnknownWorldExceptionWhenDeletingNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                loader.deleteWorld("nonexistent_delete_" + System.currentTimeMillis())
            );
        }

        @Test
        @DisplayName("should handle large world data")
        void shouldHandleLargeWorldData() throws IOException, UnknownWorldException {
            String worldName = "large_world_" + System.currentTimeMillis();
            byte[] largeData = new byte[1024 * 100]; // 100KB
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }

            loader.saveWorld(worldName, largeData);
            byte[] readData = loader.readWorld(worldName);

            assertArrayEquals(largeData, readData);
        }
    }

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
