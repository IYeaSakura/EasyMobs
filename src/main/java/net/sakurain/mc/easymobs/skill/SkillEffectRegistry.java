package net.sakurain.mc.easymobs.skill;

import net.sakurain.mc.easymobs.skill.effect.SkillEffect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class SkillEffectRegistry {

    private final Map<String, Supplier<SkillEffect>> effects = new HashMap<>();

    public void register(String type, Supplier<SkillEffect> supplier) {
        effects.put(type.toLowerCase(), supplier);
    }

    public Optional<SkillEffect> create(String type) {
        Supplier<SkillEffect> supplier = effects.get(type.toLowerCase());
        if (supplier == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(supplier.get());
    }

    public Set<String> getTypes() {
        return Collections.unmodifiableSet(effects.keySet());
    }
}
