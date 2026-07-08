package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

import java.util.Map;

public interface SkillCondition extends net.sakurain.mc.aeternumgenesis.api.SkillCondition {

    String getType();

    void load(Map<String, Object> parameters);

    boolean test(SkillContext context);
}
