package net.sakurain.mc.aeternumgenesis.api;

import net.sakurain.mc.aeternumgenesis.api.SkillCondition;
import net.sakurain.mc.aeternumgenesis.api.SkillEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface SkillAPI {

    boolean triggerSkill(@NotNull LivingEntity caster, @NotNull String skillId);

    boolean triggerSkill(@NotNull LivingEntity caster, LivingEntity target, @NotNull String skillId);

    boolean isOnCooldown(@NotNull LivingEntity entity, @NotNull String skillId);

    long getRemainingCooldown(@NotNull LivingEntity entity, @NotNull String skillId);

    void setCooldown(@NotNull LivingEntity entity, @NotNull String skillId, long cooldownMs);

    void clearAllCooldowns(@NotNull LivingEntity entity);

    @NotNull
    Collection<String> getAllSkillIds();

    boolean hasSkill(@NotNull String skillId);

    @NotNull
    Optional<Long> getSkillCooldown(@NotNull String skillId);

    void playParticle(@NotNull Location location, @NotNull String particleConfig);

    void playSound(@NotNull Location location, @NotNull String sound, float volume, float pitch);

    void registerEffect(@NotNull String type, @NotNull Supplier<SkillEffect> factory);

    void registerCondition(@NotNull String type, @NotNull Supplier<SkillCondition> factory);

    boolean registerSkill(@NotNull String skillId, @NotNull ConfigurationSection config);

    boolean unregisterSkill(@NotNull String skillId);
}
