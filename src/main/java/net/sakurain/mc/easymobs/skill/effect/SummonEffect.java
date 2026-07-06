package net.sakurain.mc.easymobs.skill.effect;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class SummonEffect extends AbstractSkillEffect {

    public SummonEffect() {
        super("summon");
    }

    @Override
    public void execute(SkillContext context) {
        String typeName = string("entity_type", "").toLowerCase();
        int amount = integer("amount", 1);
        if (typeName.isEmpty()) {
            return;
        }
        EntityType entityType = Registry.ENTITY_TYPE.get(NamespacedKey.minecraft(typeName));
        if (entityType == null) {
            return;
        }
        Location location = location(context);
        if (location == null || location.getWorld() == null) {
            return;
        }
        for (int i = 0; i < amount; i++) {
            org.bukkit.entity.Entity spawned = location.getWorld().spawnEntity(location, entityType);
            if (spawned instanceof LivingEntity living && context.getCaster() != null) {
                // Optional: set target of spawned mob to context target
                if (living instanceof org.bukkit.entity.Mob mob && context.getTarget() != null) {
                    mob.setTarget(context.getTarget());
                }
            }
        }
    }
}
