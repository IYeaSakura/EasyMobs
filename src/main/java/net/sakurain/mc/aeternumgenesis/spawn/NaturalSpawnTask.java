package net.sakurain.mc.aeternumgenesis.spawn;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobManager;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.LevelSystem;
import net.sakurain.mc.aeternumgenesis.mob.MobSpawner;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import net.sakurain.mc.aeternumgenesis.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Periodic task that handles ADD mode spawn rules around online players.
 */
public class NaturalSpawnTask implements Runnable {

    private final AeternumGenesisPlugin plugin;
    private final Random random;

    public NaturalSpawnTask() {
        this.plugin = AeternumGenesisPlugin.getInstance();
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public void run() {
        SpawnManager spawnManager = plugin.getSpawnManager();
        CustomMobManager mobManager = plugin.getMobManager();
        if (spawnManager == null || mobManager == null) {
            return;
        }

        int minDistance = plugin.getConfig().getInt("spawning.min-player-distance", 24);
        int maxDistance = plugin.getConfig().getInt("spawning.max-player-distance", 64);
        int maxAttempts = Math.max(1, Math.min(plugin.getConfig().getInt("spawning.max-spawn-attempts", 10), 50));
        int maxPerCycle = Math.max(0, Math.min(plugin.getConfig().getInt("spawning.max-spawns-per-cycle", 3), 50));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isSurvivalOrAdventure(player)) {
                continue;
            }
            Location playerLoc = player.getLocation();
            World world = playerLoc.getWorld();
            if (world == null) {
                continue;
            }

            List<SpawnRule> rules = spawnManager.getAddRules(world.getName());
            int spawned = 0;
            for (SpawnRule rule : rules) {
                if (spawned >= maxPerCycle) {
                    break;
                }
                if (Math.random() > rule.getChance()) {
                    continue;
                }

                Location point = selectSpawnPoint(world, playerLoc, rule.getPositionType(), minDistance, maxDistance, maxAttempts);
                if (point == null) {
                    continue;
                }
                if (!matchesPositionType(point, rule.getPositionType())) {
                    continue;
                }
                if (!matchesBiomes(rule, point)) {
                    continue;
                }
                if (!testConditions(rule, point)) {
                    continue;
                }
                if (!checkDensity(rule, point)) {
                    continue;
                }

                CustomMobTemplate template = mobManager.getTemplate(rule.getType());
                if (template == null) {
                    continue;
                }

                LivingEntity entity = MobSpawner.spawn(template, point);
                if (entity == null) {
                    continue;
                }

                int level = rule.getRandomLevel();
                if (level > 0) {
                    LevelSystem.applyLevel(entity, level, template);
                }
                spawned++;
            }
        }
    }

    private boolean isSurvivalOrAdventure(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    private Location selectSpawnPoint(World world, Location center, SpawnRule.PositionType type,
                                      int minDistance, int maxDistance, int maxAttempts) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                continue;
            }

            int surfaceY = world.getHighestBlockYAt(x, z);
            Location loc = switch (type) {
                case LAND, GROUND -> new Location(world, x + 0.5, surfaceY + 1, z + 0.5);
                case SEA -> {
                    int seaY = surfaceY;
                    int minHeight = world.getMinHeight();
                    while (seaY > minHeight && !world.getBlockAt(x, seaY, z).isLiquid()) {
                        seaY--;
                    }
                    yield new Location(world, x + 0.5, seaY + 1, z + 0.5);
                }
                case AIR -> {
                    int airY = surfaceY + 5 + random.nextInt(15);
                    yield new Location(world, x + 0.5, airY, z + 0.5);
                }
                case UNDERGROUND -> {
                    int undergroundY = 11 + random.nextInt(40);
                    yield new Location(world, x + 0.5, undergroundY, z + 0.5);
                }
                case null, default -> new Location(world, x + 0.5, surfaceY + 1, z + 0.5);
            };

            if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                continue;
            }
            return loc;
        }
        return null;
    }

    private boolean matchesPositionType(Location location, SpawnRule.PositionType type) {
        if (type == null) {
            return true;
        }
        return switch (type) {
            case LAND -> LocationUtil.isLand(location);
            case SEA -> LocationUtil.isSea(location);
            case GROUND -> LocationUtil.isLand(location) && LocationUtil.isOutside(location);
            case AIR -> LocationUtil.isAir(location);
            case UNDERGROUND -> LocationUtil.isUnderground(location);
        };
    }

    private boolean matchesBiomes(SpawnRule rule, Location location) {
        if (rule.getBiomes().isEmpty()) {
            return true;
        }
        Biome biome = location.getBlock().getBiome();
        return rule.getBiomes().contains(biome.getKey().getKey().toUpperCase());
    }

    private boolean testConditions(SpawnRule rule, Location location) {
        for (SpawnCondition condition : rule.getConditions()) {
            if (!condition.test(location, null)) {
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
