package com.infernalsuite.asp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SlimeNMSBridgeImpl")
class SlimeNMSBridgeImplTest {

    @Test
    @DisplayName("should expose current data version")
    void shouldExposeCurrentDataVersion() {
        SlimeNMSBridgeImpl bridge = new SlimeNMSBridgeImpl();
        int version = bridge.getCurrentVersion();
        assertTrue(version > 0);
    }
}
