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
