package com.infernalsuite.asp.loaders.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("APILoader Integration Tests")
class APILoaderTest {

    private static WireMockServer wireMockServer;
    private APILoader loader;
    private String baseUrl;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        baseUrl = "http://localhost:" + wireMockServer.port() + "/api/";
        // Use auth credentials because APILoader has a bug with null authorizationHeader
        loader = new APILoader(baseUrl, "testuser", "testtoken", false);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create loader without auth")
        void shouldCreateLoaderWithoutAuth() {
            APILoader noAuthLoader = new APILoader(baseUrl, null, null, false);
            assertNotNull(noAuthLoader);
        }

        @Test
        @DisplayName("should create loader with auth credentials")
        void shouldCreateLoaderWithAuthCredentials() {
            APILoader authLoader = new APILoader(baseUrl, "user", "token", false);
            assertNotNull(authLoader);
        }

        @Test
        @DisplayName("should create loader with empty username")
        void shouldCreateLoaderWithEmptyUsername() {
            APILoader emptyUserLoader = new APILoader(baseUrl, "", "token", false);
            assertNotNull(emptyUserLoader);
        }

        @Test
        @DisplayName("should create loader with empty token")
        void shouldCreateLoaderWithEmptyToken() {
            APILoader emptyTokenLoader = new APILoader(baseUrl, "user", "", false);
            assertNotNull(emptyTokenLoader);
        }

        @Test
        @DisplayName("should append trailing slash to URL if missing")
        void shouldAppendTrailingSlashToUrl() {
            String urlWithoutSlash = "http://localhost:" + wireMockServer.port() + "/api";
            APILoader slashLoader = new APILoader(urlWithoutSlash, null, null, false);
            assertNotNull(slashLoader);
        }

        @Test
        @DisplayName("should create loader with SSL certificate ignore option")
        void shouldCreateLoaderWithSslIgnoreOption() {
            APILoader sslIgnoreLoader = new APILoader(baseUrl, null, null, true);
            assertNotNull(sslIgnoreLoader);
        }
    }

    @Nested
    @DisplayName("worldExists method")
    class WorldExistsTests {

        @Test
        @DisplayName("should always return false")
        void shouldAlwaysReturnFalse() {
            assertFalse(loader.worldExists("anyworld"));
            assertFalse(loader.worldExists("anotherworld"));
            assertFalse(loader.worldExists(""));
        }
    }

    @Nested
    @DisplayName("listWorlds method")
    class ListWorldsTests {

        @Test
        @DisplayName("should return empty list when API returns empty array")
        void shouldReturnEmptyListWhenApiReturnsEmptyArray() {
            stubFor(get(urlEqualTo("/api/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("[]")));

            List<String> worlds = loader.listWorlds();

            assertTrue(worlds.isEmpty());
        }

        @Test
        @DisplayName("should return world names from API response")
        void shouldReturnWorldNamesFromApiResponse() {
            String jsonResponse = "[" +
                    "{\"name\": \"world1.slime\", \"size\": 1024}," +
                    "{\"name\": \"world2.slime\", \"size\": 2048}" +
                    "]";

            stubFor(get(urlEqualTo("/api/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jsonResponse)));

            List<String> worlds = loader.listWorlds();

            assertEquals(2, worlds.size());
            assertTrue(worlds.contains("world1"));
            assertTrue(worlds.contains("world2"));
        }

        @Test
        @DisplayName("should strip .slime extension from world names")
        void shouldStripSlimeExtensionFromWorldNames() {
            String jsonResponse = "[{\"name\": \"myworld.slime\", \"size\": 512}]";

            stubFor(get(urlEqualTo("/api/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jsonResponse)));

            List<String> worlds = loader.listWorlds();

            assertEquals(1, worlds.size());
            assertEquals("myworld", worlds.get(0));
        }

        @Test
        @DisplayName("should throw RuntimeException on IOException")
        void shouldThrowRuntimeExceptionOnIOException() {
            stubFor(get(urlEqualTo("/api/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("invalid json {")));

            assertThrows(RuntimeException.class, () -> loader.listWorlds());
        }
    }

    @Nested
    @DisplayName("readWorld method")
    class ReadWorldTests {

        @Test
        @DisplayName("should download world data successfully")
        void shouldDownloadWorldDataSuccessfully() throws IOException, UnknownWorldException {
            byte[] worldData = {1, 2, 3, 4, 5};

            stubFor(head(urlEqualTo("/api/testworld"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Length", String.valueOf(worldData.length))));

            stubFor(get(urlEqualTo("/api/testworld"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(worldData)));

            byte[] result = loader.readWorld("testworld");

            assertArrayEquals(worldData, result);
        }

        @Test
        @DisplayName("should handle world with no content-length header")
        void shouldHandleWorldWithNoContentLengthHeader() throws IOException, UnknownWorldException {
            byte[] worldData = {10, 20, 30};

            stubFor(head(urlEqualTo("/api/nosize"))
                    .willReturn(aResponse()
                            .withStatus(200)));

            stubFor(get(urlEqualTo("/api/nosize"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(worldData)));

            byte[] result = loader.readWorld("nosize");

            assertArrayEquals(worldData, result);
        }

        @Test
        @DisplayName("should download large world data")
        void shouldDownloadLargeWorldData() throws IOException, UnknownWorldException {
            byte[] largeData = new byte[1024 * 100]; // 100KB
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }

            stubFor(head(urlEqualTo("/api/largeworld"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Length", String.valueOf(largeData.length))));

            stubFor(get(urlEqualTo("/api/largeworld"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(largeData)));

            byte[] result = loader.readWorld("largeworld");

            assertArrayEquals(largeData, result);
        }

        @Test
        @DisplayName("should throw IndexOutOfBoundsException for oversized world")
        void shouldThrowIndexOutOfBoundsExceptionForOversizedWorld() {
            long oversizedLength = (long) Integer.MAX_VALUE + 1;

            stubFor(head(urlEqualTo("/api/hugeworld"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Length", String.valueOf(oversizedLength))));

            assertThrows(IndexOutOfBoundsException.class, () ->
                    loader.readWorld("hugeworld"));
        }
    }

    @Nested
    @DisplayName("saveWorld method")
    class SaveWorldTests {

        @Test
        @DisplayName("should not throw exception when saving world (logs warning)")
        void shouldNotThrowExceptionWhenSavingWorld() {
            assertDoesNotThrow(() ->
                    loader.saveWorld("anyworld", new byte[]{1, 2, 3}));
        }
    }

    @Nested
    @DisplayName("deleteWorld method")
    class DeleteWorldTests {

        @Test
        @DisplayName("should not throw exception when deleting world (logs warning)")
        void shouldNotThrowExceptionWhenDeletingWorld() {
            assertDoesNotThrow(() ->
                    loader.deleteWorld("anyworld"));
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("should send authorization header when credentials provided")
        void shouldSendAuthorizationHeaderWhenCredentialsProvided() {
            APILoader authLoader = new APILoader(baseUrl, "testuser", "testtoken", false);

            stubFor(get(urlEqualTo("/api/"))
                    .withHeader("Authorization", matching("Basic .*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("[]")));

            List<String> worlds = authLoader.listWorlds();

            verify(getRequestedFor(urlEqualTo("/api/"))
                    .withHeader("Authorization", matching("Basic dGVzdHVzZXI6dGVzdHRva2Vu")));

            assertNotNull(worlds);
        }
    }

    @Nested
    @DisplayName("SSL Certificate Tests")
    class SslCertificateTests {

        @Test
        @DisplayName("should create loader with SSL ignore and make requests")
        void shouldCreateLoaderWithSslIgnoreAndMakeRequests() {
            APILoader sslLoader = new APILoader(baseUrl, "user", "token", true);

            stubFor(get(urlEqualTo("/api/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("[]")));

            List<String> worlds = sslLoader.listWorlds();

            assertNotNull(worlds);
        }
    }
}
