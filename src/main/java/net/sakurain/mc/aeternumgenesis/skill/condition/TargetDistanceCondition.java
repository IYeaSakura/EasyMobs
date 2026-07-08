package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

public class TargetDistanceCondition extends AbstractSkillCondition {

    public TargetDistanceCondition() {
        super("target_distance");
    }

    @Override
    public boolean test(SkillContext context) {
        if (context.getCaster() == null || context.getTarget() == null) return false;
        double distance = context.getCaster().getLocation().distance(context.getTarget().getLocation());
        return compare(distance, string("operator", "<="), number("value", 5.0));
    }
}
