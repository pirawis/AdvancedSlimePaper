package com.infernalsuite.asp.loaders.file;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileLoader")
class FileLoaderTest {

    @TempDir
    Path tempDir;

    private FileLoader loader;
    private File worldDir;

    @BeforeEach
    void setUp() {
        worldDir = tempDir.resolve("worlds").toFile();
        loader = new FileLoader(worldDir);
    }

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create worlds directory if not exists")
        void shouldCreateWorldsDirectoryIfNotExists() {
            File newDir = tempDir.resolve("newworlds").toFile();

            new FileLoader(newDir);

            assertTrue(newDir.exists());
            assertTrue(newDir.isDirectory());
        }

        @Test
        @DisplayName("should delete file if exists with same name as directory")
        void shouldDeleteFileIfExistsWithSameNameAsDirectory() throws IOException {
            File fileAsDir = tempDir.resolve("fileasdir").toFile();
            assertTrue(fileAsDir.createNewFile());

            new FileLoader(fileAsDir);

            assertTrue(fileAsDir.exists());
            assertTrue(fileAsDir.isDirectory());
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
            createWorldFile("testworld", new byte[]{1, 2, 3});

            assertTrue(loader.worldExists("testworld"));
        }
    }

    @Nested
    @DisplayName("readWorld")
    class ReadWorldTests {

        @Test
        @DisplayName("should throw UnknownWorldException for non-existent world")
        void shouldThrowUnknownWorldExceptionForNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                    loader.readWorld("nonexistent"));
        }

        @Test
        @DisplayName("should read world bytes")
        void shouldReadWorldBytes() throws Exception {
            byte[] expectedData = {1, 2, 3, 4, 5};
            createWorldFile("testworld", expectedData);

            byte[] result = loader.readWorld("testworld");

            assertArrayEquals(expectedData, result);
        }

        @Test
        @DisplayName("should read large world file")
        void shouldReadLargeWorldFile() throws Exception {
            byte[] largeData = new byte[1024 * 1024]; // 1MB
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }
            createWorldFile("largeworld", largeData);

            byte[] result = loader.readWorld("largeworld");

            assertArrayEquals(largeData, result);
        }
    }

    @Nested
    @DisplayName("saveWorld")
    class SaveWorldTests {

        @Test
        @DisplayName("should save world bytes to file")
        void shouldSaveWorldBytesToFile() throws Exception {
            byte[] data = {10, 20, 30, 40, 50};

            loader.saveWorld("newworld", data);

            assertTrue(loader.worldExists("newworld"));
            assertArrayEquals(data, loader.readWorld("newworld"));
        }

        @Test
        @DisplayName("should overwrite existing world")
        void shouldOverwriteExistingWorld() throws Exception {
            byte[] oldData = {1, 2, 3};
            byte[] newData = {4, 5, 6, 7};

            loader.saveWorld("world", oldData);
            loader.saveWorld("world", newData);

            assertArrayEquals(newData, loader.readWorld("world"));
        }
    }

    @Nested
    @DisplayName("deleteWorld")
    class DeleteWorldTests {

        @Test
        @DisplayName("should throw UnknownWorldException for non-existent world")
        void shouldThrowUnknownWorldExceptionForNonExistentWorld() {
            assertThrows(UnknownWorldException.class, () ->
                    loader.deleteWorld("nonexistent"));
        }

        @Test
        @DisplayName("should delete existing world")
        void shouldDeleteExistingWorld() throws Exception {
            createWorldFile("todelete", new byte[]{1, 2, 3});
            assertTrue(loader.worldExists("todelete"));

            loader.deleteWorld("todelete");

            assertFalse(loader.worldExists("todelete"));
        }
    }

    @Nested
    @DisplayName("listWorlds")
    class ListWorldsTests {

        @Test
        @DisplayName("should return empty list when no worlds")
        void shouldReturnEmptyListWhenNoWorlds() throws NotDirectoryException {
            List<String> worlds = loader.listWorlds();

            assertTrue(worlds.isEmpty());
        }

        @Test
        @DisplayName("should return single world")
        void shouldReturnSingleWorld() throws Exception {
            createWorldFile("singleworld", new byte[]{1});

            List<String> worlds = loader.listWorlds();

            assertEquals(1, worlds.size());
            assertEquals("singleworld", worlds.get(0));
        }

        @Test
        @DisplayName("should list all world names")
        void shouldListAllWorldNames() throws Exception {
            createWorldFile("world1", new byte[]{1});
            createWorldFile("world2", new byte[]{2});
            createWorldFile("world3", new byte[]{3});

            List<String> worlds = loader.listWorlds();

            assertEquals(3, worlds.size());
            assertTrue(worlds.contains("world1"));
            assertTrue(worlds.contains("world2"));
            assertTrue(worlds.contains("world3"));
        }

        @Test
        @DisplayName("should not include non-slime files")
        void shouldNotIncludeNonSlimeFiles() throws Exception {
            createWorldFile("validworld", new byte[]{1});
            // Create a non-slime file
            new File(worldDir, "invalid.txt").createNewFile();

            List<String> worlds = loader.listWorlds();

            assertEquals(1, worlds.size());
            assertTrue(worlds.contains("validworld"));
        }

        @Test
        @DisplayName("should strip .slime extension from names")
        void shouldStripSlimeExtensionFromNames() throws Exception {
            createWorldFile("myworld", new byte[]{1});

            List<String> worlds = loader.listWorlds();

            assertTrue(worlds.contains("myworld"));
            assertFalse(worlds.contains("myworld.slime"));
        }
    }

    private void createWorldFile(String name, byte[] data) throws IOException {
        File file = new File(worldDir, name + ".slime");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }
}
