package com.infernalsuite.asp;

import net.kyori.adventure.nbt.ListBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SimpleDataFixerConverter")
class SimpleDataFixerConverterTest {

    @Test
    @DisplayName("should handle empty entity list")
    void shouldHandleEmptyEntityList() {
        SimpleDataFixerConverter converter = new SimpleDataFixerConverter();
        List<?> result = converter.convertEntities(List.of(), 0, 0);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should handle empty tile entity list")
    void shouldHandleEmptyTileEntityList() {
        SimpleDataFixerConverter converter = new SimpleDataFixerConverter();
        List<?> result = converter.convertTileEntities(List.of(), 0, 0);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should handle empty block palette")
    void shouldHandleEmptyBlockPalette() {
        SimpleDataFixerConverter converter = new SimpleDataFixerConverter();
        ListBinaryTag result = converter.convertBlockPalette(ListBinaryTag.empty(), 0, 0);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
