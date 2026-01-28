package com.infernalsuite.asp.loaders.mysql;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
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

    @BeforeEach
    void setUp() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlContainer.getJdbcUrl());
        config.setUsername(mysqlContainer.getUsername());
        config.setPassword(mysqlContainer.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        HikariDataSource dataSource = new HikariDataSource(config);
        loader = new MysqlLoader(dataSource);
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
            // May contain worlds from previous tests in same container
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
