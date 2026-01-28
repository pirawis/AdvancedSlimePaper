package com.infernalsuite.asp.pdc;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdventurePersistentDataContainer")
class AdventurePersistentDataContainerTest {

    private AdventurePersistentDataContainer container;

    @BeforeEach
    void setUp() {
        container = new AdventurePersistentDataContainer();
    }

    @Nested
    @DisplayName("constructors")
    class ConstructorTests {

        @Test
        @DisplayName("should create empty container with default constructor")
        void shouldCreateEmptyContainerWithDefaultConstructor() {
            AdventurePersistentDataContainer pdc = new AdventurePersistentDataContainer();

            assertTrue(pdc.isEmpty());
            assertTrue(pdc.getTags().isEmpty());
        }

        @Test
        @DisplayName("should create container with registry")
        void shouldCreateContainerWithRegistry() {
            AdventureDataTypeRegistry registry = new AdventureDataTypeRegistry();
            AdventurePersistentDataContainer pdc = new AdventurePersistentDataContainer(registry);

            assertTrue(pdc.isEmpty());
        }

        @Test
        @DisplayName("should create container from map")
        void shouldCreateContainerFromMap() {
            Map<String, BinaryTag> tags = new HashMap<>();
            tags.put("test:key", IntBinaryTag.intBinaryTag(42));

            AdventurePersistentDataContainer pdc = new AdventurePersistentDataContainer(tags);

            assertFalse(pdc.isEmpty());
            assertEquals(1, pdc.getTags().size());
        }

        @Test
        @DisplayName("should create container from compound tag")
        void shouldCreateContainerFromCompoundTag() {
            CompoundBinaryTag compound = CompoundBinaryTag.builder()
                    .putInt("test:value", 100)
                    .build();

            AdventurePersistentDataContainer pdc = new AdventurePersistentDataContainer(compound);

            assertFalse(pdc.isEmpty());
        }
    }

    @Nested
    @DisplayName("basic operations")
    class BasicOperationTests {

        @Test
        @DisplayName("should set and get integer value")
        void shouldSetAndGetIntegerValue() {
            NamespacedKey key = new NamespacedKey("test", "integer");

            container.set(key, PersistentDataType.INTEGER, 42);
            Integer result = container.get(key, PersistentDataType.INTEGER);

            assertEquals(42, result);
        }

        @Test
        @DisplayName("should set and get string value")
        void shouldSetAndGetStringValue() {
            NamespacedKey key = new NamespacedKey("test", "string");

            container.set(key, PersistentDataType.STRING, "hello");
            String result = container.get(key, PersistentDataType.STRING);

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("should return null for non-existent key")
        void shouldReturnNullForNonExistentKey() {
            NamespacedKey key = new NamespacedKey("test", "missing");

            String result = container.get(key, PersistentDataType.STRING);

            assertNull(result);
        }

        @Test
        @DisplayName("should return default value for non-existent key")
        void shouldReturnDefaultValueForNonExistentKey() {
            NamespacedKey key = new NamespacedKey("test", "missing");

            String result = container.getOrDefault(key, PersistentDataType.STRING, "default");

            assertEquals("default", result);
        }

        @Test
        @DisplayName("should check if key exists with type")
        void shouldCheckIfKeyExistsWithType() {
            NamespacedKey key = new NamespacedKey("test", "exists");
            container.set(key, PersistentDataType.INTEGER, 10);

            assertTrue(container.has(key, PersistentDataType.INTEGER));
            assertFalse(container.has(key, PersistentDataType.STRING));
        }

        @Test
        @DisplayName("should check if key exists without type")
        void shouldCheckIfKeyExistsWithoutType() {
            NamespacedKey key = new NamespacedKey("test", "exists");
            container.set(key, PersistentDataType.INTEGER, 10);

            assertTrue(container.has(key));
        }

        @Test
        @DisplayName("should remove key")
        void shouldRemoveKey() {
            NamespacedKey key = new NamespacedKey("test", "toremove");
            container.set(key, PersistentDataType.INTEGER, 5);

            container.remove(key);

            assertFalse(container.has(key));
        }
    }

    @Nested
    @DisplayName("getKeys")
    class GetKeysTests {

        @Test
        @DisplayName("should return empty set when container is empty")
        void shouldReturnEmptySetWhenContainerIsEmpty() {
            Set<NamespacedKey> keys = container.getKeys();

            assertTrue(keys.isEmpty());
        }

        @Test
        @DisplayName("should return all keys")
        void shouldReturnAllKeys() {
            container.set(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER, 1);
            container.set(new NamespacedKey("test", "key2"), PersistentDataType.INTEGER, 2);

            Set<NamespacedKey> keys = container.getKeys();

            assertEquals(2, keys.size());
        }
    }

    @Nested
    @DisplayName("toCompound")
    class ToCompoundTests {

        @Test
        @DisplayName("should convert to compound tag")
        void shouldConvertToCompoundTag() {
            container.set(new NamespacedKey("test", "value"), PersistentDataType.INTEGER, 42);

            CompoundBinaryTag compound = container.toCompound();

            assertNotNull(compound);
            assertFalse(compound.keySet().isEmpty());
        }

        @Test
        @DisplayName("should return empty compound when container is empty")
        void shouldReturnEmptyCompoundWhenContainerIsEmpty() {
            CompoundBinaryTag compound = container.toCompound();

            assertTrue(compound.keySet().isEmpty());
        }
    }

    @Nested
    @DisplayName("copyTo")
    class CopyToTests {

        @Test
        @DisplayName("should copy all tags to another container with replace")
        void shouldCopyAllTagsWithReplace() {
            container.set(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER, 1);

            AdventurePersistentDataContainer other = new AdventurePersistentDataContainer();
            other.set(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER, 999);

            container.copyTo(other, true);

            assertEquals(1, other.get(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER));
        }

        @Test
        @DisplayName("should copy tags without replacing existing")
        void shouldCopyTagsWithoutReplacing() {
            container.set(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER, 1);

            AdventurePersistentDataContainer other = new AdventurePersistentDataContainer();
            other.set(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER, 999);

            container.copyTo(other, false);

            assertEquals(999, other.get(new NamespacedKey("test", "key1"), PersistentDataType.INTEGER));
        }
    }

    @Nested
    @DisplayName("serialization")
    class SerializationTests {

        @Test
        @DisplayName("should serialize empty container to empty bytes")
        void shouldSerializeEmptyContainerToEmptyBytes() throws IOException {
            byte[] bytes = container.serializeToBytes();

            assertEquals(0, bytes.length);
        }

        @Test
        @DisplayName("should serialize and deserialize container")
        void shouldSerializeAndDeserializeContainer() throws IOException {
            container.set(new NamespacedKey("test", "value"), PersistentDataType.INTEGER, 42);

            byte[] bytes = container.serializeToBytes();
            AdventurePersistentDataContainer newContainer = new AdventurePersistentDataContainer();
            newContainer.readFromBytes(bytes, false);

            assertEquals(42, newContainer.get(new NamespacedKey("test", "value"), PersistentDataType.INTEGER));
        }

        @Test
        @DisplayName("should clear container before reading when clear is true")
        void shouldClearContainerBeforeReading() throws IOException {
            container.set(new NamespacedKey("test", "old"), PersistentDataType.STRING, "old");

            AdventurePersistentDataContainer source = new AdventurePersistentDataContainer();
            source.set(new NamespacedKey("test", "new"), PersistentDataType.STRING, "new");
            byte[] bytes = source.serializeToBytes();

            container.readFromBytes(bytes, true);

            assertFalse(container.has(new NamespacedKey("test", "old")));
            assertTrue(container.has(new NamespacedKey("test", "new")));
        }

        @Test
        @DisplayName("should handle empty bytes on read")
        void shouldHandleEmptyBytesOnRead() throws IOException {
            container.set(new NamespacedKey("test", "existing"), PersistentDataType.STRING, "value");

            container.readFromBytes(new byte[0], false);

            assertTrue(container.has(new NamespacedKey("test", "existing")));
        }
    }

    @Nested
    @DisplayName("newPersistentDataContainer")
    class NewContainerTests {

        @Test
        @DisplayName("should create new empty container")
        void shouldCreateNewEmptyContainer() {
            var newContainer = container.newPersistentDataContainer();

            assertNotNull(newContainer);
            assertTrue(newContainer.isEmpty());
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            assertEquals(container, container);
        }

        @Test
        @DisplayName("should be equal to container with same tags")
        void shouldBeEqualToContainerWithSameTags() {
            container.set(new NamespacedKey("test", "key"), PersistentDataType.INTEGER, 42);

            AdventurePersistentDataContainer other = new AdventurePersistentDataContainer();
            other.set(new NamespacedKey("test", "key"), PersistentDataType.INTEGER, 42);

            assertEquals(container, other);
        }

        @Test
        @DisplayName("should not be equal to container with different tags")
        void shouldNotBeEqualToContainerWithDifferentTags() {
            container.set(new NamespacedKey("test", "key"), PersistentDataType.INTEGER, 42);

            AdventurePersistentDataContainer other = new AdventurePersistentDataContainer();
            other.set(new NamespacedKey("test", "key"), PersistentDataType.INTEGER, 99);

            assertNotEquals(container, other);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, container);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            container.set(new NamespacedKey("test", "key"), PersistentDataType.INTEGER, 42);

            int hash1 = container.hashCode();
            int hash2 = container.hashCode();

            assertEquals(hash1, hash2);
        }
    }

    @Nested
    @DisplayName("getTags")
    class GetTagsTests {

        @Test
        @DisplayName("should return unmodifiable map")
        void shouldReturnUnmodifiableMap() {
            Map<String, BinaryTag> tags = container.getTags();

            assertThrows(UnsupportedOperationException.class, () ->
                    tags.put("test:key", StringBinaryTag.stringBinaryTag("value")));
        }
    }
}
