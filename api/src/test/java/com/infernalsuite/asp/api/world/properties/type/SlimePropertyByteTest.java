package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.ByteBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyByte")
class SlimePropertyByteTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyByte property = SlimePropertyByte.create("test.key", (byte) 42);

            assertEquals("test.key", property.getKey());
            assertEquals((byte) 42, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyByte property = SlimePropertyByte.create("range", (byte) 50,
                value -> value >= 0 && value <= 100);

            assertEquals("range", property.getKey());
            assertEquals((byte) 50, property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyByte.create(null, (byte) 0));
        }

        @Test
        @DisplayName("should handle zero default value")
        void shouldHandleZeroDefault() {
            SlimePropertyByte property = SlimePropertyByte.create("zero", (byte) 0);
            assertEquals((byte) 0, property.getDefaultValue());
        }

        @Test
        @DisplayName("should handle negative default value")
        void shouldHandleNegativeDefault() {
            SlimePropertyByte property = SlimePropertyByte.create("negative", (byte) -100);
            assertEquals((byte) -100, property.getDefaultValue());
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create ByteBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            ByteBinaryTag tag = property.createTag((byte) 99);

            assertEquals((byte) 99, tag.value());
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            ByteBinaryTag tag = property.createTag((byte) -50);

            assertEquals((byte) -50, tag.value());
        }

        @Test
        @DisplayName("should handle max byte value")
        void shouldHandleMaxValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            ByteBinaryTag tag = property.createTag(Byte.MAX_VALUE);

            assertEquals(Byte.MAX_VALUE, tag.value());
        }

        @Test
        @DisplayName("should handle min byte value")
        void shouldHandleMinValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            ByteBinaryTag tag = property.createTag(Byte.MIN_VALUE);

            assertEquals(Byte.MIN_VALUE, tag.value());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) 77);

            assertEquals((byte) 77, property.readValue(tag));
        }

        @Test
        @DisplayName("should read zero value")
        void shouldReadZeroValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 1);
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) 0);

            assertEquals((byte) 0, property.readValue(tag));
        }

        @Test
        @DisplayName("should read negative value")
        void shouldReadNegativeValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) -123);

            assertEquals((byte) -123, property.readValue(tag));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid values")
        void shouldPassValidation() {
            SlimePropertyByte property = SlimePropertyByte.create("level", (byte) 1,
                value -> value >= 1 && value <= 10);

            assertTrue(property.applyValidator((byte) 5));
        }

        @Test
        @DisplayName("should fail validation for invalid values")
        void shouldFailValidation() {
            SlimePropertyByte property = SlimePropertyByte.create("level", (byte) 1,
                value -> value >= 1 && value <= 10);

            assertFalse(property.applyValidator((byte) 0));
            assertFalse(property.applyValidator((byte) 11));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyByte property = SlimePropertyByte.create("any", (byte) 0);

            assertTrue(property.applyValidator(Byte.MIN_VALUE));
            assertTrue(property.applyValidator(Byte.MAX_VALUE));
        }

        @Test
        @DisplayName("should validate light level range")
        void shouldValidateLightLevelRange() {
            SlimePropertyByte property = SlimePropertyByte.create("light", (byte) 15,
                value -> value >= 0 && value <= 15);

            assertTrue(property.applyValidator((byte) 0));
            assertTrue(property.applyValidator((byte) 15));
            assertFalse(property.applyValidator((byte) 16));
            assertFalse(property.applyValidator((byte) -1));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyByte property = SlimePropertyByte.create("lightLevel", (byte) 0);
            assertEquals("lightLevel", property.getKey());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyByte property = SlimePropertyByte.create("myKey", (byte) 10);
            assertTrue(property.toString().contains("myKey"));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            byte original = (byte) 123;

            ByteBinaryTag tag = property.createTag(original);
            Byte result = property.readValue(tag);

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip negative value correctly")
        void shouldRoundtripNegativeValue() {
            SlimePropertyByte property = SlimePropertyByte.create("test", (byte) 0);
            byte original = (byte) -98;

            ByteBinaryTag tag = property.createTag(original);
            Byte result = property.readValue(tag);

            assertEquals(original, result);
        }
    }
}
