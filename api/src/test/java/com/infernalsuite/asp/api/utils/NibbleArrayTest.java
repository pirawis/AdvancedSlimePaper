package com.infernalsuite.asp.api.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NibbleArray")
class NibbleArrayTest {

    private NibbleArray nibbleArray;

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create array with specified size")
        void shouldCreateArrayWithSize() {
            NibbleArray array = new NibbleArray(16);
            assertEquals(8, array.getBacking().length); // 16 nibbles = 8 bytes
        }

        @Test
        @DisplayName("should create array from existing byte array")
        void shouldCreateFromByteArray() {
            byte[] backing = new byte[]{0x12, 0x34};
            NibbleArray array = new NibbleArray(backing);
            assertSame(backing, array.getBacking());
        }
    }

    @Nested
    @DisplayName("get/set operations")
    class GetSetTests {

        @BeforeEach
        void setUp() {
            nibbleArray = new NibbleArray(16);
        }

        @Test
        @DisplayName("should set and get value at even index")
        void shouldSetAndGetAtEvenIndex() {
            nibbleArray.set(0, 5);
            assertEquals(5, nibbleArray.get(0));
        }

        @Test
        @DisplayName("should set and get value at odd index")
        void shouldSetAndGetAtOddIndex() {
            nibbleArray.set(1, 10);
            assertEquals(10, nibbleArray.get(1));
        }

        @Test
        @DisplayName("should handle multiple values in same byte")
        void shouldHandleMultipleValuesInSameByte() {
            nibbleArray.set(0, 3);  // lower nibble
            nibbleArray.set(1, 12); // upper nibble

            assertEquals(3, nibbleArray.get(0));
            assertEquals(12, nibbleArray.get(1));
        }

        @Test
        @DisplayName("should only use lower 4 bits of value")
        void shouldOnlyUseLower4Bits() {
            nibbleArray.set(0, 0xFF); // Only 0xF should be stored
            assertEquals(15, nibbleArray.get(0));
        }

        @Test
        @DisplayName("should handle boundary values")
        void shouldHandleBoundaryValues() {
            nibbleArray.set(0, 0);
            nibbleArray.set(1, 15);

            assertEquals(0, nibbleArray.get(0));
            assertEquals(15, nibbleArray.get(1));
        }

        @Test
        @DisplayName("should set values across multiple bytes")
        void shouldSetValuesAcrossMultipleBytes() {
            for (int i = 0; i < 16; i++) {
                nibbleArray.set(i, i % 16);
            }

            for (int i = 0; i < 16; i++) {
                assertEquals(i % 16, nibbleArray.get(i));
            }
        }

        @Test
        @DisplayName("should not affect other nibbles when setting")
        void shouldNotAffectOtherNibbles() {
            nibbleArray.set(0, 7);
            nibbleArray.set(2, 9);
            nibbleArray.set(1, 4);

            assertEquals(7, nibbleArray.get(0));
            assertEquals(4, nibbleArray.get(1));
            assertEquals(9, nibbleArray.get(2));
        }
    }

    @Nested
    @DisplayName("clone")
    class CloneTests {

        @Test
        @DisplayName("should create independent copy")
        void shouldCreateIndependentCopy() {
            nibbleArray = new NibbleArray(8);
            nibbleArray.set(0, 5);

            NibbleArray cloned = nibbleArray.clone();
            cloned.set(0, 10);

            assertEquals(5, nibbleArray.get(0));
            assertEquals(10, cloned.get(0));
        }

        @Test
        @DisplayName("should copy all values")
        void shouldCopyAllValues() {
            nibbleArray = new NibbleArray(8);
            for (int i = 0; i < 8; i++) {
                nibbleArray.set(i, i);
            }

            NibbleArray cloned = nibbleArray.clone();

            for (int i = 0; i < 8; i++) {
                assertEquals(i, cloned.get(i));
            }
        }
    }

    @Nested
    @DisplayName("getBacking")
    class GetBackingTests {

        @Test
        @DisplayName("should return internal byte array")
        void shouldReturnInternalByteArray() {
            byte[] backing = new byte[4];
            nibbleArray = new NibbleArray(backing);

            assertSame(backing, nibbleArray.getBacking());
        }

        @Test
        @DisplayName("should return correct length")
        void shouldReturnCorrectLength() {
            nibbleArray = new NibbleArray(32);
            assertEquals(16, nibbleArray.getBacking().length);
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle standard chunk light array size (2048)")
        void shouldHandleChunkLightArraySize() {
            NibbleArray lightArray = new NibbleArray(4096);
            assertEquals(2048, lightArray.getBacking().length);

            lightArray.set(0, 15);
            lightArray.set(4095, 0);

            assertEquals(15, lightArray.get(0));
            assertEquals(0, lightArray.get(4095));
        }

        @Test
        @DisplayName("should handle all nibble values (0-15)")
        void shouldHandleAllNibbleValues() {
            nibbleArray = new NibbleArray(32);

            for (int i = 0; i < 16; i++) {
                nibbleArray.set(i, i);
            }

            for (int i = 0; i < 16; i++) {
                assertEquals(i, nibbleArray.get(i));
            }
        }

        @Test
        @DisplayName("should mask values above 15")
        void shouldMaskValuesAbove15() {
            nibbleArray = new NibbleArray(4);

            nibbleArray.set(0, 0x1F);
            nibbleArray.set(1, 0xFF);
            nibbleArray.set(2, 16);

            assertEquals(15, nibbleArray.get(0));
            assertEquals(15, nibbleArray.get(1));
            assertEquals(0, nibbleArray.get(2));
        }

        @Test
        @DisplayName("should preserve adjacent values when modifying")
        void shouldPreserveAdjacentValues() {
            nibbleArray = new NibbleArray(8);

            nibbleArray.set(0, 5);
            nibbleArray.set(1, 10);
            nibbleArray.set(2, 3);
            nibbleArray.set(3, 12);

            nibbleArray.set(1, 7);

            assertEquals(5, nibbleArray.get(0));
            assertEquals(7, nibbleArray.get(1));
            assertEquals(3, nibbleArray.get(2));
            assertEquals(12, nibbleArray.get(3));
        }
    }

    @Nested
    @DisplayName("byte array initialization")
    class ByteArrayInitTests {

        @Test
        @DisplayName("should read existing data from byte array")
        void shouldReadExistingData() {
            byte[] data = new byte[] { (byte) 0x21, (byte) 0x43 };
            nibbleArray = new NibbleArray(data);

            assertEquals(1, nibbleArray.get(0));
            assertEquals(2, nibbleArray.get(1));
            assertEquals(3, nibbleArray.get(2));
            assertEquals(4, nibbleArray.get(3));
        }

        @Test
        @DisplayName("should handle max byte values")
        void shouldHandleMaxByteValues() {
            byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFF };
            nibbleArray = new NibbleArray(data);

            assertEquals(15, nibbleArray.get(0));
            assertEquals(15, nibbleArray.get(1));
            assertEquals(15, nibbleArray.get(2));
            assertEquals(15, nibbleArray.get(3));
        }
    }
}
