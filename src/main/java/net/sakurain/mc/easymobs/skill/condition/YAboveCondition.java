package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.Location;

public class YAboveCondition extends AbstractSkillCondition {

    public YAboveCondition() {
        super("y_above");
    }

    @Override
    public boolean test(SkillContext context) {
        Location loc = context.getCaster() != null ? context.getCaster().getLocation() : context.getOrigin();
        if (loc == null) return false;
        double y = number("value", 0);
        return loc.getY() > y;
    }
}
