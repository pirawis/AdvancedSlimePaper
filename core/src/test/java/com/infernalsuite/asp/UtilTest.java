package com.infernalsuite.asp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Util")
class UtilTest {

    @Test
    @DisplayName("should reject utility instantiation")
    void shouldRejectUtilityInstantiation() throws Exception {
        var constructor = Util.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("chunkPosition")
    class ChunkPositionTests {

        @Test
        @DisplayName("should encode zero coordinates")
        void shouldEncodeZeroCoordinates() {
            long pos = Util.chunkPosition(0, 0);
            assertEquals(0L, pos);
        }

        @Test
        @DisplayName("should encode positive coordinates")
        void shouldEncodePositiveCoordinates() {
            long pos = Util.chunkPosition(10, 20);

            // x is in upper 32 bits, z in lower 32 bits
            int extractedX = (int) (pos >> 32);
            int extractedZ = (int) pos;

            assertEquals(10, extractedX);
            assertEquals(20, extractedZ);
        }

        @Test
        @DisplayName("should encode negative coordinates")
        void shouldEncodeNegativeCoordinates() {
            long pos = Util.chunkPosition(-5, -10);

            int extractedX = (int) (pos >> 32);
            int extractedZ = (int) pos;

            assertEquals(-5, extractedX);
            assertEquals(-10, extractedZ);
        }

        @Test
        @DisplayName("should encode mixed positive and negative")
        void shouldEncodeMixedCoordinates() {
            long pos = Util.chunkPosition(-100, 200);

            int extractedX = (int) (pos >> 32);
            int extractedZ = (int) pos;

            assertEquals(-100, extractedX);
            assertEquals(200, extractedZ);
        }

        @Test
        @DisplayName("should handle max integer values")
        void shouldHandleMaxValues() {
            long pos = Util.chunkPosition(Integer.MAX_VALUE, Integer.MAX_VALUE);

            int extractedX = (int) (pos >> 32);
            int extractedZ = (int) pos;

            assertEquals(Integer.MAX_VALUE, extractedX);
            assertEquals(Integer.MAX_VALUE, extractedZ);
        }

        @Test
        @DisplayName("should handle min integer values")
        void shouldHandleMinValues() {
            long pos = Util.chunkPosition(Integer.MIN_VALUE, Integer.MIN_VALUE);

            int extractedX = (int) (pos >> 32);
            int extractedZ = (int) pos;

            assertEquals(Integer.MIN_VALUE, extractedX);
            assertEquals(Integer.MIN_VALUE, extractedZ);
        }

        @Test
        @DisplayName("should produce unique values for different coordinates")
        void shouldProduceUniqueValues() {
            long pos1 = Util.chunkPosition(1, 2);
            long pos2 = Util.chunkPosition(2, 1);
            long pos3 = Util.chunkPosition(1, 1);

            assertNotEquals(pos1, pos2);
            assertNotEquals(pos1, pos3);
            assertNotEquals(pos2, pos3);
        }
    }
}
