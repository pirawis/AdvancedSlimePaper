package com.infernalsuite.asp.importer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SWMImporter")
class SWMImporterTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("getDestinationFile")
    class GetDestinationFileTests {

        @Test
        @DisplayName("should return slime file in parent directory")
        void shouldReturnSlimeFileInParentDirectory() {
            File worldFolder = new File("/path/to/worlds/myworld");

            File result = SWMImporter.getDestinationFile(worldFolder);

            assertEquals("myworld.slime", result.getName());
            assertEquals(worldFolder.getParentFile(), result.getParentFile());
        }

        @Test
        @DisplayName("should append .slime extension to world name")
        void shouldAppendSlimeExtension() {
            File worldFolder = new File("/worlds/testworld");

            File result = SWMImporter.getDestinationFile(worldFolder);

            assertTrue(result.getName().endsWith(".slime"));
        }

        @Test
        @DisplayName("should preserve world folder name")
        void shouldPreserveWorldFolderName() {
            File worldFolder = new File("/data/my_survival_world");

            File result = SWMImporter.getDestinationFile(worldFolder);

            assertEquals("my_survival_world.slime", result.getName());
        }

        @Test
        @DisplayName("should handle world folder in root")
        void shouldHandleWorldFolderInRoot() {
            File worldFolder = new File("/world");

            File result = SWMImporter.getDestinationFile(worldFolder);

            assertEquals("world.slime", result.getName());
        }

        @Test
        @DisplayName("should handle nested directory structure")
        void shouldHandleNestedDirectoryStructure() {
            File worldFolder = new File("/a/b/c/d/e/worldname");

            File result = SWMImporter.getDestinationFile(worldFolder);

            assertEquals("worldname.slime", result.getName());
            assertEquals("e", result.getParentFile().getName());
        }

        @Test
        @DisplayName("should use temp directory correctly")
        void shouldUseTempDirectoryCorrectly() throws IOException {
            Path worldPath = tempDir.resolve("testworld");
            Files.createDirectories(worldPath);

            File result = SWMImporter.getDestinationFile(worldPath.toFile());

            assertEquals("testworld.slime", result.getName());
            assertEquals(tempDir.toFile().getAbsolutePath(), result.getParentFile().getAbsolutePath());
        }
    }

}
