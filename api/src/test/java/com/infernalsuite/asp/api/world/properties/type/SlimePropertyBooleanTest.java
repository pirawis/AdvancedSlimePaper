package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.ByteBinaryTag;
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

        @Test
        @DisplayName("should throw on null validator")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullValidator() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyBoolean.create("key", true, null));
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create tag with value 1 for true")
        void shouldCreateTagForTrue() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", false);
            ByteBinaryTag tag = property.createTag(true);

            assertEquals((byte) 1, tag.value());
        }

        @Test
        @DisplayName("should create tag with value 0 for false")
        void shouldCreateTagForFalse() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", true);
            ByteBinaryTag tag = property.createTag(false);

            assertEquals((byte) 0, tag.value());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read true from tag with value 1")
        void shouldReadTrueFromTag() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", false);
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) 1);

            assertTrue(property.readValue(tag));
        }

        @Test
        @DisplayName("should read false from tag with value 0")
        void shouldReadFalseFromTag() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", true);
            ByteBinaryTag tag = ByteBinaryTag.byteBinaryTag((byte) 0);

            assertFalse(property.readValue(tag));
        }

        @Test
        @DisplayName("should read false from non-one values")
        void shouldReadFalseFromNonOneValues() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", false);

            // Implementation only considers value == 1 as true
            assertFalse(property.readValue(ByteBinaryTag.byteBinaryTag((byte) 2)));
            assertFalse(property.readValue(ByteBinaryTag.byteBinaryTag((byte) 127)));
            assertFalse(property.readValue(ByteBinaryTag.byteBinaryTag((byte) -1)));
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

        @Test
        @DisplayName("should get validator function")
        void shouldGetValidatorFunction() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", false,
                value -> !value);

            assertNotNull(property.getValidator());
            assertTrue(property.getValidator().apply(false));
            assertFalse(property.getValidator().apply(true));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("allowPvp", true);
            assertEquals("allowPvp", property.getKey());
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

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip true correctly")
        void shouldRoundtripTrue() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", false);

            ByteBinaryTag tag = property.createTag(true);
            Boolean result = property.readValue(tag);

            assertTrue(result);
        }

        @Test
        @DisplayName("should roundtrip false correctly")
        void shouldRoundtripFalse() {
            SlimePropertyBoolean property = SlimePropertyBoolean.create("test", true);

            ByteBinaryTag tag = property.createTag(false);
            Boolean result = property.readValue(tag);

            assertFalse(result);
        }
    }
}
