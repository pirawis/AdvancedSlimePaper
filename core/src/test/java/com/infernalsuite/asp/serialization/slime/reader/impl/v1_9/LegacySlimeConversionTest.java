package com.infernalsuite.asp.serialization.slime.reader.impl.v1_9;

import com.infernalsuite.asp.Util;
import com.infernalsuite.asp.api.SlimeDataConverter;
import com.infernalsuite.asp.api.SlimeNMSBridge;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.utils.NibbleArray;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeChunkSection;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.reader.impl.v1_9.upgrade.v1_13WorldUpgrade;
import com.infernalsuite.asp.serialization.slime.reader.impl.v1_9.upgrade.v1_16WorldUpgrade;
import com.infernalsuite.asp.serialization.slime.reader.impl.v1_9.upgrade.v1_18WorldUpgrade;
import com.infernalsuite.asp.skeleton.SkeletonSlimeWorld;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Legacy slime conversion")
class LegacySlimeConversionTest {

    @Mock
    private SlimeLoader loader;

    @Mock
    private SlimeDataConverter converter;

    @Mock
    private SlimeNMSBridge bridge;

    @Test
    @DisplayName("v1_v9SlimeConverter should convert legacy chunks into a skeleton world")
    void v19ConverterShouldConvertLegacyChunksIntoASkeletonWorld() {
        v1_9SlimeChunkSection emptyTagSection = new v1_9SlimeChunkSection(
                null,
                null,
                null,
                null,
                CompoundBinaryTag.empty(),
                CompoundBinaryTag.builder().putString("biome", "empty").build(),
                new NibbleArray(16),
                null
        );
        CompoundBinaryTag blockStatesTag = CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build();
        CompoundBinaryTag biomeTag = CompoundBinaryTag.builder().putString("biome", "forest").build();
        NibbleArray blockLight = new NibbleArray(16);
        NibbleArray skyLight = new NibbleArray(16);
        v1_9SlimeChunkSection dataSection = new v1_9SlimeChunkSection(
                null,
                null,
                null,
                null,
                blockStatesTag,
                biomeTag,
                blockLight,
                skyLight
        );

        List<CompoundBinaryTag> tileEntities = List.of(CompoundBinaryTag.builder().putString("id", "chest").build());
        List<CompoundBinaryTag> entities = List.of(CompoundBinaryTag.builder().putString("id", "zombie").build());
        v1_9SlimeChunk chunk = new v1_9SlimeChunk(
                "arena",
                4,
                -2,
                new v1_9SlimeChunkSection[]{emptyTagSection, dataSection, null},
                -4,
                19,
                CompoundBinaryTag.builder().putInt("MOTION_BLOCKING", 1).build(),
                null,
                tileEntities,
                entities
        );
        chunk.upgradeData = CompoundBinaryTag.builder().putInt("Up", 1).build();

        Long2ObjectOpenHashMap<v1_9SlimeChunk> chunks = new Long2ObjectOpenHashMap<>();
        chunks.put(Util.chunkPosition(4, -2), chunk);
        ConcurrentHashMap<String, BinaryTag> extraCompound = new ConcurrentHashMap<>();
        extraCompound.put("custom", CompoundBinaryTag.builder().putString("value", "world").build());
        SlimePropertyMap propertyMap = new SlimePropertyMap();
        v1_9SlimeWorld world = new v1_9SlimeWorld((byte) 0x08, "arena", loader, chunks, extraCompound, propertyMap, true);

        SlimeWorld converted = new v1_v9SlimeConverter().readFromData(world);

        SkeletonSlimeWorld skeleton = assertInstanceOf(SkeletonSlimeWorld.class, converted);
        assertEquals("arena", skeleton.getName());
        assertSame(loader, skeleton.getLoader());
        assertTrue(skeleton.isReadOnly());
        assertEquals(2975, skeleton.getDataVersion());
        assertSame(extraCompound, skeleton.getExtraData());

        SlimeChunk convertedChunk = skeleton.getChunk(4, -2);
        assertNotNull(convertedChunk);
        assertSame(tileEntities, convertedChunk.getTileEntities());
        assertSame(entities, convertedChunk.getEntities());
        assertSame(chunk.upgradeData, convertedChunk.getUpgradeData());

        SlimeChunkSection[] convertedSections = convertedChunk.getSections();
        assertEquals(3, convertedSections.length);
        assertNull(convertedSections[0].getBlockStatesTag());
        assertSame(biomeTag, convertedSections[1].getBiomeTag());
        assertSame(blockStatesTag, convertedSections[1].getBlockStatesTag());
        assertSame(blockLight, convertedSections[1].getBlockLight());
        assertSame(skyLight, convertedSections[1].getSkyLight());
        assertNull(convertedSections[2].getBlockStatesTag());
        assertNull(convertedSections[2].getBiomeTag());
    }

    @Test
    @DisplayName("v1_v9SlimeConverter should apply registered upgrades in order and skip missing ones")
    void v19ConverterShouldApplyRegisteredUpgradesInOrderAndSkipMissingOnes() {
        Upgrade upgrade13 = mock(Upgrade.class);
        Upgrade upgrade18 = mock(Upgrade.class);
        Upgrade original13 = v1_v9SlimeConverter.UPGRADES.put((byte) 0x04, upgrade13);
        Upgrade original16 = v1_v9SlimeConverter.UPGRADES.remove((byte) 0x06);
        Upgrade original18 = v1_v9SlimeConverter.UPGRADES.put((byte) 0x08, upgrade18);

        v1_9SlimeWorld world = new v1_9SlimeWorld(
                (byte) 0x03,
                "arena",
                loader,
                new Long2ObjectOpenHashMap<>(),
                new ConcurrentHashMap<>(),
                new SlimePropertyMap(),
                false
        );

        try (MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
            bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
            when(bridge.getSlimeDataConverter()).thenReturn(converter);

            int dataVersion = v1_v9SlimeConverter.upgradeWorld(world);

            InOrder order = inOrder(upgrade13, upgrade18);
            order.verify(upgrade13).upgrade(world, converter);
            order.verify(upgrade18).upgrade(world, converter);
            assertEquals((byte) 0x08, world.version);
            assertEquals(2975, dataVersion);
        } finally {
            if (original13 == null) {
                v1_v9SlimeConverter.UPGRADES.remove((byte) 0x04);
            } else {
                v1_v9SlimeConverter.UPGRADES.put((byte) 0x04, original13);
            }
            if (original16 != null) {
                v1_v9SlimeConverter.UPGRADES.put((byte) 0x06, original16);
            }
            if (original18 == null) {
                v1_v9SlimeConverter.UPGRADES.remove((byte) 0x08);
            } else {
                v1_v9SlimeConverter.UPGRADES.put((byte) 0x08, original18);
            }
        }
    }

    @Test
    @DisplayName("v1_18WorldUpgrade should create default biome sections when legacy biomes are missing")
    void v118UpgradeShouldCreateDefaultBiomeSectionsWhenLegacyBiomesAreMissing() throws Exception {
        CompoundBinaryTag[] sections = invokeCreateBiomeSections(null, false, 0);

        assertEquals(16, sections.length);
        assertEquals(Set.of("minecraft:plains"), paletteValues(sections[0].getList("palette", BinaryTagTypes.STRING)));
        assertEquals(0, sections[0].getLongArray("data").length);
    }

    @Test
    @DisplayName("v1_18WorldUpgrade should create packed extended biome sections")
    void v118UpgradeShouldCreatePackedExtendedBiomeSections() throws Exception {
        int[] biomes = new int[1536];
        for (int i = 0; i < 64; i++) {
            biomes[i] = i % 2 == 0 ? 39 : 1;
        }
        for (int i = 64; i < biomes.length; i++) {
            biomes[i] = 39;
        }

        CompoundBinaryTag[] sections = invokeCreateBiomeSections(biomes, true, 0);

        assertEquals(24, sections.length);
        Set<String> palette = paletteValues(sections[0].getList("palette", BinaryTagTypes.STRING));
        assertTrue(palette.contains("minecraft:badlands"));
        assertTrue(palette.contains("minecraft:plains"));
        assertTrue(sections[0].getLongArray("data").length > 0);
        assertEquals(0, v1_18WorldUpgrade.ceilLog2(0));
        assertEquals(2, v1_18WorldUpgrade.ceilLog2(3));
    }

    @Test
    @DisplayName("v1_13WorldUpgrade should convert chunk data into palette-based sections")
    void v113UpgradeShouldConvertChunkDataIntoPaletteBasedSections() {
        NibbleArray data = new NibbleArray(16);
        NibbleArray blockLight = new NibbleArray(new byte[]{1, 2});
        NibbleArray skyLight = new NibbleArray(new byte[]{3, 4});
        v1_9SlimeChunkSection section = new v1_9SlimeChunkSection(
                new byte[]{9, 8},
                data,
                null,
                null,
                null,
                null,
                blockLight,
                skyLight
        );

        List<CompoundBinaryTag> oldTiles = List.of(CompoundBinaryTag.builder().putString("id", "old_tile").build());
        List<CompoundBinaryTag> oldEntities = List.of(CompoundBinaryTag.builder().putString("id", "old_entity").build());
        List<CompoundBinaryTag> convertedTiles = List.of(CompoundBinaryTag.builder().putString("id", "new_tile").build());
        List<CompoundBinaryTag> convertedEntities = List.of(CompoundBinaryTag.builder().putString("id", "new_entity").build());
        ListBinaryTag convertedPalette = blockPalette("minecraft:stone");
        long[] convertedStates = new long[]{42L};
        CompoundBinaryTag upgradeData = CompoundBinaryTag.builder().putString("Status", "upgraded").build();

        v1_9SlimeChunk chunk = new v1_9SlimeChunk(
                "arena",
                7,
                -3,
                new v1_9SlimeChunkSection[]{section},
                0,
                15,
                CompoundBinaryTag.empty(),
                new int[]{1, -1, 255},
                oldTiles,
                oldEntities
        );

        Long2ObjectOpenHashMap<v1_9SlimeChunk> chunks = new Long2ObjectOpenHashMap<>();
        chunks.put(Util.chunkPosition(7, -3), chunk);
        v1_9SlimeWorld world = new v1_9SlimeWorld((byte) 0x03, "arena", loader, chunks, new ConcurrentHashMap<>(), new SlimePropertyMap(), false);

        when(converter.convertTileEntities(eq(oldTiles), eq(922), eq(1343))).thenReturn(convertedTiles);
        when(converter.convertEntities(eq(oldEntities), eq(922), eq(1343))).thenReturn(convertedEntities);
        when(converter.convertChunkTo1_13(any())).thenAnswer(invocation -> {
            CompoundBinaryTag globalTag = invocation.getArgument(0);
            assertEquals(1343, globalTag.getInt("DataVersion"));

            CompoundBinaryTag level = globalTag.getCompound("Level");
            assertEquals(7, level.getInt("xPos"));
            assertEquals(-3, level.getInt("zPos"));
            ListBinaryTag serializedSections = level.getList("Sections", BinaryTagTypes.COMPOUND);
            assertEquals(1, serializedSections.size());
            CompoundBinaryTag serializedSection = serializedSections.getCompound(0);
            assertEquals(0, serializedSection.getInt("Y"));
            assertArrayEquals(new byte[]{9, 8}, serializedSection.getByteArray("Blocks"));
            assertArrayEquals(data.getBacking(), serializedSection.getByteArray("Data"));
            assertArrayEquals(blockLight.getBacking(), serializedSection.getByteArray("BlockLight"));
            assertArrayEquals(skyLight.getBacking(), serializedSection.getByteArray("SkyLight"));

            CompoundBinaryTag convertedSection = CompoundBinaryTag.builder()
                    .put("Y", IntBinaryTag.intBinaryTag(5))
                    .put("Palette", convertedPalette)
                    .put("BlockStates", net.kyori.adventure.nbt.LongArrayBinaryTag.longArrayBinaryTag(convertedStates))
                    .put("BlockLight", ByteArrayBinaryTag.byteArrayBinaryTag(new byte[]{11, 12}))
                    .put("SkyLight", ByteArrayBinaryTag.byteArrayBinaryTag(new byte[]{13, 14}))
                    .build();

            return CompoundBinaryTag.builder()
                    .put("Level", CompoundBinaryTag.builder()
                            .put("Sections", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(convertedSection)))
                            .put("UpgradeData", upgradeData)
                            .build())
                    .build();
        });

        new v1_13WorldUpgrade().upgrade(world, converter);

        assertSame(convertedTiles, chunk.tileEntities);
        assertSame(convertedEntities, chunk.entities);
        assertEquals(16, chunk.sections.length);
        assertNull(chunk.sections[0]);
        assertSame(convertedPalette, chunk.sections[5].palette);
        assertArrayEquals(convertedStates, chunk.sections[5].blockStates);
        assertArrayEquals(new byte[]{11, 12}, chunk.sections[5].blockLight.getBacking());
        assertArrayEquals(new byte[]{13, 14}, chunk.sections[5].skyLight.getBacking());
        assertEquals(256, chunk.biomes.length);
        assertEquals(1, chunk.biomes[0]);
        assertEquals(255, chunk.biomes[1]);
        assertEquals(255, chunk.biomes[2]);
        assertSame(upgradeData, chunk.upgradeData);
    }

    @Test
    @DisplayName("v1_16WorldUpgrade should repack non power of two sections and widen biome arrays")
    void v116UpgradeShouldRepackNonPowerOfTwoSectionsAndWidenBiomeArrays() {
        List<CompoundBinaryTag> oldTiles = List.of(CompoundBinaryTag.builder().putString("id", "old_tile").build());
        List<CompoundBinaryTag> oldEntities = List.of(CompoundBinaryTag.builder().putString("id", "old_entity").build());
        List<CompoundBinaryTag> convertedTiles = List.of(CompoundBinaryTag.builder().putString("id", "new_tile").build());
        List<CompoundBinaryTag> convertedEntities = List.of(CompoundBinaryTag.builder().putString("id", "new_entity").build());

        String[] names = new String[17];
        for (int i = 0; i < names.length; i++) {
            names[i] = "minecraft:block_" + i;
        }
        ListBinaryTag originalPalette = blockPalette(names);
        long[] originalStates = new long[]{1L, 2L, 3L};
        v1_9SlimeChunkSection originalSection = new v1_9SlimeChunkSection(
                null,
                null,
                originalPalette,
                originalStates,
                null,
                null,
                new NibbleArray(16),
                new NibbleArray(16)
        );

        CompoundBinaryTag heightMap = CompoundBinaryTag.builder()
                .putLongArray("MOTION_BLOCKING", new long[]{15L})
                .putString("Status", "kept")
                .build();
        v1_9SlimeChunk chunk = new v1_9SlimeChunk(
                "arena",
                2,
                3,
                new v1_9SlimeChunkSection[]{originalSection},
                0,
                15,
                heightMap,
                new int[]{7, 8, 9},
                oldTiles,
                oldEntities
        );

        Long2ObjectOpenHashMap<v1_9SlimeChunk> chunks = new Long2ObjectOpenHashMap<>();
        chunks.put(Util.chunkPosition(2, 3), chunk);
        v1_9SlimeWorld world = new v1_9SlimeWorld((byte) 0x05, "arena", loader, chunks, new ConcurrentHashMap<>(), new SlimePropertyMap(), false);

        when(converter.convertTileEntities(eq(oldTiles), eq(1976), eq(2586))).thenReturn(convertedTiles);
        when(converter.convertEntities(eq(oldEntities), eq(1976), eq(2586))).thenReturn(convertedEntities);
        when(converter.convertBlockPalette(eq(originalPalette), eq(1976), eq(2586))).thenReturn(originalPalette);

        new v1_16WorldUpgrade().upgrade(world, converter);

        assertSame(convertedTiles, chunk.tileEntities);
        assertSame(convertedEntities, chunk.entities);
        assertNotSame(originalSection, chunk.sections[0]);
        assertSame(originalPalette, chunk.sections[0].palette);
        assertTrue(chunk.sections[0].blockStates.length > originalStates.length);
        assertEquals(1024, chunk.biomes.length);
        assertEquals(7, chunk.biomes[0]);
        assertEquals(8, chunk.biomes[1]);
        assertEquals(9, chunk.biomes[2]);
        assertEquals(-1, chunk.biomes[3]);
        assertEquals("kept", chunk.heightMap.getString("Status"));
        assertArrayEquals(new long[]{15L}, chunk.heightMap.getLongArray("MOTION_BLOCKING"));
    }

    @Test
    @DisplayName("v1_16WorldUpgrade helpers should pad arrays and round bit sizes")
    void v116UpgradeHelpersShouldPadArraysAndRoundBitSizes() throws Exception {
        Method addPadding = v1_16WorldUpgrade.class.getDeclaredMethod("addPadding", int.class, int.class, long[].class);
        addPadding.setAccessible(true);
        Method ceilLog2 = v1_16WorldUpgrade.class.getDeclaredMethod("ceillog2", int.class);
        ceilLog2.setAccessible(true);
        Method smallestPower = v1_16WorldUpgrade.class.getDeclaredMethod("smallestEncompassingPowerOfTwo", int.class);
        smallestPower.setAccessible(true);
        Method isPowerOfTwo = v1_16WorldUpgrade.class.getDeclaredMethod("isPowerOfTwo", int.class);
        isPowerOfTwo.setAccessible(true);

        long[] empty = new long[0];
        assertSame(empty, addPadding.invoke(null, 16, 4, empty));

        long[] padded = (long[]) addPadding.invoke(null, 256, 9, new long[]{15L});
        assertTrue(padded.length > 1);
        assertEquals(5, ceilLog2.invoke(null, 17));
        assertEquals(32, smallestPower.invoke(null, 17));
        assertEquals(true, isPowerOfTwo.invoke(null, 16));
        assertEquals(false, isPowerOfTwo.invoke(null, 18));
    }

    @Test
    @DisplayName("v1_18WorldUpgrade should shift sections and fill missing ones")
    void v118UpgradeShouldShiftSectionsAndFillMissingOnes() {
        List<CompoundBinaryTag> oldTiles = List.of(CompoundBinaryTag.builder().putString("id", "old_tile").build());
        List<CompoundBinaryTag> oldEntities = List.of(CompoundBinaryTag.builder().putString("id", "old_entity").build());
        List<CompoundBinaryTag> convertedTiles = List.of(CompoundBinaryTag.builder().putString("id", "new_tile").build());
        List<CompoundBinaryTag> convertedEntities = List.of(CompoundBinaryTag.builder().putString("id", "new_entity").build());
        ListBinaryTag convertedPalette = blockPalette("minecraft:stone");
        long[] blockStates = new long[]{7L, 8L};

        ListBinaryTag originalPalette = blockPalette("minecraft:dirt");
        v1_9SlimeChunkSection section = new v1_9SlimeChunkSection(
                null,
                null,
                originalPalette,
                blockStates,
                null,
                null,
                null,
                null
        );
        int[] biomes = new int[1024];
        for (int i = 0; i < 64; i++) {
            biomes[i] = i % 2 == 0 ? 39 : 1;
        }
        v1_9SlimeChunk chunk = new v1_9SlimeChunk(
                "arena",
                1,
                2,
                new v1_9SlimeChunkSection[]{section, null},
                0,
                15,
                CompoundBinaryTag.empty(),
                biomes,
                oldTiles,
                oldEntities
        );

        Long2ObjectOpenHashMap<v1_9SlimeChunk> chunks = new Long2ObjectOpenHashMap<>();
        chunks.put(Util.chunkPosition(1, 2), chunk);
        v1_9SlimeWorld world = new v1_9SlimeWorld((byte) 0x07, "arena", loader, chunks, new ConcurrentHashMap<>(), new SlimePropertyMap(), false);

        when(converter.convertTileEntities(eq(oldTiles), eq(2730), eq(2975))).thenReturn(convertedTiles);
        when(converter.convertEntities(eq(oldEntities), eq(2730), eq(2975))).thenReturn(convertedEntities);
        when(converter.convertBlockPalette(eq(originalPalette), eq(2730), eq(2975))).thenReturn(convertedPalette);

        new v1_18WorldUpgrade().upgrade(world, converter);

        assertSame(convertedTiles, chunk.tileEntities);
        assertSame(convertedEntities, chunk.entities);
        assertEquals(6, chunk.sections.length);

        v1_9SlimeChunkSection shifted = chunk.sections[4];
        assertEquals("minecraft:stone", shifted.blockStatesTag
                .getList("palette", BinaryTagTypes.COMPOUND)
                .getCompound(0)
                .getString("Name"));
        assertArrayEquals(blockStates, shifted.blockStatesTag.getLongArray("data"));
        assertEquals(Set.of("minecraft:badlands", "minecraft:plains"), paletteValues(shifted.biomeTag.getList("palette", BinaryTagTypes.STRING)));

        v1_9SlimeChunkSection filler = chunk.sections[0];
        assertEquals("minecraft:air", filler.blockStatesTag
                .getList("palette", BinaryTagTypes.COMPOUND)
                .getCompound(0)
                .getString("Name"));
        assertEquals(Set.of("minecraft:plains"), paletteValues(filler.biomeTag.getList("palette", BinaryTagTypes.STRING)));

        verify(converter).convertBlockPalette(originalPalette, 2730, 2975);
    }

    private static CompoundBinaryTag[] invokeCreateBiomeSections(
            final int[] biomes,
            final boolean wantExtendedHeight,
            final int minSection
    ) throws Exception {
        Method method = v1_18WorldUpgrade.class.getDeclaredMethod("createBiomeSections", int[].class, boolean.class, int.class);
        method.setAccessible(true);
        return (CompoundBinaryTag[]) method.invoke(null, biomes, wantExtendedHeight, minSection);
    }

    private static ListBinaryTag blockPalette(final String... names) {
        return ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(names).stream()
                .map(name -> (BinaryTag) CompoundBinaryTag.builder().putString("Name", name).build())
                .toList());
    }

    private static Set<String> paletteValues(final ListBinaryTag palette) {
        Set<String> values = new HashSet<>();
        palette.forEach(tag -> values.add(((StringBinaryTag) tag).value()));
        return values;
    }
}
