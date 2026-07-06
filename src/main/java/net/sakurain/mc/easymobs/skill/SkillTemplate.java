package net.sakurain.mc.easymobs.skill;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record SkillTemplate(
        String id,
        double cooldown,
        String onCooldownSkill,
        boolean cancelIfNoTargets,
        TargetType targetSelector,
        double radius,
        List<Map<String, Object>> conditions,
        List<Map<String, Object>> targetConditions,
        List<Map<String, Object>> triggerConditions,
        List<SkillEffectConfig> effects
) {

    public static SkillTemplate fromConfig(String id, ConfigurationSection config) {
        String selectorName = config.getString("target_selector", "TARGET");
        TargetType selector = TargetType.fromString(selectorName);

        double cooldown = config.getDouble("cooldown", 0.0);
        String onCooldownSkill = config.getString("on_cooldown_skill", null);
        boolean cancelIfNoTargets = config.getBoolean("cancel_if_no_targets", true);
        double radius = config.getDouble("radius", 5.0);

        List<Map<String, Object>> conditions = safeMapList(config, "conditions");
        List<Map<String, Object>> targetConditions = safeMapList(config, "target_conditions");
        List<Map<String, Object>> triggerConditions = safeMapList(config, "trigger_conditions");
        List<SkillEffectConfig> effects = parseEffects(config);

        return new SkillTemplate(
                id,
                cooldown,
                onCooldownSkill,
                cancelIfNoTargets,
                selector,
                radius,
                conditions,
                targetConditions,
                triggerConditions,
                effects
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> safeMapList(ConfigurationSection config, String path) {
        List<?> list = config.getList(path);
        if (list == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private static List<SkillEffectConfig> parseEffects(ConfigurationSection config) {
        List<?> list = config.getList("effects");
        if (list == null) {
            return Collections.emptyList();
        }
        List<SkillEffectConfig> result = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> raw) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) raw;
                String type = String.valueOf(map.getOrDefault("type", ""));
                Map<String, Object> params = extractParams(map);
                result.add(new SkillEffectConfig(type, params));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractParams(Map<String, Object> map) {
        Object paramsObj = map.get("params");
        if (paramsObj instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        Map<String, Object> params = new java.util.HashMap<>(map);
        params.remove("type");
        return params;
    }
}
