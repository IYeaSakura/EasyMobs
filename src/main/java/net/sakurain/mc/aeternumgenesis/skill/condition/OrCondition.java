package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.skill.SkillConditionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrCondition extends AbstractSkillCondition {

    private final SkillConditionRegistry registry;
    private final List<SkillCondition> children = new ArrayList<>();

    public OrCondition(SkillConditionRegistry registry) {
        super("or");
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Map<String, Object> parameters) {
        super.load(parameters);
        children.clear();
        Object list = parameters.get("conditions");
        if (list instanceof List<?> raw) {
            for (Object obj : raw) {
                if (obj instanceof Map<?, ?> map) {
                    SkillCondition child = ConditionParser.parse((Map<String, Object>) map, registry);
                    if (child != null) {
                        children.add(child);
                    }
                }
            }
        }
    }

    @Override
    public boolean test(SkillContext context) {
        if (children.isEmpty()) {
            return true;
        }
        for (SkillCondition child : children) {
            if (child.test(context)) {
                return true;
            }
        }
        return false;
    }
}
