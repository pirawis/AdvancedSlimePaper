package com.infernalsuite.asp.api.loaders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdatableLoader")
class UpdatableLoaderTest {

    @Test
    @DisplayName("should allow concrete subclasses to implement update")
    void shouldAllowConcreteSubclassesToImplementUpdate() throws Exception {
        TestUpdatableLoader loader = new TestUpdatableLoader();

        loader.update();

        assertTrue(loader.updated);
    }

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

    private static final class TestUpdatableLoader extends UpdatableLoader {

        private boolean updated;

        @Override
        public void update() {
            this.updated = true;
        }

        @Override
        public byte[] readWorld(final String worldName) {
            return new byte[0];
        }

        @Override
        public boolean worldExists(final String worldName) {
            return false;
        }

        @Override
        public List<String> listWorlds() {
            return List.of();
        }

        @Override
        public void saveWorld(final String worldName, final byte[] serializedWorld) throws IOException {
        }

        @Override
        public void deleteWorld(final String worldName) throws IOException {
        }
    }
}
