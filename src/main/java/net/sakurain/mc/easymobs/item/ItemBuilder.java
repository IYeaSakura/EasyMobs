package net.sakurain.mc.easymobs.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.sakurain.mc.easymobs.EasyMobsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
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

    private static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(EasyMobsPlugin.getInstance(), "ezmobs_item_id");
    private static final NamespacedKey SET_ID_KEY = new NamespacedKey(EasyMobsPlugin.getInstance(), "ezmobs_set_id");

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

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_ID_KEY, PersistentDataType.STRING, template.getId());
        if (template.getSetId() != null && !template.getSetId().isEmpty()) {
            pdc.set(SET_ID_KEY, PersistentDataType.STRING, template.getSetId());
        }
    }
}
