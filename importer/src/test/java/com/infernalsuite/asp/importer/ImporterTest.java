package com.infernalsuite.asp.importer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SWMImporter")
class ImporterTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("getDestinationFile")
    class GetDestinationFileTests {

        @Test
        @DisplayName("should return file with .slime extension")
        void shouldReturnFileWithSlimeExtension() {
            File worldFolder = tempDir.resolve("myworld").toFile();

            File destination = SWMImporter.getDestinationFile(worldFolder);

            assertTrue(destination.getName().endsWith(".slime"));
        }

        @Test
        @DisplayName("should use world folder name as base")
        void shouldUseWorldFolderNameAsBase() {
            File worldFolder = tempDir.resolve("lobby").toFile();

            File destination = SWMImporter.getDestinationFile(worldFolder);

            assertEquals("lobby.slime", destination.getName());
        }

        @Test
        @DisplayName("should place output in parent directory")
        void shouldPlaceOutputInParentDirectory() {
            File worldFolder = tempDir.resolve("testworld").toFile();

            File destination = SWMImporter.getDestinationFile(worldFolder);

            assertEquals(tempDir.toFile(), destination.getParentFile());
        }

        @Test
        @DisplayName("should handle nested directory")
        void shouldHandleNestedDirectory() {
            File nestedWorld = tempDir.resolve("worlds").resolve("arena").toFile();

            File destination = SWMImporter.getDestinationFile(nestedWorld);

            assertEquals("arena.slime", destination.getName());
            assertEquals(tempDir.resolve("worlds").toFile(), destination.getParentFile());
        }

        @Test
        @DisplayName("should handle world name with special characters")
        void shouldHandleWorldNameWithSpecialCharacters() {
            File worldFolder = tempDir.resolve("my-world_v2").toFile();

            File destination = SWMImporter.getDestinationFile(worldFolder);

            assertEquals("my-world_v2.slime", destination.getName());
        }
    }
}
