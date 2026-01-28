package com.infernalsuite.asp.plugin.commands.parser;

import com.infernalsuite.asp.api.loaders.SlimeLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NamedSlimeLoader")
@ExtendWith(MockitoExtension.class)
class NamedSlimeLoaderTest {

    @Mock
    private SlimeLoader mockLoader;

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorTests {

        @Test
        @DisplayName("should return name")
        void shouldReturnName() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader("file", mockLoader);
            assertEquals("file", namedLoader.name());
        }

        @Test
        @DisplayName("should return slimeLoader")
        void shouldReturnSlimeLoader() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader("mysql", mockLoader);
            assertSame(mockLoader, namedLoader.slimeLoader());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should return name as string")
        void shouldReturnNameAsString() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader("redis", mockLoader);
            assertEquals("redis", namedLoader.toString());
        }

        @Test
        @DisplayName("should handle special characters in name")
        void shouldHandleSpecialCharactersInName() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader("my-loader_v2", mockLoader);
            assertEquals("my-loader_v2", namedLoader.toString());
        }

        @Test
        @DisplayName("should handle empty name")
        void shouldHandleEmptyName() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader("", mockLoader);
            assertEquals("", namedLoader.toString());
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("should be equal with same name and loader")
        void shouldBeEqualWithSameNameAndLoader() {
            NamedSlimeLoader loader1 = new NamedSlimeLoader("file", mockLoader);
            NamedSlimeLoader loader2 = new NamedSlimeLoader("file", mockLoader);

            assertEquals(loader1, loader2);
            assertEquals(loader1.hashCode(), loader2.hashCode());
        }

        @Test
        @DisplayName("should not be equal with different name")
        void shouldNotBeEqualWithDifferentName() {
            NamedSlimeLoader loader1 = new NamedSlimeLoader("file", mockLoader);
            NamedSlimeLoader loader2 = new NamedSlimeLoader("mysql", mockLoader);

            assertNotEquals(loader1, loader2);
        }

        @Test
        @DisplayName("should not be equal with different loader")
        void shouldNotBeEqualWithDifferentLoader(@Mock SlimeLoader otherLoader) {
            NamedSlimeLoader loader1 = new NamedSlimeLoader("file", mockLoader);
            NamedSlimeLoader loader2 = new NamedSlimeLoader("file", otherLoader);

            assertNotEquals(loader1, loader2);
        }
    }

    @Nested
    @DisplayName("null handling")
    class NullHandlingTests {

        @Test
        @DisplayName("should handle null loader")
        void shouldHandleNullLoader() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader("test", null);

            assertEquals("test", namedLoader.name());
            assertNull(namedLoader.slimeLoader());
        }

        @Test
        @DisplayName("should handle null name")
        void shouldHandleNullName() {
            NamedSlimeLoader namedLoader = new NamedSlimeLoader(null, mockLoader);

            assertNull(namedLoader.name());
            assertNull(namedLoader.toString());
        }
    }
}
