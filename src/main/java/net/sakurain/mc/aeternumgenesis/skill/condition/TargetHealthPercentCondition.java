package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class TargetHealthPercentCondition extends AbstractSkillCondition {

    public TargetHealthPercentCondition() {
        super("target_health_percent");
    }

    @Override
    public boolean test(SkillContext context) {
        LivingEntity target = context.getTarget();
        if (target == null) return false;
        double max = target.getAttribute(Attribute.MAX_HEALTH) != null
                ? target.getAttribute(Attribute.MAX_HEALTH).getValue()
                : target.getHealth();
        if (max <= 0) return false;
        double percent = target.getHealth() / max * 100.0;
        return compare(percent, string("operator", "=="), number("value", 100.0));
    }
}
