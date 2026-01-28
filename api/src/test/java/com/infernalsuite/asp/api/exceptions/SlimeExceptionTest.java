package com.infernalsuite.asp.api.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Slime Exceptions")
class SlimeExceptionTest {

    @Nested
    @DisplayName("CorruptedWorldException")
    class CorruptedWorldExceptionTests {

        @Test
        @DisplayName("should contain world name in message")
        void shouldContainWorldName() {
            CorruptedWorldException ex = new CorruptedWorldException("test_world");
            assertTrue(ex.getMessage().contains("test_world"));
            assertTrue(ex.getMessage().toLowerCase().contains("corrupted"));
        }

        @Test
        @DisplayName("should store cause exception")
        void shouldStoreCause() {
            Exception cause = new RuntimeException("Original error");
            CorruptedWorldException ex = new CorruptedWorldException("test_world", cause);
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("should handle null cause")
        void shouldHandleNullCause() {
            CorruptedWorldException ex = new CorruptedWorldException("world", null);
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            CorruptedWorldException ex = new CorruptedWorldException("world");
            assertInstanceOf(SlimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("InvalidWorldException")
    class InvalidWorldExceptionTests {

        @Test
        @DisplayName("should contain directory path in message")
        void shouldContainPath() {
            Path worldPath = Path.of("my_world");
            InvalidWorldException ex = new InvalidWorldException(worldPath);
            assertTrue(ex.getMessage().contains("my_world"));
            assertTrue(ex.getMessage().toLowerCase().contains("valid"));
        }

        @Test
        @DisplayName("should contain reason in message")
        void shouldContainReason() {
            Path worldPath = Path.of("test_world");
            InvalidWorldException ex = new InvalidWorldException(worldPath, "Missing level.dat");
            assertTrue(ex.getMessage().contains("Missing level.dat"));
        }

        @Test
        @DisplayName("legacy should convert File to Path")
        void legacyShouldConvertFileToPath() {
            File worldDir = new File("legacy_world");
            InvalidWorldException ex = InvalidWorldException.legacy(worldDir);
            assertTrue(ex.getMessage().contains("legacy_world"));
        }

        @Test
        @DisplayName("legacy should include reason")
        void legacyShouldIncludeReason() {
            File worldDir = new File("old_world");
            InvalidWorldException ex = InvalidWorldException.legacy(worldDir, "Unsupported format");
            assertTrue(ex.getMessage().contains("Unsupported format"));
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            InvalidWorldException ex = new InvalidWorldException(Path.of("world"));
            assertInstanceOf(SlimeException.class, ex);
        }

        @Test
        @DisplayName("should handle absolute path")
        void shouldHandleAbsolutePath() {
            Path absolutePath = Path.of("/home/user/worlds/my_world").toAbsolutePath();
            InvalidWorldException ex = new InvalidWorldException(absolutePath);
            assertTrue(ex.getMessage().contains("my_world") || ex.getMessage().contains(absolutePath.toString()));
        }
    }

    @Nested
    @DisplayName("NewerFormatException")
    class NewerFormatExceptionTests {

        @Test
        @DisplayName("should contain version in message")
        void shouldContainVersion() {
            NewerFormatException ex = new NewerFormatException((byte) 15);
            assertTrue(ex.getMessage().contains("15"));
        }

        @Test
        @DisplayName("should format version with v prefix")
        void shouldFormatVersionWithVPrefix() {
            NewerFormatException ex = new NewerFormatException((byte) 20);
            assertTrue(ex.getMessage().contains("v20"));
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            NewerFormatException ex = new NewerFormatException((byte) 1);
            assertInstanceOf(SlimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("UnknownWorldException")
    class UnknownWorldExceptionTests {

        @Test
        @DisplayName("should contain world name in message")
        void shouldContainWorldName() {
            UnknownWorldException ex = new UnknownWorldException("missing_world");
            assertTrue(ex.getMessage().contains("missing_world"));
            assertTrue(ex.getMessage().toLowerCase().contains("unknown"));
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            UnknownWorldException ex = new UnknownWorldException("world");
            assertInstanceOf(SlimeException.class, ex);
        }

        @Test
        @DisplayName("should handle special characters in name")
        void shouldHandleSpecialCharacters() {
            UnknownWorldException ex = new UnknownWorldException("world-with_special.chars:123");
            assertTrue(ex.getMessage().contains("world-with_special.chars:123"));
        }
    }

    @Nested
    @DisplayName("WorldAlreadyExistsException")
    class WorldAlreadyExistsExceptionTests {

        @Test
        @DisplayName("should contain world name in message")
        void shouldContainWorldName() {
            WorldAlreadyExistsException ex = new WorldAlreadyExistsException("existing_world");
            assertTrue(ex.getMessage().contains("existing_world"));
            assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            WorldAlreadyExistsException ex = new WorldAlreadyExistsException("world");
            assertInstanceOf(SlimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("WorldLoadedException")
    class WorldLoadedExceptionTests {

        @Test
        @DisplayName("should contain world name in message")
        void shouldContainWorldName() {
            WorldLoadedException ex = new WorldLoadedException("loaded_world");
            assertTrue(ex.getMessage().contains("loaded_world"));
            assertTrue(ex.getMessage().toLowerCase().contains("loaded"));
        }

        @Test
        @DisplayName("should mention unloading")
        void shouldMentionUnloading() {
            WorldLoadedException ex = new WorldLoadedException("world");
            assertTrue(ex.getMessage().toLowerCase().contains("unload"));
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            WorldLoadedException ex = new WorldLoadedException("world");
            assertInstanceOf(SlimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("WorldTooBigException")
    class WorldTooBigExceptionTests {

        @Test
        @DisplayName("should contain world name in message")
        void shouldContainWorldName() {
            WorldTooBigException ex = new WorldTooBigException("huge_world");
            assertTrue(ex.getMessage().contains("huge_world"));
            assertTrue(ex.getMessage().toLowerCase().contains("too big"));
        }

        @Test
        @DisplayName("should extend SlimeException")
        void shouldExtendSlimeException() {
            WorldTooBigException ex = new WorldTooBigException("world");
            assertInstanceOf(SlimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("SlimeException base class")
    class SlimeExceptionBaseTests {

        @Test
        @DisplayName("all exceptions should extend SlimeException")
        void allExceptionsShouldExtendSlimeException() {
            assertInstanceOf(SlimeException.class, new CorruptedWorldException("w"));
            assertInstanceOf(SlimeException.class, new InvalidWorldException(Path.of("w")));
            assertInstanceOf(SlimeException.class, new NewerFormatException((byte) 1));
            assertInstanceOf(SlimeException.class, new UnknownWorldException("w"));
            assertInstanceOf(SlimeException.class, new WorldAlreadyExistsException("w"));
            assertInstanceOf(SlimeException.class, new WorldLoadedException("w"));
            assertInstanceOf(SlimeException.class, new WorldTooBigException("w"));
        }

        @Test
        @DisplayName("SlimeException should extend Exception")
        void slimeExceptionShouldExtendException() {
            SlimeException ex = new UnknownWorldException("test");
            assertInstanceOf(Exception.class, ex);
        }
    }
}
