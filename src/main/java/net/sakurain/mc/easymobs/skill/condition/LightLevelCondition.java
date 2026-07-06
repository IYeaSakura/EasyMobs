package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.Location;

public class LightLevelCondition extends AbstractSkillCondition {

    public LightLevelCondition() {
        super("light_level");
    }

    @Override
    public boolean test(SkillContext context) {
        Location loc = context.getCaster() != null ? context.getCaster().getLocation() : context.getOrigin();
        if (loc == null || loc.getWorld() == null) return false;
        int light = loc.getBlock().getLightLevel();
        return compare(light, string("operator", "=="), number("value", 0));
    }
}
