package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;

public class LightningEffect extends AbstractSkillEffect {

    public LightningEffect() {
        super("lightning");
    }

    @Override
    public void execute(SkillContext context) {
        Location location = location(context);
        if (location == null || location.getWorld() == null) {
            return;
        }
        boolean damage = bool("damage", true);
        if (damage) {
            location.getWorld().strikeLightning(location);
        } else {
            location.getWorld().strikeLightningEffect(location);
        }
    }
}
