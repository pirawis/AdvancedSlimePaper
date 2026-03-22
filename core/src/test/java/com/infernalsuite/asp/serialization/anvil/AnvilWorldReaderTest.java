package com.infernalsuite.asp.serialization.anvil;

import com.infernalsuite.asp.Util;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeChunkSection;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.utils.NibbleArray;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AnvilWorldReader")
@ExtendWith(MockitoExtension.class)
class AnvilWorldReaderTest {

    @TempDir
    Path tempDir;

    @Mock
    private SlimeLoader mockLoader;

    @Nested
    @DisplayName("INSTANCE")
    class InstanceTests {

        @Test
        @DisplayName("should have singleton instance")
        void shouldHaveSingletonInstance() {
            assertNotNull(AnvilWorldReader.INSTANCE);
        }

        @Test
        @DisplayName("should return same instance")
        void shouldReturnSameInstance() {
            assertSame(AnvilWorldReader.INSTANCE, AnvilWorldReader.INSTANCE);
        }
    }

    @Nested
    @DisplayName("readFromData")
    class ReadFromDataTests {

        @Test
        @DisplayName("should throw when level.dat is missing")
        void shouldThrowWhenLevelDatIsMissing() throws IOException {
            Path worldDir = tempDir.resolve("emptyworld");
            Files.createDirectories(worldDir);

            AnvilImportData importData = new AnvilImportData(worldDir, "testworld", mockLoader);

            assertThrows(RuntimeException.class, () ->
                AnvilWorldReader.INSTANCE.readFromData(importData)
            );
        }

        @Test
        @DisplayName("should throw when world directory does not exist")
        void shouldThrowWhenWorldDirectoryDoesNotExist() {
            Path nonExistentDir = tempDir.resolve("nonexistent");

            AnvilImportData importData = new AnvilImportData(nonExistentDir, "testworld", mockLoader);

            assertThrows(RuntimeException.class, () ->
                AnvilWorldReader.INSTANCE.readFromData(importData)
            );
        }

        @Test
        @DisplayName("should throw when level.dat is a directory")
        void shouldThrowWhenLevelDatIsDirectory() throws IOException {
            Path worldDir = tempDir.resolve("badworld");
            Files.createDirectories(worldDir);
            Files.createDirectories(worldDir.resolve("level.dat"));

            AnvilImportData importData = new AnvilImportData(worldDir, "testworld", mockLoader);

            assertThrows(RuntimeException.class, () ->
                AnvilWorldReader.INSTANCE.readFromData(importData)
            );
        }

        @Test
        @DisplayName("should throw when level dat has no Data compound")
        void shouldThrowWhenLevelDatHasNoDataCompound() throws Exception {
            Path worldDir = tempDir.resolve("broken-level");
            Files.createDirectories(worldDir);
            writeLevelDat(worldDir.resolve("level.dat"), CompoundBinaryTag.builder().build());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    AnvilWorldReader.INSTANCE.readFromData(new AnvilImportData(worldDir, "testworld", mockLoader))
            );

            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("private helpers")
    class PrivateHelperTests {

        @Test
        @DisplayName("should read level data from a valid level dat file")
        void shouldReadLevelDataFromAValidLevelDatFile() throws Exception {
            Path levelDat = tempDir.resolve("level.dat");
            writeLevelDat(levelDat, CompoundBinaryTag.builder()
                    .put("Data", CompoundBinaryTag.builder()
                            .putInt("DataVersion", 3700)
                            .putInt("SpawnX", 10)
                            .putInt("SpawnY", 64)
                            .putInt("SpawnZ", -5)
                            .build())
                    .build());

            Object levelData = invokeStatic("readLevelData", new Class<?>[]{Path.class}, levelDat);

            assertEquals(3700, invokeRecordAccessor(levelData, "version"));
            assertEquals(10, invokeRecordAccessor(levelData, "x"));
            assertEquals(64, invokeRecordAccessor(levelData, "y"));
            assertEquals(-5, invokeRecordAccessor(levelData, "z"));
        }

        @Test
        @DisplayName("should build a slime chunk from a fully populated chunk tag")
        void shouldBuildASlimeChunkFromAFullyPopulatedChunkTag() throws Exception {
            CompoundBinaryTag chunkTag = CompoundBinaryTag.builder()
                    .putInt("xPos", 2)
                    .putInt("zPos", 3)
                    .putInt("DataVersion", 3700)
                    .putString("Status", "minecraft:full")
                    .put("Heightmaps", CompoundBinaryTag.builder().putLongArray("MOTION_BLOCKING", new long[]{1L}).build())
                    .put("block_entities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(CompoundBinaryTag.builder().putString("id", "chest").build())))
                    .put("entities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(CompoundBinaryTag.builder().putString("id", "zombie").build())))
                    .put("sections", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            CompoundBinaryTag.builder()
                                    .putByte("Y", (byte) 0)
                                    .put("block_states", CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build())
                                    .put("biomes", CompoundBinaryTag.builder().putString("biome", "minecraft:plains").build())
                                    .putByteArray("BlockLight", new byte[]{0x21})
                                    .putByteArray("SkyLight", new byte[]{0x43})
                                    .build()
                    )))
                    .put("ChunkBukkitValues", CompoundBinaryTag.builder().putString("key", "value").build())
                    .build();

            SlimeChunk chunk = (SlimeChunk) invokeStatic("readChunk", new Class<?>[]{CompoundBinaryTag.class, int.class}, chunkTag, 3700);

            assertNotNull(chunk);
            assertEquals(2, chunk.getX());
            assertEquals(3, chunk.getZ());
            assertEquals(1, chunk.getTileEntities().size());
            assertEquals(1, chunk.getEntities().size());
            assertTrue(chunk.getExtraData().containsKey("ChunkBukkitValues"));
            assertNotNull(chunk.getSections()[0]);
            assertEquals(1, chunk.getSections()[0].getBlockLight().getBacking().length);
            assertEquals(1, chunk.getSections()[0].getSkyLight().getBacking().length);
        }

        @Test
        @DisplayName("should skip chunks with mismatched version or proto status")
        void shouldSkipChunksWithMismatchedVersionOrProtoStatus() throws Exception {
            CompoundBinaryTag mismatchedVersion = CompoundBinaryTag.builder()
                    .putInt("xPos", 1)
                    .putInt("zPos", 1)
                    .putInt("DataVersion", 3699)
                    .put("sections", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of()))
                    .build();
            CompoundBinaryTag protoChunk = CompoundBinaryTag.builder()
                    .putInt("xPos", 1)
                    .putInt("zPos", 1)
                    .putInt("DataVersion", 3700)
                    .putString("Status", "minecraft:carvers")
                    .put("sections", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of()))
                    .build();

            assertNull(invokeStatic("readChunk", new Class<?>[]{CompoundBinaryTag.class, int.class}, mismatchedVersion, 3700));
            assertNull(invokeStatic("readChunk", new Class<?>[]{CompoundBinaryTag.class, int.class}, protoChunk, 3700));
        }

        @Test
        @DisplayName("should merge entity chunks into an existing chunk")
        void shouldMergeEntityChunksIntoAnExistingChunk() throws Exception {
            SlimeChunk existingChunk = mock(SlimeChunk.class);
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            Map<String, net.kyori.adventure.nbt.BinaryTag> extraData = new HashMap<>();

            when(existingChunk.getX()).thenReturn(4);
            when(existingChunk.getZ()).thenReturn(5);
            when(existingChunk.getSections()).thenReturn(new SlimeChunkSection[]{section});
            when(existingChunk.getHeightMaps()).thenReturn(CompoundBinaryTag.empty());
            when(existingChunk.getTileEntities()).thenReturn(List.of());
            when(existingChunk.getEntities()).thenReturn(List.of(CompoundBinaryTag.builder().putString("id", "cow").build()));
            when(existingChunk.getExtraData()).thenReturn(extraData);
            when(existingChunk.getUpgradeData()).thenReturn(null);
            when(existingChunk.getPoiChunkSections()).thenReturn(null);
            when(existingChunk.getBlockTicks()).thenReturn(null);
            when(existingChunk.getFluidTicks()).thenReturn(null);

            Long2ObjectMap<SlimeChunk> chunkMap = new Long2ObjectOpenHashMap<>();
            chunkMap.put(Util.chunkPosition(4, 5), existingChunk);

            CompoundBinaryTag entityChunk = CompoundBinaryTag.builder()
                    .putIntArray("Position", new int[]{4, 5})
                    .putInt("DataVersion", 3700)
                    .put("Entities", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                            CompoundBinaryTag.builder().putString("id", "zombie").build()
                    )))
                    .build();

            invokeStatic("readEntityChunk", new Class<?>[]{CompoundBinaryTag.class, int.class, Long2ObjectMap.class}, entityChunk, 3700, chunkMap);

            SlimeChunk mergedChunk = chunkMap.get(Util.chunkPosition(4, 5));
            assertNotNull(mergedChunk);
            assertEquals(2, mergedChunk.getEntities().size());
        }

        @Test
        @DisplayName("should return null when byte array tag is missing and transformed value when present")
        void shouldReturnNullWhenByteArrayTagIsMissingAndTransformedValueWhenPresent() throws Exception {
            CompoundBinaryTag emptyTag = CompoundBinaryTag.empty();
            CompoundBinaryTag valueTag = CompoundBinaryTag.builder().putByteArray("BlockLight", new byte[]{0x12}).build();

            assertNull(invokeStatic("applyByteArrayOrNull", new Class<?>[]{CompoundBinaryTag.class, String.class, java.util.function.Function.class}, emptyTag, "BlockLight", (java.util.function.Function<byte[], NibbleArray>) NibbleArray::new));

            NibbleArray result = (NibbleArray) invokeStatic(
                    "applyByteArrayOrNull",
                    new Class<?>[]{CompoundBinaryTag.class, String.class, java.util.function.Function.class},
                    valueTag,
                    "BlockLight",
                    (java.util.function.Function<byte[], NibbleArray>) NibbleArray::new
            );
            assertNotNull(result);
            assertArrayEquals(new byte[]{0x12}, result.getBacking());
        }
    }

    @Nested
    @DisplayName("AnvilImportData")
    class AnvilImportDataTests {

        @Test
        @DisplayName("should store world directory")
        void shouldStoreWorldDirectory() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", mockLoader);

            assertEquals(worldDir, data.worldDir());
        }

        @Test
        @DisplayName("should store new name")
        void shouldStoreNewName() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", mockLoader);

            assertEquals("newname", data.newName());
        }

        @Test
        @DisplayName("should store loader")
        void shouldStoreLoader() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", mockLoader);

            assertSame(mockLoader, data.loader());
        }

        @Test
        @DisplayName("should allow null loader")
        void shouldAllowNullLoader() {
            Path worldDir = tempDir.resolve("testworld");
            AnvilImportData data = new AnvilImportData(worldDir, "newname", null);

            assertNull(data.loader());
        }
    }

    private static Object invokeStatic(final String name, final Class<?>[] parameterTypes, final Object... args) throws Exception {
        Method method = AnvilWorldReader.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    private static Object invokeRecordAccessor(final Object target, final String accessor) throws Exception {
        Method method = target.getClass().getDeclaredMethod(accessor);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static void writeLevelDat(final Path path, final CompoundBinaryTag rootTag) throws IOException {
        Files.createDirectories(path.getParent());
        try (var out = new GZIPOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            net.kyori.adventure.nbt.BinaryTagIO.writer().write(rootTag, out);
        }
    }
}
