package net.sakurain.mc.aeternumgenesis.util;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Shared helpers for parsing common YAML config elements across managers.
 */
public final class ConfigParseUtil {

    private ConfigParseUtil() {
    }

    public static Particle parseParticle(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String normalized = value.toLowerCase();
        if ("redstone".equals(normalized)) {
            normalized = "dust";
        }
        NamespacedKey key = NamespacedKey.minecraft(normalized);
        return org.bukkit.Registry.PARTICLE_TYPE.get(key);
    }

    public static Sound parseSound(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.SOUNDS.get(key);
    }

    public static PotionEffectType parsePotionEffectType(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return org.bukkit.Registry.POTION_EFFECT_TYPE.get(key);
    }

    public static PotionEffect parsePotionEffect(Map<String, Object> map, String context) {
        String typeName = getString(map, "type");
        PotionEffectType type = parsePotionEffectType(typeName);
        if (type == null) {
            warn(context + ": unknown potion effect type: " + typeName);
            return null;
        }
        int duration = toInt(map.get("duration"), 200);
        int amplifier = toInt(map.get("amplifier"), 0);
        boolean ambient = toBoolean(map.get("ambient"), false);
        boolean particles = toBoolean(map.get("show_particles"), true);
        boolean icon = toBoolean(map.get("show_icon"), particles);
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    public static List<PotionEffect> parsePotionEffects(List<?> list, String context) {
        List<PotionEffect> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> raw) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) raw;
                PotionEffect effect = parsePotionEffect(map, context);
                if (effect != null) {
                    result.add(effect);
                }
            }
        }
        return result;
    }

    public static Color parseColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        String normalized = hex.startsWith("#") ? hex.substring(1) : hex;
        if (normalized.length() != 6) {
            return null;
        }
        try {
            int rgb = Integer.parseInt(normalized, 16);
            return Color.fromRGB(rgb);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static double[] parseOffset(String value) {
        if (value == null || value.isEmpty()) {
            return new double[]{0.0, 0.0, 0.0};
        }
        String[] parts = value.split(",");
        double[] result = new double[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            result[i] = parseDouble(parts[i].trim(), 0.0);
        }
        return result;
    }

    public static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    public static int toInt(Object value, int def) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double toDouble(Object value, double def) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return def;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double parseDouble(String value, double def) {
        if (value == null || value.isEmpty()) {
            return def;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static boolean toBoolean(Object value, boolean def) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value == null) {
            return def;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public static void warn(String message) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, message);
        }
    }
}
