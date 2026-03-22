package com.infernalsuite.asp.api;

import com.infernalsuite.asp.api.testutil.DummyAdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.testutil.DummySlimeNMSBridge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("API service access")
class ServiceAccessTest {

    @Test
    @DisplayName("AdvancedSlimePaperAPI.instance should resolve the configured service once")
    void advancedSlimePaperApiInstanceShouldResolveTheConfiguredServiceOnce() {
        AdvancedSlimePaperAPI first = AdvancedSlimePaperAPI.instance();
        AdvancedSlimePaperAPI second = AdvancedSlimePaperAPI.instance();

        assertInstanceOf(DummyAdvancedSlimePaperAPI.class, first);
        assertSame(first, second);
    }

    @Test
    @DisplayName("SlimeNMSBridge.instance should resolve the configured service once")
    void slimeNmsBridgeInstanceShouldResolveTheConfiguredServiceOnce() {
        SlimeNMSBridge first = SlimeNMSBridge.instance();
        SlimeNMSBridge second = SlimeNMSBridge.instance();

        assertInstanceOf(DummySlimeNMSBridge.class, first);
        assertSame(first, second);
    }
}
