package com.infernalsuite.asp.plugin.commands.exception;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessageCommandException")
class MessageCommandExceptionTest {

    @Test
    @DisplayName("should store component")
    void shouldStoreComponent() {
        Component component = Component.text("Error message");
        MessageCommandException ex = new MessageCommandException(component);
        
        assertEquals(component, ex.getComponent());
    }

    @Test
    @DisplayName("should store styled component")
    void shouldStoreStyledComponent() {
        Component component = Component.text("Error", NamedTextColor.RED);
        MessageCommandException ex = new MessageCommandException(component);
        
        assertEquals(component, ex.getComponent());
    }

    @Test
    @DisplayName("should store complex component")
    void shouldStoreComplexComponent() {
        Component component = Component.text()
            .append(Component.text("Prefix: ", NamedTextColor.GRAY))
            .append(Component.text("Message", NamedTextColor.WHITE))
            .build();
        
        MessageCommandException ex = new MessageCommandException(component);
        
        assertEquals(component, ex.getComponent());
    }

    @Test
    @DisplayName("should be RuntimeException subclass")
    void shouldBeRuntimeExceptionSubclass() {
        MessageCommandException ex = new MessageCommandException(Component.empty());
        
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("should handle empty component")
    void shouldHandleEmptyComponent() {
        Component component = Component.empty();
        MessageCommandException ex = new MessageCommandException(component);
        
        assertEquals(Component.empty(), ex.getComponent());
    }

    @Test
    @DisplayName("should handle null component")
    void shouldHandleNullComponent() {
        MessageCommandException ex = new MessageCommandException(null);
        
        assertNull(ex.getComponent());
    }
}
