package com.infernalsuite.asp.loaders.mysql;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.UpdatableLoader.NewerStorageException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MysqlLoader Integration Tests")
@Testcontainers
class MysqlLoaderTest {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private MysqlLoader loader;
    private HikariDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlContainer.getJdbcUrl());
        config.setUsername(mysqlContainer.getUsername());
        config.setPassword(mysqlContainer.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        dataSource = new HikariDataSource(config);
        loader = new MysqlLoader(dataSource);
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create loader with URL parameters")
        void shouldCreateLoaderWithUrlParameters() throws SQLException {
            String sqlURL = "jdbc:mysql://{host}:{port}/{database}?useSSL={usessl}";
            String host = mysqlContainer.getHost();
            int port = mysqlContainer.getMappedPort(3306);
            String database = "testdb";
            boolean useSSL = false;
            String username = mysqlContainer.getUsername();
            String password = mysqlContainer.getPassword();

            MysqlLoader urlLoader = new MysqlLoader(sqlURL, host, port, database, useSSL, username, password);

            assertNotNull(urlLoader);
            assertDoesNotThrow(() -> urlLoader.listWorlds());
        }
    }

    @Nested
    @DisplayName("update method")
    class UpdateTests {

        @Test
        @DisplayName("should throw NewerStorageException when storage version is newer")
        void shouldThrowNewerStorageExceptionWhenStorageVersionIsNewer() throws SQLException {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement stmt = con.prepareStatement(
                         "INSERT INTO database_version (id, version) VALUES (1, 999) " +
                         "ON DUPLICATE KEY UPDATE version = 999")) {
                stmt.executeUpdate();
            }

            NewerStorageException exception = assertThrows(NewerStorageException.class, () -> loader.update());
            assertEquals(1, exception.getImplementationVersion());
            assertEquals(999, exception.getStorageVersion());
        }

        @Test
        @DisplayName("should not throw when version matches current")
        void shouldNotThrowWhenVersionMatchesCurrent() throws SQLException {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement stmt = con.prepareStatement(
                         "INSERT INTO database_version (id, version) VALUES (1, 1) " +
                         "ON DUPLICATE KEY UPDATE version = 1")) {
                stmt.executeUpdate();
            }

            assertDoesNotThrow(() -> loader.update());
        }

        @Test
        @DisplayName("should handle interrupted migration gracefully")
        void shouldHandleInterruptedMigrationGracefully() throws SQLException {
            try (Connection con = dataSource.getConnection();
                 PreparedStatement stmt = con.prepareStatement("DELETE FROM database_version WHERE id = 1")) {
                stmt.executeUpdate();
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

    }

    @Nested
    @DisplayName("worldExists")
    class WorldExistsTests {

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
    }

    @Nested
    @DisplayName("listWorlds")
    class ListWorldsTests {

        @Test
        @DisplayName("should return list of worlds")
        void shouldReturnListOfWorlds() throws IOException {
            List<String> worlds = loader.listWorlds();
            assertNotNull(worlds);
        }

        @Test
        @DisplayName("should list saved worlds")
        void shouldListSavedWorlds() throws IOException {
            String uniqueName1 = "world_list_" + System.currentTimeMillis() + "_1";
            String uniqueName2 = "world_list_" + System.currentTimeMillis() + "_2";

            loader.saveWorld(uniqueName1, new byte[]{1, 2, 3});
            loader.saveWorld(uniqueName2, new byte[]{4, 5, 6});

            List<String> worlds = loader.listWorlds();

            assertTrue(worlds.contains(uniqueName1));
            assertTrue(worlds.contains(uniqueName2));
        }
    }

    @Nested
    @DisplayName("saveWorld and readWorld")
    class SaveAndReadWorldTests {

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
        @DisplayName("should overwrite existing world")
        void shouldOverwriteExistingWorld() throws IOException, UnknownWorldException {
            String worldName = "overwrite_" + System.currentTimeMillis();
            byte[] oldData = new byte[]{1, 2, 3};
            byte[] newData = new byte[]{4, 5, 6, 7, 8};

            loader.saveWorld(worldName, oldData);
            loader.saveWorld(worldName, newData);

            byte[] readData = loader.readWorld(worldName);
            assertArrayEquals(newData, readData);
        }

        @Test
        @DisplayName("should throw UnknownWorldException for non-existent world")
        void shouldThrowUnknownWorldExceptionForNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                loader.readWorld("does_not_exist_" + System.currentTimeMillis())
            );
        }
    }

    @Nested
    @DisplayName("deleteWorld")
    class DeleteWorldTests {

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
                loader.deleteWorld("nonexistent_" + System.currentTimeMillis())
            );
        }
    }

    @Nested
    @DisplayName("large data")
    class LargeDataTests {

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
}
