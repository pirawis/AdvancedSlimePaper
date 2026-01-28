package com.infernalsuite.asp.serialization.slime.reader;

import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.utils.SlimeFormat;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeWorldReaderRegistry")
@ExtendWith(MockitoExtension.class)
class SlimeWorldReaderRegistryTest {

    @Mock
    private SlimeLoader loader;

    @Mock
    private SlimePropertyMap propertyMap;

    @Nested
    @DisplayName("readWorld")
    class ReadWorldTests {

        @Test
        @DisplayName("should throw CorruptedWorldException for invalid header")
        void shouldThrowCorruptedWorldExceptionForInvalidHeader() {
            byte[] invalidData = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};

            assertThrows(CorruptedWorldException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "test", invalidData, propertyMap, true));
        }

        @Test
        @DisplayName("should throw IOException for partial header")
        void shouldThrowIOExceptionForPartialHeader() {
            byte[] partialHeader = new byte[]{(byte) 0xB1, 0x0B}; // Only first 2 bytes of header

            assertThrows(IOException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "test", partialHeader, propertyMap, true));
        }

        @Test
        @DisplayName("should throw NewerFormatException for unsupported version")
        void shouldThrowNewerFormatExceptionForUnsupportedVersion() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // Write valid header
            dos.write(SlimeFormat.SLIME_HEADER);
            // Write version higher than current
            dos.writeByte(SlimeFormat.SLIME_VERSION + 1);

            byte[] data = baos.toByteArray();

            NewerFormatException exception = assertThrows(NewerFormatException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "test", data, propertyMap, true));

            assertNotNull(exception);
        }

        @Test
        @DisplayName("should throw CorruptedWorldException for empty data")
        void shouldThrowCorruptedWorldExceptionForEmptyData() {
            byte[] emptyData = new byte[0];

            assertThrows(Exception.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "empty", emptyData, propertyMap, true));
        }

        @Test
        @DisplayName("should throw CorruptedWorldException for wrong magic bytes")
        void shouldThrowCorruptedWorldExceptionForWrongMagicBytes() {
            byte[] wrongMagic = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x0A}; // ZIP file magic

            assertThrows(CorruptedWorldException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "zipfile", wrongMagic, propertyMap, true));
        }
    }
}
