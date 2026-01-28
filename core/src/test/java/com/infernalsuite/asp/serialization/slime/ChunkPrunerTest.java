package com.infernalsuite.asp.serialization.slime;

import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeChunkSection;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ChunkPruner")
@ExtendWith(MockitoExtension.class)
class ChunkPrunerTest {

    @Mock
    private SlimeWorld world;

    @Mock
    private SlimeChunk chunk;

    @Mock
    private SlimePropertyMap propertyMap;

    @BeforeEach
    void setUp() {
        when(world.getPropertyMap()).thenReturn(propertyMap);
    }

    @Nested
    @DisplayName("canBePruned with limit save disabled")
    class LimitSaveDisabledTests {

        @BeforeEach
        void setUp() {
            when(propertyMap.getValue(SlimeProperties.SHOULD_LIMIT_SAVE)).thenReturn(false);
        }

        @Test
        @DisplayName("should not prune when pruning is not aggressive")
        void shouldNotPruneWhenPruningNotAggressive() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("none");

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should not prune when chunk has entities")
        void shouldNotPruneWhenChunkHasEntities() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(List.of(CompoundBinaryTag.empty()));

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should not prune when chunk has tile entities")
        void shouldNotPruneWhenChunkHasTileEntities() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(List.of(CompoundBinaryTag.empty()));

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should prune empty chunk with aggressive pruning")
        void shouldPruneEmptyChunkWithAggressivePruning() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(Collections.emptyList());

            // Create empty section with only air
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder()
                    .put("palette", ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                            .add(CompoundBinaryTag.builder().putString("Name", "minecraft:air").build())
                            .build())
                    .build();
            when(section.getBlockStatesTag()).thenReturn(blockStates);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section});

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }

        @Test
        @DisplayName("should not prune chunk with non-air blocks")
        void shouldNotPruneChunkWithNonAirBlocks() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(Collections.emptyList());

            // Create section with stone block
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder()
                    .put("palette", ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                            .add(CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build())
                            .build())
                    .build();
            when(section.getBlockStatesTag()).thenReturn(blockStates);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section});

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should not prune chunk with multiple palette entries")
        void shouldNotPruneChunkWithMultiplePaletteEntries() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(Collections.emptyList());

            // Create section with air and stone
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder()
                    .put("palette", ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                            .add(CompoundBinaryTag.builder().putString("Name", "minecraft:air").build())
                            .add(CompoundBinaryTag.builder().putString("Name", "minecraft:stone").build())
                            .build())
                    .build();
            when(section.getBlockStatesTag()).thenReturn(blockStates);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section});

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should not prune when section throws exception")
        void shouldNotPruneWhenSectionThrowsException() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(Collections.emptyList());

            // Create section that throws exception
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            when(section.getBlockStatesTag()).thenThrow(new RuntimeException("Test exception"));
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section});

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should skip section with non-compound palette type")
        void shouldSkipSectionWithNonCompoundPaletteType() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(Collections.emptyList());

            // Create section with non-compound palette (string type)
            SlimeChunkSection section = mock(SlimeChunkSection.class);
            CompoundBinaryTag blockStates = CompoundBinaryTag.builder()
                    .put("palette", ListBinaryTag.builder(BinaryTagTypes.STRING)
                            .add(net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag("test"))
                            .build())
                    .build();
            when(section.getBlockStatesTag()).thenReturn(blockStates);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section});

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }

        @Test
        @DisplayName("should prune with multiple empty sections")
        void shouldPruneWithMultipleEmptySections() {
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("aggressive");
            when(chunk.getTileEntities()).thenReturn(Collections.emptyList());
            when(chunk.getEntities()).thenReturn(Collections.emptyList());

            // Create multiple empty sections
            SlimeChunkSection section1 = mock(SlimeChunkSection.class);
            SlimeChunkSection section2 = mock(SlimeChunkSection.class);
            CompoundBinaryTag emptyBlockStates = CompoundBinaryTag.builder()
                    .put("palette", ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                            .add(CompoundBinaryTag.builder().putString("Name", "minecraft:air").build())
                            .build())
                    .build();
            when(section1.getBlockStatesTag()).thenReturn(emptyBlockStates);
            when(section2.getBlockStatesTag()).thenReturn(emptyBlockStates);
            when(chunk.getSections()).thenReturn(new SlimeChunkSection[]{section1, section2});

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("canBePruned with limit save enabled")
    class LimitSaveEnabledTests {

        @BeforeEach
        void setUp() {
            when(propertyMap.getValue(SlimeProperties.SHOULD_LIMIT_SAVE)).thenReturn(true);
            when(propertyMap.getValue(SlimeProperties.SAVE_MIN_X)).thenReturn(-10);
            when(propertyMap.getValue(SlimeProperties.SAVE_MAX_X)).thenReturn(10);
            when(propertyMap.getValue(SlimeProperties.SAVE_MIN_Z)).thenReturn(-10);
            when(propertyMap.getValue(SlimeProperties.SAVE_MAX_Z)).thenReturn(10);
        }

        @Test
        @DisplayName("should prune chunk outside X bounds (too low)")
        void shouldPruneChunkOutsideXBoundsTooLow() {
            when(chunk.getX()).thenReturn(-20);
            when(chunk.getZ()).thenReturn(0);

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }

        @Test
        @DisplayName("should prune chunk outside X bounds (too high)")
        void shouldPruneChunkOutsideXBoundsTooHigh() {
            when(chunk.getX()).thenReturn(20);
            when(chunk.getZ()).thenReturn(0);

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }

        @Test
        @DisplayName("should prune chunk outside Z bounds (too low)")
        void shouldPruneChunkOutsideZBoundsTooLow() {
            when(chunk.getX()).thenReturn(0);
            when(chunk.getZ()).thenReturn(-20);

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }

        @Test
        @DisplayName("should prune chunk outside Z bounds (too high)")
        void shouldPruneChunkOutsideZBoundsTooHigh() {
            when(chunk.getX()).thenReturn(0);
            when(chunk.getZ()).thenReturn(20);

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertTrue(result);
        }

        @Test
        @DisplayName("should not prune chunk within bounds")
        void shouldNotPruneChunkWithinBounds() {
            when(chunk.getX()).thenReturn(0);
            when(chunk.getZ()).thenReturn(0);
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("none");

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }

        @Test
        @DisplayName("should not prune chunk at boundary edge")
        void shouldNotPruneChunkAtBoundaryEdge() {
            when(chunk.getX()).thenReturn(10);
            when(chunk.getZ()).thenReturn(-10);
            when(propertyMap.getValue(SlimeProperties.CHUNK_PRUNING)).thenReturn("none");

            boolean result = ChunkPruner.canBePruned(world, chunk);

            assertFalse(result);
        }
    }
}
