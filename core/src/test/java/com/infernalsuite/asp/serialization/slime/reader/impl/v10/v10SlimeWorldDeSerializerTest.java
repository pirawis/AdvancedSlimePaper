package com.infernalsuite.asp.serialization.slime.reader.impl.v10;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("v10 slime deserializer")
class v10SlimeWorldDeSerializerTest {

    @Test
    @DisplayName("should deserialize chunks, entities, tile entities and extra properties")
    void shouldDeserializeChunksEntitiesTileEntitiesAndExtraProperties() throws Exception {
        SlimeLoader loader = mock(SlimeLoader.class);
        SlimePropertyMap propertyMap = VersionedDeserializerTestSupport.propertyMap(0, 1);
        propertyMap.setValue(SlimeProperties.ALLOW_MONSTERS, false);

        byte[] chunkBytes;
        try (ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
             DataOutputStream chunkOut = new DataOutputStream(chunkBuffer)) {
            chunkOut.writeInt(1);
            chunkOut.writeInt(0);
            chunkOut.writeInt(0);
            byte[] heightMaps = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder().putInt("height", 7).build());
            chunkOut.writeInt(heightMaps.length);
            chunkOut.write(heightMaps);
            chunkOut.writeInt(1);
            chunkOut.writeBoolean(true);
            chunkOut.write(VersionedDeserializerTestSupport.lightBytes((byte) 0x11));
            chunkOut.writeBoolean(false);
            byte[] blockStates = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.blockStatesTag("minecraft:stone"));
            chunkOut.writeInt(blockStates.length);
            chunkOut.write(blockStates);
            byte[] biomes = VersionedDeserializerTestSupport.nbt(VersionedDeserializerTestSupport.biomeTag("minecraft:plains"));
            chunkOut.writeInt(biomes.length);
            chunkOut.write(biomes);
            chunkBytes = chunkBuffer.toByteArray();
        }

        byte[] tileEntities = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                .put("tiles", net.kyori.adventure.nbt.ListBinaryTag.listBinaryTag(
                        net.kyori.adventure.nbt.BinaryTagTypes.COMPOUND,
                        java.util.List.of(VersionedDeserializerTestSupport.tileEntity("minecraft:chest", 1, 1))
                ))
                .build());
        byte[] entities = VersionedDeserializerTestSupport.nbt(CompoundBinaryTag.builder()
                .put("entities", net.kyori.adventure.nbt.ListBinaryTag.listBinaryTag(
                        net.kyori.adventure.nbt.BinaryTagTypes.COMPOUND,
                        java.util.List.of(VersionedDeserializerTestSupport.entity("minecraft:zombie", 1.0, 64.0, 3.0))
                ))
                .build());
        SlimePropertyMap override = new SlimePropertyMap();
        override.setValue(SlimeProperties.PVP, false);
        byte[] extra = VersionedDeserializerTestSupport.nbt(
                VersionedDeserializerTestSupport.worldExtra(override, "note", StringBinaryTag.stringBinaryTag("v10"))
        );

        byte[] worldBytes;
        try (ByteArrayOutputStream worldBuffer = new ByteArrayOutputStream();
             DataOutputStream worldOut = new DataOutputStream(worldBuffer)) {
            worldOut.writeInt(3001);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, chunkBytes);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, tileEntities);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, entities);
            VersionedDeserializerTestSupport.writeCompressedBlock(worldOut, extra);
            worldBytes = worldBuffer.toByteArray();
        }

        SlimeWorld world = new v10SlimeWorldDeSerializer().deserializeWorld(
                (byte) 10,
                loader,
                "v10-world",
                VersionedDeserializerTestSupport.input(worldBytes),
                propertyMap,
                false
        );

        assertEquals("v10-world", world.getName());
        assertSame(loader, world.getLoader());
        assertFalse(world.isReadOnly());
        assertEquals(3001, world.getDataVersion());
        assertEquals("v10", ((StringBinaryTag) world.getExtraData().get("note")).value());
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.PVP));
        assertFalse(world.getPropertyMap().getValue(SlimeProperties.ALLOW_MONSTERS));

        SlimeChunk chunk = world.getChunk(0, 0);
        assertNotNull(chunk);
        assertEquals(2, chunk.getSections().length);
        assertNotNull(chunk.getSections()[0]);
        assertNull(chunk.getSections()[1]);
        assertEquals(1, chunk.getTileEntities().size());
        assertEquals(1, chunk.getEntities().size());
        assertTrue(chunk.getSections()[0].getBlockLight().getBacking()[0] != 0);
        assertNull(chunk.getSections()[0].getSkyLight());
    }
}
