package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class MobsInRadiusCondition extends AbstractSkillCondition {

    public MobsInRadiusCondition() {
        super("mobs_in_radius");
    }

    @Override
    public boolean test(SkillContext context) {
        double radius = number("radius", 5.0);
        Location center = context.getCaster() != null ? context.getCaster().getLocation() : context.getOrigin();
        if (center == null || center.getWorld() == null) return false;
        int count = (int) center.getWorld().getNearbyLivingEntities(center, radius, radius, radius).stream()
                .filter(e -> e != context.getCaster())
                .count();
        return compare(count, string("operator", ">="), number("value", 1));
    }
}
