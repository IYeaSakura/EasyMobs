package net.sakurain.mc.aeternumgenesis.mob;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles per-mob level scaling using persistent data.
 */
public final class LevelSystem {

    private static final NamespacedKey MOB_LEVEL_KEY = new NamespacedKey("genesis", "genesis_level");
    private static final int MAX_LEVEL = 10_000;

    private LevelSystem() {
    }

    /**
     * Applies a level to the given mob. The level is stored in PDC and health is scaled.
     *
     * @param entity   the mob entity
     * @param level    the level to apply
     * @param template the mob template
     */
    public static void applyLevel(LivingEntity entity, int level, CustomMobTemplate template) {
        if (entity == null || level <= 0) {
            return;
        }
        level = Math.min(level, MAX_LEVEL);
        setMobLevel(entity, level);

        if (template != null && template.getDisplayName() != null && template.getDisplayName().contains("<level>")) {
            String updated = template.getDisplayName().replace("<level>", String.valueOf(level));
            entity.customName(net.sakurain.mc.aeternumgenesis.util.MessageUtil.color(updated));
            entity.setCustomNameVisible(true);
        }

        double multiplier = 1.0 + ((level - 1) * 0.15);
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double base = template != null && template.getMaxHealth() > 0
                    ? template.getMaxHealth()
                    : maxHealth.getBaseValue();
            double scaled = base * multiplier;
            if (!Double.isFinite(scaled) || scaled <= 0) {
                return;
            }
            maxHealth.setBaseValue(scaled);
            entity.setHealth(scaled);
        }
    }

    /**
     * Returns the stored mob level, or 1 if none is set.
     *
     * @param entity the mob entity
     * @return the mob level
     */
    public static int getMobLevel(LivingEntity entity) {
        if (entity == null) {
            return 1;
        }
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        Integer value = pdc.get(MOB_LEVEL_KEY, PersistentDataType.INTEGER);
        return value != null ? value : 1;
    }

    /**
     * Sets the stored mob level without applying any scaling.
     *
     * @param entity the mob entity
     * @param level  the level to store
     */
    public static void setMobLevel(LivingEntity entity, int level) {
        if (entity == null) {
            return;
        }
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(MOB_LEVEL_KEY, PersistentDataType.INTEGER, Math.max(1, level));
    }

    /**
     * Returns the PDC key used to store mob levels.
     *
     * @return the level key
     */
    public static NamespacedKey getMobLevelKey() {
        return MOB_LEVEL_KEY;
    }
}
