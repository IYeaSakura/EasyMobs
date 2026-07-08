package net.sakurain.mc.aeternumgenesis.skill;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public final class SkillCooldownManager {

    private static final String KEY_PREFIX = "genesis_cd_";

    private SkillCooldownManager() {
    }

    public static boolean isOnCooldown(LivingEntity entity, String skillId) {
        return getRemainingCooldown(entity, skillId) > 0;
    }

    public static double getRemainingCooldown(LivingEntity entity, String skillId) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        NamespacedKey key = key(skillId);
        if (!pdc.has(key, PersistentDataType.LONG)) {
            return 0.0;
        }
        Long value = pdc.get(key, PersistentDataType.LONG);
        if (value == null) {
            return 0.0;
        }
        long remaining = value - System.currentTimeMillis();
        return Math.max(0.0, remaining / 1000.0);
    }

    public static void setCooldown(LivingEntity entity, String skillId, double seconds) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        long expire = System.currentTimeMillis() + (long) (seconds * 1000.0);
        pdc.set(key(skillId), PersistentDataType.LONG, expire);
    }

    public static void clearCooldown(LivingEntity entity, String skillId) {
        entity.getPersistentDataContainer().remove(key(skillId));
    }

    public static void clearAllCooldowns(LivingEntity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        Set<NamespacedKey> toRemove = new HashSet<>();
        for (NamespacedKey key : pdc.getKeys()) {
            if (key.getKey().startsWith(KEY_PREFIX)) {
                toRemove.add(key);
            }
        }
        for (NamespacedKey key : toRemove) {
            pdc.remove(key);
        }
    }

    private static NamespacedKey key(String skillId) {
        return new NamespacedKey("genesis", KEY_PREFIX + sanitize(skillId));
    }

    private static String sanitize(String skillId) {
        if (skillId == null) {
            return "null";
        }
        String lowered = skillId.toLowerCase();
        StringBuilder sb = new StringBuilder(lowered.length());
        for (char c : lowered.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }
}
