package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.entity.LivingEntity;

public class DamageEffect extends AbstractSkillEffect {

    public DamageEffect() {
        super("damage");
    }

    @Override
    public void execute(SkillContext context) {
        double amount = number("amount", 1.0);
        if (!Double.isFinite(amount) || amount < 0) {
            return;
        }
        LivingEntity target = singleTarget(context);
        if (target == null || target.isDead()) {
            return;
        }
        if (context.getCaster() != null) {
            target.damage(amount, context.getCaster());
        } else {
            target.damage(amount);
        }
    }
}
