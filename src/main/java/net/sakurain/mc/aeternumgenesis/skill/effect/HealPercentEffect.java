package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

public class HealPercentEffect extends AbstractSkillEffect {

    public HealPercentEffect() {
        super("heal_percent");
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
        AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttribute != null ? maxHealthAttribute.getValue() : target.getHealth();
        double newHealth = target.getHealth() + maxHealth * (percent / 100.0);
        if (!Double.isFinite(newHealth)) {
            return;
        }
        target.setHealth(Math.min(maxHealth, newHealth));
    }
}
