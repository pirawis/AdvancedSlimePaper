package com.infernalsuite.asp.plugin.commands.parser;

import com.infernalsuite.asp.plugin.config.WorldData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NamedWorldData")
class NamedWorldDataTest {

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should return name")
        void shouldReturnName() {
            WorldData worldData = new WorldData();
            NamedWorldData namedData = new NamedWorldData("lobby", worldData);

            assertEquals("lobby", namedData.name());
        }

        @Test
        @DisplayName("should return worldData")
        void shouldReturnWorldData() {
            WorldData worldData = new WorldData();
            NamedWorldData namedData = new NamedWorldData("arena", worldData);

            assertSame(worldData, namedData.worldData());
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("should be equal with same name and worldData instance")
        void shouldBeEqualWithSameNameAndWorldDataInstance() {
            WorldData worldData = new WorldData();
            NamedWorldData data1 = new NamedWorldData("world", worldData);
            NamedWorldData data2 = new NamedWorldData("world", worldData);

            assertEquals(data1, data2);
            assertEquals(data1.hashCode(), data2.hashCode());
        }

        @Test
        @DisplayName("should not be equal with different name")
        void shouldNotBeEqualWithDifferentName() {
            WorldData worldData = new WorldData();
            NamedWorldData data1 = new NamedWorldData("world1", worldData);
            NamedWorldData data2 = new NamedWorldData("world2", worldData);

            assertNotEquals(data1, data2);
        }

        @Test
        @DisplayName("should not be equal with different worldData instance")
        void shouldNotBeEqualWithDifferentWorldDataInstance() {
            NamedWorldData data1 = new NamedWorldData("world", new WorldData());
            NamedWorldData data2 = new NamedWorldData("world", new WorldData());

            assertNotEquals(data1, data2);
        }
    }

    @Nested
    @DisplayName("null handling")
    class NullHandlingTests {

        @Test
        @DisplayName("should handle null worldData")
        void shouldHandleNullWorldData() {
            NamedWorldData namedData = new NamedWorldData("test", null);

            assertEquals("test", namedData.name());
            assertNull(namedData.worldData());
        }

        @Test
        @DisplayName("should handle null name")
        void shouldHandleNullName() {
            WorldData worldData = new WorldData();
            NamedWorldData namedData = new NamedWorldData(null, worldData);

            assertNull(namedData.name());
            assertSame(worldData, namedData.worldData());
        }

        @Test
        @DisplayName("should handle both null")
        void shouldHandleBothNull() {
            NamedWorldData namedData = new NamedWorldData(null, null);

            assertNull(namedData.name());
            assertNull(namedData.worldData());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should contain record name")
        void shouldContainRecordName() {
            WorldData worldData = new WorldData();
            NamedWorldData namedData = new NamedWorldData("myworld", worldData);

            String str = namedData.toString();
            assertTrue(str.contains("NamedWorldData"));
            assertTrue(str.contains("myworld"));
        }
    }

    @Nested
    @DisplayName("with worldData properties")
    class WithWorldDataPropertiesTests {

        @Test
        @DisplayName("should preserve worldData properties")
        void shouldPreserveWorldDataProperties() {
            WorldData worldData = new WorldData();
            worldData.setDifficulty("hard");
            worldData.setSpawn("100, 64, 200");

            NamedWorldData namedData = new NamedWorldData("spawn", worldData);

            assertEquals("hard", namedData.worldData().getDifficulty());
            assertEquals("100, 64, 200", namedData.worldData().getSpawn());
        }

        @Test
        @DisplayName("should allow modifying worldData through accessor")
        void shouldAllowModifyingWorldDataThroughAccessor() {
            WorldData worldData = new WorldData();
            NamedWorldData namedData = new NamedWorldData("test", worldData);

            namedData.worldData().setPvp(false);

            assertFalse(worldData.isPvp());
        }
    }
}
