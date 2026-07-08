package net.sakurain.mc.aeternumgenesis.api;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface SpawnAPI {

    @NotNull
    Collection<String> getAllRuleIds();

    boolean hasRule(@NotNull String ruleId);

    @NotNull
    Optional<LivingEntity> triggerRule(@NotNull String ruleId, @NotNull Location location);

    boolean canSpawn(@NotNull String mobTemplateId, @NotNull Location location);

    int countActiveSpawnRules();

    boolean registerRule(@NotNull String ruleId, @NotNull ConfigurationSection config);

    boolean unregisterRule(@NotNull String ruleId);
}
