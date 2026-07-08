package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

public class HealthCondition extends AbstractSkillCondition {

    public HealthCondition() {
        super("health");
    }

    @Override
    public boolean test(SkillContext context) {
        if (context.getCaster() == null) return false;
        return compare(context.getCaster().getHealth(), string("operator", "=="), number("value", 1.0));
    }
}
