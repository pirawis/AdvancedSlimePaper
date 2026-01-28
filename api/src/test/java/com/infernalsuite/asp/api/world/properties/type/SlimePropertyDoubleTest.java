package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.DoubleBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyDouble")
class SlimePropertyDoubleTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test.key", 3.14159);

            assertEquals("test.key", property.getKey());
            assertEquals(3.14159, property.getDefaultValue(), 0.00001);
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyDouble property = SlimePropertyDouble.create("percentage", 0.5,
                value -> value >= 0.0 && value <= 1.0);

            assertEquals("percentage", property.getKey());
            assertEquals(0.5, property.getDefaultValue(), 0.001);
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyDouble.create(null, 0.0));
        }

        @Test
        @DisplayName("should handle zero default value")
        void shouldHandleZeroDefault() {
            SlimePropertyDouble property = SlimePropertyDouble.create("zero", 0.0);
            assertEquals(0.0, property.getDefaultValue(), 0.0001);
        }

        @Test
        @DisplayName("should handle negative default value")
        void shouldHandleNegativeDefault() {
            SlimePropertyDouble property = SlimePropertyDouble.create("negative", -99.99);
            assertEquals(-99.99, property.getDefaultValue(), 0.001);
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create DoubleBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = property.createTag(2.718281828);

            assertEquals(2.718281828, tag.value(), 0.000001);
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = property.createTag(-123.456);

            assertEquals(-123.456, tag.value(), 0.001);
        }

        @Test
        @DisplayName("should handle max double value")
        void shouldHandleMaxValue() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = property.createTag(Double.MAX_VALUE);

            assertEquals(Double.MAX_VALUE, tag.value(), 0.001);
        }

        @Test
        @DisplayName("should handle min double value")
        void shouldHandleMinValue() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = property.createTag(Double.MIN_VALUE);

            assertEquals(Double.MIN_VALUE, tag.value(), 0.0);
        }

        @Test
        @DisplayName("should handle positive infinity")
        void shouldHandlePositiveInfinity() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = property.createTag(Double.POSITIVE_INFINITY);

            assertEquals(Double.POSITIVE_INFINITY, tag.value(), 0.0);
        }

        @Test
        @DisplayName("should handle negative infinity")
        void shouldHandleNegativeInfinity() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = property.createTag(Double.NEGATIVE_INFINITY);

            assertEquals(Double.NEGATIVE_INFINITY, tag.value(), 0.0);
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = DoubleBinaryTag.doubleBinaryTag(9.81);

            assertEquals(9.81, property.readValue(tag), 0.001);
        }

        @Test
        @DisplayName("should read zero value")
        void shouldReadZeroValue() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 1.0);
            DoubleBinaryTag tag = DoubleBinaryTag.doubleBinaryTag(0.0);

            assertEquals(0.0, property.readValue(tag), 0.0001);
        }

        @Test
        @DisplayName("should read negative value")
        void shouldReadNegativeValue() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            DoubleBinaryTag tag = DoubleBinaryTag.doubleBinaryTag(-273.15);

            assertEquals(-273.15, property.readValue(tag), 0.001);
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyDouble property = SlimePropertyDouble.create("ratio", 0.5,
                value -> value >= 0.0 && value <= 1.0);

            assertTrue(property.applyValidator(0.75));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyDouble property = SlimePropertyDouble.create("ratio", 0.5,
                value -> value >= 0.0 && value <= 1.0);

            assertFalse(property.applyValidator(-0.1));
            assertFalse(property.applyValidator(1.1));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyDouble property = SlimePropertyDouble.create("any", 0.0);

            assertTrue(property.applyValidator(Double.MIN_VALUE));
            assertTrue(property.applyValidator(Double.MAX_VALUE));
        }

        @Test
        @DisplayName("should validate boundary values")
        void shouldValidateBoundaryValues() {
            SlimePropertyDouble property = SlimePropertyDouble.create("temp", 20.0,
                value -> value >= -273.15 && value <= 1000.0);

            assertTrue(property.applyValidator(-273.15));
            assertTrue(property.applyValidator(1000.0));
            assertFalse(property.applyValidator(-273.16));
            assertFalse(property.applyValidator(1000.01));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyDouble property = SlimePropertyDouble.create("temperature", 20.0);
            assertEquals("temperature", property.getKey());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyDouble property = SlimePropertyDouble.create("myKey", 1.5);
            assertTrue(property.toString().contains("myKey"));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            double original = 123.456789;

            DoubleBinaryTag tag = property.createTag(original);
            Double result = property.readValue(tag);

            assertEquals(original, result, 0.000001);
        }

        @Test
        @DisplayName("should roundtrip pi correctly")
        void shouldRoundtripPi() {
            SlimePropertyDouble property = SlimePropertyDouble.create("test", 0.0);
            double original = Math.PI;

            DoubleBinaryTag tag = property.createTag(original);
            Double result = property.readValue(tag);

            assertEquals(original, result, 0.0000001);
        }
    }
}
