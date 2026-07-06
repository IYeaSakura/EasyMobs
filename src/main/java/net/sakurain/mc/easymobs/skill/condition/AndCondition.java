package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import net.sakurain.mc.easymobs.skill.SkillConditionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndCondition extends AbstractSkillCondition {

    private final SkillConditionRegistry registry;
    private final List<SkillCondition> children = new ArrayList<>();

    public AndCondition(SkillConditionRegistry registry) {
        super("and");
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
        for (SkillCondition child : children) {
            if (!child.test(context)) {
                return false;
            }
        }
        return true;
    }
}
