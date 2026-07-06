package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;

import java.util.Map;

public interface SkillCondition extends net.sakurain.mc.easymobs.api.SkillCondition {

    String getType();

    void load(Map<String, Object> parameters);

    boolean test(SkillContext context);
}
