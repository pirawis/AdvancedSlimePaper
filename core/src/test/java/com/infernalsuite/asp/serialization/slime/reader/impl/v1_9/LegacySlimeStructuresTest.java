package com.infernalsuite.asp.serialization.slime.reader.impl.v1_9;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.utils.NibbleArray;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("Legacy slime v1_9 data structures")
class LegacySlimeStructuresTest {

    @Test
    @DisplayName("v1_9SlimeWorld should expose constructor data and supported data versions")
    void v19SlimeWorldShouldExposeConstructorDataAndSupportedDataVersions() {
        SlimeLoader loader = mock(SlimeLoader.class);
        Long2ObjectOpenHashMap<v1_9SlimeChunk> chunks = new Long2ObjectOpenHashMap<>();
        ConcurrentMap<String, BinaryTag> extraCompound = new ConcurrentHashMap<>();
        SlimePropertyMap propertyMap = new SlimePropertyMap();

        v1_9SlimeWorld world = new v1_9SlimeWorld((byte) 0x01, "arena", loader, chunks, extraCompound, propertyMap, true);

        assertEquals("arena", world.worldName);
        assertSame(loader, world.loader);
        assertSame(chunks, world.chunks);
        assertSame(extraCompound, world.extraCompound);
        assertSame(propertyMap, world.propertyMap);
        assertEquals((byte) 0x01, world.version);
        assertEquals(99, world.getDataVersion());

        world.version = 0x02;
        assertEquals(184, world.getDataVersion());
        world.version = 0x03;
        assertEquals(922, world.getDataVersion());
        world.version = 0x04;
        assertEquals(1631, world.getDataVersion());
        world.version = 0x05;
        assertEquals(1976, world.getDataVersion());
        world.version = 0x06;
        assertEquals(2586, world.getDataVersion());
        world.version = 0x07;
        assertEquals(2730, world.getDataVersion());
        world.version = 0x08;
        assertEquals(2975, world.getDataVersion());
    }

    @Test
    @DisplayName("v1_9SlimeWorld should reject unsupported versions")
    void v19SlimeWorldShouldRejectUnsupportedVersions() {
        v1_9SlimeWorld world = new v1_9SlimeWorld(
                (byte) 0x09,
                "arena",
                mock(SlimeLoader.class),
                new Long2ObjectOpenHashMap<>(),
                new ConcurrentHashMap<>(),
                new SlimePropertyMap(),
                false
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, world::getDataVersion);
        assertEquals("Unexpected value: 9", exception.getMessage());
    }

    @Test
    @DisplayName("v1_9SlimeChunk should retain all provided chunk data")
    void v19SlimeChunkShouldRetainAllProvidedChunkData() {
        v1_9SlimeChunkSection[] sections = {mock(v1_9SlimeChunkSection.class)};
        CompoundBinaryTag heightMap = CompoundBinaryTag.empty();
        int[] biomes = {1, 2, 3};
        List<CompoundBinaryTag> tileEntities = List.of(CompoundBinaryTag.empty());
        List<CompoundBinaryTag> entities = List.of(CompoundBinaryTag.empty());

        v1_9SlimeChunk chunk = new v1_9SlimeChunk("arena", 3, -2, sections, -4, 19, heightMap, biomes, tileEntities, entities);

        assertEquals("arena", chunk.worldName);
        assertEquals(3, chunk.x);
        assertEquals(-2, chunk.z);
        assertSame(sections, chunk.sections);
        assertEquals(-4, chunk.minY);
        assertEquals(19, chunk.maxY);
        assertSame(heightMap, chunk.heightMap);
        assertArrayEquals(biomes, chunk.biomes);
        assertSame(tileEntities, chunk.tileEntities);
        assertSame(entities, chunk.entities);
    }

    @Test
    @DisplayName("v1_9SlimeChunkSection should retain all provided section data")
    void v19SlimeChunkSectionShouldRetainAllProvidedSectionData() {
        byte[] blocks = {1, 2, 3};
        NibbleArray data = new NibbleArray(8);
        ListBinaryTag palette = ListBinaryTag.empty();
        long[] blockStates = {7L, 8L};
        CompoundBinaryTag blockStatesTag = CompoundBinaryTag.empty();
        CompoundBinaryTag biomeTag = CompoundBinaryTag.empty();
        NibbleArray blockLight = new NibbleArray(8);
        NibbleArray skyLight = new NibbleArray(8);

        v1_9SlimeChunkSection section = new v1_9SlimeChunkSection(
                blocks,
                data,
                palette,
                blockStates,
                blockStatesTag,
                biomeTag,
                blockLight,
                skyLight
        );

        assertSame(blocks, section.blocks);
        assertSame(data, section.data);
        assertSame(palette, section.palette);
        assertSame(blockStates, section.blockStates);
        assertSame(blockStatesTag, section.blockStatesTag);
        assertSame(biomeTag, section.biomeTag);
        assertSame(blockLight, section.blockLight);
        assertSame(skyLight, section.skyLight);
    }
}
