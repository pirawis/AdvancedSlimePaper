package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.utils.NibbleArray;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeChunkSection;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SkeletonCloning")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SkeletonCloningTest {

    @Mock
    private SlimeWorld sourceWorld;

    @Mock
    private SlimeLoader sourceLoader;

    @Mock
    private SlimeLoader targetLoader;

    @Mock
    private SlimePropertyMap propertyMap;

    @Mock
    private SlimePropertyMap clonedPropertyMap;

    private ConcurrentMap<String, BinaryTag> extraData;

    @BeforeEach
    void setUp() {
        extraData = new ConcurrentHashMap<>();
        extraData.put("test", IntBinaryTag.intBinaryTag(42));
    }

    @Nested
    @DisplayName("fullClone")
    class FullCloneTests {

        @BeforeEach
        void setUp() {
            when(sourceWorld.getChunkStorage()).thenReturn(new ArrayList<>());
            when(sourceWorld.getExtraData()).thenReturn(extraData);
            when(sourceWorld.getPropertyMap()).thenReturn(propertyMap);
            when(sourceWorld.getDataVersion()).thenReturn(3465);
            when(sourceWorld.getLoader()).thenReturn(sourceLoader);
            when(propertyMap.clone()).thenReturn(clonedPropertyMap);
        }

        @Test
        @DisplayName("should create new world with given name")
        void shouldCreateNewWorldWithGivenName() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertEquals("newworld", cloned.getName());
        }

        @Test
        @DisplayName("should use source loader when target is null")
        void shouldUseSourceLoaderWhenTargetIsNull() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, null, false);

            assertSame(sourceLoader, cloned.getLoader());
        }

        @Test
        @DisplayName("should clone extra data")
        void shouldCloneExtraData() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertEquals(1, cloned.getExtraData().size());
            assertEquals(IntBinaryTag.intBinaryTag(42), cloned.getExtraData().get("test"));
        }

        @Test
        @DisplayName("should preserve data version")
        void shouldPreserveDataVersion() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertEquals(3465, cloned.getDataVersion());
        }

        @Test
        @DisplayName("should clone property map")
        void shouldClonePropertyMap() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertSame(clonedPropertyMap, cloned.getPropertyMap());
            verify(propertyMap).clone();
        }

        @Test
        @DisplayName("should deep clone chunk storage and respect requested read only state")
        void shouldDeepCloneChunkStorageAndRespectRequestedReadOnlyState() {
            SlimeChunk chunk = mock(SlimeChunk.class);
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            NibbleArray blockLight = new NibbleArray(8);
            NibbleArray skyLight = new NibbleArray(8);
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build();
            CompoundBinaryTag biome = CompoundBinaryTag.builder().putString("biome", "minecraft:plains").build();
            CompoundBinaryTag heightMaps = CompoundBinaryTag.builder().putLongArray("MOTION_BLOCKING", new long[]{1L}).build();
            List<CompoundBinaryTag> tileEntities = new ArrayList<>(List.of(CompoundBinaryTag.empty()));
            List<CompoundBinaryTag> entities = new ArrayList<>(List.of(CompoundBinaryTag.empty()));
            Map<String, BinaryTag> chunkExtraData = new ConcurrentHashMap<>();
            chunkExtraData.put("extra", IntBinaryTag.intBinaryTag(7));
            ListBinaryTag blockTicks = ListBinaryTag.empty();
            ListBinaryTag fluidTicks = ListBinaryTag.empty();
            CompoundBinaryTag poiChunks = CompoundBinaryTag.empty();

            blockLight.set(0, 4);
            skyLight.set(0, 9);

            when(section.getBlockStatesTag()).thenReturn(blockStates);
            when(section.getBiomeTag()).thenReturn(biome);
            when(section.getBlockLight()).thenReturn(blockLight);
            when(section.getSkyLight()).thenReturn(skyLight);

            when(chunk.getX()).thenReturn(3);
            when(chunk.getZ()).thenReturn(4);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section, null});
            when(chunk.getHeightMaps()).thenReturn(heightMaps);
            when(chunk.getTileEntities()).thenReturn(tileEntities);
            when(chunk.getEntities()).thenReturn(entities);
            when(chunk.getExtraData()).thenReturn(chunkExtraData);
            when(chunk.getPoiChunkSections()).thenReturn(poiChunks);
            when(chunk.getBlockTicks()).thenReturn(blockTicks);
            when(chunk.getFluidTicks()).thenReturn(fluidTicks);

            when(sourceWorld.getChunkStorage()).thenReturn(List.of(chunk));

            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, true);

            assertSame(targetLoader, cloned.getLoader());
            assertTrue(cloned.isReadOnly());
            assertEquals(1, cloned.getChunkStorage().size());

            SlimeChunk clonedChunk = cloned.getChunk(3, 4);
            assertNotNull(clonedChunk);
            assertNotSame(chunk, clonedChunk);
            assertSame(heightMaps, clonedChunk.getHeightMaps());
            assertSame(blockTicks, clonedChunk.getBlockTicks());
            assertSame(fluidTicks, clonedChunk.getFluidTicks());
            assertSame(poiChunks, clonedChunk.getPoiChunkSections());
            assertNotSame(tileEntities, clonedChunk.getTileEntities());
            assertNotSame(entities, clonedChunk.getEntities());
            assertNotSame(chunkExtraData, clonedChunk.getExtraData());

            SlimeChunkSection clonedSection = clonedChunk.getSections()[0];
            assertNotNull(clonedSection);
            assertSame(blockStates, clonedSection.getBlockStatesTag());
            assertSame(biome, clonedSection.getBiomeTag());
            assertNotSame(blockLight, clonedSection.getBlockLight());
            assertNotSame(skyLight, clonedSection.getSkyLight());
            assertEquals(blockLight.get(0), clonedSection.getBlockLight().get(0));
            assertEquals(skyLight.get(0), clonedSection.getSkyLight().get(0));
            assertNull(clonedChunk.getSections()[1]);
        }
    }

    @Nested
    @DisplayName("weakCopy")
    class WeakCopyTests {

        @BeforeEach
        void setUp() {
            when(sourceWorld.getName()).thenReturn("original");
            when(sourceWorld.getLoader()).thenReturn(sourceLoader);
            when(sourceWorld.isReadOnly()).thenReturn(false);
            when(sourceWorld.getChunkStorage()).thenReturn(new ArrayList<>());
            when(sourceWorld.getExtraData()).thenReturn(extraData);
            when(sourceWorld.getPropertyMap()).thenReturn(propertyMap);
            when(sourceWorld.getDataVersion()).thenReturn(3465);
            when(propertyMap.clone()).thenReturn(clonedPropertyMap);
        }

        @Test
        @DisplayName("should preserve world name")
        void shouldPreserveWorldName() {
            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertEquals("original", copy.getName());
        }

        @Test
        @DisplayName("should preserve loader")
        void shouldPreserveLoader() {
            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertSame(sourceLoader, copy.getLoader());
        }

        @Test
        @DisplayName("should preserve readOnly flag")
        void shouldPreserveReadOnlyFlag() {
            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertFalse(copy.readOnly());
        }

        @Test
        @DisplayName("should copy chunks by reference")
        void shouldCopyChunksByReference() {
            SlimeChunk mockChunk = mock(SlimeChunk.class);
            when(mockChunk.getX()).thenReturn(1);
            when(mockChunk.getZ()).thenReturn(2);
            when(sourceWorld.getChunkStorage()).thenReturn(List.of(mockChunk));

            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertEquals(1, copy.getChunkStorage().size());
            assertSame(mockChunk, copy.getChunk(1, 2));
        }
    }
}
