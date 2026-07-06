package net.sakurain.mc.easymobs.api.impl;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.api.SkillAPI;
import net.sakurain.mc.easymobs.api.SkillCondition;
import net.sakurain.mc.easymobs.api.SkillEffect;
import net.sakurain.mc.easymobs.skill.SkillManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class SkillAPIImpl implements SkillAPI {

    private final EasyMobsPlugin plugin;

    public SkillAPIImpl(@NotNull EasyMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean triggerSkill(@NotNull LivingEntity caster, @NotNull String skillId) {
        return triggerSkill(caster, null, skillId);
    }

    @Override
    public boolean triggerSkill(@NotNull LivingEntity caster, LivingEntity target, @NotNull String skillId) {
        SkillManager manager = plugin.getSkillManager();
        if (!manager.hasSkill(skillId)) {
            return false;
        }
        manager.triggerSkill(caster, target, skillId);
        return true;
    }

    @Override
    public boolean isOnCooldown(@NotNull LivingEntity entity, @NotNull String skillId) {
        return plugin.getSkillManager().isOnCooldown(entity, skillId);
    }

    @Override
    public long getRemainingCooldown(@NotNull LivingEntity entity, @NotNull String skillId) {
        double remainingSeconds = plugin.getSkillManager().getRemainingCooldown(entity, skillId);
        return (long) (remainingSeconds * 1000.0);
    }

    @Override
    public void setCooldown(@NotNull LivingEntity entity, @NotNull String skillId, long cooldownMs) {
        plugin.getSkillManager().setCooldown(entity, skillId, cooldownMs / 1000.0);
    }

    @Override
    public void clearAllCooldowns(@NotNull LivingEntity entity) {
        plugin.getSkillManager().clearAllCooldowns(entity);
    }

    @Override
    @NotNull
    public Collection<String> getAllSkillIds() {
        return plugin.getSkillManager().getAllSkillIds();
    }

    @Override
    public boolean hasSkill(@NotNull String skillId) {
        return plugin.getSkillManager().hasSkill(skillId);
    }

    @Override
    @NotNull
    public Optional<Long> getSkillCooldown(@NotNull String skillId) {
        double cd = plugin.getSkillManager().getSkillCooldown(skillId);
        return cd > 0 ? Optional.of((long) (cd * 1000)) : Optional.empty();
    }

    @Override
    public void playParticle(@NotNull Location location, @NotNull String particleConfig) {
        Particle particle = Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(particleConfig.toLowerCase()));
        if (particle != null && location.getWorld() != null) {
            location.getWorld().spawnParticle(particle, location, 10, 0.5, 0.5, 0.5);
        }
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, float volume, float pitch) {
        Sound s = Registry.SOUNDS.get(NamespacedKey.minecraft(sound.toLowerCase()));
        if (s != null && location.getWorld() != null) {
            location.getWorld().playSound(location, s, volume, pitch);
        }
    }

    @Override
    public void registerEffect(@NotNull String type, @NotNull Supplier<SkillEffect> factory) {
        plugin.getSkillManager().registerEffect(type, () -> new SkillEffectAdapter(factory.get()));
    }

    @Override
    public void registerCondition(@NotNull String type, @NotNull Supplier<SkillCondition> factory) {
        plugin.getSkillManager().registerCondition(type, () -> new SkillConditionAdapter(factory.get()));
    }

    public record SkillEffectAdapter(SkillEffect apiEffect) implements net.sakurain.mc.easymobs.skill.effect.SkillEffect {
        @Override
        public String getType() {
            return apiEffect.getType();
        }

        @Override
        public void loadParameters(java.util.Map<String, Object> parameters) {
            apiEffect.loadParameters(parameters);
        }

        @Override
        public void execute(net.sakurain.mc.easymobs.skill.SkillContext context) {
            apiEffect.execute(context);
        }
    }

    public record SkillConditionAdapter(SkillCondition apiCondition) implements net.sakurain.mc.easymobs.skill.condition.SkillCondition {
        @Override
        public String getType() {
            return apiCondition.getType();
        }

        @Override
        public void load(java.util.Map<String, Object> parameters) {
            apiCondition.load(parameters);
        }

        @Override
        public boolean test(net.sakurain.mc.easymobs.skill.SkillContext context) {
            return apiCondition.test(context);
        }
    }
}
