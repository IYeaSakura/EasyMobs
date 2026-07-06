package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.World;

public class WeatherCondition extends AbstractSkillCondition {

    public WeatherCondition() {
        super("weather");
    }

    @Override
    public boolean test(SkillContext context) {
        World world = context.getCaster() != null ? context.getCaster().getWorld() : null;
        if (world == null) return false;
        String weather = string("type", "clear").toLowerCase();
        return switch (weather) {
            case "rain" -> world.hasStorm();
            case "thunder" -> world.isThundering();
            default -> !world.hasStorm() && !world.isThundering();
        };
    }
}
