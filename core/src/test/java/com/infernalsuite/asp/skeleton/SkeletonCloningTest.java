package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SkeletonCloning")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SkeletonCloningTest {

    @Mock
    private SlimeWorld sourceWorld;

    @Mock
    private SlimeLoader sourceLoader;

    @Mock
    private SlimeLoader targetLoader;

    @Mock
    private SlimePropertyMap propertyMap;

    @Mock
    private SlimePropertyMap clonedPropertyMap;

    private ConcurrentMap<String, BinaryTag> extraData;

    @BeforeEach
    void setUp() {
        extraData = new ConcurrentHashMap<>();
        extraData.put("test", IntBinaryTag.intBinaryTag(42));
    }

    @Nested
    @DisplayName("fullClone")
    class FullCloneTests {

        @BeforeEach
        void setUp() {
            when(sourceWorld.getChunkStorage()).thenReturn(new ArrayList<>());
            when(sourceWorld.getExtraData()).thenReturn(extraData);
            when(sourceWorld.getPropertyMap()).thenReturn(propertyMap);
            when(sourceWorld.getDataVersion()).thenReturn(3465);
            when(sourceWorld.getLoader()).thenReturn(sourceLoader);
            when(propertyMap.clone()).thenReturn(clonedPropertyMap);
        }

        @Test
        @DisplayName("should create new world with given name")
        void shouldCreateNewWorldWithGivenName() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertEquals("newworld", cloned.getName());
        }

        @Test
        @DisplayName("should use source loader when target is null")
        void shouldUseSourceLoaderWhenTargetIsNull() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, null, false);

            assertSame(sourceLoader, cloned.getLoader());
        }

        @Test
        @DisplayName("should clone extra data")
        void shouldCloneExtraData() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertEquals(1, cloned.getExtraData().size());
            assertEquals(IntBinaryTag.intBinaryTag(42), cloned.getExtraData().get("test"));
        }

        @Test
        @DisplayName("should preserve data version")
        void shouldPreserveDataVersion() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertEquals(3465, cloned.getDataVersion());
        }

        @Test
        @DisplayName("should clone property map")
        void shouldClonePropertyMap() {
            SkeletonSlimeWorld cloned = SkeletonCloning.fullClone("newworld", sourceWorld, targetLoader, false);

            assertSame(clonedPropertyMap, cloned.getPropertyMap());
            verify(propertyMap).clone();
        }
    }

    @Nested
    @DisplayName("weakCopy")
    class WeakCopyTests {

        @BeforeEach
        void setUp() {
            when(sourceWorld.getName()).thenReturn("original");
            when(sourceWorld.getLoader()).thenReturn(sourceLoader);
            when(sourceWorld.isReadOnly()).thenReturn(false);
            when(sourceWorld.getChunkStorage()).thenReturn(new ArrayList<>());
            when(sourceWorld.getExtraData()).thenReturn(extraData);
            when(sourceWorld.getPropertyMap()).thenReturn(propertyMap);
            when(sourceWorld.getDataVersion()).thenReturn(3465);
            when(propertyMap.clone()).thenReturn(clonedPropertyMap);
        }

        @Test
        @DisplayName("should preserve world name")
        void shouldPreserveWorldName() {
            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertEquals("original", copy.getName());
        }

        @Test
        @DisplayName("should preserve loader")
        void shouldPreserveLoader() {
            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertSame(sourceLoader, copy.getLoader());
        }

        @Test
        @DisplayName("should preserve readOnly flag")
        void shouldPreserveReadOnlyFlag() {
            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertFalse(copy.readOnly());
        }

        @Test
        @DisplayName("should copy chunks by reference")
        void shouldCopyChunksByReference() {
            SlimeChunk mockChunk = mock(SlimeChunk.class);
            when(mockChunk.getX()).thenReturn(1);
            when(mockChunk.getZ()).thenReturn(2);
            when(sourceWorld.getChunkStorage()).thenReturn(List.of(mockChunk));

            SkeletonSlimeWorld copy = SkeletonCloning.weakCopy(sourceWorld);

            assertEquals(1, copy.getChunkStorage().size());
            assertSame(mockChunk, copy.getChunk(1, 2));
        }
    }
}
