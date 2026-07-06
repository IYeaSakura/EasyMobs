package net.sakurain.mc.easymobs.item;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;

/**
 * Utility helpers for identifying custom items and sets from ItemStacks.
 */
public final class ItemIdentifier {

    private static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(EasyMobsPlugin.getInstance(), "ezmobs_item_id");
    private static final NamespacedKey SET_ID_KEY = new NamespacedKey(EasyMobsPlugin.getInstance(), "ezmobs_set_id");

    private ItemIdentifier() {
    }

    /**
     * Returns true if the stack is a custom EasyMobs item.
     *
     * @param item the item stack
     * @return true if custom
     */
    public static boolean isCustomItem(ItemStack item) {
        return getTemplateId(item) != null;
    }

    /**
     * Returns the template id stored on the item, or null.
     *
     * @param item the item stack
     * @return template id or null
     */
    @Nullable
    public static String getTemplateId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(ITEM_ID_KEY, PersistentDataType.STRING, null);
    }

    /**
     * Returns the set id stored on the item, or null.
     *
     * @param item the item stack
     * @return set id or null
     */
    @Nullable
    public static String getSetId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(SET_ID_KEY, PersistentDataType.STRING, null);
    }

    /**
     * Returns the loaded template for this item, or null.
     *
     * @param item the item stack
     * @return template or null
     */
    @Nullable
    public static CustomItemTemplate getTemplate(ItemStack item) {
        String id = getTemplateId(item);
        if (id == null) {
            return null;
        }
        return EasyMobsPlugin.getInstance().getItemManager().getTemplate(id);
    }
}
