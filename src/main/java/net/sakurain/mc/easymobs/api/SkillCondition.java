package net.sakurain.mc.easymobs.api;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface SkillCondition {

    @NotNull
    String getType();

    void load(@NotNull Map<String, Object> params);

    boolean test(@NotNull SkillContext context);
}
