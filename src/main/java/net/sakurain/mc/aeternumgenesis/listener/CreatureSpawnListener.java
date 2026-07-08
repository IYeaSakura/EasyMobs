package net.sakurain.mc.aeternumgenesis.listener;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.LevelSystem;
import net.sakurain.mc.aeternumgenesis.mob.MobSpawner;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import net.sakurain.mc.aeternumgenesis.spawn.SpawnManager;
import net.sakurain.mc.aeternumgenesis.spawn.SpawnRule;
import net.sakurain.mc.aeternumgenesis.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles REPLACE and DENY natural spawn rules via {@link CreatureSpawnEvent}.
 */
public class CreatureSpawnListener implements Listener {

    private static final Set<CreatureSpawnEvent.SpawnReason> TRACKED_REASONS = Set.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
            CreatureSpawnEvent.SpawnReason.PATROL
    );

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!TRACKED_REASONS.contains(event.getSpawnReason())) {
            return;
        }

        Location loc = event.getLocation();
        World world = loc.getWorld();
        if (world == null) {
            return;
        }

        EntityType originalType = event.getEntityType();
        String worldName = world.getName();
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        SpawnManager spawnManager = plugin.getSpawnManager();

        // DENY rules take priority over REPLACE rules.
        List<SpawnRule> denyRules = spawnManager.getDenyRules(originalType, worldName);
        for (SpawnRule rule : denyRules) {
            if (!matchesSpawnReasons(rule, event.getSpawnReason())) {
                continue;
            }
            if (!matchesBiomes(rule, loc)) {
                continue;
            }
            if (!testConditions(rule, loc, originalType)) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() > rule.getChance()) {
                continue;
            }
            event.setCancelled(true);
            return;
        }

        List<SpawnRule> replaceRules = spawnManager.getReplaceRules(originalType, worldName);
        for (SpawnRule rule : replaceRules) {
            if (!matchesSpawnReasons(rule, event.getSpawnReason())) {
                continue;
            }
            if (!matchesBiomes(rule, loc)) {
                continue;
            }
            if (!testConditions(rule, loc, originalType)) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() > rule.getChance()) {
                continue;
            }
            if (!checkDensity(rule, loc)) {
                continue;
            }

            CustomMobTemplate template = plugin.getMobManager().getTemplate(rule.getType());
            if (template == null) {
                continue;
            }

            event.setCancelled(true);
            int level = rule.getRandomLevel();
            SchedulerUtil.runLater(1L, () -> {
                LivingEntity entity = MobSpawner.spawn(template, loc);
                if (entity != null && level > 0) {
                    LevelSystem.applyLevel(entity, level, template);
                }
            });
            return;
        }
    }

    private boolean matchesSpawnReasons(SpawnRule rule, CreatureSpawnEvent.SpawnReason reason) {
        Set<CreatureSpawnEvent.SpawnReason> reasons = rule.getSpawnReasons();
        return reasons.isEmpty() || reasons.contains(reason);
    }

    private boolean matchesBiomes(SpawnRule rule, Location location) {
        if (rule.getBiomes().isEmpty()) {
            return true;
        }
        Biome biome = location.getBlock().getBiome();
        return rule.getBiomes().contains(biome.getKey().getKey().toUpperCase());
    }

    private boolean testConditions(SpawnRule rule, Location location, EntityType originalType) {
        for (SpawnCondition condition : rule.getConditions()) {
            if (!condition.test(location, originalType)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDensity(SpawnRule rule, Location location) {
        SpawnRule.DensityLimits limits = rule.getDensityLimits();
        if (limits == null) {
            return true;
        }
        World world = location.getWorld();
        if (world == null || !world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return true;
        }
        if (limits.maxPerChunk() > 0) {
            int count = MobTracker.getInstance().countMobsInChunk(location.getChunk());
            if (count >= limits.maxPerChunk()) {
                return false;
            }
        }
        SpawnRule.MaxPerRadius radius = limits.maxPerRadius();
        if (radius != null && radius.amount() > 0 && radius.radius() > 0 && radius.template() != null) {
            int count = MobTracker.getInstance().countNearbyMobs(location, radius.radius(), radius.template());
            if (count >= radius.amount()) {
                return false;
            }
        }
        return true;
    }
}
