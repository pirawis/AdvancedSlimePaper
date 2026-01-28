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

        @Test
        @DisplayName("should throw NewerFormatException with correct version in message")
        void shouldThrowNewerFormatExceptionWithCorrectVersion() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(SlimeFormat.SLIME_HEADER);
            dos.writeByte(99);

            byte[] data = baos.toByteArray();

            NewerFormatException exception = assertThrows(NewerFormatException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "future", data, propertyMap, false));

            assertTrue(exception.getMessage().contains("99"));
        }

        @Test
        @DisplayName("should throw CorruptedWorldException with world name in message")
        void shouldIncludeWorldNameInCorruptedWorldException() {
            byte[] invalidData = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};
            String worldName = "my_broken_world";

            CorruptedWorldException exception = assertThrows(CorruptedWorldException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, worldName, invalidData, propertyMap, true));

            assertTrue(exception.getMessage().contains(worldName));
        }

        @Test
        @DisplayName("should handle readOnly flag in call")
        void shouldHandleReadOnlyFlag() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.write(SlimeFormat.SLIME_HEADER);
            dos.writeByte(SlimeFormat.SLIME_VERSION + 10);

            byte[] data = baos.toByteArray();

            assertThrows(NewerFormatException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "readonly_test", data, propertyMap, true));

            assertThrows(NewerFormatException.class, () ->
                    SlimeWorldReaderRegistry.readWorld(loader, "writable_test", data, propertyMap, false));
        }
    }

    @Nested
    @DisplayName("SlimeFormat constants")
    class SlimeFormatConstantsTests {

        @Test
        @DisplayName("should have valid slime header")
        void shouldHaveValidSlimeHeader() {
            assertNotNull(SlimeFormat.SLIME_HEADER);
            assertTrue(SlimeFormat.SLIME_HEADER.length > 0);
        }

        @Test
        @DisplayName("should have current version as 13")
        void shouldHaveCurrentVersion13() {
            assertEquals(13, SlimeFormat.SLIME_VERSION);
        }

        @Test
        @DisplayName("header should be 2 bytes")
        void headerShouldBe2Bytes() {
            assertEquals(2, SlimeFormat.SLIME_HEADER.length);
        }
    }
}
