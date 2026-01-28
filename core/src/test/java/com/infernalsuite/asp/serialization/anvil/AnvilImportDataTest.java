package com.infernalsuite.asp.serialization.anvil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnvilImportData")
class AnvilImportDataTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should store world directory path")
        void shouldStoreWorldDirectoryPath() {
            Path worldDir = tempDir.resolve("world");

            AnvilImportData data = new AnvilImportData(worldDir, "newWorld", null);

            assertEquals(worldDir, data.worldDir());
        }

        @Test
        @DisplayName("should store new name")
        void shouldStoreNewName() {
            AnvilImportData data = new AnvilImportData(tempDir, "imported_world", null);

            assertEquals("imported_world", data.newName());
        }

        @Test
        @DisplayName("should allow null loader")
        void shouldAllowNullLoader() {
            AnvilImportData data = new AnvilImportData(tempDir, "test", null);

            assertNull(data.loader());
        }
    }

    @Nested
    @DisplayName("legacy factory method")
    class LegacyFactoryTests {

        @Test
        @DisplayName("should convert File to Path")
        void shouldConvertFileToPath() {
            File worldDir = tempDir.toFile();

            AnvilImportData data = AnvilImportData.legacy(worldDir, "legacyWorld", null);

            assertEquals(worldDir.toPath(), data.worldDir());
        }

        @Test
        @DisplayName("should preserve new name")
        void shouldPreserveNewName() {
            File worldDir = tempDir.toFile();

            AnvilImportData data = AnvilImportData.legacy(worldDir, "myWorld", null);

            assertEquals("myWorld", data.newName());
        }

        @Test
        @DisplayName("should preserve null loader")
        void shouldPreserveNullLoader() {
            File worldDir = tempDir.toFile();

            AnvilImportData data = AnvilImportData.legacy(worldDir, "test", null);

            assertNull(data.loader());
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("worldDir accessor should work")
        void worldDirAccessorShouldWork() {
            Path path = tempDir.resolve("myworld");
            AnvilImportData data = new AnvilImportData(path, "name", null);

            assertEquals(path, data.worldDir());
        }

        @Test
        @DisplayName("newName accessor should work")
        void newNameAccessorShouldWork() {
            AnvilImportData data = new AnvilImportData(tempDir, "testName", null);

            assertEquals("testName", data.newName());
        }

        @Test
        @DisplayName("loader accessor should work")
        void loaderAccessorShouldWork() {
            AnvilImportData data = new AnvilImportData(tempDir, "test", null);

            assertNull(data.loader());
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("equal data should be equal")
        void equalDataShouldBeEqual() {
            Path path = tempDir.resolve("world");
            AnvilImportData data1 = new AnvilImportData(path, "name", null);
            AnvilImportData data2 = new AnvilImportData(path, "name", null);

            assertEquals(data1, data2);
            assertEquals(data1.hashCode(), data2.hashCode());
        }

        @Test
        @DisplayName("different names should not be equal")
        void differentNamesShouldNotBeEqual() {
            AnvilImportData data1 = new AnvilImportData(tempDir, "name1", null);
            AnvilImportData data2 = new AnvilImportData(tempDir, "name2", null);

            assertNotEquals(data1, data2);
        }
    }
}
