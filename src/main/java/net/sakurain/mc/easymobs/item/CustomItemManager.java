package net.sakurain.mc.easymobs.item;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Loads and stores {@link CustomItemTemplate}s from YAML configurations.
 */
public final class CustomItemManager {

    private final EasyMobsPlugin plugin;
    private Map<String, YamlConfiguration> configs = Map.of();
    private final Map<String, CustomItemTemplate> templates = new HashMap<>();

    public CustomItemManager(Map<String, YamlConfiguration> configs) {
        this.plugin = EasyMobsPlugin.getInstance();
        this.configs = configs == null ? Map.of() : Map.copyOf(configs);
        load();
    }

    /**
     * Loads or reloads all item templates from the current configs.
     */
    public void load() {
        templates.clear();
        for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
            loadConfig(entry.getKey(), entry.getValue());
        }
    }

    private void loadConfig(String fileName, YamlConfiguration config) {
        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) {
                continue;
            }
            try {
                CustomItemTemplate template = parseTemplate(key, config.getConfigurationSection(key));
                if (template != null) {
                    templates.put(template.getId(), template);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse item '" + key + "' in " + fileName, e);
            }
        }
    }

    private CustomItemTemplate parseTemplate(String id, ConfigurationSection section) {
        Material material = parseMaterial(section.getString("material", "STONE"));
        if (material == null) {
            plugin.getLogger().warning("Unknown material for item " + id);
            material = Material.STONE;
        }

        String name = section.getString("name", id);
        List<String> lore = section.getStringList("lore");
        int amount = parseAmount(section.getString("amount", "1"));
        Integer customModelData = section.contains("custom_model_data") ? section.getInt("custom_model_data") : null;
        boolean glow = section.getBoolean("glow", false);
        Map<Enchantment, Integer> enchantments = parseEnchantments(section.getConfigurationSection("enchantments"));
        boolean hideEnchants = section.getBoolean("hide_enchants", false);
        List<CustomItemTemplate.ItemAttribute> attributes = parseAttributes(section.getMapList("attributes"));
        List<CustomItemTemplate.ItemPassiveEffect> passiveEffects = parsePassiveEffects(section.getMapList("passive_effects"));
        List<CustomItemTemplate.ItemAttackEffect> attackEffects = parseAttackEffects(section.getMapList("attack_effects"));
        String setId = section.getString("set_id");
        boolean unbreakable = section.getBoolean("unbreakable", false);
        List<String> itemFlags = section.getStringList("item_flags");

        return new CustomItemTemplate(id, material, name, lore, amount, customModelData, glow,
                enchantments, hideEnchants, attributes, passiveEffects, attackEffects, setId, unbreakable, itemFlags);
    }

    private Material parseMaterial(String value) {
        if (value == null) {
            return null;
        }
        return Material.matchMaterial(value);
    }

    /**
     * Parses an amount string such as "1-3" or "5".
     *
     * @param value the amount string
     * @return parsed or random amount
     */
    public static int parseAmount(String value) {
        if (value == null || value.isEmpty()) {
            return 1;
        }
        String trimmed = value.trim();
        if (trimmed.contains("-")) {
            String[] parts = trimmed.split("-");
            try {
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return min <= max ? ThreadLocalRandom.current().nextInt(min, max + 1) : min;
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return 1;
            }
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Map<Enchantment, Integer> parseEnchantments(ConfigurationSection section) {
        Map<Enchantment, Integer> map = new HashMap<>();
        if (section == null) {
            return map;
        }
        for (String key : section.getKeys(false)) {
            Enchantment enchantment = parseEnchantment(key);
            if (enchantment == null) {
                plugin.getLogger().warning("Unknown enchantment: " + key);
                continue;
            }
            map.put(enchantment, section.getInt(key, 1));
        }
        return map;
    }

    private Enchantment parseEnchantment(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.ENCHANTMENT.get(key);
    }

    @SuppressWarnings("unchecked")
    private List<CustomItemTemplate.ItemAttribute> parseAttributes(List<?> list) {
        List<CustomItemTemplate.ItemAttribute> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            Attribute attribute = parseAttribute(getString(map, "type"));
            if (attribute == null) {
                plugin.getLogger().warning("Unknown attribute: " + getString(map, "type"));
                continue;
            }
            double amount = getDouble(map, "amount", 0);
            Operation operation = parseOperation(getString(map, "operation"));
            EquipmentSlot slot = parseEquipmentSlot(getString(map, "slot"));
            result.add(new CustomItemTemplate.ItemAttribute(attribute, amount, operation, slot));
        }
        return result;
    }

    private Attribute parseAttribute(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Attribute.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
            return Registry.ATTRIBUTE.get(key);
        }
    }

    private Operation parseOperation(String value) {
        if (value == null) {
            return Operation.ADD_NUMBER;
        }
        try {
            return Operation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Operation.ADD_NUMBER;
        }
    }

    private EquipmentSlot parseEquipmentSlot(String value) {
        if (value == null) {
            return null;
        }
        try {
            return EquipmentSlot.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<CustomItemTemplate.ItemPassiveEffect> parsePassiveEffects(List<?> list) {
        List<CustomItemTemplate.ItemPassiveEffect> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            CustomItemTemplate.TriggerType trigger = parseTrigger(getString(map, "trigger"));
            List<CustomItemTemplate.EffectEntry> effects = parseEffectEntries(getList(map, "effects"));
            result.add(new CustomItemTemplate.ItemPassiveEffect(trigger, effects));
        }
        return result;
    }

    private CustomItemTemplate.TriggerType parseTrigger(String value) {
        if (value == null) {
            return CustomItemTemplate.TriggerType.HOLD;
        }
        try {
            return CustomItemTemplate.TriggerType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CustomItemTemplate.TriggerType.HOLD;
        }
    }

    @SuppressWarnings("unchecked")
    private List<CustomItemTemplate.ItemAttackEffect> parseAttackEffects(List<?> list) {
        List<CustomItemTemplate.ItemAttackEffect> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            double chance = getDouble(map, "chance", 100);
            CustomItemTemplate.TargetType target = parseTarget(getString(map, "target"));
            List<CustomItemTemplate.EffectEntry> effects = parseEffectEntries(getList(map, "effects"));
            result.add(new CustomItemTemplate.ItemAttackEffect(chance, target, effects));
        }
        return result;
    }

    private CustomItemTemplate.TargetType parseTarget(String value) {
        if (value == null) {
            return CustomItemTemplate.TargetType.VICTIM;
        }
        try {
            return CustomItemTemplate.TargetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CustomItemTemplate.TargetType.VICTIM;
        }
    }

    @SuppressWarnings("unchecked")
    private List<CustomItemTemplate.EffectEntry> parseEffectEntries(List<?> list) {
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
                plugin.getLogger().warning("Unknown effect type: " + getString(map, "type"));
                continue;
            }
            CustomItemTemplate.EffectEntry entry = parseEffectEntry(type, map);
            if (entry != null) {
                result.add(entry);
            }
        }
        return result;
    }

    private CustomItemTemplate.EffectType parseEffectType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return CustomItemTemplate.EffectType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private CustomItemTemplate.EffectEntry parseEffectEntry(CustomItemTemplate.EffectType type, Map<String, Object> map) {
        return switch (type) {
            case POTION -> {
                PotionEffectType potion = parsePotionEffectType(getString(map, "potion_type"));
                if (potion == null) {
                    plugin.getLogger().warning("Unknown potion effect type: " + getString(map, "potion_type"));
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
                    plugin.getLogger().warning("Unknown attribute: " + getString(map, "attribute"));
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
                    plugin.getLogger().warning("Unknown particle: " + getString(map, "particle"));
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
                    plugin.getLogger().warning("Unknown sound: " + getString(map, "sound"));
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

    private PotionEffectType parsePotionEffectType(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.EFFECT.get(key);
    }

    private org.bukkit.Particle parseParticle(String value) {
        if (value == null) {
            return null;
        }
        try {
            return org.bukkit.Particle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private org.bukkit.Sound parseSound(String value) {
        if (value == null) {
            return null;
        }
        try {
            return org.bukkit.Sound.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    private List<?> getList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?> list) {
            return list;
        }
        return null;
    }

    private int getInt(Map<String, Object> map, String key, int def) {
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

    private double getDouble(Map<String, Object> map, String key, double def) {
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

    private boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        Object value = map.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        return value == null ? def : Boolean.parseBoolean(value.toString());
    }

    /**
     * Reloads templates from the plugin's current item configs.
     */
    public void reload() {
        this.configs = Map.copyOf(plugin.getConfigManager().getItemConfigs());
        load();
    }

    public CustomItemTemplate getTemplate(String id) {
        return templates.get(id);
    }

    public boolean hasTemplate(String id) {
        return templates.containsKey(id);
    }

    public Set<String> getTemplateIds() {
        return Set.copyOf(templates.keySet());
    }

    public int getTemplateCount() {
        return templates.size();
    }

    public List<CustomItemTemplate> getItemsBySetId(String setId) {
        List<CustomItemTemplate> result = new ArrayList<>();
        if (setId == null) {
            return result;
        }
        for (CustomItemTemplate template : templates.values()) {
            if (setId.equals(template.getSetId())) {
                result.add(template);
            }
        }
        return result;
    }
}
