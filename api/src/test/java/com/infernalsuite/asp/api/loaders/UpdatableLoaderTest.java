package com.infernalsuite.asp.api.loaders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdatableLoader")
class UpdatableLoaderTest {

    @Nested
    @DisplayName("NewerStorageException")
    class NewerStorageExceptionTests {

        @Test
        @DisplayName("should store implementation version")
        void shouldStoreImplementationVersion() {
            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(1, 5);

            assertEquals(1, ex.getImplementationVersion());
        }

        @Test
        @DisplayName("should store storage version")
        void shouldStoreStorageVersion() {
            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(1, 5);

            assertEquals(5, ex.getStorageVersion());
        }

        @Test
        @DisplayName("should handle same versions")
        void shouldHandleSameVersions() {
            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(3, 3);

            assertEquals(3, ex.getImplementationVersion());
            assertEquals(3, ex.getStorageVersion());
        }

        @Test
        @DisplayName("should handle zero versions")
        void shouldHandleZeroVersions() {
            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(0, 0);

            assertEquals(0, ex.getImplementationVersion());
            assertEquals(0, ex.getStorageVersion());
        }

        @Test
        @DisplayName("should handle negative versions")
        void shouldHandleNegativeVersions() {
            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(-1, -2);

            assertEquals(-1, ex.getImplementationVersion());
            assertEquals(-2, ex.getStorageVersion());
        }

        @Test
        @DisplayName("should be Exception subclass")
        void shouldBeExceptionSubclass() {
            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(1, 2);

            assertInstanceOf(Exception.class, ex);
        }

        @Test
        @DisplayName("typical usage: newer storage than implementation")
        void typicalUsageNewerStorage() {
            int implementationVersion = 1;
            int storageVersion = 5;

            UpdatableLoader.NewerStorageException ex = 
                new UpdatableLoader.NewerStorageException(implementationVersion, storageVersion);

            assertTrue(ex.getStorageVersion() > ex.getImplementationVersion());
        }
    }
}
