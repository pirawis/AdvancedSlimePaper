package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.FloatBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyFloat")
class SlimePropertyFloatTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test.key", 3.14f);

            assertEquals("test.key", property.getKey());
            assertEquals(3.14f, property.getDefaultValue(), 0.001f);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyFloat property = SlimePropertyFloat.create("percentage", 0.5f,
                value -> value >= 0.0f && value <= 1.0f);

            assertEquals("percentage", property.getKey());
            assertEquals(0.5f, property.getDefaultValue(), 0.001f);
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyFloat.create(null, 0.0f));
        }

        @Test
        @DisplayName("should handle zero default value")
        void shouldHandleZeroDefault() {
            SlimePropertyFloat property = SlimePropertyFloat.create("zero", 0.0f);
            assertEquals(0.0f, property.getDefaultValue(), 0.001f);
        }

        @Test
        @DisplayName("should handle negative default value")
        void shouldHandleNegativeDefault() {
            SlimePropertyFloat property = SlimePropertyFloat.create("negative", -3.5f);
            assertEquals(-3.5f, property.getDefaultValue(), 0.001f);
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create FloatBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 0.0f);
            FloatBinaryTag tag = property.createTag(2.5f);

            assertEquals(2.5f, tag.value(), 0.001f);
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 0.0f);
            FloatBinaryTag tag = property.createTag(-99.9f);

            assertEquals(-99.9f, tag.value(), 0.001f);
        }

        @Test
        @DisplayName("should handle max float value")
        void shouldHandleMaxValue() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 0.0f);
            FloatBinaryTag tag = property.createTag(Float.MAX_VALUE);

            assertEquals(Float.MAX_VALUE, tag.value(), 0.001f);
        }

        @Test
        @DisplayName("should handle min float value")
        void shouldHandleMinValue() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 0.0f);
            FloatBinaryTag tag = property.createTag(Float.MIN_VALUE);

            assertEquals(Float.MIN_VALUE, tag.value(), 0.001f);
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 0.0f);
            FloatBinaryTag tag = FloatBinaryTag.floatBinaryTag(7.77f);

            assertEquals(7.77f, property.readValue(tag), 0.001f);
        }

        @Test
        @DisplayName("should read zero value")
        void shouldReadZeroValue() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 1.0f);
            FloatBinaryTag tag = FloatBinaryTag.floatBinaryTag(0.0f);

            assertEquals(0.0f, property.readValue(tag), 0.001f);
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyFloat property = SlimePropertyFloat.create("ratio", 0.5f,
                value -> value >= 0.0f && value <= 1.0f);

            assertTrue(property.applyValidator(0.75f));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyFloat property = SlimePropertyFloat.create("ratio", 0.5f,
                value -> value >= 0.0f && value <= 1.0f);

            assertFalse(property.applyValidator(-0.1f));
            assertFalse(property.applyValidator(1.1f));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyFloat property = SlimePropertyFloat.create("any", 0.0f);

            assertTrue(property.applyValidator(Float.MIN_VALUE));
            assertTrue(property.applyValidator(Float.MAX_VALUE));
        }

        @Test
        @DisplayName("should validate boundary values")
        void shouldValidateBoundaryValues() {
            SlimePropertyFloat property = SlimePropertyFloat.create("angle", 0.0f,
                value -> value >= 0.0f && value <= 360.0f);

            assertTrue(property.applyValidator(0.0f));
            assertTrue(property.applyValidator(360.0f));
            assertFalse(property.applyValidator(360.1f));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyFloat property = SlimePropertyFloat.create("spawnYaw", 0.0f);
            assertEquals("spawnYaw", property.getNbtName());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyFloat property = SlimePropertyFloat.create("myKey", 1.5f);
            assertTrue(property.toString().contains("myKey"));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyFloat property = SlimePropertyFloat.create("test", 0.0f);
            float original = 123.456f;

            FloatBinaryTag tag = property.createTag(original);
            Float result = property.readValue(tag);

            assertEquals(original, result, 0.001f);
        }
    }
}
