package com.infernalsuite.asp.api.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Slime Exceptions")
class SlimeExceptionTest {

    @Test
    @DisplayName("CorruptedWorldException should contain world name in message")
    void corruptedWorldExceptionShouldContainWorldName() {
        CorruptedWorldException ex = new CorruptedWorldException("test_world");
        assertTrue(ex.getMessage().contains("test_world"));
        assertTrue(ex.getMessage().toLowerCase().contains("corrupted"));
    }

    @Test
    @DisplayName("CorruptedWorldException should store cause exception")
    void corruptedWorldExceptionShouldStoreCause() {
        Exception cause = new RuntimeException("Original error");
        CorruptedWorldException ex = new CorruptedWorldException("test_world", cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("InvalidWorldException should contain directory path in message")
    void invalidWorldExceptionShouldContainPath() {
        Path worldPath = Path.of("my_world");
        InvalidWorldException ex = new InvalidWorldException(worldPath);
        assertTrue(ex.getMessage().contains("my_world"));
        assertTrue(ex.getMessage().toLowerCase().contains("valid"));
    }

    @Test
    @DisplayName("InvalidWorldException should contain reason in message")
    void invalidWorldExceptionShouldContainReason() {
        Path worldPath = Path.of("test_world");
        InvalidWorldException ex = new InvalidWorldException(worldPath, "Missing level.dat");
        assertTrue(ex.getMessage().contains("Missing level.dat"));
    }

    @Test
    @DisplayName("NewerFormatException should contain version in message")
    void newerFormatExceptionShouldContainVersion() {
        NewerFormatException ex = new NewerFormatException((byte) 15);
        assertTrue(ex.getMessage().contains("15"));
    }

    @Test
    @DisplayName("UnknownWorldException should contain world name in message")
    void unknownWorldExceptionShouldContainWorldName() {
        UnknownWorldException ex = new UnknownWorldException("missing_world");
        assertTrue(ex.getMessage().contains("missing_world"));
        assertTrue(ex.getMessage().toLowerCase().contains("unknown"));
    }

    @Test
    @DisplayName("WorldAlreadyExistsException should contain world name in message")
    void worldAlreadyExistsExceptionShouldContainWorldName() {
        WorldAlreadyExistsException ex = new WorldAlreadyExistsException("existing_world");
        assertTrue(ex.getMessage().contains("existing_world"));
        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
    }

    @Test
    @DisplayName("WorldLoadedException should contain world name in message")
    void worldLoadedExceptionShouldContainWorldName() {
        WorldLoadedException ex = new WorldLoadedException("loaded_world");
        assertTrue(ex.getMessage().contains("loaded_world"));
        assertTrue(ex.getMessage().toLowerCase().contains("loaded"));
    }

    @Test
    @DisplayName("WorldTooBigException should contain world name in message")
    void worldTooBigExceptionShouldContainWorldName() {
        WorldTooBigException ex = new WorldTooBigException("huge_world");
        assertTrue(ex.getMessage().contains("huge_world"));
        assertTrue(ex.getMessage().toLowerCase().contains("too big"));
    }
}
