package net.sakurain.mc.aeternumgenesis.util;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParseUtilTest {

    @Test
    void toIntFromNumber() {
        assertEquals(42, ConfigParseUtil.toInt(42, 0));
    }

    @Test
    void toIntFromString() {
        assertEquals(42, ConfigParseUtil.toInt("42", 0));
    }

    @Test
    void toIntInvalidReturnsDefault() {
        assertEquals(0, ConfigParseUtil.toInt("not_a_number", 0));
        assertEquals(0, ConfigParseUtil.toInt(null, 0));
    }

    @Test
    void toDoubleFromNumber() {
        assertEquals(3.14, ConfigParseUtil.toDouble(3.14, 0.0), 0.001);
    }

    @Test
    void toDoubleFromString() {
        assertEquals(3.14, ConfigParseUtil.toDouble("3.14", 0.0), 0.001);
    }

    @Test
    void toBooleanFromBoolean() {
        assertTrue(ConfigParseUtil.toBoolean(true, false));
        assertFalse(ConfigParseUtil.toBoolean(false, true));
    }

    @Test
    void toBooleanFromString() {
        assertTrue(ConfigParseUtil.toBoolean("true", false));
        assertFalse(ConfigParseUtil.toBoolean("false", true));
    }

    @Test
    void getStringFromMap() {
        Map<String, Object> map = Map.of("key", "value");
        assertEquals("value", ConfigParseUtil.getString(map, "key"));
        assertNull(ConfigParseUtil.getString(map, "missing"));
    }

    @Test
    void parseColorValidHex() {
        Color color = ConfigParseUtil.parseColor("#8B0000");
        assertNotNull(color);
        assertEquals(139, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    void parseColorInvalidReturnsNull() {
        assertNull(ConfigParseUtil.parseColor("xyz"));
        assertNull(ConfigParseUtil.parseColor("#12345"));
        assertNull(ConfigParseUtil.parseColor(null));
    }

    @Test
    void parseOffsetDefaults() {
        double[] offset = ConfigParseUtil.parseOffset("1,2,3");
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, offset, 0.001);
    }

    @Test
    void parseOffsetPartial() {
        double[] offset = ConfigParseUtil.parseOffset("5");
        assertEquals(5.0, offset[0], 0.001);
        assertEquals(0.0, offset[1], 0.001);
        assertEquals(0.0, offset[2], 0.001);
    }

    @Test
    void parseDoubleValid() {
        assertEquals(2.5, ConfigParseUtil.parseDouble("2.5", 0.0), 0.001);
    }

    @Test
    void parseDoubleInvalidReturnsDefault() {
        assertEquals(0.0, ConfigParseUtil.parseDouble("bad", 0.0), 0.001);
        assertEquals(0.0, ConfigParseUtil.parseDouble(null, 0.0), 0.001);
    }
}
