package net.sakurain.mc.aeternumgenesis.skill;

import net.sakurain.mc.aeternumgenesis.skill.effect.SkillEffect;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public record SkillEffectConfig(
        String type,
        Map<String, Object> parameters
) {

    public SkillEffectConfig {
        parameters = parameters != null ? Map.copyOf(parameters) : Collections.emptyMap();
    }

    public Optional<SkillEffect> createEffect(SkillEffectRegistry registry) {
        return registry.create(type).map(effect -> {
            effect.loadParameters(parameters);
            return effect;
        });
    }
}
