package com.infernalsuite.asp;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdvancedSlimePaper")
class AdvancedSlimePaperTest {

    @Test
    @DisplayName("should implement AdvancedSlimePaperAPI")
    void shouldImplementApiInterface() {
        assertTrue(AdvancedSlimePaperAPI.class.isAssignableFrom(AdvancedSlimePaper.class));
    }

    @Test
    @DisplayName("should expose static instance method")
    void shouldExposeStaticInstanceMethod() throws Exception {
        Method method = AdvancedSlimePaper.class.getMethod("instance");
        assertTrue(Modifier.isStatic(method.getModifiers()));
        assertEquals(AdvancedSlimePaper.class, method.getReturnType());
    }
}
