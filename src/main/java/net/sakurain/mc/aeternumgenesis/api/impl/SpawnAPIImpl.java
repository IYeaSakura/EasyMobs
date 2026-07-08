package net.sakurain.mc.aeternumgenesis.api.impl;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.SpawnAPI;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobPreSpawnEvent;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobSpawnEvent;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.MobSpawner;
import net.sakurain.mc.aeternumgenesis.spawn.SpawnRule;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public class SpawnAPIImpl implements SpawnAPI {

    private final AeternumGenesisPlugin plugin;

    public SpawnAPIImpl(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public Collection<String> getAllRuleIds() {
        return plugin.getSpawnManager().getRules().stream().map(SpawnRule::getId).toList();
    }

    @Override
    public boolean hasRule(@NotNull String ruleId) {
        return plugin.getSpawnManager().getRules().stream().anyMatch(r -> r.getId().equals(ruleId));
    }

    @Override
    @NotNull
    public Optional<LivingEntity> triggerRule(@NotNull String ruleId, @NotNull Location location) {
        SpawnRule rule = plugin.getSpawnManager().getRules().stream()
                .filter(r -> r.getId().equals(ruleId))
                .findFirst().orElse(null);
        if (rule == null) {
            return Optional.empty();
        }
        CustomMobTemplate template = plugin.getMobManager().getTemplate(rule.getType());
        if (template == null) {
            return Optional.empty();
        }
        int level = rule.getLevel() > 0 ? rule.getLevel() : rule.getRandomLevel();
        CustomMobPreSpawnEvent preEvent = new CustomMobPreSpawnEvent(rule.getType(), location, level);
        org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return Optional.empty();
        }
        LivingEntity entity = MobSpawner.spawn(template, location);
        if (entity == null) {
            return Optional.empty();
        }
        if (preEvent.getLevel() > 1) {
            net.sakurain.mc.aeternumgenesis.mob.LevelSystem.applyLevel(entity, preEvent.getLevel(), template);
        }
        org.bukkit.Bukkit.getPluginManager().callEvent(new CustomMobSpawnEvent(rule.getType(), entity, location, preEvent.getLevel()));
        return Optional.of(entity);
    }

    @Override
    public boolean canSpawn(@NotNull String mobTemplateId, @NotNull Location location) {
        return plugin.getMobManager().hasTemplate(mobTemplateId);
    }

    @Override
    public int countActiveSpawnRules() {
        return plugin.getSpawnManager().getRuleCount();
    }

    @Override
    public boolean registerRule(@NotNull String ruleId, @NotNull ConfigurationSection config) {
        SpawnRule rule = SpawnRule.fromConfig(ruleId.toLowerCase(), config);
        if (rule == null) {
            return false;
        }
        return plugin.getSpawnManager().addRule(rule);
    }

    @Override
    public boolean unregisterRule(@NotNull String ruleId) {
        return plugin.getSpawnManager().removeRule(ruleId);
    }
}
