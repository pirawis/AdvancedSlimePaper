package com.infernalsuite.asp.api.world.properties.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyBoolean")
class SlimePropertyBooleanTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value true")
        void shouldCreateWithDefaultTrue() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test.enabled", true);

            assertEquals("test.enabled", property.getKey());
            assertTrue(property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with key and default value false")
        void shouldCreateWithDefaultFalse() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test.disabled", false);

            assertEquals("test.disabled", property.getKey());
            assertFalse(property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("feature", true,
                value -> value);

            assertEquals("feature", property.getKey());
            assertTrue(property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyBoolean.create(null, true));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation when no validator")
        void shouldPassValidationWhenNoValidator() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("any", false);

            assertTrue(property.applyValidator(true));
            assertTrue(property.applyValidator(false));
        }

        @Test
        @DisplayName("should apply custom validator")
        void shouldApplyCustomValidator() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("mustBeTrue", true,
                value -> value);

            assertTrue(property.applyValidator(true));
            assertFalse(property.applyValidator(false));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("myKey", true);
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
