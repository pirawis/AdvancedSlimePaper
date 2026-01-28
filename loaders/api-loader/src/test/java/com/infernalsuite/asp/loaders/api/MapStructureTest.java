package com.infernalsuite.asp.loaders.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MapStructure")
class MapStructureTest {

    private MapStructure mapStructure;

    @BeforeEach
    void setUp() {
        mapStructure = new MapStructure();
    }

    @Nested
    @DisplayName("worldId")
    class WorldIdTests {

        @Test
        @DisplayName("should set and get worldId")
        void shouldSetAndGetWorldId() {
            mapStructure.setWorldId("abc123");
            assertEquals("abc123", mapStructure.getWorldId());
        }

        @Test
        @DisplayName("should handle null worldId")
        void shouldHandleNullWorldId() {
            mapStructure.setWorldId(null);
            assertNull(mapStructure.getWorldId());
        }
    }

    @Nested
    @DisplayName("name")
    class NameTests {

        @Test
        @DisplayName("should set and get name")
        void shouldSetAndGetName() {
            mapStructure.setName("lobby.slime");
            assertEquals("lobby.slime", mapStructure.getName());
        }
    }

    @Nested
    @DisplayName("size")
    class SizeTests {

        @Test
        @DisplayName("should set and get size")
        void shouldSetAndGetSize() {
            mapStructure.setSize(1024);
            assertEquals(1024, mapStructure.getSize());
        }

        @Test
        @DisplayName("should handle zero size")
        void shouldHandleZeroSize() {
            mapStructure.setSize(0);
            assertEquals(0, mapStructure.getSize());
        }

        @Test
        @DisplayName("should handle large size")
        void shouldHandleLargeSize() {
            mapStructure.setSize(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, mapStructure.getSize());
        }
    }

    @Nested
    @DisplayName("timestamps")
    class TimestampTests {

        @Test
        @DisplayName("should set and get uploadTimestamp")
        void shouldSetAndGetUploadTimestamp() {
            long timestamp = System.currentTimeMillis();
            mapStructure.setUploadTimestamp(timestamp);
            assertEquals(timestamp, mapStructure.getUploadTimestamp());
        }

        @Test
        @DisplayName("should set and get updateTimestamp")
        void shouldSetAndGetUpdateTimestamp() {
            long timestamp = System.currentTimeMillis();
            mapStructure.setUpdateTimestamp(timestamp);
            assertEquals(timestamp, mapStructure.getUpdateTimestamp());
        }
    }

    @Nested
    @DisplayName("version")
    class VersionTests {

        @Test
        @DisplayName("should set and get version")
        void shouldSetAndGetVersion() {
            mapStructure.setVersion(13);
            assertEquals(13, mapStructure.getVersion());
        }
    }

    @Nested
    @DisplayName("notes")
    class NotesTests {

        @Test
        @DisplayName("should set and get notes")
        void shouldSetAndGetNotes() {
            Map<String, String> notes = new HashMap<>();
            notes.put("description", "Test map");
            notes.put("author", "Admin");

            mapStructure.setNotes(notes);

            assertEquals(notes, mapStructure.getNotes());
            assertEquals("Test map", mapStructure.getNotes().get("description"));
        }

        @Test
        @DisplayName("should handle null notes")
        void shouldHandleNullNotes() {
            mapStructure.setNotes(null);
            assertNull(mapStructure.getNotes());
        }
    }

    @Nested
    @DisplayName("authors")
    class AuthorsTests {

        @Test
        @DisplayName("should set and get authors")
        void shouldSetAndGetAuthors() {
            List<String> authors = List.of("Author1", "Author2");

            mapStructure.setAuthors(authors);

            assertEquals(authors, mapStructure.getAuthors());
            assertEquals(2, mapStructure.getAuthors().size());
        }

        @Test
        @DisplayName("should handle empty authors list")
        void shouldHandleEmptyAuthorsList() {
            mapStructure.setAuthors(List.of());
            assertTrue(mapStructure.getAuthors().isEmpty());
        }
    }

    @Nested
    @DisplayName("pictureUrls")
    class PictureUrlsTests {

        @Test
        @DisplayName("should set and get pictureUrls")
        void shouldSetAndGetPictureUrls() {
            Map<String, String> urls = new HashMap<>();
            urls.put("thumbnail", "https://example.com/thumb.png");
            urls.put("full", "https://example.com/full.png");

            mapStructure.setPictureUrls(urls);

            assertEquals(urls, mapStructure.getPictureUrls());
        }
    }

    @Nested
    @DisplayName("full object")
    class FullObjectTests {

        @Test
        @DisplayName("should handle all properties set")
        void shouldHandleAllPropertiesSet() {
            mapStructure.setWorldId("world-123");
            mapStructure.setName("arena.slime");
            mapStructure.setSize(2048);
            mapStructure.setUploadTimestamp(1000L);
            mapStructure.setUpdateTimestamp(2000L);
            mapStructure.setVersion(13);
            mapStructure.setNotes(Map.of("key", "value"));
            mapStructure.setAuthors(List.of("Author"));
            mapStructure.setPictureUrls(Map.of("url", "https://test.com"));

            assertEquals("world-123", mapStructure.getWorldId());
            assertEquals("arena.slime", mapStructure.getName());
            assertEquals(2048, mapStructure.getSize());
            assertEquals(1000L, mapStructure.getUploadTimestamp());
            assertEquals(2000L, mapStructure.getUpdateTimestamp());
            assertEquals(13, mapStructure.getVersion());
            assertNotNull(mapStructure.getNotes());
            assertNotNull(mapStructure.getAuthors());
            assertNotNull(mapStructure.getPictureUrls());
        }
    }
}
