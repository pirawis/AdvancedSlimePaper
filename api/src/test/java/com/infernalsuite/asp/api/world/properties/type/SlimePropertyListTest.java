package com.infernalsuite.asp.api.world.properties.type;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyList")
class SlimePropertyListTest {

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create property with string list")
        void shouldCreateWithStringList() {
            List<String> defaultValue = Arrays.asList("a", "b", "c");
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "strings",
                defaultValue,
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            assertEquals("strings", property.getKey());
            assertEquals(defaultValue, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with integer list")
        void shouldCreateWithIntegerList() {
            List<Integer> defaultValue = Arrays.asList(1, 2, 3);
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "numbers",
                defaultValue,
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );

            assertEquals("numbers", property.getKey());
            assertEquals(defaultValue, property.getDefaultValue());
        }

        @Test
        @DisplayName("should create property with empty list")
        void shouldCreateWithEmptyList() {
            List<String> defaultValue = Collections.emptyList();
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "empty",
                defaultValue,
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            assertEquals("empty", property.getKey());
            assertTrue(property.getDefaultValue().isEmpty());
        }

        @Test
        @DisplayName("should create property with validator")
        void shouldCreateWithValidator() {
            List<Integer> defaultValue = Arrays.asList(1, 2, 3);
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "limited",
                defaultValue,
                list -> list.size() <= 5,
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );

            assertEquals("limited", property.getKey());
            assertNotNull(property.getValidator());
        }

        @Test
        @DisplayName("should throw on null key")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullKey() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyList.create(
                    null,
                    Collections.emptyList(),
                    BinaryTagTypes.STRING,
                    StringBinaryTag::stringBinaryTag,
                    StringBinaryTag::value
                ));
        }

        @Test
        @DisplayName("should throw on null default value")
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowOnNullDefaultValue() {
            assertThrows(NullPointerException.class, () ->
                SlimePropertyList.create(
                    "test",
                    null,
                    BinaryTagTypes.STRING,
                    StringBinaryTag::stringBinaryTag,
                    StringBinaryTag::value
                ));
        }
    }

    @Nested
    @DisplayName("validator")
    class ValidatorTests {

        @Test
        @DisplayName("should pass validation for valid list")
        void shouldPassValidation() {
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "limited",
                Collections.emptyList(),
                list -> list.size() <= 3,
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );

            assertTrue(property.applyValidator(Arrays.asList(1, 2)));
        }

        @Test
        @DisplayName("should fail validation for invalid list")
        void shouldFailValidation() {
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "limited",
                Collections.emptyList(),
                list -> list.size() <= 3,
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );

            assertFalse(property.applyValidator(Arrays.asList(1, 2, 3, 4)));
        }

        @Test
        @DisplayName("should always pass without validator")
        void shouldAlwaysPassWithoutValidator() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "any",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            assertTrue(property.applyValidator(Collections.emptyList()));
            assertTrue(property.applyValidator(Arrays.asList("a", "b", "c", "d", "e")));
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("should create ListBinaryTag from string list")
        void shouldCreateTagFromStringList() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );
            var tag = property.createTag(Arrays.asList("apple", "banana", "cherry"));

            assertEquals(3, tag.size());
        }

        @Test
        @DisplayName("should create ListBinaryTag from integer list")
        void shouldCreateTagFromIntegerList() {
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );
            var tag = property.createTag(Arrays.asList(1, 2, 3, 4, 5));

            assertEquals(5, tag.size());
        }

        @Test
        @DisplayName("should create empty ListBinaryTag")
        void shouldCreateEmptyTag() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );
            var tag = property.createTag(Collections.emptyList());

            assertEquals(0, tag.size());
        }
    }

    @Nested
    @DisplayName("readValue")
    class ReadValueTests {

        @Test
        @DisplayName("should read string list from tag")
        void shouldReadStringList() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            var tag = property.createTag(Arrays.asList("one", "two", "three"));
            List<String> result = property.readValue(tag);

            assertEquals(3, result.size());
            assertEquals("one", result.get(0));
            assertEquals("two", result.get(1));
            assertEquals("three", result.get(2));
        }

        @Test
        @DisplayName("should read integer list from tag")
        void shouldReadIntegerList() {
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );

            var tag = property.createTag(Arrays.asList(100, 200, 300));
            List<Integer> result = property.readValue(tag);

            assertEquals(3, result.size());
            assertEquals(100, result.get(0));
            assertEquals(200, result.get(1));
            assertEquals(300, result.get(2));
        }

        @Test
        @DisplayName("should read empty list from tag")
        void shouldReadEmptyList() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            var tag = property.createTag(Collections.emptyList());
            List<String> result = property.readValue(tag);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class RoundtripTests {

        @Test
        @DisplayName("should roundtrip string list correctly")
        void shouldRoundtripStringList() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            List<String> original = Arrays.asList("hello", "world", "test");
            var tag = property.createTag(original);
            List<String> result = property.readValue(tag);

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip integer list correctly")
        void shouldRoundtripIntegerList() {
            SlimePropertyList<Integer, IntBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag,
                IntBinaryTag::value
            );

            List<Integer> original = Arrays.asList(-100, 0, 100, Integer.MAX_VALUE);
            var tag = property.createTag(original);
            List<Integer> result = property.readValue(tag);

            assertEquals(original, result);
        }

        @Test
        @DisplayName("should roundtrip empty list correctly")
        void shouldRoundtripEmptyList() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            List<String> original = Collections.emptyList();
            var tag = property.createTag(original);
            List<String> result = property.readValue(tag);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should roundtrip single element list correctly")
        void shouldRoundtripSingleElement() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "test",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );

            List<String> original = Collections.singletonList("single");
            var tag = property.createTag(original);
            List<String> result = property.readValue(tag);

            assertEquals(original, result);
        }
    }

    @Nested
    @DisplayName("getKey")
    class GetKeyTests {

        @Test
        @DisplayName("should return key")
        void shouldReturnKey() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "authorList",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );
            assertEquals("authorList", property.getKey());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain key in toString")
        void shouldContainKey() {
            SlimePropertyList<String, StringBinaryTag> property = SlimePropertyList.create(
                "myKey",
                Collections.emptyList(),
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag,
                StringBinaryTag::value
            );
            assertTrue(property.toString().contains("myKey"));
        }
    }
}
