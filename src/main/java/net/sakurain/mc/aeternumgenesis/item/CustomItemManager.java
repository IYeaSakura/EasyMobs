package net.sakurain.mc.aeternumgenesis.item;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Loads and stores {@link CustomItemTemplate}s from YAML configurations.
 */
public final class CustomItemManager {

    private final AeternumGenesisPlugin plugin;
    private Map<String, YamlConfiguration> configs = Map.of();
    private final Map<String, CustomItemTemplate> templates = new HashMap<>();

    public CustomItemManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
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

    private CustomItemTemplate parseTemplate(String rawId, ConfigurationSection section) {
        String id = TemplateIdUtil.normalize(rawId);
        if (!TemplateIdUtil.isValid(id)) {
            plugin.getLogger().warning("Invalid item template id (must be lowercase [a-z0-9._-] and <= 64 chars): " + rawId);
            return null;
        }
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
        List<CustomItemTemplate.ItemAttribute> attributes = parseAttributes(section.getMapList("attributes"), id);
        List<CustomItemTemplate.ItemPassiveEffect> passiveEffects = parsePassiveEffects(section.getMapList("passive_effects"), id);
        List<CustomItemTemplate.ItemAttackEffect> attackEffects = parseAttackEffects(section.getMapList("attack_effects"), id);
        String setId = section.getString("set_id");
        String blockId = section.getString("block");
        boolean unbreakable = section.getBoolean("unbreakable", false);
        List<String> itemFlags = section.getStringList("item_flags");

        return new CustomItemTemplate(id, material, name, lore, amount, customModelData, glow,
                enchantments, hideEnchants, attributes, passiveEffects, attackEffects, setId, blockId, unbreakable, itemFlags);
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
        int result;
        if (trimmed.contains("-")) {
            String[] parts = trimmed.split("-");
            try {
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                if (min > max) {
                    result = min;
                } else {
                    long bound = (long) max + 1L;
                    if (bound > Integer.MAX_VALUE) {
                        bound = Integer.MAX_VALUE;
                    }
                    result = ThreadLocalRandom.current().nextInt(min, (int) bound);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return 1;
            }
        } else {
            try {
                result = Integer.parseInt(trimmed);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return Math.max(0, Math.min(result, 64));
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    private Enchantment parseEnchantment(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.ENCHANTMENT.get(key);
    }

    @SuppressWarnings("unchecked")
    private List<CustomItemTemplate.ItemAttribute> parseAttributes(List<?> list, String itemId) {
        List<CustomItemTemplate.ItemAttribute> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            Attribute attribute = ItemEffectParser.parseAttribute(ItemEffectParser.getString(map, "type"));
            if (attribute == null) {
                plugin.getLogger().warning("[" + itemId + "] Unknown attribute: " + ItemEffectParser.getString(map, "type"));
                continue;
            }
            double amount = ItemEffectParser.getDouble(map, "amount", 0);
            String operationRaw = ItemEffectParser.getString(map, "operation");
            boolean setValue = "SET_VALUE".equalsIgnoreCase(operationRaw);
            Operation operation = setValue ? Operation.ADD_NUMBER : ItemEffectParser.parseOperation(operationRaw);
            EquipmentSlot slot = ItemEffectParser.parseEquipmentSlot(ItemEffectParser.getString(map, "slot"));
            result.add(new CustomItemTemplate.ItemAttribute(attribute, amount, operation, slot, setValue));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<CustomItemTemplate.ItemPassiveEffect> parsePassiveEffects(List<?> list, String itemId) {
        List<CustomItemTemplate.ItemPassiveEffect> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            CustomItemTemplate.TriggerType trigger = parseTrigger(ItemEffectParser.getString(map, "trigger"));
            List<CustomItemTemplate.EffectEntry> effects = ItemEffectParser.parseEffectEntries(
                    ItemEffectParser.getList(map, "effects"), itemId);
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
    private List<CustomItemTemplate.ItemAttackEffect> parseAttackEffects(List<?> list, String itemId) {
        List<CustomItemTemplate.ItemAttackEffect> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            double chance = ItemEffectParser.getDouble(map, "chance", 100);
            CustomItemTemplate.TargetType target = parseTarget(ItemEffectParser.getString(map, "target"));
            List<CustomItemTemplate.EffectEntry> effects = ItemEffectParser.parseEffectEntries(
                    ItemEffectParser.getList(map, "effects"), itemId);
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

    /**
     * Reloads templates from the plugin's current item configs.
     */
    public void reload() {
        this.configs = Map.copyOf(plugin.getConfigManager().getItemConfigs());
        load();
    }

    public CustomItemTemplate getTemplate(String id) {
        return id != null ? templates.get(id.toLowerCase(Locale.ROOT)) : null;
    }

    public boolean hasTemplate(String id) {
        return id != null && templates.containsKey(id.toLowerCase(Locale.ROOT));
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

    public boolean registerTemplate(String id, ConfigurationSection section) {
        if (id == null || section == null) {
            return false;
        }
        try {
            CustomItemTemplate template = parseTemplate(id.toLowerCase(), section);
            if (template == null) {
                return false;
            }
            templates.put(template.getId(), template);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to register item template '" + id + "'", e);
            return false;
        }
    }

    public boolean unregisterTemplate(String id) {
        return id != null && templates.remove(id.toLowerCase()) != null;
    }
}
