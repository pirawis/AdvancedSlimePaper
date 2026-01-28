package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.world.SlimeChunkSection;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeChunkSkeleton")
class SlimeChunkSkeletonTest {

    @Nested
    @DisplayName("constructor and getters")
    class ConstructorTests {

        @Test
        @DisplayName("should store coordinates")
        void shouldStoreCoordinates() {
            SlimeChunkSkeleton chunk = createChunk(10, -5);

            assertEquals(10, chunk.getX());
            assertEquals(-5, chunk.getZ());
        }

        @Test
        @DisplayName("should store sections")
        void shouldStoreSections() {
            SlimeChunkSection[] sections = new SlimeChunkSection[24];

            SlimeChunkSkeleton chunk = new SlimeChunkSkeleton(
                0, 0, sections,
                CompoundBinaryTag.empty(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                null, null, null, null
            );

            assertSame(sections, chunk.getSections());
        }

        @Test
        @DisplayName("should store height maps")
        void shouldStoreHeightMaps() {
            CompoundBinaryTag heightMaps = CompoundBinaryTag.builder()
                .putLongArray("WORLD_SURFACE", new long[]{1, 2, 3})
                .build();

            SlimeChunkSkeleton chunk = new SlimeChunkSkeleton(
                0, 0, new SlimeChunkSection[0],
                heightMaps,
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                null, null, null, null
            );

            assertEquals(heightMaps, chunk.getHeightMaps());
        }

        @Test
        @DisplayName("should store tile entities")
        void shouldStoreTileEntities() {
            List<CompoundBinaryTag> tileEntities = new ArrayList<>();
            tileEntities.add(CompoundBinaryTag.builder().putString("id", "minecraft:chest").build());

            SlimeChunkSkeleton chunk = new SlimeChunkSkeleton(
                0, 0, new SlimeChunkSection[0],
                CompoundBinaryTag.empty(),
                tileEntities,
                new ArrayList<>(),
                new HashMap<>(),
                null, null, null, null
            );

            assertEquals(1, chunk.getTileEntities().size());
        }

        @Test
        @DisplayName("should store entities")
        void shouldStoreEntities() {
            List<CompoundBinaryTag> entities = new ArrayList<>();
            entities.add(CompoundBinaryTag.builder().putString("id", "minecraft:pig").build());
            entities.add(CompoundBinaryTag.builder().putString("id", "minecraft:cow").build());

            SlimeChunkSkeleton chunk = new SlimeChunkSkeleton(
                0, 0, new SlimeChunkSection[0],
                CompoundBinaryTag.empty(),
                new ArrayList<>(),
                entities,
                new HashMap<>(),
                null, null, null, null
            );

            assertEquals(2, chunk.getEntities().size());
        }

        @Test
        @DisplayName("should store extra data")
        void shouldStoreExtraData() {
            Map<String, BinaryTag> extraData = new HashMap<>();
            extraData.put("custom", IntBinaryTag.intBinaryTag(42));

            SlimeChunkSkeleton chunk = new SlimeChunkSkeleton(
                0, 0, new SlimeChunkSection[0],
                CompoundBinaryTag.empty(),
                new ArrayList<>(),
                new ArrayList<>(),
                extraData,
                null, null, null, null
            );

            assertEquals(1, chunk.getExtraData().size());
            assertEquals(IntBinaryTag.intBinaryTag(42), chunk.getExtraData().get("custom"));
        }
    }

    @Nested
    @DisplayName("optional data")
    class OptionalDataTests {

        @Test
        @DisplayName("should handle null upgrade data")
        void shouldHandleNullUpgradeData() {
            SlimeChunkSkeleton chunk = createChunk(0, 0);

            assertNull(chunk.getUpgradeData());
        }

        @Test
        @DisplayName("should handle null block ticks")
        void shouldHandleNullBlockTicks() {
            SlimeChunkSkeleton chunk = createChunk(0, 0);

            assertNull(chunk.getBlockTicks());
        }

        @Test
        @DisplayName("should handle null fluid ticks")
        void shouldHandleNullFluidTicks() {
            SlimeChunkSkeleton chunk = createChunk(0, 0);

            assertNull(chunk.getFluidTicks());
        }

        @Test
        @DisplayName("should handle null poi chunk sections")
        void shouldHandleNullPoiChunkSections() {
            SlimeChunkSkeleton chunk = createChunk(0, 0);

            assertNull(chunk.getPoiChunkSections());
        }

        @Test
        @DisplayName("should store block ticks when provided")
        void shouldStoreBlockTicks() {
            ListBinaryTag blockTicks = ListBinaryTag.empty();

            SlimeChunkSkeleton chunk = new SlimeChunkSkeleton(
                0, 0, new SlimeChunkSection[0],
                CompoundBinaryTag.empty(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                null, null, blockTicks, null
            );

            assertNotNull(chunk.getBlockTicks());
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should access x via record accessor")
        void shouldAccessXViaRecordAccessor() {
            SlimeChunkSkeleton chunk = createChunk(15, 0);

            assertEquals(15, chunk.x());
        }

        @Test
        @DisplayName("should access z via record accessor")
        void shouldAccessZViaRecordAccessor() {
            SlimeChunkSkeleton chunk = createChunk(0, -20);

            assertEquals(-20, chunk.z());
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        private SlimeChunkSkeleton createChunkWithSharedInstances(int x, int z,
                SlimeChunkSection[] sections, CompoundBinaryTag heightMaps,
                List<CompoundBinaryTag> tileEntities, List<CompoundBinaryTag> entities,
                Map<String, BinaryTag> extraData) {
            return new SlimeChunkSkeleton(x, z, sections, heightMaps, tileEntities, entities, extraData,
                null, null, null, null);
        }

        @Test
        @DisplayName("chunks with same instance references should be equal")
        void chunksWithSameInstanceReferencesShouldBeEqual() {
            // Records use reference equality for arrays, so we must share instances
            SlimeChunkSection[] sections = new SlimeChunkSection[0];
            CompoundBinaryTag heightMaps = CompoundBinaryTag.empty();
            List<CompoundBinaryTag> tileEntities = new ArrayList<>();
            List<CompoundBinaryTag> entities = new ArrayList<>();
            Map<String, BinaryTag> extraData = new HashMap<>();

            SlimeChunkSkeleton chunk1 = createChunkWithSharedInstances(5, 10, sections, heightMaps, tileEntities, entities, extraData);
            SlimeChunkSkeleton chunk2 = createChunkWithSharedInstances(5, 10, sections, heightMaps, tileEntities, entities, extraData);

            assertEquals(chunk1, chunk2);
        }

        @Test
        @DisplayName("chunks with different array instances should not be equal")
        void chunksWithDifferentArrayInstancesShouldNotBeEqual() {
            // Even with same content, different array instances mean not equal
            SlimeChunkSkeleton chunk1 = createChunk(5, 10);
            SlimeChunkSkeleton chunk2 = createChunk(5, 10);

            assertNotEquals(chunk1, chunk2);
        }

        @Test
        @DisplayName("chunks with different coordinates should not be equal")
        void chunksWithDifferentCoordinatesShouldNotBeEqual() {
            SlimeChunkSection[] sections = new SlimeChunkSection[0];
            CompoundBinaryTag heightMaps = CompoundBinaryTag.empty();
            List<CompoundBinaryTag> tileEntities = new ArrayList<>();
            List<CompoundBinaryTag> entities = new ArrayList<>();
            Map<String, BinaryTag> extraData = new HashMap<>();

            SlimeChunkSkeleton chunk1 = createChunkWithSharedInstances(5, 10, sections, heightMaps, tileEntities, entities, extraData);
            SlimeChunkSkeleton chunk2 = createChunkWithSharedInstances(10, 5, sections, heightMaps, tileEntities, entities, extraData);

            assertNotEquals(chunk1, chunk2);
        }
    }

    private SlimeChunkSkeleton createChunk(int x, int z) {
        return new SlimeChunkSkeleton(
            x, z, new SlimeChunkSection[0],
            CompoundBinaryTag.empty(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashMap<>(),
            null, null, null, null
        );
    }
}
