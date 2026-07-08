package net.sakurain.mc.aeternumgenesis.eventchain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventConditionEvaluatorTest {

    private final EventConditionEvaluator evaluator = new EventConditionEvaluator();

    @Test
    void blankConditionIsTrue() {
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition(null), new TestInstance(false)));
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("   "), new TestInstance(false)));
    }

    @Test
    void bossDefeatedWhenNoBossAlive() {
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("boss_defeated"), new TestInstance(false)));
    }

    @Test
    void bossDefeatedFalseWhenBossAlive() {
        assertFalse(evaluator.evaluate(new EventChainTemplate.Condition("boss_defeated"), new TestInstance(true)));
    }

    @Test
    void bossAliveWhenBossAlive() {
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("boss_alive"), new TestInstance(true)));
    }

    @Test
    void logicalAnd() {
        TestInstance instance = new TestInstance(true);
        instance.setContext("kills", 10);
        assertFalse(evaluator.evaluate(new EventChainTemplate.Condition("and(boss_defeated, kills > 5)"), instance));
        instance = new TestInstance(false);
        instance.setContext("kills", 10);
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("and(boss_defeated, kills > 5)"), instance));
    }

    @Test
    void logicalOr() {
        TestInstance instance = new TestInstance(true);
        instance.setContext("kills", 2);
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("or(boss_alive, kills > 5)"), instance));
        instance = new TestInstance(false);
        instance.setContext("kills", 2);
        assertFalse(evaluator.evaluate(new EventChainTemplate.Condition("or(boss_alive, kills > 5)"), instance));
    }

    @Test
    void logicalNot() {
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("not(boss_alive)"), new TestInstance(false)));
        assertFalse(evaluator.evaluate(new EventChainTemplate.Condition("not(boss_defeated)"), new TestInstance(false)));
    }

    @Test
    void nestedLogical() {
        TestInstance instance = new TestInstance(false);
        instance.setContext("kills", 8);
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition(
                "and(boss_defeated, or(kills > 10, kills == 8))"), instance));
    }

    @Test
    void bossKeyState() {
        TestInstance instance = new TestInstance(false);
        instance.setBossAlive("main", true);
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("boss_alive(main)"), instance));
        assertFalse(evaluator.evaluate(new EventChainTemplate.Condition("boss_defeated(main)"), instance));
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("boss_alive(main) == true"), instance));
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("boss_defeated(main) == false"), instance));
    }

    @Test
    void contextComparison() {
        TestInstance instance = new TestInstance(false);
        instance.setContext("phase", 3);
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("phase == 3"), instance));
        assertTrue(evaluator.evaluate(new EventChainTemplate.Condition("phase >= 2"), instance));
        assertFalse(evaluator.evaluate(new EventChainTemplate.Condition("phase < 3"), instance));
    }

    @Test
    void elapsedTicksRequiresBukkitServer() {
        // Cannot unit test without Bukkit; parsing is validated by context tests above.
    }

    private static class TestInstance extends EventChainInstance {
        private boolean anyBossAlive;
        private final java.util.Map<String, Boolean> bossStates = new java.util.HashMap<>();

        TestInstance(boolean bossAlive) {
            super(UUID.randomUUID(), new EventChainTemplate("test", null, java.util.List.of(), null), null, 0);
            this.anyBossAlive = bossAlive;
        }

        @Override
        public boolean isAnyBossAlive() {
            return anyBossAlive;
        }

        void setBossAlive(String key, boolean alive) {
            bossStates.put(key, alive);
        }

        @Override
        public boolean isBossAlive(String key) {
            return bossStates.getOrDefault(key, false);
        }
    }
}
