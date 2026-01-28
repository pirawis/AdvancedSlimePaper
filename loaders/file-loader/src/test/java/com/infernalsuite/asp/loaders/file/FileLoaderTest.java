package com.infernalsuite.asp.loaders.file;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileLoader")
class FileLoaderTest {

    @TempDir
    Path tempDir;

    private FileLoader loader;

    @BeforeEach
    void setUp() {
        loader = new FileLoader(tempDir.toFile());
    }

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create world directory if not exists")
        void shouldCreateWorldDirectory(@TempDir Path newTempDir) {
            File worldDir = newTempDir.resolve("worlds").toFile();
            assertFalse(worldDir.exists());

            new FileLoader(worldDir);

            assertTrue(worldDir.exists());
            assertTrue(worldDir.isDirectory());
        }

        @Test
        @DisplayName("should use existing directory")
        void shouldUseExistingDirectory() {
            File existingDir = tempDir.toFile();
            assertTrue(existingDir.exists());

            FileLoader loader = new FileLoader(existingDir);
            assertNotNull(loader);
        }
    }

    @Nested
    @DisplayName("worldExists")
    class WorldExistsTests {

        @Test
        @DisplayName("should return false for non-existent world")
        void shouldReturnFalseForNonExistentWorld() {
            assertFalse(loader.worldExists("nonexistent"));
        }

        @Test
        @DisplayName("should return true for existing world")
        void shouldReturnTrueForExistingWorld() throws IOException {
            Files.write(tempDir.resolve("testworld.slime"), new byte[]{1, 2, 3});

            assertTrue(loader.worldExists("testworld"));
        }
    }

    @Nested
    @DisplayName("saveWorld")
    class SaveWorldTests {

        @Test
        @DisplayName("should save world data to file")
        void shouldSaveWorldDataToFile() throws IOException {
            byte[] worldData = {1, 2, 3, 4, 5};

            loader.saveWorld("myworld", worldData);

            Path savedFile = tempDir.resolve("myworld.slime");
            assertTrue(Files.exists(savedFile));
            assertArrayEquals(worldData, Files.readAllBytes(savedFile));
        }

        @Test
        @DisplayName("should overwrite existing world")
        void shouldOverwriteExistingWorld() throws IOException {
            byte[] oldData = {1, 2, 3};
            byte[] newData = {4, 5, 6, 7};

            loader.saveWorld("world", oldData);
            loader.saveWorld("world", newData);

            Path savedFile = tempDir.resolve("world.slime");
            assertArrayEquals(newData, Files.readAllBytes(savedFile));
        }
    }

    @Nested
    @DisplayName("readWorld")
    class ReadWorldTests {

        @Test
        @DisplayName("should read world data from file")
        void shouldReadWorldDataFromFile() throws Exception {
            byte[] expectedData = {10, 20, 30, 40};
            Files.write(tempDir.resolve("readtest.slime"), expectedData);

            byte[] actualData = loader.readWorld("readtest");

            assertArrayEquals(expectedData, actualData);
        }

        @Test
        @DisplayName("should throw UnknownWorldException for non-existent world")
        void shouldThrowUnknownWorldException() {
            assertThrows(UnknownWorldException.class, () ->
                loader.readWorld("doesnotexist"));
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
        @DisplayName("should list all world names without extension")
        void shouldListAllWorldNames() throws IOException {
            Files.write(tempDir.resolve("world1.slime"), new byte[]{1});
            Files.write(tempDir.resolve("world2.slime"), new byte[]{2});
            Files.write(tempDir.resolve("world3.slime"), new byte[]{3});

            List<String> worlds = loader.listWorlds();

            assertEquals(3, worlds.size());
            assertTrue(worlds.contains("world1"));
            assertTrue(worlds.contains("world2"));
            assertTrue(worlds.contains("world3"));
        }

        @Test
        @DisplayName("should ignore non-slime files")
        void shouldIgnoreNonSlimeFiles() throws IOException {
            Files.write(tempDir.resolve("valid.slime"), new byte[]{1});
            Files.write(tempDir.resolve("invalid.txt"), new byte[]{2});
            Files.write(tempDir.resolve("other.dat"), new byte[]{3});

            List<String> worlds = loader.listWorlds();

            assertEquals(1, worlds.size());
            assertTrue(worlds.contains("valid"));
        }
    }

    @Nested
    @DisplayName("deleteWorld")
    class DeleteWorldTests {

        @Test
        @DisplayName("should delete existing world")
        void shouldDeleteExistingWorld() throws Exception {
            Path worldFile = tempDir.resolve("todelete.slime");
            Files.write(worldFile, new byte[]{1, 2, 3});
            assertTrue(Files.exists(worldFile));

            loader.deleteWorld("todelete");

            assertFalse(Files.exists(worldFile));
        }

        @Test
        @DisplayName("should throw UnknownWorldException when deleting non-existent world")
        void shouldThrowWhenDeletingNonExistent() {
            assertThrows(UnknownWorldException.class, () ->
                loader.deleteWorld("ghost"));
        }
    }
}
