package com.infernalsuite.asp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("NmsUtil")
class NmsUtilTest {

    @Test
    @DisplayName("should encode chunk coordinates")
    void shouldEncodeChunkCoordinates() {
        int chunkX = 12345;
        int chunkZ = -54321;
        long expected = ((long) chunkZ) * Integer.MAX_VALUE + ((long) chunkX);

        assertEquals(expected, NmsUtil.asLong(chunkX, chunkZ));
    }
}
