package net.sakurain.mc.aeternumgenesis.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface RegistryAPI {

    void registerEffect(@NotNull String type, @NotNull EffectFactory factory);

    void registerCondition(@NotNull String type, @NotNull ConditionFactory factory);

    void registerSpawnCondition(@NotNull String type, @NotNull SpawnConditionFactory factory);

    void unregisterEffect(@NotNull String type);

    void unregisterCondition(@NotNull String type);

    void unregisterSpawnCondition(@NotNull String type);

    @NotNull
    Collection<String> getRegisteredEffectTypes();

    @NotNull
    Collection<String> getRegisteredConditionTypes();

    @NotNull
    Collection<String> getRegisteredSpawnConditionTypes();

    @FunctionalInterface
    interface EffectFactory {
        SkillEffect create();
    }

    @FunctionalInterface
    interface ConditionFactory {
        SkillCondition create();
    }

    @FunctionalInterface
    interface SpawnConditionFactory {
        net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition create();
    }
}
