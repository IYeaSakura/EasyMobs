package net.sakurain.mc.aeternumgenesis.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateIdUtilTest {

    @Test
    void validSimpleId() {
        assertTrue(TemplateIdUtil.isValid("blood_zombie"));
    }

    @Test
    void validWithDashDotUnderscore() {
        assertTrue(TemplateIdUtil.isValid("my.custom_id-1"));
    }

    @Test
    void invalidUppercase() {
        assertFalse(TemplateIdUtil.isValid("BloodZombie"));
    }

    @Test
    void invalidSpecialCharacters() {
        assertFalse(TemplateIdUtil.isValid("blood@zombie"));
    }

    @Test
    void invalidTooLong() {
        assertFalse(TemplateIdUtil.isValid("a".repeat(65)));
    }

    @Test
    void invalidBlank() {
        assertFalse(TemplateIdUtil.isValid(""));
        assertFalse(TemplateIdUtil.isValid("  "));
        assertFalse(TemplateIdUtil.isValid(null));
    }

    @Test
    void normalizeLowercases() {
        assertEquals("bloodzombie", TemplateIdUtil.normalize("BloodZombie"));
    }
}
