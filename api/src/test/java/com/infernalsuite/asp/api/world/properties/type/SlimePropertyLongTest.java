package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyLong")
class SlimePropertyLongTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyLong property = SlimePropertyLong.create("test.key", 9876543210L);

            assertEquals("test.key", property.getKey());
            assertEquals(9876543210L, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyLong property = SlimePropertyLong.create("timestamp", 0L,
                value -> value >= 0);

            assertEquals("timestamp", property.getKey());
            assertEquals(0L, property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyLong.create(null, 0L));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyLong property = SlimePropertyLong.create("positiveOnly", 1L,
                value -> value > 0);

            assertTrue(property.applyValidator(100L));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyLong property = SlimePropertyLong.create("positiveOnly", 1L,
                value -> value > 0);

            assertFalse(property.applyValidator(0L));
            assertFalse(property.applyValidator(-1L));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyLong property = SlimePropertyLong.create("any", 0L);

            assertTrue(property.applyValidator(Long.MIN_VALUE));
            assertTrue(property.applyValidator(Long.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyLong property = SlimePropertyLong.create("myKey", 100L);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
