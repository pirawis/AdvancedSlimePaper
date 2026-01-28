package com.infernalsuite.asp.loaders.mongo;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MongoLoader Integration Tests")
@Testcontainers
class MongoLoaderTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    private MongoLoader loader;

    @BeforeEach
    void setUp() {
        String uri = mongoDBContainer.getReplicaSetUrl();

        // Clean database before each test for isolation
        try (MongoClient client = MongoClients.create(uri)) {
            client.getDatabase("testdb").drop();
        }

        loader = new MongoLoader("testdb", "worlds", null, null, null, null, null, uri);
    }

    @Nested
    @DisplayName("worldExists")
    class WorldExistsTests {

        @Test
        @DisplayName("should return false for non-existent world")
        void shouldReturnFalseForNonExistentWorld() throws IOException {
            assertFalse(loader.worldExists("nonexistent"));
        }

        @Test
        @DisplayName("should return true after saving world")
        void shouldReturnTrueAfterSavingWorld() throws IOException {
            byte[] worldData = new byte[]{1, 2, 3, 4, 5};
            loader.saveWorld("testworld", worldData);

            assertTrue(loader.worldExists("testworld"));
        }
    }

    @Nested
    @DisplayName("listWorlds")
    class ListWorldsTests {

        @Test
        @DisplayName("should return empty list when no worlds")
        void shouldReturnEmptyListWhenNoWorlds() throws IOException {
            List<String> worlds = loader.listWorlds();
            assertTrue(worlds.isEmpty());
        }

        @Test
        @DisplayName("should list saved worlds")
        void shouldListSavedWorlds() throws IOException {
            loader.saveWorld("world1", new byte[]{1, 2, 3});
            loader.saveWorld("world2", new byte[]{4, 5, 6});

            List<String> worlds = loader.listWorlds();

            assertEquals(2, worlds.size());
            assertTrue(worlds.contains("world1"));
            assertTrue(worlds.contains("world2"));
        }
    }

    @Nested
    @DisplayName("saveWorld and readWorld")
    class SaveAndReadWorldTests {

        @Test
        @DisplayName("should save and read world data")
        void shouldSaveAndReadWorldData() throws IOException, UnknownWorldException {
            byte[] originalData = new byte[]{10, 20, 30, 40, 50};
            loader.saveWorld("myworld", originalData);

            byte[] readData = loader.readWorld("myworld");

            assertArrayEquals(originalData, readData);
        }

        @Test
        @DisplayName("should overwrite existing world")
        void shouldOverwriteExistingWorld() throws IOException, UnknownWorldException {
            byte[] oldData = new byte[]{1, 2, 3};
            byte[] newData = new byte[]{4, 5, 6, 7, 8};

            loader.saveWorld("overwrite_test", oldData);
            loader.saveWorld("overwrite_test", newData);

            byte[] readData = loader.readWorld("overwrite_test");
            assertArrayEquals(newData, readData);
        }

        @Test
        @DisplayName("should throw UnknownWorldException for non-existent world")
        void shouldThrowUnknownWorldExceptionForNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                loader.readWorld("does_not_exist")
            );
        }
    }

    @Nested
    @DisplayName("deleteWorld")
    class DeleteWorldTests {

        @Test
        @DisplayName("should delete existing world")
        void shouldDeleteExistingWorld() throws IOException, UnknownWorldException {
            loader.saveWorld("to_delete", new byte[]{1, 2, 3});
            assertTrue(loader.worldExists("to_delete"));

            loader.deleteWorld("to_delete");

            assertFalse(loader.worldExists("to_delete"));
        }

        @Test
        @DisplayName("should throw UnknownWorldException when deleting non-existent world")
        void shouldThrowUnknownWorldExceptionWhenDeletingNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                loader.deleteWorld("nonexistent")
            );
        }
    }

    @Nested
    @DisplayName("large data")
    class LargeDataTests {

        @Test
        @DisplayName("should handle large world data")
        void shouldHandleLargeWorldData() throws IOException, UnknownWorldException {
            byte[] largeData = new byte[1024 * 1024]; // 1MB
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }

            loader.saveWorld("large_world", largeData);
            byte[] readData = loader.readWorld("large_world");

            assertArrayEquals(largeData, readData);
        }
    }
}
