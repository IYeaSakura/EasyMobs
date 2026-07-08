package net.sakurain.mc.aeternumgenesis.skill;

import net.sakurain.mc.aeternumgenesis.skill.condition.SkillCondition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class SkillConditionRegistry {

    private final Map<String, Supplier<SkillCondition>> conditions = new HashMap<>();

    public void register(String type, Supplier<SkillCondition> supplier) {
        conditions.put(type.toLowerCase(), supplier);
    }

    public Optional<SkillCondition> create(String type) {
        Supplier<SkillCondition> supplier = conditions.get(type.toLowerCase());
        if (supplier == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(supplier.get());
    }

    public Set<String> getTypes() {
        return Collections.unmodifiableSet(conditions.keySet());
    }

    public void unregister(String type) {
        conditions.remove(type.toLowerCase());
    }
}
