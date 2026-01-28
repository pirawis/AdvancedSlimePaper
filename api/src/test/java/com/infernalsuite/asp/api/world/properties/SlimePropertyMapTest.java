package com.infernalsuite.asp.api.world.properties;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimePropertyMap")
class SlimePropertyMapTest {

    private SlimePropertyMap propertyMap;

    @BeforeEach
    void setUp() {
        propertyMap = new SlimePropertyMap();
    }

    @Nested
    @DisplayName("constructors")
    class ConstructorTests {

        @Test
        @DisplayName("should create empty map with default constructor")
        void shouldCreateEmptyMapWithDefaultConstructor() {
            SlimePropertyMap map = new SlimePropertyMap();

            assertTrue(map.getProperties().isEmpty());
        }

        @Test
        @DisplayName("should create map with provided properties")
        void shouldCreateMapWithProvidedProperties() {
            Map<String, BinaryTag> props = new HashMap<>();
            props.put("test", IntBinaryTag.intBinaryTag(42));

            SlimePropertyMap map = new SlimePropertyMap(props);

            assertEquals(1, map.getProperties().size());
            assertEquals(IntBinaryTag.intBinaryTag(42), map.getProperties().get("test"));
        }
    }

    @Nested
    @DisplayName("getValue and setValue")
    class GetSetValueTests {

        @Test
        @DisplayName("should return default value when property not set")
        void shouldReturnDefaultValueWhenPropertyNotSet() {
            Integer result = propertyMap.getValue(SlimeProperties.SPAWN_X);

            assertEquals(SlimeProperties.SPAWN_X.getDefaultValue(), result);
        }

        @Test
        @DisplayName("should return set value when property is set")
        void shouldReturnSetValueWhenPropertyIsSet() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 100);

            Integer result = propertyMap.getValue(SlimeProperties.SPAWN_X);

            assertEquals(100, result);
        }

        @Test
        @DisplayName("should throw when setting invalid value")
        void shouldThrowWhenSettingInvalidValue() {
            assertThrows(IllegalArgumentException.class, () ->
                    propertyMap.setValue(SlimeProperties.DIFFICULTY, "invalid"));
        }

        @Test
        @DisplayName("should accept valid difficulty value")
        void shouldAcceptValidDifficultyValue() {
            assertDoesNotThrow(() ->
                    propertyMap.setValue(SlimeProperties.DIFFICULTY, "normal"));

            assertEquals("normal", propertyMap.getValue(SlimeProperties.DIFFICULTY));
        }

        @Test
        @DisplayName("should set and get boolean property")
        void shouldSetAndGetBooleanProperty() {
            propertyMap.setValue(SlimeProperties.ALLOW_MONSTERS, false);

            assertFalse(propertyMap.getValue(SlimeProperties.ALLOW_MONSTERS));
        }

        @Test
        @DisplayName("should set and get float property")
        void shouldSetAndGetFloatProperty() {
            propertyMap.setValue(SlimeProperties.SPAWN_YAW, 90.5f);

            assertEquals(90.5f, propertyMap.getValue(SlimeProperties.SPAWN_YAW), 0.01f);
        }
    }

    @Nested
    @DisplayName("merge")
    class MergeTests {

        @Test
        @DisplayName("should merge properties from other map")
        void shouldMergePropertiesFromOtherMap() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 10);

            SlimePropertyMap other = new SlimePropertyMap();
            other.setValue(SlimeProperties.SPAWN_Y, 20);

            propertyMap.merge(other);

            assertEquals(10, propertyMap.getValue(SlimeProperties.SPAWN_X));
            assertEquals(20, propertyMap.getValue(SlimeProperties.SPAWN_Y));
        }

        @Test
        @DisplayName("should override existing properties when merging")
        void shouldOverrideExistingPropertiesWhenMerging() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 10);

            SlimePropertyMap other = new SlimePropertyMap();
            other.setValue(SlimeProperties.SPAWN_X, 99);

            propertyMap.merge(other);

            assertEquals(99, propertyMap.getValue(SlimeProperties.SPAWN_X));
        }
    }

    @Nested
    @DisplayName("toCompound and fromCompound")
    class CompoundTests {

        @Test
        @DisplayName("should convert to compound tag")
        void shouldConvertToCompoundTag() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 50);

            CompoundBinaryTag compound = propertyMap.toCompound();

            assertNotNull(compound);
            assertTrue(compound.keySet().contains(SlimeProperties.SPAWN_X.getKey()));
        }

        @Test
        @DisplayName("should create map from compound tag")
        void shouldCreateMapFromCompoundTag() {
            CompoundBinaryTag compound = CompoundBinaryTag.builder()
                    .putString("test.key", "testvalue")
                    .build();

            SlimePropertyMap map = SlimePropertyMap.fromCompound(compound);

            assertEquals(1, map.getProperties().size());
            assertEquals(StringBinaryTag.stringBinaryTag("testvalue"), map.getProperties().get("test.key"));
        }

        @Test
        @DisplayName("should handle empty compound tag")
        void shouldHandleEmptyCompoundTag() {
            SlimePropertyMap map = SlimePropertyMap.fromCompound(CompoundBinaryTag.empty());

            assertTrue(map.getProperties().isEmpty());
        }
    }

    @Nested
    @DisplayName("clone")
    class CloneTests {

        @Test
        @DisplayName("should create independent copy")
        void shouldCreateIndependentCopy() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 100);

            SlimePropertyMap cloned = propertyMap.clone();
            cloned.setValue(SlimeProperties.SPAWN_X, 200);

            assertEquals(100, propertyMap.getValue(SlimeProperties.SPAWN_X));
            assertEquals(200, cloned.getValue(SlimeProperties.SPAWN_X));
        }

        @Test
        @DisplayName("should copy all properties")
        void shouldCopyAllProperties() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 10);
            propertyMap.setValue(SlimeProperties.SPAWN_Y, 20);

            SlimePropertyMap cloned = propertyMap.clone();

            assertEquals(10, cloned.getValue(SlimeProperties.SPAWN_X));
            assertEquals(20, cloned.getValue(SlimeProperties.SPAWN_Y));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include class name")
        void shouldIncludeClassName() {
            assertTrue(propertyMap.toString().contains("SlimePropertyMap"));
        }

        @Test
        @DisplayName("should include properties")
        void shouldIncludeProperties() {
            propertyMap.setValue(SlimeProperties.SPAWN_X, 42);

            String str = propertyMap.toString();

            assertTrue(str.contains(SlimeProperties.SPAWN_X.getKey()));
        }
    }

    @Nested
    @DisplayName("getProperties")
    class GetPropertiesTests {

        @Test
        @DisplayName("should return mutable map")
        void shouldReturnMutableMap() {
            Map<String, BinaryTag> props = propertyMap.getProperties();

            props.put("direct.key", IntBinaryTag.intBinaryTag(999));

            assertEquals(IntBinaryTag.intBinaryTag(999), propertyMap.getProperties().get("direct.key"));
        }
    }
}
