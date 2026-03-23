package com.infernalsuite.asp.serialization.slime.reader.impl.v1_9;

import com.github.luben.zstd.Zstd;
import com.infernalsuite.asp.Util;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.VersionedDeserializerTestSupport;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("v1_9 slime deserializer")
class v1_9SlimeWorldDeserializerTest {

    @Test
    @DisplayName("should deserialize v3 legacy sections and skip hypixel block data")
    void shouldDeserializeV3LegacySectionsAndSkipHypixelBlockData() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);

        byte[] chunkData;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            writeLegacyHeightMap(chunkOut);
            byte[] biomes = new byte[256];
            biomes[3] = 7;
            chunkOut.write(biomes);
            chunkOut.write(new byte[]{1, 0});
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x11));
            byte[] blocks = new byte[4096];
            blocks[0] = 5;
            chunkOut.write(blocks);
            byte[] data = new byte[2048];
            data[0] = 0x21;
            chunkOut.write(data);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x22));
            chunkOut.writeShort(3);
            chunkOut.write(new byte[]{9, 8, 7});
            chunkData = chunkBuffer.toByteArray();
        }

        byte[] worldBytes = buildWorldBytes(
                (byte) 3,
                null,
                null,
                (short) 0,
                (short) 0,
                (short) 1,
                (short) 1,
                new byte[]{1},
                chunkData,
                new byte[0],
                false,
                new byte[0],
                new byte[0],
                null,
                null
        );

        v1_9SlimeWorld world = new v1_9SlimeWorldDeserializer().deserializeWorld(
                (byte) 3,
                loader,
                "legacy-v3",
                VersionedDeserializerTestSupport.input(worldBytes),
                new SlimePropertyMap(),
                false
        );

        assertEquals(0, world.version);
        v1_9SlimeChunk chunk = world.chunks.get(Util.chunkPosition(0, 0));
        assertNotNull(chunk);
        assertEquals(16, chunk.sections.length);
        assertEquals(0, chunk.minY);
        assertEquals(16, chunk.maxY);
        assertNotNull(chunk.sections[0].blocks);
        assertNull(chunk.sections[0].palette);
        assertEquals(5, chunk.sections[0].blocks[0]);
        assertEquals(1, chunk.sections[0].data.get(0));
        assertNotNull(chunk.sections[0].blockLight);
        assertNotNull(chunk.sections[0].skyLight);
        assertEquals(64, chunk.biomes.length);
        assertEquals(7, chunk.biomes[0]);
    }

    @Test
    @DisplayName("should deserialize v8 pre-1.13 worlds and route entities by floored coordinates")
    void shouldDeserializeV8Pre13WorldAndRouteEntitiesByFlooredCoordinates() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);

        byte[] chunkData;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            writeLegacyHeightMap(chunkOut);
            chunkOut.writeInt(1234);
            byte[] biomes = new byte[256];
            biomes[3] = 7;
            biomes[7] = 9;
            chunkOut.write(biomes);
            chunkOut.write(new byte[]{1, 0});
            chunkOut.writeBoolean(false);
            byte[] blocks = new byte[4096];
            blocks[0] = 3;
            chunkOut.write(blocks);
            byte[] data = new byte[2048];
            data[0] = 0x43;
            chunkOut.write(data);
            chunkOut.writeBoolean(true);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x33));
            chunkData = chunkBuffer.toByteArray();
        }

        byte[] tileEntities = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                .put("tiles", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                        VersionedDeserializerTestSupport.tileEntity("minecraft:chest", -1, -1)
                )))
                .build());
        byte[] entities = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                .put("entities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                        CompoundBinaryTag.builder()
                                .putString("id", "minecraft:zombie")
                                .put("Pos", ListBinaryTag.listBinaryTag(BinaryTagTypes.DOUBLE, List.of(
                                        DoubleBinaryTag.doubleBinaryTag(-1.2),
                                        DoubleBinaryTag.doubleBinaryTag(64.0),
                                        DoubleBinaryTag.doubleBinaryTag(-0.1)
                                )))
                                .build()
                )))
                .build());

        byte[] worldBytes = buildWorldBytes(
                (byte) 8,
                (byte) 0x03,
                null,
                (short) -1,
                (short) -1,
                (short) 1,
                (short) 1,
                new byte[]{1},
                chunkData,
                tileEntities,
                true,
                entities,
                new byte[0],
                new byte[0],
                null
        );

        v1_9SlimeWorld world = new v1_9SlimeWorldDeserializer().deserializeWorld(
                (byte) 8,
                loader,
                "legacy-v8",
                VersionedDeserializerTestSupport.input(worldBytes),
                null,
                true
        );

        assertEquals(0x03, world.version);
        assertNotNull(world.propertyMap);
        assertTrue(world.extraCompound.isEmpty());
        assertTrue(world.readOnly);

        v1_9SlimeChunk chunk = world.chunks.get(Util.chunkPosition(-1, -1));
        assertNotNull(chunk);
        assertEquals(1, chunk.tileEntities.size());
        assertEquals(1, chunk.entities.size());
        assertNull(chunk.sections[0].blockLight);
        assertNotNull(chunk.sections[0].skyLight);
        assertEquals(64, chunk.biomes.length);
        assertEquals(7, chunk.biomes[0]);
        assertEquals(9, chunk.biomes[1]);
    }

    @Test
    @DisplayName("should deserialize v4 post-1.13 sections and merge world properties")
    void shouldDeserializeV4Post13SectionsAndMergeWorldProperties() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);
        SlimePropertyMap baseProperties = new SlimePropertyMap();
        baseProperties.setValue(SlimeProperties.ALLOW_MONSTERS, false);

        byte[] chunkData;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            byte[] heightMaps = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder().putInt("height", 99).build());
            chunkOut.writeInt(heightMaps.length);
            chunkOut.write(heightMaps);
            for (int i = 0; i < 256; i++) {
                chunkOut.writeInt(i);
            }
            chunkOut.write(new byte[]{1, 0});
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x41));
            CompoundBinaryTag paletteEntry = CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build();
            byte[] serializedPalette = VersionedDeserializerTestSupport.nbt(paletteEntry);
            chunkOut.writeInt(1);
            chunkOut.writeInt(serializedPalette.length);
            chunkOut.write(serializedPalette);
            chunkOut.writeInt(1);
            chunkOut.writeLong(55L);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x42));
            chunkData = chunkBuffer.toByteArray();
        }

        SlimePropertyMap overrideProperties = new SlimePropertyMap();
        overrideProperties.setValue(SlimeProperties.PVP, false);
        byte[] extra = VersionedDeserializerTestSupport.nbt(
                VersionedDeserializerTestSupport.worldExtra(overrideProperties, "source", StringBinaryTag.stringBinaryTag("v4"))
        );

        byte[] worldBytes = buildWorldBytes(
                (byte) 4,
                null,
                true,
                (short) 2,
                (short) 3,
                (short) 1,
                (short) 1,
                new byte[]{1},
                chunkData,
                new byte[0],
                false,
                new byte[0],
                extra,
                null,
                null
        );

        v1_9SlimeWorld world = new v1_9SlimeWorldDeserializer().deserializeWorld(
                (byte) 4,
                loader,
                "legacy-v4",
                VersionedDeserializerTestSupport.input(worldBytes),
                baseProperties,
                false
        );

        assertEquals(0x04, world.version);
        assertEquals("v4", ((StringBinaryTag) world.extraCompound.get("source")).value());
        assertFalse(world.propertyMap.getValue(SlimeProperties.PVP));
        assertFalse(world.propertyMap.getValue(SlimeProperties.ALLOW_MONSTERS));

        v1_9SlimeChunk chunk = world.chunks.get(Util.chunkPosition(2, 3));
        assertNotNull(chunk);
        assertEquals(256, chunk.biomes.length);
        assertEquals(0, chunk.biomes[0]);
        assertEquals(255, chunk.biomes[255]);
        assertNotNull(chunk.sections[0].palette);
        assertEquals(1, chunk.sections[0].palette.size());
        assertArrayEquals(new long[]{55L}, chunk.sections[0].blockStates);
        assertNotNull(chunk.sections[0].blockLight);
        assertNotNull(chunk.sections[0].skyLight);
    }

    @Test
    @DisplayName("should deserialize v6+ worlds using the new chunk section format")
    void shouldDeserializeV6WorldsUsingNewChunkSectionFormat() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);
        SlimePropertyMap baseProperties = new SlimePropertyMap();
        baseProperties.setValue(SlimeProperties.ALLOW_ANIMALS, false);

        byte[] chunkData;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            chunkOut.writeInt(0);
            chunkOut.writeInt(0);
            chunkOut.writeInt(2);
            chunkOut.writeInt(1);
            chunkOut.writeInt(1);
            chunkOut.writeBoolean(false);
            byte[] blockStates = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.blockStatesTag("minecraft:diamond_block"));
            chunkOut.writeInt(blockStates.length);
            chunkOut.write(blockStates);
            byte[] biomes = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.biomeTag("minecraft:the_end"));
            chunkOut.writeInt(biomes.length);
            chunkOut.write(biomes);
            chunkOut.writeBoolean(true);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x55));
            chunkData = chunkBuffer.toByteArray();
        }

        SlimePropertyMap overrideProperties = new SlimePropertyMap();
        overrideProperties.setValue(SlimeProperties.PVP, false);
        byte[] extra = VersionedDeserializerTestSupport.nbt(
                VersionedDeserializerTestSupport.worldExtra(overrideProperties, "source", StringBinaryTag.stringBinaryTag("v6"))
        );

        byte[] worldBytes = buildWorldBytes(
                (byte) 6,
                (byte) 0x08,
                null,
                (short) 4,
                (short) 5,
                (short) 1,
                (short) 1,
                new byte[]{1},
                chunkData,
                new byte[0],
                false,
                new byte[0],
                extra,
                null,
                null
        );

        v1_9SlimeWorld world = new v1_9SlimeWorldDeserializer().deserializeWorld(
                (byte) 6,
                loader,
                "legacy-v6",
                VersionedDeserializerTestSupport.input(worldBytes),
                baseProperties,
                false
        );

        assertEquals(0x08, world.version);
        assertEquals("v6", ((StringBinaryTag) world.extraCompound.get("source")).value());
        assertFalse(world.propertyMap.getValue(SlimeProperties.PVP));
        assertFalse(world.propertyMap.getValue(SlimeProperties.ALLOW_ANIMALS));

        v1_9SlimeChunk chunk = world.chunks.get(Util.chunkPosition(4, 5));
        assertNotNull(chunk);
        assertNull(chunk.biomes);
        assertEquals(0, chunk.minY);
        assertEquals(2, chunk.maxY);
        assertEquals(2, chunk.sections.length);
        assertNull(chunk.sections[0]);
        assertNull(chunk.sections[1].blockLight);
        assertNotNull(chunk.sections[1].skyLight);
        assertEquals("minecraft:diamond_block", chunk.sections[1].blockStatesTag
                .getList("palette", BinaryTagTypes.COMPOUND)
                .getCompound(0)
                .getString("Name"));
    }

    @Test
    @DisplayName("should reject tile entities that point to missing chunks")
    void shouldRejectTileEntitiesThatPointToMissingChunks() throws Exception {
        byte[] worldBytes = buildWorldBytes(
                (byte) 8,
                (byte) 0x03,
                null,
                (short) 0,
                (short) 0,
                (short) 1,
                (short) 1,
                new byte[]{1},
                minimalLegacyChunkData(),
                VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                        .put("tiles", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                                VersionedDeserializerTestSupport.tileEntity("minecraft:chest", 16, 16)
                        )))
                        .build()),
                false,
                new byte[0],
                new byte[0],
                new byte[0],
                null
        );

        assertThrows(CorruptedWorldException.class, () -> new v1_9SlimeWorldDeserializer().deserializeWorld(
                (byte) 8,
                mock(SlimeLoader.class),
                "broken-tiles",
                VersionedDeserializerTestSupport.input(worldBytes),
                new SlimePropertyMap(),
                false
        ));
    }

    @Test
    @DisplayName("should reject trailing bytes after the world payload")
    void shouldRejectTrailingBytesAfterTheWorldPayload() throws Exception {
        byte[] worldBytes = buildWorldBytes(
                (byte) 8,
                (byte) 0x03,
                null,
                (short) 0,
                (short) 0,
                (short) 1,
                (short) 1,
                new byte[]{0},
                new byte[0],
                new byte[0],
                false,
                new byte[0],
                new byte[0],
                new byte[0],
                (byte) 99
        );

        assertThrows(CorruptedWorldException.class, () -> new v1_9SlimeWorldDeserializer().deserializeWorld(
                (byte) 8,
                mock(SlimeLoader.class),
                "trailing",
                VersionedDeserializerTestSupport.input(worldBytes),
                new SlimePropertyMap(),
                false
        ));
    }

    private static byte[] buildWorldBytes(
            final byte version,
            final Byte worldVersion,
            final Boolean post13World,
            final short minX,
            final short minZ,
            final short width,
            final short depth,
            final byte[] chunkBitmask,
            final byte[] chunkData,
            final byte[] tileEntities,
            final boolean hasEntities,
            final byte[] entities,
            final byte[] extra,
            final byte[] maps,
            final Byte trailingByte
    ) throws Exception {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(buffer)) {
            if (version >= 6) {
                out.writeByte(worldVersion);
            } else if (version >= 4) {
                out.writeBoolean(Boolean.TRUE.equals(post13World));
            }

            out.writeShort(minX);
            out.writeShort(minZ);
            out.writeShort(width);
            out.writeShort(depth);
            out.write(chunkBitmask);
            writeCompressed(out, chunkData);
            writeCompressed(out, tileEntities);

            if (version >= 3) {
                out.writeBoolean(hasEntities);
                if (hasEntities) {
                    writeCompressed(out, entities);
                }
            }

            if (version >= 2) {
                writeCompressed(out, extra);
            }

            if (version >= 7) {
                writeCompressed(out, maps == null ? new byte[0] : maps);
            }

            if (trailingByte != null) {
                out.writeByte(trailingByte);
            }
            return buffer.toByteArray();
        }
    }

    private static void writeCompressed(final DataOutputStream out, final byte[] raw) throws Exception {
        byte[] compressed = Zstd.compress(raw);
        out.writeInt(compressed.length);
        out.writeInt(raw.length);
        out.write(compressed);
    }

    private static void writeLegacyHeightMap(final DataOutputStream out) throws Exception {
        for (int i = 0; i < 256; i++) {
            out.writeInt(i);
        }
    }

    private static byte[] minimalLegacyChunkData() throws Exception {
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            writeLegacyHeightMap(chunkOut);
            chunkOut.writeInt(0);
            chunkOut.write(new byte[256]);
            chunkOut.write(new byte[]{0, 0});
            return chunkBuffer.toByteArray();
        }
    }
}
