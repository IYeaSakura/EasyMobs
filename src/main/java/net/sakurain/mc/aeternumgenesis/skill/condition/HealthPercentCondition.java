package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class HealthPercentCondition extends AbstractSkillCondition {

    public HealthPercentCondition() {
        super("health_percent");
    }

    @Override
    public boolean test(SkillContext context) {
        LivingEntity caster = context.getCaster();
        if (caster == null) return false;
        double max = caster.getAttribute(Attribute.MAX_HEALTH) != null
                ? caster.getAttribute(Attribute.MAX_HEALTH).getValue()
                : caster.getHealth();
        if (max <= 0) return false;
        double percent = caster.getHealth() / max * 100.0;
        return compare(percent, string("operator", "=="), number("value", 100.0));
    }
}
