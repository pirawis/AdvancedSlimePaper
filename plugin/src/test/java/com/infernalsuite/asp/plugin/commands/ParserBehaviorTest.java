package com.infernalsuite.asp.plugin.commands;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import com.infernalsuite.asp.plugin.commands.parser.BukkitWorldParser;
import com.infernalsuite.asp.plugin.commands.parser.NamedSlimeLoader;
import com.infernalsuite.asp.plugin.commands.parser.NamedSlimeLoaderParser;
import com.infernalsuite.asp.plugin.commands.parser.NamedWorldData;
import com.infernalsuite.asp.plugin.commands.parser.NamedWorldDataParser;
import com.infernalsuite.asp.plugin.commands.parser.SlimeWorldParser;
import com.infernalsuite.asp.plugin.commands.parser.suggestion.KnownSlimeWorldSuggestionProvider;
import com.infernalsuite.asp.plugin.config.ConfigManager;
import com.infernalsuite.asp.plugin.config.WorldData;
import com.infernalsuite.asp.plugin.config.WorldsConfig;
import com.infernalsuite.asp.plugin.loader.LoaderManager;
import com.infernalsuite.asp.plugin.testutil.TestAdvancedSlimePaperAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.suggestion.Suggestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Plugin parser behavior")
class ParserBehaviorTest {

    @Mock
    private LoaderManager loaderManager;

    @Mock
    private SlimeLoader loader;

    @Mock
    private World bukkitWorld;

    @Mock
    private CommandContext<Source> commandContext;

    private static String plainText(final Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static List<String> suggestions(final CompletableFuture<? extends Iterable<? extends Suggestion>> future) {
        Iterable<? extends Suggestion> iterable = future.join();
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(Suggestion::suggestion)
                .toList();
    }

    @Nested
    @DisplayName("BukkitWorldParser")
    class BukkitWorldParserTests {

        @Test
        @DisplayName("should parse loaded worlds successfully")
        void shouldParseLoadedWorldsSuccessfully() {
            BukkitWorldParser parser = new BukkitWorldParser();

            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getWorld("arena")).thenReturn(bukkitWorld);

                ArgumentParseResult<World> result = parser.parse(commandContext, CommandInput.of("arena"));

                assertEquals(bukkitWorld, result.parsedValue().orElseThrow());
                assertTrue(result.failure().isEmpty());
            }
        }

        @Test
        @DisplayName("should fail when the world is not loaded")
        void shouldFailWhenTheWorldIsNotLoaded() {
            BukkitWorldParser parser = new BukkitWorldParser();

            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(() -> Bukkit.getWorld("missing")).thenReturn(null);

                ArgumentParseResult<World> result = parser.parse(commandContext, CommandInput.of("missing"));

                MessageCommandException exception = assertInstanceOf(
                        MessageCommandException.class,
                        result.failure().orElseThrow()
                );
                assertTrue(plainText(exception.getComponent()).contains("World missing is not loaded!"));
            }
        }

    }

    @Nested
    @DisplayName("NamedWorldDataParser")
    class NamedWorldDataParserTests {

        @Test
        @DisplayName("should parse configured worlds")
        void shouldParseConfiguredWorlds() {
            NamedWorldDataParser parser = new NamedWorldDataParser();
            WorldData worldData = new WorldData();
            Map<String, WorldData> worlds = new HashMap<>();
            worlds.put("arena", worldData);
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(worlds);

            try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                ArgumentParseResult<NamedWorldData> result = parser.parse(commandContext, CommandInput.of("arena"));

                NamedWorldData parsed = result.parsedValue().orElseThrow();
                assertEquals("arena", parsed.name());
                assertEquals(worldData, parsed.worldData());
            }
        }

        @Test
        @DisplayName("should fail when the world is missing from config")
        void shouldFailWhenTheWorldIsMissingFromConfig() {
            NamedWorldDataParser parser = new NamedWorldDataParser();
            WorldsConfig worldsConfig = mock(WorldsConfig.class);
            when(worldsConfig.getWorlds()).thenReturn(new HashMap<>());

            try (MockedStatic<ConfigManager> configManager = mockStatic(ConfigManager.class)) {
                configManager.when(ConfigManager::getWorldConfig).thenReturn(worldsConfig);

                ArgumentParseResult<NamedWorldData> result = parser.parse(commandContext, CommandInput.of("arena"));

                MessageCommandException exception = assertInstanceOf(
                        MessageCommandException.class,
                        result.failure().orElseThrow()
                );
                assertTrue(plainText(exception.getComponent()).contains("Failed to find world arena"));
            }
        }

        @Test
        @DisplayName("should expose a suggestion provider")
        void shouldExposeASuggestionProvider() {
            NamedWorldDataParser parser = new NamedWorldDataParser();
            assertNotNull(parser.suggestionProvider());
            assertInstanceOf(KnownSlimeWorldSuggestionProvider.class, parser.suggestionProvider());
        }
    }

    @Nested
    @DisplayName("NamedSlimeLoaderParser")
    class NamedSlimeLoaderParserTests {

        @Test
        @DisplayName("should parse known loader names")
        void shouldParseKnownLoaderNames() {
            when(loaderManager.getLoader("file")).thenReturn(loader);
            NamedSlimeLoaderParser parser = new NamedSlimeLoaderParser(loaderManager);

            ArgumentParseResult<NamedSlimeLoader> result = parser.parse(commandContext, CommandInput.of("file"));

            NamedSlimeLoader parsed = result.parsedValue().orElseThrow();
            assertEquals("file", parsed.name());
            assertEquals(loader, parsed.slimeLoader());
        }

        @Test
        @DisplayName("should fail for unknown loaders")
        void shouldFailForUnknownLoaders() {
            when(loaderManager.getLoader("ghost")).thenReturn(null);
            NamedSlimeLoaderParser parser = new NamedSlimeLoaderParser(loaderManager);

            ArgumentParseResult<NamedSlimeLoader> result = parser.parse(commandContext, CommandInput.of("ghost"));

            MessageCommandException exception = assertInstanceOf(
                    MessageCommandException.class,
                    result.failure().orElseThrow()
            );
            assertTrue(plainText(exception.getComponent()).contains("Unknown data source ghost!"));
        }

        @Test
        @DisplayName("should suggest registered loader names")
        void shouldSuggestRegisteredLoaderNames() {
            Map<String, SlimeLoader> loaders = new HashMap<>();
            loaders.put("redis", loader);
            loaders.put("mysql", loader);
            when(loaderManager.getLoaders()).thenReturn(loaders);
            NamedSlimeLoaderParser parser = new NamedSlimeLoaderParser(loaderManager);

            List<String> suggestions = suggestions(parser.suggestionProvider().suggestionsFuture(commandContext, CommandInput.empty()));

            assertEquals(2, suggestions.size());
            assertTrue(suggestions.contains("redis"));
            assertTrue(suggestions.contains("mysql"));
        }
    }

    @Nested
    @DisplayName("SlimeWorldParser")
    class SlimeWorldParserTests {

        @Test
        @DisplayName("should parse loaded slime worlds")
        void shouldParseLoadedSlimeWorlds() {
            SlimeWorldParser parser = new SlimeWorldParser();
            SlimeWorldInstance slimeWorldInstance = mock(SlimeWorldInstance.class);

            AdvancedSlimePaperAPI api = mock(AdvancedSlimePaperAPI.class);
            TestAdvancedSlimePaperAPI.setDelegate(api);
            try {
                when(api.getLoadedWorld("arena")).thenReturn(slimeWorldInstance);

                ArgumentParseResult<SlimeWorld> result = parser.parse(commandContext, CommandInput.of("arena"));

                assertEquals(slimeWorldInstance, result.parsedValue().orElseThrow());
            } finally {
                TestAdvancedSlimePaperAPI.setDelegate(null);
            }
        }

        @Test
        @DisplayName("should fail when slime world is not loaded")
        void shouldFailWhenSlimeWorldIsNotLoaded() {
            SlimeWorldParser parser = new SlimeWorldParser();
            AdvancedSlimePaperAPI api = mock(AdvancedSlimePaperAPI.class);
            TestAdvancedSlimePaperAPI.setDelegate(api);
            try {
                when(api.getLoadedWorld("arena")).thenReturn(null);

                ArgumentParseResult<SlimeWorld> result = parser.parse(commandContext, CommandInput.of("arena"));

                MessageCommandException exception = assertInstanceOf(
                        MessageCommandException.class,
                        result.failure().orElseThrow()
                );
                assertTrue(plainText(exception.getComponent()).contains("World arena is not loaded!"));
            } finally {
                TestAdvancedSlimePaperAPI.setDelegate(null);
            }
        }

        @Test
        @DisplayName("should suggest currently loaded slime worlds")
        void shouldSuggestCurrentlyLoadedSlimeWorlds() {
            SlimeWorldParser parser = new SlimeWorldParser();
            SlimeWorldInstance alpha = mock(SlimeWorldInstance.class);
            SlimeWorldInstance beta = mock(SlimeWorldInstance.class);
            when(alpha.getName()).thenReturn("alpha");
            when(beta.getName()).thenReturn("beta");
            AdvancedSlimePaperAPI api = mock(AdvancedSlimePaperAPI.class);
            TestAdvancedSlimePaperAPI.setDelegate(api);
            try {
                when(api.getLoadedWorlds()).thenReturn(List.of(alpha, beta));

                List<String> suggestions = suggestions(parser.suggestionProvider().suggestionsFuture(commandContext, CommandInput.empty()));

                assertEquals(List.of("alpha", "beta"), suggestions);
            } finally {
                TestAdvancedSlimePaperAPI.setDelegate(null);
            }
        }
    }

}
