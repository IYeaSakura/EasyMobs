package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.entity.LivingEntity;

public class ExtinguishEffect extends AbstractSkillEffect {

    public ExtinguishEffect() {
        super("extinguish");
    }

    @Override
    public void execute(SkillContext context) {
        LivingEntity target = singleTarget(context);
        if (target == null) {
            return;
        }
        target.setFireTicks(0);
    }
}
