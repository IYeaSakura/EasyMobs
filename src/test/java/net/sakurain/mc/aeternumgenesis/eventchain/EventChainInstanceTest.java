package net.sakurain.mc.aeternumgenesis.eventchain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventChainInstanceTest {

    @Test
    void contextStorage() {
        EventChainTemplate template = new EventChainTemplate("test", null, java.util.List.of(), null);
        EventChainInstance instance = new EventChainInstance(UUID.randomUUID(), template, null, 0);
        instance.setContext("key", "value");
        assertEquals("value", instance.getContext("key"));
    }

    @Test
    void elapsedTicksCalculation() {
        EventChainTemplate template = new EventChainTemplate("test", null, java.util.List.of(), null);
        EventChainInstance instance = new EventChainInstance(UUID.randomUUID(), template, null, 100);
        assertEquals(50, instance.getElapsedTicks(150));
    }

    @Test
    void bossTrackingWithoutBossReturnsDefeated() {
        EventChainTemplate template = new EventChainTemplate("test", null, java.util.List.of(), null);
        EventChainInstance instance = new EventChainInstance(UUID.randomUUID(), template, null, 0);
        assertFalse(instance.isAnyBossAlive());
        assertFalse(instance.isBossAlive("main"));
    }
}
