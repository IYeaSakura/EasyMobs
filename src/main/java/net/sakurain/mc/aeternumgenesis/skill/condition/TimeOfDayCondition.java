package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.World;

public class TimeOfDayCondition extends AbstractSkillCondition {

    public TimeOfDayCondition() {
        super("time_of_day");
    }

    @Override
    public boolean test(SkillContext context) {
        World world = context.getCaster() != null ? context.getCaster().getWorld() : null;
        if (world == null) return false;
        long time = world.getTime();
        long min = (long) number("min", 0);
        long max = (long) number("max", 24000);
        if (min <= max) {
            return time >= min && time <= max;
        }
        return time >= min || time <= max;
    }
}
