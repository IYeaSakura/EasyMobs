package net.sakurain.mc.easymobs.skill.effect;

import net.sakurain.mc.easymobs.skill.SkillContext;

import java.util.Map;

public interface SkillEffect extends net.sakurain.mc.easymobs.api.SkillEffect {

    String getType();

    void loadParameters(Map<String, Object> parameters);

    void execute(SkillContext context);
}
