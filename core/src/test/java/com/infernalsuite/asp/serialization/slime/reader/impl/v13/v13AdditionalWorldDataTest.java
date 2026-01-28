package com.infernalsuite.asp.serialization.slime.reader.impl.v13;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("v13AdditionalWorldData")
class v13AdditionalWorldDataTest {

    @Nested
    @DisplayName("isSet")
    class IsSetTests {

        @Test
        @DisplayName("POI_CHUNKS should be set when bit 0 is set")
        void poiChunksShouldBeSetWhenBit0IsSet() {
            byte bitset = 0b00000001;
            assertTrue(v13AdditionalWorldData.POI_CHUNKS.isSet(bitset));
        }

        @Test
        @DisplayName("POI_CHUNKS should not be set when bit 0 is not set")
        void poiChunksShouldNotBeSetWhenBit0IsNotSet() {
            byte bitset = 0b00000010;
            assertFalse(v13AdditionalWorldData.POI_CHUNKS.isSet(bitset));
        }

        @Test
        @DisplayName("BLOCK_TICKS should be set when bit 1 is set")
        void blockTicksShouldBeSetWhenBit1IsSet() {
            byte bitset = 0b00000010;
            assertTrue(v13AdditionalWorldData.BLOCK_TICKS.isSet(bitset));
        }

        @Test
        @DisplayName("BLOCK_TICKS should not be set when bit 1 is not set")
        void blockTicksShouldNotBeSetWhenBit1IsNotSet() {
            byte bitset = 0b00000001;
            assertFalse(v13AdditionalWorldData.BLOCK_TICKS.isSet(bitset));
        }

        @Test
        @DisplayName("FLUID_TICKS should be set when bit 2 is set")
        void fluidTicksShouldBeSetWhenBit2IsSet() {
            byte bitset = 0b00000100;
            assertTrue(v13AdditionalWorldData.FLUID_TICKS.isSet(bitset));
        }

        @Test
        @DisplayName("FLUID_TICKS should not be set when bit 2 is not set")
        void fluidTicksShouldNotBeSetWhenBit2IsNotSet() {
            byte bitset = 0b00000011;
            assertFalse(v13AdditionalWorldData.FLUID_TICKS.isSet(bitset));
        }

        @Test
        @DisplayName("all flags should be set when all bits are set")
        void allFlagsShouldBeSetWhenAllBitsAreSet() {
            byte bitset = 0b00000111;
            assertTrue(v13AdditionalWorldData.POI_CHUNKS.isSet(bitset));
            assertTrue(v13AdditionalWorldData.BLOCK_TICKS.isSet(bitset));
            assertTrue(v13AdditionalWorldData.FLUID_TICKS.isSet(bitset));
        }

        @Test
        @DisplayName("no flags should be set when bitset is zero")
        void noFlagsShouldBeSetWhenBitsetIsZero() {
            byte bitset = 0;
            assertFalse(v13AdditionalWorldData.POI_CHUNKS.isSet(bitset));
            assertFalse(v13AdditionalWorldData.BLOCK_TICKS.isSet(bitset));
            assertFalse(v13AdditionalWorldData.FLUID_TICKS.isSet(bitset));
        }
    }

    @Nested
    @DisplayName("countUnsupportedFlags")
    class CountUnsupportedFlagsTests {

        @Test
        @DisplayName("should return 0 when only supported flags are set")
        void shouldReturnZeroWhenOnlySupportedFlagsAreSet() {
            byte bitset = 0b00000111;
            assertEquals(0, v13AdditionalWorldData.countUnsupportedFlags(bitset));
        }

        @Test
        @DisplayName("should return 0 when no flags are set")
        void shouldReturnZeroWhenNoFlagsAreSet() {
            byte bitset = 0;
            assertEquals(0, v13AdditionalWorldData.countUnsupportedFlags(bitset));
        }

        @Test
        @DisplayName("should return 1 when bit 3 is set")
        void shouldReturnOneWhenBit3IsSet() {
            byte bitset = 0b00001000;
            assertEquals(1, v13AdditionalWorldData.countUnsupportedFlags(bitset));
        }

        @Test
        @DisplayName("should return count of unsupported bits in positive range")
        void shouldReturnCountOfUnsupportedBitsInPositiveRange() {
            // Use positive byte values to avoid sign extension issues
            byte bitset = 0b01111000; // bits 3,4,5,6 are unsupported
            assertEquals(4, v13AdditionalWorldData.countUnsupportedFlags(bitset));
        }

        @Test
        @DisplayName("should count unsupported bits correctly with mixed flags")
        void shouldCountUnsupportedBitsWithMixedFlags() {
            // bits 0,1,2 are supported; bit 3,4,5 are unsupported
            byte bitset = 0b00111111;
            assertEquals(3, v13AdditionalWorldData.countUnsupportedFlags(bitset));
        }
    }

    @Nested
    @DisplayName("fromSet")
    class FromSetTests {

        @Test
        @DisplayName("should return 0 for empty set")
        void shouldReturnZeroForEmptySet() {
            EnumSet<v13AdditionalWorldData> set = EnumSet.noneOf(v13AdditionalWorldData.class);
            assertEquals(0, v13AdditionalWorldData.fromSet(set));
        }

        @Test
        @DisplayName("should set bit 0 for POI_CHUNKS")
        void shouldSetBit0ForPoiChunks() {
            EnumSet<v13AdditionalWorldData> set = EnumSet.of(v13AdditionalWorldData.POI_CHUNKS);
            assertEquals(0b00000001, v13AdditionalWorldData.fromSet(set));
        }

        @Test
        @DisplayName("should set bit 1 for BLOCK_TICKS")
        void shouldSetBit1ForBlockTicks() {
            EnumSet<v13AdditionalWorldData> set = EnumSet.of(v13AdditionalWorldData.BLOCK_TICKS);
            assertEquals(0b00000010, v13AdditionalWorldData.fromSet(set));
        }

        @Test
        @DisplayName("should set bit 2 for FLUID_TICKS")
        void shouldSetBit2ForFluidTicks() {
            EnumSet<v13AdditionalWorldData> set = EnumSet.of(v13AdditionalWorldData.FLUID_TICKS);
            assertEquals(0b00000100, v13AdditionalWorldData.fromSet(set));
        }

        @Test
        @DisplayName("should set all bits for all flags")
        void shouldSetAllBitsForAllFlags() {
            EnumSet<v13AdditionalWorldData> set = EnumSet.allOf(v13AdditionalWorldData.class);
            assertEquals(0b00000111, v13AdditionalWorldData.fromSet(set));
        }

        @Test
        @DisplayName("should combine multiple flags correctly")
        void shouldCombineMultipleFlagsCorrectly() {
            EnumSet<v13AdditionalWorldData> set = EnumSet.of(
                    v13AdditionalWorldData.POI_CHUNKS,
                    v13AdditionalWorldData.FLUID_TICKS
            );
            assertEquals(0b00000101, v13AdditionalWorldData.fromSet(set));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip empty set")
        void shouldRoundtripEmptySet() {
            EnumSet<v13AdditionalWorldData> original = EnumSet.noneOf(v13AdditionalWorldData.class);
            byte bitset = v13AdditionalWorldData.fromSet(original);

            EnumSet<v13AdditionalWorldData> result = EnumSet.noneOf(v13AdditionalWorldData.class);
            for (v13AdditionalWorldData data : v13AdditionalWorldData.values()) {
                if (data.isSet(bitset)) {
                    result.add(data);
                }
            }

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip all flags")
        void shouldRoundtripAllFlags() {
            EnumSet<v13AdditionalWorldData> original = EnumSet.allOf(v13AdditionalWorldData.class);
            byte bitset = v13AdditionalWorldData.fromSet(original);

            EnumSet<v13AdditionalWorldData> result = EnumSet.noneOf(v13AdditionalWorldData.class);
            for (v13AdditionalWorldData data : v13AdditionalWorldData.values()) {
                if (data.isSet(bitset)) {
                    result.add(data);
                }
            }

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip partial set")
        void shouldRoundtripPartialSet() {
            EnumSet<v13AdditionalWorldData> original = EnumSet.of(
                    v13AdditionalWorldData.BLOCK_TICKS
            );
            byte bitset = v13AdditionalWorldData.fromSet(original);

            EnumSet<v13AdditionalWorldData> result = EnumSet.noneOf(v13AdditionalWorldData.class);
            for (v13AdditionalWorldData data : v13AdditionalWorldData.values()) {
                if (data.isSet(bitset)) {
                    result.add(data);
                }
            }

            assertEquals(original, result);
        }
    }

    @Nested
    @DisplayName("enum values")
    class EnumValuesTests {

        @Test
        @DisplayName("should have exactly 3 values")
        void shouldHaveExactly3Values() {
            assertEquals(3, v13AdditionalWorldData.values().length);
        }

        @Test
        @DisplayName("POI_CHUNKS should have ordinal 0")
        void poiChunksShouldHaveOrdinal0() {
            assertEquals(0, v13AdditionalWorldData.POI_CHUNKS.ordinal());
        }

        @Test
        @DisplayName("BLOCK_TICKS should have ordinal 1")
        void blockTicksShouldHaveOrdinal1() {
            assertEquals(1, v13AdditionalWorldData.BLOCK_TICKS.ordinal());
        }

        @Test
        @DisplayName("FLUID_TICKS should have ordinal 2")
        void fluidTicksShouldHaveOrdinal2() {
            assertEquals(2, v13AdditionalWorldData.FLUID_TICKS.ordinal());
        }
    }
}
