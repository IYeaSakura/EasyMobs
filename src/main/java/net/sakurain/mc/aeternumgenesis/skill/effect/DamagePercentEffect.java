package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class DamagePercentEffect extends AbstractSkillEffect {

    public DamagePercentEffect() {
        super("damage_percent");
    }

    @Override
    public void execute(SkillContext context) {
        double percent = number("percent", 10.0);
        if (!Double.isFinite(percent)) {
            return;
        }
        LivingEntity target = singleTarget(context);
        if (target == null || target.isDead()) {
            return;
        }
        double maxHealth = target.getAttribute(Attribute.MAX_HEALTH) != null
                ? target.getAttribute(Attribute.MAX_HEALTH).getValue()
                : target.getHealth();
        double amount = maxHealth * (percent / 100.0);
        if (!Double.isFinite(amount) || amount < 0) {
            return;
        }
        if (context.getCaster() != null) {
            target.damage(amount, context.getCaster());
        } else {
            target.damage(amount);
        }
    }
}
