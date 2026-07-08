package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillConditionRegistry;

import java.util.Map;
import java.util.Optional;

public class ConditionParser {

    private static SkillConditionRegistry defaultRegistry;

    private ConditionParser() {
    }

    public static void setDefaultRegistry(SkillConditionRegistry registry) {
        defaultRegistry = registry;
    }

    public static SkillCondition parse(Map<String, Object> map) {
        return parse(map, defaultRegistry);
    }

    public static SkillCondition parse(Map<String, Object> map, SkillConditionRegistry registry) {
        if (map == null || registry == null) {
            return null;
        }
        String type = String.valueOf(map.getOrDefault("type", "")).toLowerCase();
        if (type.isEmpty()) {
            return null;
        }

        return switch (type) {
            case "and" -> {
                AndCondition condition = new AndCondition(registry);
                condition.load(map);
                yield condition;
            }
            case "or" -> {
                OrCondition condition = new OrCondition(registry);
                condition.load(map);
                yield condition;
            }
            case "not" -> {
                NotCondition condition = new NotCondition(registry);
                condition.load(map);
                yield condition;
            }
            default -> {
                Optional<SkillCondition> optional = registry.create(type);
                if (optional.isPresent()) {
                    SkillCondition condition = optional.get();
                    condition.load(map);
                    yield condition;
                }
                yield null;
            }
        };
    }
}
