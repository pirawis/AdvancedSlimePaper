package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.Util;
import com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.SlimeSerializer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.kyori.adventure.nbt.BinaryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SkeletonSlimeWorld")
@ExtendWith(MockitoExtension.class)
class SkeletonSlimeWorldTest {

    @Mock
    private SlimeLoader loader;

    @Mock
    private SlimePropertyMap propertyMap;

    private SkeletonSlimeWorld world;
    private Long2ObjectMap<SlimeChunk> chunkStorage;
    private ConcurrentMap<String, BinaryTag> extraData;

    @BeforeEach
    void setUp() {
        chunkStorage = new Long2ObjectOpenHashMap<>();
        extraData = new ConcurrentHashMap<>();
        world = new SkeletonSlimeWorld(
                "testworld",
                loader,
                false,
                chunkStorage,
                extraData,
                propertyMap,
                3465
        );
    }

    @Nested
    @DisplayName("constructor and getters")
    class ConstructorAndGetterTests {

        @Test
        @DisplayName("should store name")
        void shouldStoreName() {
            assertEquals("testworld", world.getName());
            assertEquals("testworld", world.name());
        }

        @Test
        @DisplayName("should store loader")
        void shouldStoreLoader() {
            assertSame(loader, world.getLoader());
            assertSame(loader, world.loader());
        }

        @Test
        @DisplayName("should store readOnly flag")
        void shouldStoreReadOnlyFlag() {
            assertFalse(world.readOnly());
        }

        @Test
        @DisplayName("should store chunk storage")
        void shouldStoreChunkStorage() {
            assertSame(chunkStorage, world.chunkStorage());
        }

        @Test
        @DisplayName("should store extra data")
        void shouldStoreExtraData() {
            assertSame(extraData, world.getExtraData());
            assertSame(extraData, world.extraSerialized());
        }

        @Test
        @DisplayName("should store property map")
        void shouldStorePropertyMap() {
            assertSame(propertyMap, world.getPropertyMap());
            assertSame(propertyMap, world.slimePropertyMap());
        }

        @Test
        @DisplayName("should store data version")
        void shouldStoreDataVersion() {
            assertEquals(3465, world.getDataVersion());
            assertEquals(3465, world.dataVersion());
        }

        @Test
        @DisplayName("should return empty world maps")
        void shouldReturnEmptyWorldMaps() {
            assertTrue(world.getWorldMaps().isEmpty());
        }

        @Test
        @DisplayName("should return persistent data container")
        void shouldReturnPersistentDataContainer() {
            assertNotNull(world.getPersistentDataContainer());
        }
    }

    @Nested
    @DisplayName("isReadOnly")
    class IsReadOnlyTests {

        @Test
        @DisplayName("should return true when explicitly read only")
        void shouldReturnTrueWhenExplicitlyReadOnly() {
            SkeletonSlimeWorld readOnlyWorld = new SkeletonSlimeWorld(
                    "readonly", loader, true, chunkStorage, extraData, propertyMap, 3465
            );

            assertTrue(readOnlyWorld.isReadOnly());
        }

        @Test
        @DisplayName("should return true when loader is null")
        void shouldReturnTrueWhenLoaderIsNull() {
            SkeletonSlimeWorld noLoaderWorld = new SkeletonSlimeWorld(
                    "noloader", null, false, chunkStorage, extraData, propertyMap, 3465
            );

            assertTrue(noLoaderWorld.isReadOnly());
        }

        @Test
        @DisplayName("should return false when not read only and has loader")
        void shouldReturnFalseWhenNotReadOnlyAndHasLoader() {
            assertFalse(world.isReadOnly());
        }
    }

    @Nested
    @DisplayName("getChunk")
    class GetChunkTests {

        @Test
        @DisplayName("should return null for non-existent chunk")
        void shouldReturnNullForNonExistentChunk() {
            assertNull(world.getChunk(0, 0));
        }

        @Test
        @DisplayName("should return stored chunk for coordinates")
        void shouldReturnStoredChunkForCoordinates() {
            SlimeChunk chunk = mock(SlimeChunk.class);
            chunkStorage.put(Util.chunkPosition(2, -3), chunk);

            assertSame(chunk, world.getChunk(2, -3));
        }
    }

    @Nested
    @DisplayName("getChunkStorage")
    class GetChunkStorageTests {

        @Test
        @DisplayName("should expose stored chunk collection")
        void shouldExposeStoredChunkCollection() {
            SlimeChunk chunk = mock(SlimeChunk.class);
            chunkStorage.put(Util.chunkPosition(1, 1), chunk);

            assertEquals(1, world.getChunkStorage().size());
            assertTrue(world.getChunkStorage().contains(chunk));
        }
    }

    @Nested
    @DisplayName("clone")
    class CloneTests {

        @Test
        @DisplayName("should throw when clone name equals original name")
        void shouldThrowWhenCloneNameEqualsOriginalName() {
            assertThrows(IllegalArgumentException.class, () ->
                    world.clone("testworld", loader));
        }

        @Test
        @DisplayName("should throw when world name is null")
        void shouldThrowWhenWorldNameIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    world.clone(null, loader));
        }

        @Test
        @DisplayName("should throw when target loader already contains the world")
        void shouldThrowWhenTargetLoaderAlreadyContainsTheWorld() throws Exception {
            when(loader.worldExists("existing")).thenReturn(true);

            assertThrows(WorldAlreadyExistsException.class, () ->
                    world.clone("existing", loader));
        }

        @Test
        @DisplayName("should clone with provided loader and persist serialized bytes")
        void shouldCloneWithProvidedLoaderAndPersistSerializedBytes() throws Exception {
            SkeletonSlimeWorld cloned = mock(SkeletonSlimeWorld.class);
            byte[] serialized = new byte[]{1, 2, 3};

            when(loader.worldExists("copy")).thenReturn(false);

            try (MockedStatic<SkeletonCloning> cloning = mockStatic(SkeletonCloning.class);
                 MockedStatic<SlimeSerializer> serializer = mockStatic(SlimeSerializer.class)) {
                cloning.when(() -> SkeletonCloning.fullClone("copy", world, loader, false)).thenReturn(cloned);
                serializer.when(() -> SlimeSerializer.serialize(cloned)).thenReturn(serialized);

                assertSame(cloned, world.clone("copy", loader));
                verify(loader).saveWorld("copy", serialized);
            }
        }

        @Test
        @DisplayName("should clone without persistence when loader is null")
        void shouldCloneWithoutPersistenceWhenLoaderIsNull() {
            SkeletonSlimeWorld cloned = mock(SkeletonSlimeWorld.class);

            try (MockedStatic<SkeletonCloning> cloning = mockStatic(SkeletonCloning.class)) {
                cloning.when(() -> SkeletonCloning.fullClone("copy", world, null, false)).thenReturn(cloned);

                assertSame(cloned, world.clone("copy"));
                verifyNoInteractions(loader);
            }
        }

        @Test
        @DisplayName("should return null when simple clone hits an impossible checked exception")
        void shouldReturnNullWhenSimpleCloneHitsAnImpossibleCheckedException() throws Exception {
            SkeletonSlimeWorld spyWorld = spy(world);
            doThrow(new IOException("boom")).when(spyWorld).clone("copy", null);

            assertNull(spyWorld.clone("copy"));
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            assertEquals(world, world);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, world);
        }

        @Test
        @DisplayName("should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertNotEquals("string", world);
        }

        @Test
        @DisplayName("should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            SkeletonSlimeWorld other = new SkeletonSlimeWorld(
                    "testworld", loader, false, chunkStorage, extraData, propertyMap, 3465
            );

            assertEquals(world, other);
        }

        @Test
        @DisplayName("should not be equal when name differs")
        void shouldNotBeEqualWhenNameDiffers() {
            SkeletonSlimeWorld other = new SkeletonSlimeWorld(
                    "different", loader, false, chunkStorage, extraData, propertyMap, 3465
            );

            assertNotEquals(world, other);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            int hash1 = world.hashCode();
            int hash2 = world.hashCode();

            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("equal objects should have same hashCode")
        void equalObjectsShouldHaveSameHashCode() {
            SkeletonSlimeWorld other = new SkeletonSlimeWorld(
                    "testworld", loader, false, chunkStorage, extraData, propertyMap, 3465
            );

            assertEquals(world.hashCode(), other.hashCode());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include class name")
        void shouldIncludeClassName() {
            assertTrue(world.toString().contains("SkeletonSlimeWorld"));
        }

        @Test
        @DisplayName("should include world name")
        void shouldIncludeWorldName() {
            assertTrue(world.toString().contains("testworld"));
        }

        @Test
        @DisplayName("should include data version")
        void shouldIncludeDataVersion() {
            assertTrue(world.toString().contains("3465"));
        }
    }
}
