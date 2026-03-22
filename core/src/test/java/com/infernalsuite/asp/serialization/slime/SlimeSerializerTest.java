package com.infernalsuite.asp.serialization.slime;

import com.github.luben.zstd.Zstd;
import com.infernalsuite.asp.api.utils.NibbleArray;
import com.infernalsuite.asp.api.utils.SlimeFormat;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeChunkSection;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.reader.impl.v13.v13AdditionalWorldData;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SlimeSerializer")
class SlimeSerializerTest {

    @Nested
    @DisplayName("serialize")
    class SerializeTests {

        @Test
        @DisplayName("should serialize world metadata chunks and extra data")
        void shouldSerializeWorldMetadataChunksAndExtraData() throws Exception {
            SlimeWorld world = mock(SlimeWorld.class);
            SlimeChunk chunk = mock(SlimeChunk.class);
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            SlimePropertyMap propertyMap = new SlimePropertyMap();
            propertyMap.setValue(SlimeProperties.CHUNK_PRUNING, "never");
            propertyMap.setValue(SlimeProperties.SAVE_POI, true);
            propertyMap.setValue(SlimeProperties.SAVE_BLOCK_TICKS, true);
            propertyMap.setValue(SlimeProperties.SAVE_FLUID_TICKS, true);

            NibbleArray blockLight = new NibbleArray(8);
            NibbleArray skyLight = new NibbleArray(8);
            blockLight.set(0, 4);
            skyLight.set(0, 9);

            CompoundBinaryTag blockStates = CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build();
            CompoundBinaryTag biomes = CompoundBinaryTag.builder().putString("biome", "minecraft:plains").build();
            CompoundBinaryTag heightMaps = CompoundBinaryTag.builder().putLongArray("MOTION_BLOCKING", new long[]{1L, 2L}).build();
            CompoundBinaryTag poiData = CompoundBinaryTag.builder().putString("poi", "home").build();
            ListBinaryTag blockTicks = ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(CompoundBinaryTag.builder().putString("i", "block").build()));
            ListBinaryTag fluidTicks = ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(CompoundBinaryTag.builder().putString("i", "fluid").build()));
            List<CompoundBinaryTag> tileEntities = List.of(CompoundBinaryTag.builder().putString("id", "chest").build());
            List<CompoundBinaryTag> entities = List.of(CompoundBinaryTag.builder().putString("id", "zombie").build());
            Map<String, BinaryTag> chunkExtraData = new ConcurrentHashMap<>();
            chunkExtraData.put("custom", CompoundBinaryTag.builder().putString("value", "chunk").build());
            ConcurrentHashMap<String, BinaryTag> worldExtraData = new ConcurrentHashMap<>();
            worldExtraData.put("properties", CompoundBinaryTag.builder().putString("stale", "value").build());
            worldExtraData.put("custom", CompoundBinaryTag.builder().putString("value", "world").build());

            when(section.getBlockLight()).thenReturn(blockLight);
            when(section.getSkyLight()).thenReturn(skyLight);
            when(section.getBlockStatesTag()).thenReturn(blockStates);
            when(section.getBiomeTag()).thenReturn(biomes);

            when(chunk.getX()).thenReturn(4);
            when(chunk.getZ()).thenReturn(5);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section, null});
            when(chunk.getHeightMaps()).thenReturn(heightMaps);
            when(chunk.getPoiChunkSections()).thenReturn(poiData);
            when(chunk.getBlockTicks()).thenReturn(blockTicks);
            when(chunk.getFluidTicks()).thenReturn(fluidTicks);
            when(chunk.getTileEntities()).thenReturn(tileEntities);
            when(chunk.getEntities()).thenReturn(entities);
            when(chunk.getExtraData()).thenReturn(chunkExtraData);

            when(world.getName()).thenReturn("arena");
            when(world.getExtraData()).thenReturn(worldExtraData);
            when(world.getPropertyMap()).thenReturn(propertyMap);
            when(world.getDataVersion()).thenReturn(3700);
            when(world.getChunkStorage()).thenReturn(List.of(chunk));

            byte[] serializedWorld = SlimeSerializer.serialize(world);

            assertNotNull(serializedWorld);
            assertTrue(serializedWorld.length > 0);
            assertEquals(propertyMap.toCompound(), worldExtraData.get("properties"));

            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(serializedWorld))) {
                byte[] header = input.readNBytes(SlimeFormat.SLIME_HEADER.length);
                assertArrayEquals(SlimeFormat.SLIME_HEADER, header);
                assertEquals(SlimeFormat.SLIME_VERSION, input.readUnsignedByte());
                assertEquals(3700, input.readInt());
                assertEquals(v13AdditionalWorldData.fromSet(EnumSet.allOf(v13AdditionalWorldData.class)), input.readUnsignedByte());

                byte[] chunkPayload = readCompressedSection(input);
                assertChunkPayload(chunkPayload, blockLight, skyLight, blockStates, biomes, heightMaps, poiData, blockTicks, fluidTicks, tileEntities, entities, chunkExtraData);

                byte[] extraPayload = readCompressedSection(input);
                CompoundBinaryTag deserializedExtra = BinaryTagIO.reader().read(new ByteArrayInputStream(extraPayload));
                assertEquals("world", deserializedExtra.getCompound("custom").getString("value"));
                assertEquals(propertyMap.toCompound(), deserializedExtra.getCompound("properties"));
            }
        }

        @Test
        @DisplayName("should skip chunks pruned by save bounds")
        void shouldSkipChunksPrunedBySaveBounds() throws Exception {
            SlimeWorld world = mock(SlimeWorld.class);
            SlimeChunk keptChunk = mock(SlimeChunk.class);
            SlimeChunk prunedChunk = mock(SlimeChunk.class);
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            SlimePropertyMap propertyMap = new SlimePropertyMap();
            propertyMap.setValue(SlimeProperties.SHOULD_LIMIT_SAVE, true);
            propertyMap.setValue(SlimeProperties.SAVE_MIN_X, 0);
            propertyMap.setValue(SlimeProperties.SAVE_MAX_X, 0);
            propertyMap.setValue(SlimeProperties.SAVE_MIN_Z, 0);
            propertyMap.setValue(SlimeProperties.SAVE_MAX_Z, 0);
            propertyMap.setValue(SlimeProperties.CHUNK_PRUNING, "never");

            when(section.getBlockLight()).thenReturn(null);
            when(section.getSkyLight()).thenReturn(null);
            when(section.getBlockStatesTag()).thenReturn(CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build());
            when(section.getBiomeTag()).thenReturn(CompoundBinaryTag.builder().putString("biome", "minecraft:plains").build());

            when(keptChunk.getX()).thenReturn(0);
            when(keptChunk.getZ()).thenReturn(0);
            when(keptChunk.getSections()).thenReturn(new SlimeChunkSection[]{section});
            when(keptChunk.getHeightMaps()).thenReturn(CompoundBinaryTag.empty());
            when(keptChunk.getTileEntities()).thenReturn(List.of());
            when(keptChunk.getEntities()).thenReturn(List.of());
            when(keptChunk.getExtraData()).thenReturn(new ConcurrentHashMap<>());
            when(keptChunk.getPoiChunkSections()).thenReturn(null);
            when(keptChunk.getBlockTicks()).thenReturn(null);
            when(keptChunk.getFluidTicks()).thenReturn(null);

            when(prunedChunk.getX()).thenReturn(5);
            when(prunedChunk.getZ()).thenReturn(5);

            when(world.getName()).thenReturn("arena");
            when(world.getPropertyMap()).thenReturn(propertyMap);

            byte[] chunkData = SlimeSerializer.serializeChunks(world, List.of(keptChunk, prunedChunk), java.util.EnumSet.noneOf(v13AdditionalWorldData.class));

            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(chunkData))) {
                assertEquals(1, input.readInt());
                assertEquals(0, input.readInt());
                assertEquals(0, input.readInt());
                assertEquals(1, input.readInt());
                assertEquals(0, input.readUnsignedByte());
            }
        }
    }

    @Nested
    @DisplayName("serializeCompoundTag")
    class SerializeCompoundTagTests {

        @Test
        @DisplayName("should return empty array for null tag")
        void shouldReturnEmptyArrayForNullTag() throws IOException {
            byte[] result = SlimeSerializer.serializeCompoundTag(null);
            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should return empty array for empty tag")
        void shouldReturnEmptyArrayForEmptyTag() throws IOException {
            CompoundBinaryTag emptyTag = CompoundBinaryTag.empty();
            byte[] result = SlimeSerializer.serializeCompoundTag(emptyTag);
            assertEquals(0, result.length);
        }

        @Test
        @DisplayName("should serialize simple compound tag")
        void shouldSerializeSimpleCompoundTag() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putInt("testInt", 42)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);
        }

        @Test
        @DisplayName("should serialize tag with string value")
        void shouldSerializeTagWithStringValue() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putString("name", "testWorld")
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);
        }

        @Test
        @DisplayName("should produce deserializable output")
        void shouldProduceDeserializableOutput() throws IOException {
            CompoundBinaryTag original = CompoundBinaryTag.builder()
                    .putInt("value", 123)
                    .putString("key", "test")
                    .build();

            byte[] serialized = SlimeSerializer.serializeCompoundTag(original);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(serialized));

            assertEquals(123, deserialized.getInt("value"));
            assertEquals("test", deserialized.getString("key"));
        }

        @Test
        @DisplayName("should serialize nested compound tag")
        void shouldSerializeNestedCompoundTag() throws IOException {
            CompoundBinaryTag innerTag = CompoundBinaryTag.builder()
                    .putInt("innerValue", 99)
                    .build();

            CompoundBinaryTag outerTag = CompoundBinaryTag.builder()
                    .put("nested", innerTag)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(outerTag);

            assertNotNull(result);
            assertTrue(result.length > 0);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertEquals(99, deserialized.getCompound("nested").getInt("innerValue"));
        }

        @Test
        @DisplayName("should serialize tag with array values")
        void shouldSerializeTagWithArrayValues() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putIntArray("intArray", new int[]{1, 2, 3, 4, 5})
                    .putByteArray("byteArray", new byte[]{10, 20, 30})
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, deserialized.getIntArray("intArray"));
            assertArrayEquals(new byte[]{10, 20, 30}, deserialized.getByteArray("byteArray"));
        }

        @Test
        @DisplayName("should serialize tag with multiple fields")
        void shouldSerializeTagWithMultipleFields() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putInt("int", 1)
                    .putLong("long", 2L)
                    .putFloat("float", 3.0f)
                    .putDouble("double", 4.0)
                    .putString("string", "five")
                    .putByte("byte", (byte) 6)
                    .putShort("short", (short) 7)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            assertNotNull(result);
            assertTrue(result.length > 0);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertEquals(1, deserialized.getInt("int"));
            assertEquals(2L, deserialized.getLong("long"));
            assertEquals(3.0f, deserialized.getFloat("float"), 0.001f);
            assertEquals(4.0, deserialized.getDouble("double"), 0.001);
            assertEquals("five", deserialized.getString("string"));
            assertEquals((byte) 6, deserialized.getByte("byte"));
            assertEquals((short) 7, deserialized.getShort("short"));
        }

        @Test
        @DisplayName("should handle tag with boolean as byte")
        void shouldHandleTagWithBooleanAsByte() throws IOException {
            CompoundBinaryTag tag = CompoundBinaryTag.builder()
                    .putBoolean("enabled", true)
                    .putBoolean("disabled", false)
                    .build();

            byte[] result = SlimeSerializer.serializeCompoundTag(tag);

            CompoundBinaryTag deserialized = BinaryTagIO.reader().read(new ByteArrayInputStream(result));
            assertTrue(deserialized.getBoolean("enabled"));
            assertFalse(deserialized.getBoolean("disabled"));
        }
    }

    private static byte[] readCompressedSection(final DataInputStream input) throws IOException {
        int compressedLength = input.readInt();
        int originalLength = input.readInt();
        byte[] compressed = input.readNBytes(compressedLength);
        return Zstd.decompress(compressed, originalLength);
    }

    private static void assertChunkPayload(
            final byte[] payload,
            final NibbleArray blockLight,
            final NibbleArray skyLight,
            final CompoundBinaryTag blockStates,
            final CompoundBinaryTag biomes,
            final CompoundBinaryTag heightMaps,
            final CompoundBinaryTag poiData,
            final ListBinaryTag blockTicks,
            final ListBinaryTag fluidTicks,
            final List<CompoundBinaryTag> tileEntities,
            final List<CompoundBinaryTag> entities,
            final Map<String, BinaryTag> chunkExtraData
    ) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload))) {
            assertEquals(1, input.readInt());
            assertEquals(4, input.readInt());
            assertEquals(5, input.readInt());
            assertEquals(1, input.readInt());
            assertEquals(3, input.readUnsignedByte());
            assertArrayEquals(blockLight.getBacking(), input.readNBytes(blockLight.getBacking().length));
            assertArrayEquals(skyLight.getBacking(), input.readNBytes(skyLight.getBacking().length));
            assertEquals(blockStates, readCompound(input));
            assertEquals(biomes, readCompound(input));
            assertEquals(heightMaps, readCompound(input));
            assertEquals(poiData, readCompound(input));
            assertEquals(CompoundBinaryTag.builder().put("block_ticks", blockTicks).build(), readCompound(input));
            assertEquals(CompoundBinaryTag.builder().put("fluid_ticks", fluidTicks).build(), readCompound(input));
            assertEquals(ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, tileEntities.stream().map(tag -> (BinaryTag) tag).toList()), readCompound(input).getList("tileEntities", BinaryTagTypes.COMPOUND));
            assertEquals(ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, entities.stream().map(tag -> (BinaryTag) tag).toList()), readCompound(input).getList("entities", BinaryTagTypes.COMPOUND));
            assertEquals(CompoundBinaryTag.from(chunkExtraData), readCompound(input));
        }
    }

    private static CompoundBinaryTag readCompound(final DataInputStream input) throws IOException {
        int length = input.readInt();
        byte[] payload = input.readNBytes(length);
        return length == 0 ? CompoundBinaryTag.empty() : BinaryTagIO.reader().read(new ByteArrayInputStream(payload));
    }
}
