package com.infernalsuite.asp.level;

import com.infernalsuite.asp.api.world.SlimeWorld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("SlimeBootstrap")
class SlimeBootstrapTest {

    @Test
    @DisplayName("should keep initial slime world reference")
    void shouldKeepInitialSlimeWorldReference() {
        SlimeWorld world = Mockito.mock(SlimeWorld.class);
        SlimeBootstrap bootstrap = new SlimeBootstrap(world);

        assertSame(world, bootstrap.initial());
    }
}
