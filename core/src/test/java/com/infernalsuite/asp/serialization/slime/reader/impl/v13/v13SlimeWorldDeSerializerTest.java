package com.infernalsuite.asp.serialization.slime.reader.impl.v13;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.VersionedDeserializerTestSupport;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("v13 slime deserializer")
class v13SlimeWorldDeSerializerTest {

    @Test
    @DisplayName("should deserialize supported additional world data and ignore unsupported flags")
    void shouldDeserializeSupportedAdditionalWorldDataAndIgnoreUnsupportedFlags() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);
        SlimePropertyMap propertyMap = VersionedDeserializerTestSupport.propertyMap(0, 0);
        propertyMap.setValue(SlimeProperties.ALLOW_ANIMALS, false);

        byte additionalWorldData = (byte) (v13AdditionalWorldData.fromSet(EnumSet.allOf(v13AdditionalWorldData.class)) | (1 << 4));
        byte[] chunkBytes;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            chunkOut.writeInt(1);
            chunkOut.writeInt(4);
            chunkOut.writeInt(5);
            chunkOut.writeInt(1);
            chunkOut.writeByte(3);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x31));
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x32));
            byte[] blockStates = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.blockStatesTag("minecraft:emerald_block"));
            chunkOut.writeInt(blockStates.length);
            chunkOut.write(blockStates);
            byte[] biomes = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.biomeTag("minecraft:jungle"));
            chunkOut.writeInt(biomes.length);
            chunkOut.write(biomes);
            byte[] heightMaps = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder().putInt("height", 23).build());
            chunkOut.writeInt(heightMaps.length);
            chunkOut.write(heightMaps);
            byte[] poi = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder().putString("poi", "data").build());
            chunkOut.writeInt(poi.length);
            chunkOut.write(poi);
            byte[] blockTicks = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                    .put("block_ticks", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            CompoundBinaryTag.builder().putString("i", "block").build()
                    )))
                    .build());
            chunkOut.writeInt(blockTicks.length);
            chunkOut.write(blockTicks);
            byte[] fluidTicks = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                    .put("fluid_ticks", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            CompoundBinaryTag.builder().putString("i", "fluid").build()
                    )))
                    .build());
            chunkOut.writeInt(fluidTicks.length);
            chunkOut.write(fluidTicks);
            chunkOut.writeInt(3);
            chunkOut.write(new byte[]{9, 8, 7});
            VersionedDeserializerTestSupport.writeRawBlock(chunkOut, new byte[0]);
            VersionedDeserializerTestSupport.writeRawBlock(chunkOut, VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                    .put("entities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            VersionedDeserializerTestSupport.entity("minecraft:allay", 65.0, 80.0, 81.0)
                    )))
                    .build()));
            VersionedDeserializerTestSupport.writeRawBlock(chunkOut, VersionedDeserializerTestSupport.nbt(
                    CompoundBinaryTag.builder().putString("chunk_source", "v13-extra").build()
            ));
            chunkBytes = chunkBuffer.toByteArray();
        }

        SlimePropertyMap override = new SlimePropertyMap();
        override.setValue(SlimeProperties.PVP, false);
        byte[] extra = VersionedDeserializerTestSupport.nbt(
                VersionedDeserializerTestSupport.worldExtra(override, "source", StringBinaryTag.stringBinaryTag("v13"))
        );

        byte[] worldBytes;
        try (ByteArrayOutputStream worldBuffer = new ByteArrayOutputStream();
             DataOutputStream worldOut = new DataOutputStream(worldBuffer)) {
            worldOut.writeInt(3013);
            worldOut.writeByte(additionalWorldData);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, chunkBytes);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, extra);
            worldBytes = worldBuffer.toByteArray();
        }

        SlimeWorld world = new v13SlimeWorldDeSerializer().deserializeWorld(
                (byte) 13,
                loader,
                "v13-world",
                VersionedDeserializerTestSupport.input(worldBytes),
                propertyMap,
                false
        );

        assertEquals(3013, world.getDataVersion());
        assertEquals("v13", ((StringBinaryTag) world.getExtraData().get("source")).value());
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.PVP));
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.ALLOW_ANIMALS));

        SlimeChunk chunk = world.getChunk(4, 5);
        assertNotNull(chunk);
        assertNotNull(chunk.getPoiChunkSections());
        assertNotNull(chunk.getBlockTicks());
        assertNotNull(chunk.getFluidTicks());
        assertEquals(1, chunk.getEntities().size());
        assertEquals("v13-extra", ((StringBinaryTag) chunk.getExtraData().get("chunk_source")).value());
    }
}
