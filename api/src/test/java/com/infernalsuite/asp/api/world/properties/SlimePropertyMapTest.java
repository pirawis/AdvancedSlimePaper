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
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create empty map with default constructor")
        void shouldCreateEmptyMap() {
            SlimePropertyMap map = new SlimePropertyMap();
            assertNotNull(map.getProperties());
            assertTrue(map.getProperties().isEmpty());
        }

        @Test
        @DisplayName("should create map with provided properties")
        void shouldCreateMapWithProperties() {
            Map<String, BinaryTag> props = new HashMap<>();
            props.put("test", IntBinaryTag.intBinaryTag(42));

            SlimePropertyMap map = new SlimePropertyMap(props);

            assertEquals(1, map.getProperties().size());
            assertEquals(IntBinaryTag.intBinaryTag(42), map.getProperties().get("test"));
        }
    }

    @Nested
    @DisplayName("getProperties")
    class GetPropertiesTests {

        @Test
        @DisplayName("should return mutable map")
        void shouldReturnMutableMap() {
            Map<String, BinaryTag> props = propertyMap.getProperties();
            props.put("key", StringBinaryTag.stringBinaryTag("value"));

            assertEquals(1, propertyMap.getProperties().size());
        }
    }

    @Nested
    @DisplayName("merge")
    class MergeTests {

        @Test
        @DisplayName("should merge properties from another map")
        void shouldMergeProperties() {
            propertyMap.getProperties().put("key1", IntBinaryTag.intBinaryTag(1));

            SlimePropertyMap other = new SlimePropertyMap();
            other.getProperties().put("key2", IntBinaryTag.intBinaryTag(2));

            propertyMap.merge(other);

            assertEquals(2, propertyMap.getProperties().size());
            assertEquals(IntBinaryTag.intBinaryTag(1), propertyMap.getProperties().get("key1"));
            assertEquals(IntBinaryTag.intBinaryTag(2), propertyMap.getProperties().get("key2"));
        }

        @Test
        @DisplayName("should override existing keys when merging")
        void shouldOverrideExistingKeys() {
            propertyMap.getProperties().put("key", IntBinaryTag.intBinaryTag(1));

            SlimePropertyMap other = new SlimePropertyMap();
            other.getProperties().put("key", IntBinaryTag.intBinaryTag(2));

            propertyMap.merge(other);

            assertEquals(IntBinaryTag.intBinaryTag(2), propertyMap.getProperties().get("key"));
        }
    }

    @Nested
    @DisplayName("clone")
    class CloneTests {

        @Test
        @DisplayName("should create independent copy")
        void shouldCreateIndependentCopy() {
            propertyMap.getProperties().put("key", IntBinaryTag.intBinaryTag(1));

            SlimePropertyMap cloned = propertyMap.clone();
            cloned.getProperties().put("key", IntBinaryTag.intBinaryTag(2));

            assertEquals(IntBinaryTag.intBinaryTag(1), propertyMap.getProperties().get("key"));
            assertEquals(IntBinaryTag.intBinaryTag(2), cloned.getProperties().get("key"));
        }

        @Test
        @DisplayName("should copy all properties")
        void shouldCopyAllProperties() {
            propertyMap.getProperties().put("a", IntBinaryTag.intBinaryTag(1));
            propertyMap.getProperties().put("b", IntBinaryTag.intBinaryTag(2));

            SlimePropertyMap cloned = propertyMap.clone();

            assertEquals(2, cloned.getProperties().size());
        }
    }

    @Nested
    @DisplayName("toCompound / fromCompound")
    class CompoundTests {

        @Test
        @DisplayName("should convert to CompoundBinaryTag")
        void shouldConvertToCompound() {
            propertyMap.getProperties().put("test", IntBinaryTag.intBinaryTag(123));

            CompoundBinaryTag compound = propertyMap.toCompound();

            assertEquals(IntBinaryTag.intBinaryTag(123), compound.get("test"));
        }

        @Test
        @DisplayName("should create from CompoundBinaryTag")
        void shouldCreateFromCompound() {
            CompoundBinaryTag compound = CompoundBinaryTag.builder()
                .putInt("value", 456)
                .build();

            SlimePropertyMap map = SlimePropertyMap.fromCompound(compound);

            assertEquals(IntBinaryTag.intBinaryTag(456), map.getProperties().get("value"));
        }

        @Test
        @DisplayName("should roundtrip through compound")
        void shouldRoundtripThroughCompound() {
            propertyMap.getProperties().put("x", IntBinaryTag.intBinaryTag(100));
            propertyMap.getProperties().put("y", StringBinaryTag.stringBinaryTag("hello"));

            CompoundBinaryTag compound = propertyMap.toCompound();
            SlimePropertyMap restored = SlimePropertyMap.fromCompound(compound);

            assertEquals(propertyMap.getProperties().size(), restored.getProperties().size());
            assertEquals(propertyMap.getProperties().get("x"), restored.getProperties().get("x"));
            assertEquals(propertyMap.getProperties().get("y"), restored.getProperties().get("y"));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should return readable string")
        void shouldReturnReadableString() {
            propertyMap.getProperties().put("test", IntBinaryTag.intBinaryTag(1));

            String result = propertyMap.toString();

            assertTrue(result.contains("SlimePropertyMap"));
            assertTrue(result.contains("test"));
        }
    }
}
