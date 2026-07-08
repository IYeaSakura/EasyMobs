package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

import java.util.Map;

public interface SkillEffect extends net.sakurain.mc.aeternumgenesis.api.SkillEffect {

    String getType();

    void loadParameters(Map<String, Object> parameters);

    void execute(SkillContext context);
}
