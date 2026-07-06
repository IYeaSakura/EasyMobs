package net.sakurain.mc.easymobs.item.set;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.item.CustomItemTemplate;
import net.sakurain.mc.easymobs.item.ItemEffectParser;
import net.sakurain.mc.easymobs.item.ItemIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Loads and manages {@link ItemSetTemplate}s from YAML configurations.
 */
public final class ItemSetManager {

    private final EasyMobsPlugin plugin;
    private final Map<String, ItemSetTemplate> sets = new HashMap<>();

    public ItemSetManager(EasyMobsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads sets from the provided configuration map.
     *
     * @param configs map of file name to yaml configuration
     */
    public void load(Map<String, YamlConfiguration> configs) {
        sets.clear();
        if (configs == null) {
            return;
        }
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
                ConfigurationSection section = config.getConfigurationSection(key);
                ItemSetTemplate set = parseSet(key, section);
                if (set != null) {
                    registerSet(set);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse set '" + key + "' in " + fileName, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ItemSetTemplate parseSet(String id, ConfigurationSection section) {
        String name = section.getString("name", id);
        List<String> lore = section.getStringList("lore");
        List<String> itemIds = section.getStringList("item_ids");

        List<ItemSetTemplate.SetRequirement> requirements = new ArrayList<>();
        for (Object obj : section.getList("requirements", List.of())) {
            if (obj instanceof Map<?, ?> raw) {
                Map<String, Object> map = (Map<String, Object>) raw;
                requirements.add(new ItemSetTemplate.SetRequirement(
                        ItemEffectParser.getString(map, "type"),
                        ItemEffectParser.getString(map, "value"),
                        ItemEffectParser.getInt(map, "amount", 1)));
            }
        }

        List<ItemSetTemplate.SetBonus> bonuses = new ArrayList<>();
        for (Object obj : section.getList("bonuses", List.of())) {
            if (obj instanceof Map<?, ?> raw) {
                Map<String, Object> map = (Map<String, Object>) raw;
                List<CustomItemTemplate.EffectEntry> effects = ItemEffectParser.parseEffectEntries(
                        ItemEffectParser.getList(map, "effects"), id);
                List<String> messages = map.get("messages") instanceof List<?> list
                        ? list.stream().map(Object::toString).toList()
                        : List.of();
                bonuses.add(new ItemSetTemplate.SetBonus(
                        ItemEffectParser.getInt(map, "required_pieces", 2), effects, messages));
            }
        }

        List<ItemSetTemplate.AdvancedBonus> advancedBonuses = new ArrayList<>();
        for (Object obj : section.getList("advanced_bonuses", List.of())) {
            if (obj instanceof Map<?, ?> raw) {
                Map<String, Object> map = (Map<String, Object>) raw;
                List<CustomItemTemplate.EffectEntry> effects = ItemEffectParser.parseEffectEntries(
                        ItemEffectParser.getList(map, "effects"), id);
                List<String> messages = map.get("messages") instanceof List<?> list
                        ? list.stream().map(Object::toString).toList()
                        : List.of();
                advancedBonuses.add(new ItemSetTemplate.AdvancedBonus(
                        ItemEffectParser.getInt(map, "min_pieces", 2),
                        ItemEffectParser.getInt(map, "max_pieces", 0),
                        ItemEffectParser.getString(map, "condition"),
                        effects, messages));
            }
        }

        return new ItemSetTemplate(id, name, lore, itemIds, requirements, bonuses, advancedBonuses);
    }

    /**
     * Registers a set template.
     *
     * @param set the set to register
     */
    public void registerSet(ItemSetTemplate set) {
        sets.put(set.getId(), set);
    }

    /**
     * Reloads sets from the plugin's current set configs.
     */
    public void reload() {
        load(plugin.getConfigManager().getSetConfigs());
    }

    public ItemSetTemplate getSet(String id) {
        return sets.get(id);
    }

    public Collection<ItemSetTemplate> getSets() {
        return List.copyOf(sets.values());
    }

    /**
     * Checks the player's equipped set pieces and applies any set bonuses.
     *
     * @param player       the player to check
     * @param appliedPotions set of already applied potion effect type names; may be added to
     * @param appliedAttrs   set of already applied attribute modifier keys; may be added to
     */
    public void checkAndApply(Player player, Set<String> appliedPotions, Set<String> appliedAttrs) {
        for (ItemSetTemplate set : sets.values()) {
            int count = countEquippedPieces(player, set);
            if (count <= 0) {
                continue;
            }
            for (ItemSetTemplate.SetBonus bonus : set.getBonuses()) {
                if (count < bonus.getRequiredPieces()) {
                    continue;
                }
                for (CustomItemTemplate.EffectEntry entry : bonus.getEffects()) {
                    applySetEffect(player, entry, appliedPotions, appliedAttrs);
                }
            }
            for (ItemSetTemplate.AdvancedBonus bonus : set.getAdvancedBonuses()) {
                if (count < bonus.getMinPieces()) {
                    continue;
                }
                if (bonus.getMaxPieces() > 0 && count > bonus.getMaxPieces()) {
                    continue;
                }
                for (CustomItemTemplate.EffectEntry entry : bonus.getEffects()) {
                    applySetEffect(player, entry, appliedPotions, appliedAttrs);
                }
            }
        }
    }

    private int countEquippedPieces(Player player, ItemSetTemplate set) {
        int count = 0;
        ItemStack[] items = new ItemStack[6];
        items[0] = player.getInventory().getItemInMainHand();
        items[1] = player.getInventory().getItemInOffHand();
        System.arraycopy(player.getInventory().getArmorContents(), 0, items, 2, 4);
        for (ItemStack item : items) {
            if (item == null || item.isEmpty()) {
                continue;
            }
            String templateId = ItemIdentifier.getTemplateId(item);
            if (templateId != null && set.getItemIds().contains(templateId)) {
                count++;
            }
        }
        return count;
    }

    private void applySetEffect(Player player, CustomItemTemplate.EffectEntry entry,
                                Set<String> appliedPotions, Set<String> appliedAttrs) {
        switch (entry.getType()) {
            case POTION -> {
                if (entry.getPotionType() == null) {
                    break;
                }
                String id = entry.getPotionType().getKey().toString();
                if (!appliedPotions.add(id)) {
                    break;
                }
                player.addPotionEffect(new PotionEffect(entry.getPotionType(), entry.getDuration(),
                        entry.getAmplifier(), entry.isAmbient(), entry.isParticles(), entry.isIcon()));
            }
            case ATTRIBUTE -> {
                if (entry.getAttribute() == null) {
                    break;
                }
                AttributeInstance instance = player.getAttribute(entry.getAttribute());
                if (instance == null) {
                    break;
                }
                String modifierId = "set_" + entry.getAttribute().getKey().getKey() + "_" + entry.getAttributeSlot();
                if (!appliedAttrs.add(modifierId)) {
                    break;
                }
                NamespacedKey key = new NamespacedKey(plugin, modifierId);
                instance.getModifiers().stream()
                        .filter(m -> m.getKey().equals(key))
                        .findFirst()
                        .ifPresent(instance::removeModifier);
                instance.addModifier(new AttributeModifier(key, entry.getAttributeAmount(),
                        entry.getAttributeOperation(), toSlotGroup(entry.getAttributeSlot())));
            }
            case PARTICLE -> {
                if (entry.getParticle() != null) {
                    player.getWorld().spawnParticle(entry.getParticle(), player.getLocation().clone().add(0, 1, 0),
                            entry.getParticleCount(), entry.getParticleOffsetX(), entry.getParticleOffsetY(), entry.getParticleOffsetZ());
                }
            }
            case SOUND -> {
                if (entry.getSound() != null) {
                    player.playSound(player.getLocation(), entry.getSound(), entry.getSoundVolume(), entry.getSoundPitch());
                }
            }
        }
    }

    private static EquipmentSlotGroup toSlotGroup(org.bukkit.inventory.EquipmentSlot slot) {
        if (slot == null) {
            return EquipmentSlotGroup.ANY;
        }
        return switch (slot) {
            case HAND -> EquipmentSlotGroup.MAINHAND;
            case OFF_HAND -> EquipmentSlotGroup.OFFHAND;
            case FEET -> EquipmentSlotGroup.FEET;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case HEAD -> EquipmentSlotGroup.HEAD;
            case BODY -> EquipmentSlotGroup.BODY;
            case SADDLE -> EquipmentSlotGroup.SADDLE;
        };
    }
}
