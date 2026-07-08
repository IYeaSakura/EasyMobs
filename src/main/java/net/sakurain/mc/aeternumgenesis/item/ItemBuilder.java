package net.sakurain.mc.aeternumgenesis.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds an {@link ItemStack} from a {@link CustomItemTemplate}.
 */
@SuppressWarnings("deprecation")
public final class ItemBuilder {

    private static final NamespacedKey ITEM_ID_KEY = new NamespacedKey("genesis", "genesis_item_id");
    private static final NamespacedKey SET_ID_KEY = new NamespacedKey("genesis", "genesis_set_id");

    private ItemBuilder() {
    }

    /**
     * Builds an ItemStack from the provided template.
     *
     * @param template the template to build from
     * @return the built item stack
     */
    public static ItemStack build(CustomItemTemplate template) {
        ItemStack item = new ItemStack(template.getMaterial(), template.getAmount());
        item.editMeta(meta -> applyMeta(meta, template));
        return item;
    }

    private static AttributeModifier createAttributeModifier(CustomItemTemplate template, CustomItemTemplate.ItemAttribute attr) {
        Attribute attribute = attr.getAttribute();
        EquipmentSlot slot = attr.getSlot() == null ? EquipmentSlot.HAND : attr.getSlot();
        if (attr.isSetValue()) {
            // SET_VALUE: amount is the final value shown in the vanilla tooltip.
            // The modifier replaces the item's default modifier (same vanilla key),
            // so the delta is simply final - playerBase.
            double delta = attr.getAmount() - getPlayerBase(attribute);
            NamespacedKey key = getVanillaBaseModifierKey(attribute, slot);
            if (key == null) {
                key = new NamespacedKey("genesis",
                        template.getId() + "_" + attribute.getKey().getKey());
            }
            return new AttributeModifier(key, delta, Operation.ADD_NUMBER, toSlotGroup(slot));
        }
        NamespacedKey key = new NamespacedKey("genesis",
                template.getId() + "_" + attribute.getKey().getKey());
        return new AttributeModifier(key, attr.getAmount(), attr.getOperation(), toSlotGroup(slot));
    }

    private static double getPlayerBase(Attribute attribute) {
        if (attribute == Attribute.ATTACK_DAMAGE) return 1.0;
        if (attribute == Attribute.ATTACK_SPEED) return 4.0;
        if (attribute == Attribute.MOVEMENT_SPEED) return 0.1;
        if (attribute == Attribute.MAX_HEALTH) return 20.0;
        if (attribute == Attribute.ARMOR || attribute == Attribute.ARMOR_TOUGHNESS || attribute == Attribute.KNOCKBACK_RESISTANCE) return 0.0;
        return 0.0;
    }

    private static NamespacedKey getVanillaBaseModifierKey(Attribute attribute, EquipmentSlot slot) {
        if (attribute == Attribute.ATTACK_DAMAGE) return NamespacedKey.minecraft("base_attack_damage");
        if (attribute == Attribute.ATTACK_SPEED) return NamespacedKey.minecraft("base_attack_speed");
        if (attribute == Attribute.ARMOR || attribute == Attribute.ARMOR_TOUGHNESS || attribute == Attribute.KNOCKBACK_RESISTANCE) {
            return getArmorModifierKey(slot);
        }
        return null;
    }

    private static NamespacedKey getArmorModifierKey(EquipmentSlot slot) {
        String suffix = switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            case BODY -> "body";
            default -> null;
        };
        return suffix == null ? null : NamespacedKey.minecraft("armor." + suffix);
    }

    private static EquipmentSlotGroup toSlotGroup(EquipmentSlot slot) {
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

    private static void applyMeta(ItemMeta meta, CustomItemTemplate template) {
        if (template.getName() != null && !template.getName().isEmpty()) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(template.getName()));
        }

        if (!template.getLore().isEmpty()) {
            List<Component> lore = new ArrayList<>(template.getLore().size());
            for (String line : template.getLore()) {
                lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            meta.lore(lore);
        }

        if (template.getCustomModelData() != null) {
            meta.setCustomModelData(template.getCustomModelData());
        }

        for (Map.Entry<Enchantment, Integer> entry : template.getEnchantments().entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        if (template.isGlow() && template.getEnchantments().isEmpty()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (template.isHideEnchants()) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        for (String flagName : template.getItemFlags()) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (template.isUnbreakable()) {
            meta.setUnbreakable(true);
        }

        for (CustomItemTemplate.ItemAttribute attr : template.getAttributes()) {
            AttributeModifier modifier = createAttributeModifier(template, attr);
            meta.addAttributeModifier(attr.getAttribute(), modifier);
            AeternumGenesisPlugin.getInstance().getLogger().fine("Applied attribute " + attr.getAttribute().getKey()
                    + " to " + template.getId() + " with amount " + modifier.getAmount());
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_ID_KEY, PersistentDataType.STRING, template.getId());
        if (template.getSetId() != null && !template.getSetId().isEmpty()) {
            pdc.set(SET_ID_KEY, PersistentDataType.STRING, template.getSetId());
        }
    }
}
