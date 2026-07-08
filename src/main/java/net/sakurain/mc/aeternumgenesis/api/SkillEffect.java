package net.sakurain.mc.aeternumgenesis.api;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface SkillEffect {

    @NotNull
    String getType();

    void loadParameters(@NotNull Map<String, Object> params);

    void execute(@NotNull SkillContext context);
}
