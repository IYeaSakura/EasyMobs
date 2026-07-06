package net.sakurain.mc.easymobs.skill;

import org.bukkit.entity.LivingEntity;

public enum TargetType {
    CASTER,
    TARGET,
    NEARBY,
    ALL_NEARBY,
    OWNER,
    RANDOM_NEARBY,
    ORIGIN;

    public static TargetType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return TARGET;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TARGET;
        }
    }
}
