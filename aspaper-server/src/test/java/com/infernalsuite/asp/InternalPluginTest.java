package com.infernalsuite.asp;

import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InternalPlugin")
class InternalPluginTest {

    @Test
    @DisplayName("should be enabled by default")
    void shouldBeEnabledByDefault() {
        InternalPlugin plugin = new InternalPlugin();
        assertTrue(plugin.isEnabled());
    }

    @Test
    @DisplayName("should allow toggling enabled state")
    void shouldAllowTogglingEnabledState() {
        InternalPlugin plugin = new InternalPlugin();
        plugin.setEnabled(false);
        assertFalse(plugin.isEnabled());
        plugin.setEnabled(true);
        assertTrue(plugin.isEnabled());
    }

    @Test
    @DisplayName("should expose plugin description")
    void shouldExposePluginDescription() {
        InternalPlugin plugin = new InternalPlugin();
        PluginDescriptionFile description = plugin.getDescription();
        assertEquals("Minecraft", description.getName());
        assertEquals("1.0", description.getVersion());
        assertEquals("nms", description.getMain());
    }
}
