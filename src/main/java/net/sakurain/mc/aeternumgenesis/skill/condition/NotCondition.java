package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.skill.SkillConditionRegistry;

import java.util.Map;

public class NotCondition extends AbstractSkillCondition {

    private final SkillConditionRegistry registry;
    private SkillCondition child;

    public NotCondition(SkillConditionRegistry registry) {
        super("not");
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Map<String, Object> parameters) {
        super.load(parameters);
        Object raw = parameters.get("condition");
        if (raw instanceof Map<?, ?> map) {
            child = ConditionParser.parse((Map<String, Object>) map, registry);
        } else {
            child = null;
        }
    }

    @Override
    public boolean test(SkillContext context) {
        if (child == null) {
            return true;
        }
        return !child.test(context);
    }
}
