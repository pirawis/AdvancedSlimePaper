package com.infernalsuite.asp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeLogger")
class SlimeLoggerTest {

    private boolean originalDebugState;

    @BeforeEach
    void setUp() {
        originalDebugState = SlimeLogger.DEBUG;
    }

    @AfterEach
    void tearDown() {
        SlimeLogger.DEBUG = originalDebugState;
    }

    @Nested
    @DisplayName("debug")
    class DebugTests {

        @Test
        @DisplayName("should not throw when debug is enabled")
        void shouldNotThrowWhenDebugEnabled() {
            SlimeLogger.DEBUG = true;
            assertDoesNotThrow(() -> SlimeLogger.debug("Test debug message"));
        }

        @Test
        @DisplayName("should not throw when debug is disabled")
        void shouldNotThrowWhenDebugDisabled() {
            SlimeLogger.DEBUG = false;
            assertDoesNotThrow(() -> SlimeLogger.debug("Test debug message"));
        }

        @Test
        @DisplayName("should handle null message when debug enabled")
        void shouldHandleNullMessageWhenEnabled() {
            SlimeLogger.DEBUG = true;
            assertDoesNotThrow(() -> SlimeLogger.debug(null));
        }

        @Test
        @DisplayName("should handle empty message")
        void shouldHandleEmptyMessage() {
            SlimeLogger.DEBUG = true;
            assertDoesNotThrow(() -> SlimeLogger.debug(""));
        }
    }

    @Nested
    @DisplayName("warn")
    class WarnTests {

        @Test
        @DisplayName("should not throw with valid message")
        void shouldNotThrowWithValidMessage() {
            assertDoesNotThrow(() -> SlimeLogger.warn("Test warning message"));
        }

        @Test
        @DisplayName("should handle null message")
        void shouldHandleNullMessage() {
            assertDoesNotThrow(() -> SlimeLogger.warn(null));
        }

        @Test
        @DisplayName("should handle empty message")
        void shouldHandleEmptyMessage() {
            assertDoesNotThrow(() -> SlimeLogger.warn(""));
        }
    }
}
