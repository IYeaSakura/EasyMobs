package net.sakurain.mc.aeternumgenesis.eventchain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventChainTemplateTest {

    @Test
    void parseDurationTicks() {
        assertEquals(100, EventChainTemplate.parseDuration("100"));
    }

    @Test
    void parseDurationSeconds() {
        assertEquals(60, EventChainTemplate.parseDuration("3s"));
    }

    @Test
    void parseDurationMinutes() {
        assertEquals(1200, EventChainTemplate.parseDuration("1m"));
    }

    @Test
    void parseDurationHours() {
        assertEquals(72000, EventChainTemplate.parseDuration("1h"));
    }

    @Test
    void parseDurationDays() {
        assertEquals(1728000, EventChainTemplate.parseDuration("1d"));
    }

    @Test
    void parseDurationCombined() {
        assertEquals(1_801_221, EventChainTemplate.parseDuration("1d1h1m1s1t"));
    }

    @Test
    void parseDurationBlankReturnsZero() {
        assertEquals(0, EventChainTemplate.parseDuration(null));
        assertEquals(0, EventChainTemplate.parseDuration(""));
        assertEquals(0, EventChainTemplate.parseDuration("   "));
    }

    @Test
    void parseDurationInvalidReturnsZero() {
        assertEquals(0, EventChainTemplate.parseDuration("abc"));
    }
}
