package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.entity.LivingEntity;

public class IgniteEffect extends AbstractSkillEffect {

    public IgniteEffect() {
        super("ignite");
    }

    @Override
    public void execute(SkillContext context) {
        double duration = number("duration", 5.0);
        LivingEntity target = singleTarget(context);
        if (target == null) {
            return;
        }
        target.setFireTicks((int) (duration * 20.0));
    }
}
