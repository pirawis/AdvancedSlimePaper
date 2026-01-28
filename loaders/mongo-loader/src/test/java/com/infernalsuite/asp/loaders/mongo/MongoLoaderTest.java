package com.infernalsuite.asp.loaders.mongo;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
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
    private String uri;

    @BeforeEach
    void setUp() {
        uri = mongoDBContainer.getReplicaSetUrl();

        // Clean database before each test for isolation
        try (MongoClient client = MongoClients.create(uri)) {
            client.getDatabase("testdb").drop();
        }

        loader = new MongoLoader("testdb", "worlds", null, null, null, null, null, uri);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create loader with MongoClient parameter")
        void shouldCreateLoaderWithMongoClientParameter() {
            MongoClient client = MongoClients.create(uri);
            MongoLoader clientLoader = new MongoLoader(client, "testdb2", "worlds2");

            assertNotNull(clientLoader);
            assertDoesNotThrow(() -> clientLoader.listWorlds());
        }

        @Test
        @DisplayName("should create loader with host and port parameters")
        void shouldCreateLoaderWithHostAndPortParameters() {
            String host = mongoDBContainer.getHost();
            Integer port = mongoDBContainer.getMappedPort(27017);

            MongoLoader hostLoader = new MongoLoader("testdb3", "worlds3", null, null, null, host, port, null);

            assertNotNull(hostLoader);
            assertDoesNotThrow(() -> hostLoader.listWorlds());
        }

        @Test
        @DisplayName("should create loader with auth parameters")
        void shouldCreateLoaderWithAuthParameters() {
            String host = mongoDBContainer.getHost();
            Integer port = mongoDBContainer.getMappedPort(27017);

            MongoLoader authLoader = new MongoLoader("testdb4", "worlds4", "user", "pass", "admin", host, port, null);

            assertNotNull(authLoader);
        }
    }

    @Nested
    @DisplayName("update method")
    class UpdateTests {

        @Test
        @DisplayName("should handle update when no migration needed")
        void shouldHandleUpdateWhenNoMigrationNeeded() {
            assertDoesNotThrow(() -> loader.update());
        }

        @Test
        @DisplayName("should handle interrupted lock migration gracefully")
        void shouldHandleInterruptedLockMigrationGracefully() {
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase("testdb");
                MongoCollection<Document> collection = db.getCollection("worlds");
                collection.insertOne(new Document("name", "test_world").append("locked", true));
            }

            Thread testThread = Thread.currentThread();
            Thread interrupter = new Thread(() -> {
                try {
                    Thread.sleep(100);
                    testThread.interrupt();
                } catch (InterruptedException ignored) {}
            });
            interrupter.start();

            assertDoesNotThrow(() -> loader.update());
            Thread.interrupted();
        }

        @Test
        @DisplayName("should perform lock migration when old format detected")
        @Timeout(15)
        void shouldPerformLockMigrationWhenOldFormatDetected() {
            // Insert documents with boolean locked field (old format)
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase("testdb");
                MongoCollection<Document> collection = db.getCollection("worlds");
                collection.insertOne(new Document("name", "world1").append("locked", false));
                collection.insertOne(new Document("name", "world2").append("locked", true));
            }

            // This will wait 10 seconds and perform migration
            assertDoesNotThrow(() -> loader.update());

            // Verify locked field was updated to Long
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase("testdb");
                MongoCollection<Document> collection = db.getCollection("worlds");
                Document doc = collection.find(new Document("name", "world1")).first();
                assertNotNull(doc);
                assertEquals(0L, doc.get("locked"));
            }
        }

        @Test
        @DisplayName("should rename old GridFS collections")
        void shouldRenameOldGridFSCollections() {
            // Create old format GridFS collections
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase("testdb");
                db.createCollection("worlds_files.files");
                db.createCollection("worlds_files.chunks");
                db.getCollection("worlds_files.files").insertOne(new Document("test", "data"));
                db.getCollection("worlds_files.chunks").insertOne(new Document("test", "data"));
            }

            assertDoesNotThrow(() -> loader.update());

            // Verify collections were renamed
            try (MongoClient client = MongoClients.create(uri)) {
                MongoDatabase db = client.getDatabase("testdb");
                boolean hasOldFiles = false;
                boolean hasNewFiles = false;
                for (String name : db.listCollectionNames()) {
                    if (name.equals("worlds_files.files")) hasOldFiles = true;
                    if (name.equals("worlds.files")) hasNewFiles = true;
                }
                assertFalse(hasOldFiles);
                assertTrue(hasNewFiles);
            }
        }
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
