package com.infernalsuite.asp.api.world.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SlimePropertyMap functionality.
 */
@DisplayName("SlimePropertyMap Tests")
class SlimePropertiesTest {

    @Test
    @DisplayName("should create empty property map")
    void shouldCreateEmptyPropertyMap() {
        SlimePropertyMap map = new SlimePropertyMap();
        assertNotNull(map, "SlimePropertyMap should not be null");
    }

    @Test
    @DisplayName("should return non-null properties map")
    void shouldReturnNonNullPropertiesMap() {
        SlimePropertyMap map = new SlimePropertyMap();
        assertNotNull(map.getProperties(), "Properties map should not be null");
    }

    @Test
    @DisplayName("should clone property map")
    void shouldClonePropertyMap() {
        SlimePropertyMap original = new SlimePropertyMap();
        SlimePropertyMap copy = original.clone();

        assertNotNull(copy, "Cloned map should not be null");
        assertNotSame(original, copy, "Clone should be a different instance");
    }
}
