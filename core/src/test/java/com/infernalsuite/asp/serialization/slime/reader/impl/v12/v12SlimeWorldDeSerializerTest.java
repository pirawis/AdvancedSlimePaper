package com.infernalsuite.asp.serialization.slime.reader.impl.v12;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.VersionedDeserializerTestSupport;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("v12 slime deserializer")
class v12SlimeWorldDeSerializerTest {

    @Test
    @DisplayName("should deserialize raw chunk extra tags and empty entity payloads")
    void shouldDeserializeRawChunkExtraTagsAndEmptyEntityPayloads() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);
        SlimePropertyMap propertyMap = VersionedDeserializerTestSupport.propertyMap(0, 0);
        propertyMap.setValue(SlimeProperties.ALLOW_MONSTERS, false);

        byte[] chunkBytes;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            chunkOut.writeInt(1);
            chunkOut.writeInt(-2);
            chunkOut.writeInt(3);
            chunkOut.writeInt(1);
            chunkOut.writeBoolean(false);
            chunkOut.writeBoolean(true);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x22));
            byte[] blockStates = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.blockStatesTag("minecraft:gold_block"));
            chunkOut.writeInt(blockStates.length);
            chunkOut.write(blockStates);
            byte[] biomes = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.biomeTag("minecraft:desert"));
            chunkOut.writeInt(biomes.length);
            chunkOut.write(biomes);
            byte[] heightMaps = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder().putInt("height", 19).build());
            chunkOut.writeInt(heightMaps.length);
            chunkOut.write(heightMaps);
            VersionedDeserializerTestSupport.writeRawBlock(chunkOut, new byte[0]);
            VersionedDeserializerTestSupport.writeRawBlock(chunkOut, new byte[0]);
            VersionedDeserializerTestSupport.writeRawBlock(chunkOut, VersionedDeserializerTestSupport.nbt(
                    CompoundBinaryTag.builder().putString("chunk_source", "v12-extra").build()
            ));
            chunkBytes = chunkBuffer.toByteArray();
        }

        SlimePropertyMap override = new SlimePropertyMap();
        override.setValue(SlimeProperties.PVP, false);
        byte[] extra = VersionedDeserializerTestSupport.nbt(
                VersionedDeserializerTestSupport.worldExtra(override, "source", StringBinaryTag.stringBinaryTag("v12"))
        );

        byte[] worldBytes;
        try (ByteArrayOutputStream worldBuffer = new ByteArrayOutputStream();
             DataOutputStream worldOut = new DataOutputStream(worldBuffer)) {
            worldOut.writeInt(3012);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, chunkBytes);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, extra);
            worldBytes = worldBuffer.toByteArray();
        }

        SlimeWorld world = new v12SlimeWorldDeSerializer().deserializeWorld(
                (byte) 12,
                loader,
                "v12-world",
                VersionedDeserializerTestSupport.input(worldBytes),
                propertyMap,
                false
        );

        assertEquals(3012, world.getDataVersion());
        assertEquals("v12", ((StringBinaryTag) world.getExtraData().get("source")).value());
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.PVP));
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.ALLOW_MONSTERS));

        SlimeChunk chunk = world.getChunk(-2, 3);
        assertNotNull(chunk);
        assertTrue(chunk.getTileEntities().isEmpty());
        assertTrue(chunk.getEntities().isEmpty());
        assertEquals("v12-extra", ((StringBinaryTag) chunk.getExtraData().get("chunk_source")).value());
        assertNotNull(chunk.getSections()[0].getSkyLight());
    }
}
