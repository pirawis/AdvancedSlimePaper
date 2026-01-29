package com.infernalsuite.asp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InternalPlugin Unsupported Operations")
class InternalPluginUnsupportedOperationsTest {

    private final InternalPlugin plugin = new InternalPlugin();

    @Test
    @DisplayName("should throw for unsupported config operations")
    void shouldThrowForConfigOperations() {
        assertThrows(UnsupportedOperationException.class, plugin::getDataFolder);
        assertThrows(UnsupportedOperationException.class, plugin::getConfig);
        assertThrows(UnsupportedOperationException.class, () -> plugin.getResource("any"));
        assertThrows(UnsupportedOperationException.class, plugin::saveConfig);
        assertThrows(UnsupportedOperationException.class, plugin::saveDefaultConfig);
        assertThrows(UnsupportedOperationException.class, () -> plugin.saveResource("any", false));
        assertThrows(UnsupportedOperationException.class, plugin::reloadConfig);
    }

    @Test
    @DisplayName("should throw for lifecycle operations")
    void shouldThrowForLifecycleOperations() {
        assertThrows(UnsupportedOperationException.class, plugin::getPluginLoader);
        assertThrows(UnsupportedOperationException.class, plugin::onDisable);
        assertThrows(UnsupportedOperationException.class, plugin::onLoad);
        assertThrows(UnsupportedOperationException.class, plugin::onEnable);
        assertThrows(UnsupportedOperationException.class, plugin::isNaggable);
        assertThrows(UnsupportedOperationException.class, () -> plugin.setNaggable(true));
        assertThrows(UnsupportedOperationException.class, () -> plugin.getDefaultWorldGenerator("world", "id"));
        assertThrows(UnsupportedOperationException.class, () -> plugin.getDefaultBiomeProvider("world", "id"));
        assertThrows(UnsupportedOperationException.class, () -> plugin.onCommand(null, null, "", new String[0]));
        assertThrows(UnsupportedOperationException.class, () -> plugin.onTabComplete(null, null, "", new String[0]));
        assertThrows(UnsupportedOperationException.class, plugin::getLifecycleManager);
    }
}
