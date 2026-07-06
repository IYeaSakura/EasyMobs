package net.sakurain.mc.easymobs.api.impl;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.api.MobAPI;
import net.sakurain.mc.easymobs.api.event.CustomMobPreSpawnEvent;
import net.sakurain.mc.easymobs.api.event.CustomMobSpawnEvent;
import net.sakurain.mc.easymobs.mob.CustomMobManager;
import net.sakurain.mc.easymobs.mob.CustomMobTemplate;
import net.sakurain.mc.easymobs.mob.LevelSystem;
import net.sakurain.mc.easymobs.mob.MobSpawner;
import net.sakurain.mc.easymobs.mob.MobTracker;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class MobAPIImpl implements MobAPI {

    private final EasyMobsPlugin plugin;

    public MobAPIImpl(@NotNull EasyMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isCustomMob(@Nullable Entity entity) {
        return entity instanceof LivingEntity living && MobTracker.getInstance().isCustomMob(living);
    }

    @Override
    public boolean isCustomMob(@Nullable Entity entity, @NotNull String templateId) {
        return isCustomMob(entity) && templateId.equals(getMobTemplateId(entity).orElse(null));
    }

    @Override
    @NotNull
    public Optional<String> getMobTemplateId(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return Optional.empty();
        }
        return Optional.ofNullable(MobTracker.getInstance().getMobTemplateId(living));
    }

    @Override
    @NotNull
    public Optional<LivingEntity> spawnMob(@NotNull String templateId, @NotNull Location location) {
        return spawnMob(templateId, location, 1);
    }

    @Override
    @NotNull
    public Optional<LivingEntity> spawnMob(@NotNull String templateId, @NotNull Location location, int level) {
        CustomMobManager manager = plugin.getMobManager();
        CustomMobTemplate template = manager.getTemplate(templateId);
        if (template == null) {
            return Optional.empty();
        }
        CustomMobPreSpawnEvent preEvent = new CustomMobPreSpawnEvent(templateId, location, level);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return Optional.empty();
        }
        LivingEntity entity = MobSpawner.spawn(template, location);
        if (entity == null) {
            return Optional.empty();
        }
        LevelSystem.applyLevel(entity, preEvent.getLevel(), template);
        Bukkit.getPluginManager().callEvent(new CustomMobSpawnEvent(templateId, entity, location, preEvent.getLevel()));
        return Optional.of(entity);
    }

    @Override
    @NotNull
    public Collection<LivingEntity> getAllActiveMobs() {
        return MobTracker.getInstance().getTrackedMobs();
    }

    @Override
    @NotNull
    public Collection<LivingEntity> getActiveMobs(@NotNull String templateId) {
        return getAllActiveMobs().stream()
                .filter(e -> templateId.equals(MobTracker.getInstance().getMobTemplateId(e)))
                .toList();
    }

    @Override
    @NotNull
    public Collection<LivingEntity> getActiveMobs(@NotNull Predicate<LivingEntity> filter) {
        return getAllActiveMobs().stream().filter(filter).toList();
    }

    @Override
    @NotNull
    public Optional<LivingEntity> getActiveMob(@NotNull UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity instanceof LivingEntity living && isCustomMob(living)) {
            return Optional.of(living);
        }
        return Optional.empty();
    }

    @Override
    @NotNull
    public Collection<LivingEntity> getNearbyMobs(@NotNull Location location, double radius) {
        return getAllActiveMobs().stream()
                .filter(e -> e.getWorld().equals(location.getWorld())
                        && e.getLocation().distanceSquared(location) <= radius * radius)
                .toList();
    }

    @Override
    @NotNull
    public Collection<LivingEntity> getNearbyMobs(@NotNull Location location, double radius, @NotNull String templateId) {
        return getNearbyMobs(location, radius).stream()
                .filter(e -> templateId.equals(MobTracker.getInstance().getMobTemplateId(e)))
                .toList();
    }

    @Override
    @NotNull
    public Optional<String> getTemplateDisplayName(@NotNull String templateId) {
        CustomMobTemplate template = plugin.getMobManager().getTemplate(templateId);
        return template != null ? Optional.ofNullable(template.getDisplayName()) : Optional.empty();
    }

    @Override
    @NotNull
    public Optional<Double> getTemplateMaxHealth(@NotNull String templateId) {
        CustomMobTemplate template = plugin.getMobManager().getTemplate(templateId);
        return template != null ? Optional.ofNullable(template.getMaxHealth()) : Optional.empty();
    }

    @Override
    @NotNull
    public Collection<String> getAllTemplateIds() {
        return plugin.getMobManager().getTemplateIds();
    }

    @Override
    public int countNearbyMobs(@NotNull Location location, double radius, @NotNull String templateId) {
        return MobTracker.getInstance().countNearbyMobs(location, radius, templateId);
    }

    @Override
    public int countMobsInChunk(@NotNull Chunk chunk) {
        return MobTracker.getInstance().countMobsInChunk(chunk);
    }

    @Override
    public void removeCustomTag(@NotNull LivingEntity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.remove(new org.bukkit.NamespacedKey(plugin, "ezmobs_mob_id"));
        pdc.remove(new org.bukkit.NamespacedKey(plugin, "ezmobs_level"));
    }

    @Override
    public boolean applyTemplate(@NotNull LivingEntity entity, @NotNull String templateId) {
        CustomMobTemplate template = plugin.getMobManager().getTemplate(templateId);
        if (template == null) {
            return false;
        }
        LivingEntity spawned = MobSpawner.spawn(template, entity.getLocation());
        if (spawned == null) {
            return false;
        }
        entity.remove();
        return true;
    }

    @Override
    public int getMobLevel(@NotNull LivingEntity entity) {
        Integer level = entity.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "ezmobs_level"), PersistentDataType.INTEGER);
        return level != null ? level : 1;
    }

    @Override
    public void setMobLevel(@NotNull LivingEntity entity, int level) {
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(entity);
        if (template != null) {
            LevelSystem.applyLevel(entity, level, template);
        }
    }
}
