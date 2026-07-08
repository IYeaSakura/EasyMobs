package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class ExplosionEffect extends AbstractSkillEffect {

    public ExplosionEffect() {
        super("explosion");
    }

    @Override
    public void execute(SkillContext context) {
        Location location = location(context);
        if (location == null || location.getWorld() == null) {
            return;
        }
        float power = (float) number("power", 1.0);
        boolean fire = bool("fire", false);
        boolean breakBlocks = bool("break_blocks", false);
        Entity source = context.getCaster();
        location.getWorld().createExplosion(location, power, fire, breakBlocks, source);
    }
}
