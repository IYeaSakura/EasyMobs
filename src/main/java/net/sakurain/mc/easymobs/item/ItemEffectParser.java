package net.sakurain.mc.easymobs.item;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Shared parser for effect entries used by items and sets.
 */
public final class ItemEffectParser {

    private ItemEffectParser() {
    }

    @SuppressWarnings("unchecked")
    public static List<CustomItemTemplate.EffectEntry> parseEffectEntries(List<?> list, String context) {
        List<CustomItemTemplate.EffectEntry> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            CustomItemTemplate.EffectType type = parseEffectType(getString(map, "type"));
            if (type == null) {
                log(context, "Unknown effect type: " + getString(map, "type"));
                continue;
            }
            CustomItemTemplate.EffectEntry entry = parseEffectEntry(type, map, context);
            if (entry != null) {
                result.add(entry);
            }
        }
        return result;
    }

    private static CustomItemTemplate.EffectEntry parseEffectEntry(CustomItemTemplate.EffectType type,
                                                                   Map<String, Object> map, String context) {
        return switch (type) {
            case POTION -> {
                org.bukkit.potion.PotionEffectType potion = parsePotionEffectType(getString(map, "potion_type"));
                if (potion == null) {
                    log(context, "Unknown potion effect type: " + getString(map, "potion_type"));
                    yield null;
                }
                yield new CustomItemTemplate.EffectEntry(type,
                        potion, getInt(map, "duration", 200), getInt(map, "amplifier", 0),
                        getBoolean(map, "ambient", false), getBoolean(map, "particles", true), getBoolean(map, "icon", true),
                        null, 0, null, null,
                        null, 0, 0, 0, 0,
                        null, 0, 0,
                        0);
            }
            case ATTRIBUTE -> {
                Attribute attribute = parseAttribute(getString(map, "attribute"));
                if (attribute == null) {
                    log(context, "Unknown attribute: " + getString(map, "attribute"));
                    yield null;
                }
                yield new CustomItemTemplate.EffectEntry(type,
                        null, 0, 0, false, false, false,
                        attribute, getDouble(map, "amount", 0), parseOperation(getString(map, "operation")), parseEquipmentSlot(getString(map, "slot")),
                        null, 0, 0, 0, 0,
                        null, 0, 0,
                        0);
            }
            case PARTICLE -> {
                org.bukkit.Particle particle = parseParticle(getString(map, "particle"));
                if (particle == null) {
                    log(context, "Unknown particle: " + getString(map, "particle"));
                    yield null;
                }
                yield new CustomItemTemplate.EffectEntry(type,
                        null, 0, 0, false, false, false,
                        null, 0, null, null,
                        particle, getInt(map, "count", 1), getDouble(map, "offset_x", 0.5),
                        getDouble(map, "offset_y", 0.5), getDouble(map, "offset_z", 0.5),
                        null, 0, 0,
                        getDouble(map, "radius", 0));
            }
            case SOUND -> {
                org.bukkit.Sound sound = parseSound(getString(map, "sound"));
                if (sound == null) {
                    log(context, "Unknown sound: " + getString(map, "sound"));
                    yield null;
                }
                yield new CustomItemTemplate.EffectEntry(type,
                        null, 0, 0, false, false, false,
                        null, 0, null, null,
                        null, 0, 0, 0, 0,
                        sound, (float) getDouble(map, "volume", 1.0), (float) getDouble(map, "pitch", 1.0),
                        getDouble(map, "radius", 0));
            }
        };
    }

    public static CustomItemTemplate.EffectType parseEffectType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return CustomItemTemplate.EffectType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Attribute parseAttribute(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.ATTRIBUTE.get(key);
    }

    public static Operation parseOperation(String value) {
        if (value == null) {
            return Operation.ADD_NUMBER;
        }
        try {
            return Operation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Operation.ADD_NUMBER;
        }
    }

    public static EquipmentSlot parseEquipmentSlot(String value) {
        if (value == null) {
            return null;
        }
        try {
            return EquipmentSlot.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static org.bukkit.potion.PotionEffectType parsePotionEffectType(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.EFFECT.get(key);
    }

    public static org.bukkit.Particle parseParticle(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.PARTICLE_TYPE.get(key);
    }

    public static org.bukkit.Sound parseSound(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.SOUNDS.get(key);
    }

    public static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<?> getList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?> list) {
            return list;
        }
        return null;
    }

    public static int getInt(Map<String, Object> map, String key, int def) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? def : Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double getDouble(Map<String, Object> map, String key, double def) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? def : Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        Object value = map.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        return value == null ? def : Boolean.parseBoolean(value.toString());
    }

    private static void log(String context, String message) {
        EasyMobsPlugin plugin = EasyMobsPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, "[" + context + "] " + message);
        }
    }
}
