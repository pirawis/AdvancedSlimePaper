package com.infernalsuite.asp.plugin.commands.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SlimeWorldParser")
class SlimeWorldParserTest {

    private SlimeWorldParser parser;

    @BeforeEach
    void setUp() {
        parser = new SlimeWorldParser();
    }

    @Test
    @DisplayName("should implement ArgumentParser interface")
    void shouldImplementArgumentParserInterface() {
        assertNotNull(parser);
        assertTrue(parser instanceof org.incendo.cloud.parser.ArgumentParser);
    }

    @Test
    @DisplayName("should provide suggestion provider")
    void shouldProvideSuggestionProvider() {
        var suggestionProvider = parser.suggestionProvider();
        assertNotNull(suggestionProvider);
        assertTrue(suggestionProvider instanceof org.incendo.cloud.suggestion.SuggestionProvider);
    }

    @Test
    @DisplayName("should not be null after instantiation")
    void shouldNotBeNullAfterInstantiation() {
        assertNotNull(parser);
        assertNotNull(parser.suggestionProvider());
    }

    @Test
    @DisplayName("should be single-purpose parser class")
    void shouldBeSinglePurposeParserClass() {
        var className = parser.getClass().getSimpleName();
        assertEquals("SlimeWorldParser", className);
    }
}
