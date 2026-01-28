package com.infernalsuite.asp.serialization;

import com.infernalsuite.asp.api.utils.SlimeFormat;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
