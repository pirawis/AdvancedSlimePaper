package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyString")
class SlimePropertyStringTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with key and default value")
        void shouldCreateWithKeyAndDefault() {
            SlimePropertyString property = SlimePropertyString.create("world.name", "default");

            assertEquals("world.name", property.getKey());
            assertEquals("default", property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with empty default")
        void shouldCreateWithEmptyDefault() {
            SlimePropertyString property = SlimePropertyString.create("empty", "");

            assertEquals("", property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            SlimePropertyString property = SlimePropertyString.create("difficulty", "normal",
                value -> value.equals("easy") || value.equals("normal") || value.equals("hard"));

            assertEquals("difficulty", property.getKey());
            assertEquals("normal", property.getDefaultValue());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyString.create(null, "value"));
        }

        @Test
        @DisplayName("should throw on null validator")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullValidator() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyString.create("key", "value", null));
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create StringBinaryTag from value")
        void shouldCreateTag() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            StringBinaryTag tag = property.createTag("hello world");

            assertEquals("hello world", tag.value());
        }

        @Test
        @DisplayName("should handle empty string")
        void shouldHandleEmptyString() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            StringBinaryTag tag = property.createTag("");

            assertEquals("", tag.value());
        }

        @Test
        @DisplayName("should handle special characters")
        void shouldHandleSpecialCharacters() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            StringBinaryTag tag = property.createTag("Hello\nWorld\t!");

            assertEquals("Hello\nWorld\t!", tag.value());
        }

        @Test
        @DisplayName("should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            StringBinaryTag tag = property.createTag("こんにちは世界");

            assertEquals("こんにちは世界", tag.value());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read value from tag")
        void shouldReadValue() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            StringBinaryTag tag = StringBinaryTag.stringBinaryTag("test value");

            assertEquals("test value", property.readValue(tag));
        }

        @Test
        @DisplayName("should read empty value")
        void shouldReadEmptyValue() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            StringBinaryTag tag = StringBinaryTag.stringBinaryTag("");

            assertEquals("", property.readValue(tag));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation when no validator")
        void shouldPassValidationWhenNoValidator() {
            SlimePropertyString property = SlimePropertyString.create("any", "");

            assertTrue(property.applyValidator("anything"));
            assertTrue(property.applyValidator(""));
            assertTrue(property.applyValidator("12345"));
        }

        @Test
        @DisplayName("should apply difficulty validator")
        void shouldApplyDifficultyValidator() {
            SlimePropertyString property = SlimePropertyString.create("difficulty", "normal",
                value -> value.equalsIgnoreCase("peaceful") ||
                        value.equalsIgnoreCase("easy") ||
                        value.equalsIgnoreCase("normal") ||
                        value.equalsIgnoreCase("hard"));

            assertTrue(property.applyValidator("peaceful"));
            assertTrue(property.applyValidator("EASY"));
            assertTrue(property.applyValidator("Normal"));
            assertTrue(property.applyValidator("hard"));
            assertFalse(property.applyValidator("impossible"));
            assertFalse(property.applyValidator(""));
        }

        @Test
        @DisplayName("should apply environment validator")
        void shouldApplyEnvironmentValidator() {
            SlimePropertyString property = SlimePropertyString.create("environment", "normal",
                value -> value.equalsIgnoreCase("normal") ||
                        value.equalsIgnoreCase("nether") ||
                        value.equalsIgnoreCase("the_end"));

            assertTrue(property.applyValidator("normal"));
            assertTrue(property.applyValidator("nether"));
            assertTrue(property.applyValidator("the_end"));
            assertFalse(property.applyValidator("void"));
        }

        @Test
        @DisplayName("should get validator function")
        void shouldGetValidatorFunction() {
            SlimePropertyString property = SlimePropertyString.create("test", "default",
                value -> value.length() > 3);

            assertNotNull(property.getValidator());
            assertTrue(property.getValidator().apply("long"));
            assertFalse(property.getValidator().apply("ab"));
        }
    }

    @Nested
    @DisplayName("getNbtName")
    class GetNbtNameTests {

        @Test
        @DisplayName("should return key as nbt name")
        void shouldReturnKeyAsNbtName() {
            SlimePropertyString property = SlimePropertyString.create("myNbtKey", "value");
            assertEquals("myNbtKey", property.getKey());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyString property = SlimePropertyString.create("myKey", "value");
            assertTrue(property.toString().contains("myKey"));
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip value correctly")
        void shouldRoundtripValue() {
            SlimePropertyString property = SlimePropertyString.create("test", "");
            String original = "test roundtrip value";

            StringBinaryTag tag = property.createTag(original);
            String result = property.readValue(tag);

            assertEquals(original, result);
        }
    }
}
