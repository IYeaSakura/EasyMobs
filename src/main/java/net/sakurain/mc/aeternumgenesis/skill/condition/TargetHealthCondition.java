package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

public class TargetHealthCondition extends AbstractSkillCondition {

    public TargetHealthCondition() {
        super("target_health");
    }

    @Override
    public boolean test(SkillContext context) {
        if (context.getTarget() == null) return false;
        return compare(context.getTarget().getHealth(), string("operator", "=="), number("value", 1.0));
    }
}
