package com.infernalsuite.asp.serialization.anvil;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnvilWorldReader")
@ExtendWith(MockitoExtension.class)
class AnvilWorldReaderTest {

    @TempDir
    Path tempDir;

    @Mock
    private SlimeLoader mockLoader;

    @Nested
    @DisplayName("INSTANCE")
    class InstanceTests {

        @Test
        @DisplayName("should have singleton instance")
        void shouldHaveSingletonInstance() {
            assertNotNull(AnvilWorldReader.INSTANCE);
        }

        @Test
        @DisplayName("should return same instance")
        void shouldReturnSameInstance() {
            assertSame(AnvilWorldReader.INSTANCE, AnvilWorldReader.INSTANCE);
        }
    }

    @Nested
    @DisplayName("readFromData")
    class ReadFromDataTests {

        @Test
        @DisplayName("should throw when level.dat is missing")
        void shouldThrowWhenLevelDatIsMissing() throws IOException {
            Path worldDir = tempDir.resolve("emptyworld");
            Files.createDirectories(worldDir);

            AnvilImportData importData = new AnvilImportData(worldDir, "testworld", mockLoader);

            assertThrows(RuntimeException.class, () ->
                AnvilWorldReader.INSTANCE.readFromData(importData)
            );
        }

        @Test
        @DisplayName("should throw when world directory does not exist")
        void shouldThrowWhenWorldDirectoryDoesNotExist() {
            Path nonExistentDir = tempDir.resolve("nonexistent");

            AnvilImportData importData = new AnvilImportData(nonExistentDir, "testworld", mockLoader);

            assertThrows(RuntimeException.class, () ->
                AnvilWorldReader.INSTANCE.readFromData(importData)
            );
        }

        @Test
        @DisplayName("should throw when level.dat is a directory")
        void shouldThrowWhenLevelDatIsDirectory() throws IOException {
            Path worldDir = tempDir.resolve("badworld");
            Files.createDirectories(worldDir);
            Files.createDirectories(worldDir.resolve("level.dat"));

            AnvilImportData importData = new AnvilImportData(worldDir, "testworld", mockLoader);

            assertThrows(RuntimeException.class, () ->
                AnvilWorldReader.INSTANCE.readFromData(importData)
            );
        }
    }

    @Nested
    @DisplayName("AnvilImportData")
    class AnvilImportDataTests {

        @Test
        @DisplayName("should store world directory")
        void shouldStoreWorldDirectory() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", mockLoader);

            assertEquals(worldDir, data.worldDir());
        }

        @Test
        @DisplayName("should store new name")
        void shouldStoreNewName() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", mockLoader);

            assertEquals("newname", data.newName());
        }

        @Test
        @DisplayName("should store loader")
        void shouldStoreLoader() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", mockLoader);

            assertSame(mockLoader, data.loader());
        }

        @Test
        @DisplayName("should allow null loader")
        void shouldAllowNullLoader() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", null);

            assertNull(data.loader());
        }
    }
}
