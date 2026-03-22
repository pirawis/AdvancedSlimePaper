package com.infernalsuite.asp.serialization;

import com.infernalsuite.asp.api.SlimeDataConverter;
import com.infernalsuite.asp.api.SlimeNMSBridge;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.utils.SlimeFormat;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.serialization.slime.SlimeSerializer;
import com.infernalsuite.asp.serialization.slime.reader.SlimeWorldReaderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SlimeSerializationAdapterImpl")
@ExtendWith(MockitoExtension.class)
class SlimeSerializationAdapterImplTest {

    private SlimeSerializationAdapterImpl adapter;

    @Mock
    private SlimeWorld mockSlimeWorld;

    @Mock
    private SlimeWorldInstance mockSlimeWorldInstance;

    @Mock
    private SlimeLoader mockLoader;

    @Mock
    private SlimePropertyMap propertyMap;

    @BeforeEach
    void setUp() {
        adapter = new SlimeSerializationAdapterImpl();
    }

    @Nested
    @DisplayName("serializeWorld")
    class SerializeWorldTests {

        @Test
        @DisplayName("should throw IllegalArgumentException for SlimeWorldInstance")
        void shouldThrowForSlimeWorldInstance() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.serializeWorld(mockSlimeWorldInstance)
            );

            assertTrue(exception.getMessage().contains("SlimeWorldInstances cannot be serialized directly"));
            assertTrue(exception.getMessage().contains("getSerializableCopy()"));
        }

        @Test
        @DisplayName("should delegate serialization for serializable slime worlds")
        void shouldDelegateSerializationForSerializableSlimeWorlds() {
            byte[] serialized = new byte[]{4, 2, 0};

            try (MockedStatic<SlimeSerializer> serializer = mockStatic(SlimeSerializer.class)) {
                serializer.when(() -> SlimeSerializer.serialize(mockSlimeWorld)).thenReturn(serialized);

                assertSame(serialized, adapter.serializeWorld(mockSlimeWorld));
            }
        }
    }

    @Nested
    @DisplayName("getSlimeFormat")
    class GetSlimeFormatTests {

        @Test
        @DisplayName("should return current slime format version")
        void shouldReturnCurrentSlimeFormatVersion() {
            int version = adapter.getSlimeFormat();

            assertEquals(SlimeFormat.SLIME_VERSION, version);
        }

        @Test
        @DisplayName("should return version 13")
        void shouldReturnVersion13() {
            assertEquals(13, adapter.getSlimeFormat());
        }
    }

    @Nested
    @DisplayName("deserializeWorld")
    class DeserializeWorldTests {

        @Test
        @DisplayName("should read the world and apply data fixers")
        void shouldReadTheWorldAndApplyDataFixers() throws Exception {
            SlimeWorld deserializedWorld = mock(SlimeWorld.class);
            SlimeWorld fixedWorld = mock(SlimeWorld.class);
            SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
            SlimeDataConverter converter = mock(SlimeDataConverter.class);
            byte[] serializedWorld = {1, 2, 3};

            try (MockedStatic<SlimeWorldReaderRegistry> registry = mockStatic(SlimeWorldReaderRegistry.class);
                 MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
                registry.when(() -> SlimeWorldReaderRegistry.readWorld(mockLoader, "arena", serializedWorld, propertyMap, false))
                        .thenReturn(deserializedWorld);
                bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
                when(bridge.getSlimeDataConverter()).thenReturn(converter);
                when(converter.applyDataFixers(deserializedWorld)).thenReturn(fixedWorld);

                SlimeWorld result = adapter.deserializeWorld("arena", serializedWorld, mockLoader, propertyMap, false);

                assertSame(fixedWorld, result);
                verify(converter).applyDataFixers(deserializedWorld);
            }
        }

        @Test
        @DisplayName("should force read only mode when no loader is provided")
        void shouldForceReadOnlyModeWhenNoLoaderIsProvided() throws Exception {
            SlimeWorld deserializedWorld = mock(SlimeWorld.class);
            SlimeWorld fixedWorld = mock(SlimeWorld.class);
            SlimeNMSBridge bridge = mock(SlimeNMSBridge.class);
            SlimeDataConverter converter = mock(SlimeDataConverter.class);
            byte[] serializedWorld = {9, 9, 9};

            try (MockedStatic<SlimeWorldReaderRegistry> registry = mockStatic(SlimeWorldReaderRegistry.class);
                 MockedStatic<SlimeNMSBridge> bridgeStatic = mockStatic(SlimeNMSBridge.class)) {
                registry.when(() -> SlimeWorldReaderRegistry.readWorld(null, "arena", serializedWorld, propertyMap, true))
                        .thenReturn(deserializedWorld);
                bridgeStatic.when(SlimeNMSBridge::instance).thenReturn(bridge);
                when(bridge.getSlimeDataConverter()).thenReturn(converter);
                when(converter.applyDataFixers(deserializedWorld)).thenReturn(fixedWorld);

                SlimeWorld result = adapter.deserializeWorld("arena", serializedWorld, null, propertyMap, false);

                assertSame(fixedWorld, result);
            }
        }
    }
}
