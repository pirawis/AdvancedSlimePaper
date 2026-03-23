package com.infernalsuite.asp.serialization.slime.reader.impl.v11;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@DisplayName("v11 slime deserializer")
class v11SlimeWorldDeSerializerTest {

    @Test
    @DisplayName("should deserialize compressed per-chunk entity payloads and merge properties")
    void shouldDeserializeCompressedPerChunkEntityPayloadsAndMergeProperties() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);
        SlimePropertyMap propertyMap = VersionedDeserializerTestSupport.propertyMap(0, 0);
        propertyMap.setValue(SlimeProperties.ALLOW_ANIMALS, false);

        byte[] chunkBytes;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            chunkOut.writeInt(1);
            chunkOut.writeInt(1);
            chunkOut.writeInt(-1);
            chunkOut.writeInt(1);
            chunkOut.writeBoolean(true);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x21));
            chunkOut.writeBoolean(false);
            byte[] blockStates = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.blockStatesTag("minecraft:dirt"));
            chunkOut.writeInt(blockStates.length);
            chunkOut.write(blockStates);
            byte[] biomes = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.biomeTag("minecraft:forest"));
            chunkOut.writeInt(biomes.length);
            chunkOut.write(biomes);
            byte[] heightMaps = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder().putInt("height", 11).build());
            chunkOut.writeInt(heightMaps.length);
            chunkOut.write(heightMaps);

            byte[] tileEntities = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                    .put("tileEntities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            VersionedDeserializerTestSupport.tileEntity("minecraft:beacon", 16, -16)
                    )))
                    .build());
            VersionedDeserializerTestSupport.writeCompressedBlock(chunkOut, tileEntities);

            byte[] entities = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                    .put("entities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            VersionedDeserializerTestSupport.entity("minecraft:cow", 17.0, 64.0, -14.0)
                    )))
                    .build());
            VersionedDeserializerTestSupport.writeCompressedBlock(chunkOut, entities);
            chunkBytes = chunkBuffer.toByteArray();
        }

        SlimePropertyMap override = new SlimePropertyMap();
        override.setValue(SlimeProperties.PVP, false);
        byte[] extra = VersionedDeserializerTestSupport.nbt(
                VersionedDeserializerTestSupport.worldExtra(override, "source", StringBinaryTag.stringBinaryTag("v11"))
        );

        byte[] worldBytes;
        try (ByteArrayOutputStream worldBuffer = new ByteArrayOutputStream();
             DataOutputStream worldOut = new DataOutputStream(worldBuffer)) {
            worldOut.writeInt(3011);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, chunkBytes);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, extra);
            worldBytes = worldBuffer.toByteArray();
        }

        SlimeWorld world = new v11SlimeWorldDeSerializer().deserializeWorld(
                (byte) 11,
                loader,
                "v11-world",
                VersionedDeserializerTestSupport.input(worldBytes),
                propertyMap,
                false
        );

        assertEquals(3011, world.getDataVersion());
        assertEquals("v11", ((StringBinaryTag) world.getExtraData().get("source")).value());
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.PVP));
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.ALLOW_ANIMALS));

        SlimeChunk chunk = world.getChunk(1, -1);
        assertNotNull(chunk);
        assertEquals(1, chunk.getSections().length);
        assertNotNull(chunk.getSections()[0].getBlockStatesTag());
        assertNull(chunk.getSections()[0].getSkyLight());
        assertEquals(1, chunk.getTileEntities().size());
        assertEquals(1, chunk.getEntities().size());
    }
}
