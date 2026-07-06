package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.Location;

public class YBelowCondition extends AbstractSkillCondition {

    public YBelowCondition() {
        super("y_below");
    }

    @Override
    public boolean test(SkillContext context) {
        Location loc = context.getCaster() != null ? context.getCaster().getLocation() : context.getOrigin();
        if (loc == null) return false;
        double y = number("value", 0);
        return loc.getY() < y;
    }
}
