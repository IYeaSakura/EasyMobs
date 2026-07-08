package net.sakurain.mc.aeternumgenesis.api;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface SkillCondition {

    @NotNull
    String getType();

    void load(@NotNull Map<String, Object> params);

    boolean test(@NotNull SkillContext context);
}
