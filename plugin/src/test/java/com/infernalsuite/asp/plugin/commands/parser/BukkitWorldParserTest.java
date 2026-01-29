package com.infernalsuite.asp.plugin.commands.parser;

import com.infernalsuite.asp.plugin.commands.exception.MessageCommandException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BukkitWorldParser")
class BukkitWorldParserTest {

    private BukkitWorldParser parser;

    @Mock
    private CommandContext<Source> mockCommandContext;

    @Mock
    private CommandInput mockCommandInput;

    @Mock
    private World mockWorld;

    @BeforeEach
    void setUp() {
        parser = new BukkitWorldParser();
    }

    @Nested
    @DisplayName("parse")
    class ParseTests {

        @Test
        @DisplayName("should return success when world is loaded")
        void shouldReturnSuccessWhenWorldIsLoaded() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                when(mockCommandInput.peekString()).thenReturn("TestWorld");
                bukkit.when(() -> Bukkit.getWorld("TestWorld")).thenReturn(mockWorld);

                ArgumentParseResult<World> result = parser.parse(mockCommandContext, mockCommandInput);

                assertTrue(result.success());
                assertEquals(mockWorld, result.parsedValue());
                verify(mockCommandInput).readString();
            }
        }

        @Test
        @DisplayName("should return failure when world is not loaded")
        void shouldReturnFailureWhenWorldIsNotLoaded() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                when(mockCommandInput.peekString()).thenReturn("NonExistentWorld");
                bukkit.when(() -> Bukkit.getWorld("NonExistentWorld")).thenReturn(null);

                ArgumentParseResult<World> result = parser.parse(mockCommandContext, mockCommandInput);

                assertFalse(result.success());
                assertTrue(result.failure() instanceof MessageCommandException);
            }
        }

        @Test
        @DisplayName("should read string from input after successful parse")
        void shouldReadStringFromInputAfterSuccessfulParse() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                when(mockCommandInput.peekString()).thenReturn("TestWorld");
                bukkit.when(() -> Bukkit.getWorld("TestWorld")).thenReturn(mockWorld);

                parser.parse(mockCommandContext, mockCommandInput);

                verify(mockCommandInput, times(1)).readString();
            }
        }

        @Test
        @DisplayName("should create failure exception with correct message format")
        void shouldCreateFailureExceptionWithCorrectMessageFormat() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                String worldName = "InvalidWorld";
                when(mockCommandInput.peekString()).thenReturn(worldName);
                bukkit.when(() -> Bukkit.getWorld(worldName)).thenReturn(null);

                ArgumentParseResult<World> result = parser.parse(mockCommandContext, mockCommandInput);

                assertFalse(result.success());
                Throwable exception = result.failure();
                assertNotNull(exception);
                assertTrue(exception.getMessage().contains(worldName));
            }
        }

        @Test
        @DisplayName("should handle different world names")
        void shouldHandleDifferentWorldNames() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                when(mockCommandInput.peekString()).thenReturn("AnotherWorld");
                World anotherWorld = mock(World.class);
                bukkit.when(() -> Bukkit.getWorld("AnotherWorld")).thenReturn(anotherWorld);

                ArgumentParseResult<World> result = parser.parse(mockCommandContext, mockCommandInput);

                assertTrue(result.success());
                assertEquals(anotherWorld, result.parsedValue());
            }
        }
    }

    @Nested
    @DisplayName("suggestionProvider")
    class SuggestionProviderTests {

        @Test
        @DisplayName("should return suggestion provider")
        void shouldReturnSuggestionProvider() {
            SuggestionProvider<Source> provider = parser.suggestionProvider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should provide suggestions for loaded worlds")
        void shouldProvideSuggestionsForLoadedWorlds() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                World world1 = mock(World.class);
                World world2 = mock(World.class);
                when(world1.getName()).thenReturn("Overworld");
                when(world2.getName()).thenReturn("Nether");
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(world1, world2));

                SuggestionProvider<Source> provider = parser.suggestionProvider();
                CompletableFuture<List<Suggestion>> suggestions = provider.getSuggestions(mockCommandContext, "");

                assertNotNull(suggestions);
                assertTrue(suggestions.isDone());
                
                List<Suggestion> suggestionList = suggestions.join();
                assertEquals(2, suggestionList.size());
            }
        }

        @Test
        @DisplayName("should provide empty suggestions when no worlds loaded")
        void shouldProvideEmptySuggestionsWhenNoWorldsLoaded() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of());

                SuggestionProvider<Source> provider = parser.suggestionProvider();
                CompletableFuture<List<Suggestion>> suggestions = provider.getSuggestions(mockCommandContext, "");

                assertNotNull(suggestions);
                assertTrue(suggestions.isDone());
                
                List<Suggestion> suggestionList = suggestions.join();
                assertTrue(suggestionList.isEmpty());
            }
        }

        @Test
        @DisplayName("should create suggestions with world names")
        void shouldCreateSuggestionsWithWorldNames() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                World world = mock(World.class);
                when(world.getName()).thenReturn("TestWorld");
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(world));

                SuggestionProvider<Source> provider = parser.suggestionProvider();
                CompletableFuture<List<Suggestion>> suggestions = provider.getSuggestions(mockCommandContext, "");

                List<Suggestion> suggestionList = suggestions.join();
                assertEquals(1, suggestionList.size());
                assertEquals("TestWorld", suggestionList.get(0).suggestion());
            }
        }

        @Test
        @DisplayName("should handle multiple worlds in suggestions")
        void shouldHandleMultipleWorldsInSuggestions() {
            try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
                World world1 = mock(World.class);
                World world2 = mock(World.class);
                World world3 = mock(World.class);
                when(world1.getName()).thenReturn("World1");
                when(world2.getName()).thenReturn("World2");
                when(world3.getName()).thenReturn("World3");
                bukkit.when(Bukkit::getWorlds).thenReturn(List.of(world1, world2, world3));

                SuggestionProvider<Source> provider = parser.suggestionProvider();
                CompletableFuture<List<Suggestion>> suggestions = provider.getSuggestions(mockCommandContext, "");

                List<Suggestion> suggestionList = suggestions.join();
                assertEquals(3, suggestionList.size());
                assertTrue(suggestionList.stream().anyMatch(s -> s.suggestion().equals("World1")));
                assertTrue(suggestionList.stream().anyMatch(s -> s.suggestion().equals("World2")));
                assertTrue(suggestionList.stream().anyMatch(s -> s.suggestion().equals("World3")));
            }
        }
    }
}
