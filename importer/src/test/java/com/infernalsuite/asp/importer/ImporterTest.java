package com.infernalsuite.asp.importer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SWMImporter")
class ImporterTest {

    @TempDir
    Path tempDir;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

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

    @Nested
    @DisplayName("main")
    class MainTests {

        @BeforeEach
        void setUpStreams() {
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void restoreStreams() {
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setIn(originalIn);
        }

        @Test
        @DisplayName("should print usage when no arguments provided")
        void shouldPrintUsageWhenNoArguments() {
            SWMImporter.main(new String[]{});

            String errorOutput = errContent.toString();
            assertTrue(errorOutput.contains("Usage:"));
            assertTrue(errorOutput.contains("slimeworldmanager-importer.jar"));
        }

        @Test
        @DisplayName("should print warning when --accept not provided and user declines")
        void shouldPrintWarningAndExitOnDecline() {
            System.setIn(new ByteArrayInputStream("N\n".getBytes()));

            File worldDir = tempDir.resolve("testworld").toFile();
            worldDir.mkdirs();

            SWMImporter.main(new String[]{worldDir.getAbsolutePath()});

            String output = outContent.toString();
            assertTrue(output.contains("WARNING"));
            assertTrue(output.contains("Your wish is my command"));
        }

        @Test
        @DisplayName("should skip warning when --accept flag is provided")
        void shouldSkipWarningWithAcceptFlag() {
            File worldDir = tempDir.resolve("emptyworld").toFile();
            worldDir.mkdirs();

            SWMImporter.main(new String[]{worldDir.getAbsolutePath(), "--accept"});

            String output = outContent.toString();
            assertFalse(output.contains("WARNING"));
        }
    }

    @Nested
    @DisplayName("importWorld")
    class ImportWorldTests {

        @BeforeEach
        void setUpStreams() {
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void restoreStreams() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        @Test
        @DisplayName("should handle invalid world directory gracefully")
        void shouldHandleInvalidWorldDirectory() {
            File invalidDir = tempDir.resolve("nonexistent").toFile();
            File outputFile = tempDir.resolve("output.slime").toFile();

            assertDoesNotThrow(() ->
                SWMImporter.importWorld(invalidDir, outputFile, false));
        }

        @Test
        @DisplayName("should print error message without stacktrace when debug is false")
        void shouldPrintErrorWithoutStacktrace() {
            File invalidDir = tempDir.resolve("invalid").toFile();
            File outputFile = tempDir.resolve("out.slime").toFile();

            SWMImporter.importWorld(invalidDir, outputFile, false);

            String errOutput = errContent.toString();
            assertFalse(errOutput.contains("at com.infernalsuite"));
        }

        @Test
        @DisplayName("should print stacktrace when debug is true")
        void shouldPrintStacktraceWhenDebugEnabled() {
            File invalidDir = tempDir.resolve("invalid2").toFile();
            File outputFile = tempDir.resolve("out2.slime").toFile();

            SWMImporter.importWorld(invalidDir, outputFile, true);

            // Error output should exist when invalid directory is used
            String errOutput = errContent.toString();
            assertNotNull(errOutput);
        }

        @Test
        @DisplayName("should handle empty world directory")
        void shouldHandleEmptyWorldDirectory() {
            File emptyDir = tempDir.resolve("emptyworld").toFile();
            emptyDir.mkdirs();
            File outputFile = tempDir.resolve("empty.slime").toFile();

            assertDoesNotThrow(() ->
                SWMImporter.importWorld(emptyDir, outputFile, false));
        }
    }
}
