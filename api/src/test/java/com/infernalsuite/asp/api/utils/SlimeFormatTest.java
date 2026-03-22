package com.infernalsuite.asp.api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeFormat")
class SlimeFormatTest {

    @Test
    @DisplayName("should allow creating a format holder instance")
    void shouldAllowCreatingAFormatHolderInstance() {
        assertDoesNotThrow(SlimeFormat::new);
    }

    @Nested
    @DisplayName("SLIME_HEADER")
    class SlimeHeaderTests {

        @Test
        @DisplayName("should have correct length")
        void shouldHaveCorrectLength() {
            assertEquals(2, SlimeFormat.SLIME_HEADER.length);
        }

        @Test
        @DisplayName("should have correct first byte")
        void shouldHaveCorrectFirstByte() {
            assertEquals(-79, SlimeFormat.SLIME_HEADER[0]);
        }

        @Test
        @DisplayName("should have correct second byte")
        void shouldHaveCorrectSecondByte() {
            assertEquals(11, SlimeFormat.SLIME_HEADER[1]);
        }

        @Test
        @DisplayName("should be consistent")
        void shouldBeConsistent() {
            assertArrayEquals(new byte[] { -79, 11 }, SlimeFormat.SLIME_HEADER);
        }
    }

    @Nested
    @DisplayName("SLIME_VERSION")
    class SlimeVersionTests {

        @Test
        @DisplayName("should be version 13")
        void shouldBeVersion13() {
            assertEquals(13, SlimeFormat.SLIME_VERSION);
        }

        @Test
        @DisplayName("should be positive")
        void shouldBePositive() {
            assertTrue(SlimeFormat.SLIME_VERSION > 0);
        }

        @Test
        @DisplayName("should fit in a byte")
        void shouldFitInByte() {
            assertTrue(SlimeFormat.SLIME_VERSION >= Byte.MIN_VALUE);
            assertTrue(SlimeFormat.SLIME_VERSION <= Byte.MAX_VALUE);
        }

        @Test
        @DisplayName("should be castable to byte without loss")
        void shouldBeCastableToByteWithoutLoss() {
            byte versionAsByte = (byte) SlimeFormat.SLIME_VERSION;
            assertEquals(SlimeFormat.SLIME_VERSION, versionAsByte);
        }
    }
}
