package net.sakurain.mc.aeternumgenesis.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRuleUtilTest {

    @Test
    void nullNameReturnsNull() {
        assertNull(GameRuleUtil.getByName(null));
    }

    @Test
    void blankNameReturnsNull() {
        assertNull(GameRuleUtil.getByName(""));
        assertNull(GameRuleUtil.getByName("   "));
    }
}
