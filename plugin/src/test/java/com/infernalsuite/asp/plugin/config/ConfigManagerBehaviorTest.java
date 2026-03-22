package com.infernalsuite.asp.plugin.config;

import com.infernalsuite.asp.plugin.SWPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@DisplayName("ConfigManager behavior")
class ConfigManagerBehaviorTest {

    private static final Path PLUGIN_DIR = Path.of("plugins", "SlimeWorldManager");

    @AfterEach
    void tearDown() throws IOException {
        deletePluginDir();
    }

    @Test
    @DisplayName("should copy defaults and initialize both config accessors")
    void shouldCopyDefaultsAndInitializeBothConfigAccessors() throws Exception {
        deletePluginDir();
        SWPlugin plugin = mock(SWPlugin.class);

        when(plugin.getResource("worlds.yml")).thenAnswer(invocation ->
                new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8))
        );

        try (MockedStatic<SWPlugin> pluginStatic = mockStatic(SWPlugin.class)) {
            pluginStatic.when(SWPlugin::getInstance).thenReturn(plugin);

            ConfigManager.initialize();

            assertTrue(Files.exists(PLUGIN_DIR.resolve("worlds.yml")));
            assertTrue(Files.exists(PLUGIN_DIR.resolve("sources.yml")));
            assertNotNull(ConfigManager.getWorldConfig());
            assertNotNull(ConfigManager.getDatasourcesConfig());
            assertNotNull(ConfigManager.getWorldConfigLoader());
        }
    }

    private static void deletePluginDir() throws IOException {
        if (!Files.exists(PLUGIN_DIR)) {
            return;
        }

        try (var paths = Files.walk(PLUGIN_DIR)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }
}
