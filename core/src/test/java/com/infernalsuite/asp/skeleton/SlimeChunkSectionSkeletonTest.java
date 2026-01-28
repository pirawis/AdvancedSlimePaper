package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.utils.NibbleArray;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeChunkSectionSkeleton")
class SlimeChunkSectionSkeletonTest {

    @Nested
    @DisplayName("constructor and getters")
    class ConstructorTests {

        @Test
        @DisplayName("should store all parameters")
        void shouldStoreAllParameters() {
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder().putInt("test", 1).build();
            CompoundBinaryTag biome = CompoundBinaryTag.builder().putString("biome", "plains").build();
            NibbleArray blockLight = new NibbleArray(2048);
            NibbleArray skyLight = new NibbleArray(2048);

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                blockStates, biome, blockLight, skyLight
            );

            assertSame(blockStates, section.getBlockStatesTag());
            assertSame(biome, section.getBiomeTag());
            assertSame(blockLight, section.getBlockLight());
            assertSame(skyLight, section.getSkyLight());
        }

        @Test
        @DisplayName("should allow null block light")
        void shouldAllowNullBlockLight() {
            CompoundBinaryTag blockStates = CompoundBinaryTag.empty();
            CompoundBinaryTag biome = CompoundBinaryTag.empty();

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                blockStates, biome, null, new NibbleArray(2048)
            );

            assertNull(section.getBlockLight());
        }

        @Test
        @DisplayName("should allow null sky light")
        void shouldAllowNullSkyLight() {
            CompoundBinaryTag blockStates = CompoundBinaryTag.empty();
            CompoundBinaryTag biome = CompoundBinaryTag.empty();

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                blockStates, biome, new NibbleArray(2048), null
            );

            assertNull(section.getSkyLight());
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should access blockStates via record accessor")
        void shouldAccessBlockStatesViaRecordAccessor() {
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder().putInt("data", 42).build();

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                blockStates, CompoundBinaryTag.empty(), null, null
            );

            assertSame(blockStates, section.blockStates());
        }

        @Test
        @DisplayName("should access biome via record accessor")
        void shouldAccessBiomeViaRecordAccessor() {
            CompoundBinaryTag biome = CompoundBinaryTag.builder().putString("type", "desert").build();

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                CompoundBinaryTag.empty(), biome, null, null
            );

            assertSame(biome, section.biome());
        }

        @Test
        @DisplayName("should access block via record accessor")
        void shouldAccessBlockViaRecordAccessor() {
            NibbleArray blockLight = new NibbleArray(2048);

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                CompoundBinaryTag.empty(), CompoundBinaryTag.empty(), blockLight, null
            );

            assertSame(blockLight, section.block());
        }

        @Test
        @DisplayName("should access light via record accessor")
        void shouldAccessLightViaRecordAccessor() {
            NibbleArray skyLight = new NibbleArray(2048);

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                CompoundBinaryTag.empty(), CompoundBinaryTag.empty(), null, skyLight
            );

            assertSame(skyLight, section.light());
        }
    }

    @Nested
    @DisplayName("SlimeChunkSection interface")
    class InterfaceTests {

        @Test
        @DisplayName("getBlockStatesTag should return blockStates")
        void getBlockStatesTagShouldReturnBlockStates() {
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder().putInt("value", 100).build();

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                blockStates, CompoundBinaryTag.empty(), null, null
            );

            assertEquals(blockStates, section.getBlockStatesTag());
        }

        @Test
        @DisplayName("getBiomeTag should return biome")
        void getBiomeTagShouldReturnBiome() {
            CompoundBinaryTag biome = CompoundBinaryTag.builder().putString("name", "ocean").build();

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                CompoundBinaryTag.empty(), biome, null, null
            );

            assertEquals(biome, section.getBiomeTag());
        }

        @Test
        @DisplayName("getBlockLight should return block light array")
        void getBlockLightShouldReturnBlockLightArray() {
            NibbleArray blockLight = new NibbleArray(2048);
            blockLight.set(0, 15);

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                CompoundBinaryTag.empty(), CompoundBinaryTag.empty(), blockLight, null
            );

            assertNotNull(section.getBlockLight());
            assertEquals(15, section.getBlockLight().get(0));
        }

        @Test
        @DisplayName("getSkyLight should return sky light array")
        void getSkyLightShouldReturnSkyLightArray() {
            NibbleArray skyLight = new NibbleArray(2048);
            skyLight.set(100, 8);

            SlimeChunkSectionSkeleton section = new SlimeChunkSectionSkeleton(
                CompoundBinaryTag.empty(), CompoundBinaryTag.empty(), null, skyLight
            );

            assertNotNull(section.getSkyLight());
            assertEquals(8, section.getSkyLight().get(100));
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("equal sections should be equal")
        void equalSectionsShouldBeEqual() {
            CompoundBinaryTag blockStates = CompoundBinaryTag.empty();
            CompoundBinaryTag biome = CompoundBinaryTag.empty();

            SlimeChunkSectionSkeleton section1 = new SlimeChunkSectionSkeleton(
                blockStates, biome, null, null
            );
            SlimeChunkSectionSkeleton section2 = new SlimeChunkSectionSkeleton(
                blockStates, biome, null, null
            );

            assertEquals(section1, section2);
            assertEquals(section1.hashCode(), section2.hashCode());
        }
    }
}
