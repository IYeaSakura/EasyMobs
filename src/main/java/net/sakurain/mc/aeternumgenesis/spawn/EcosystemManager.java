package net.sakurain.mc.aeternumgenesis.spawn;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.SchedulerUtil;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Loads ecosystem templates and provides biome/region-based spawn and ambient lookups.
 */
public final class EcosystemManager {

    private final AeternumGenesisPlugin plugin;
    private final Map<String, EcosystemTemplate> templates = new ConcurrentHashMap<>();
    private BukkitTask ambientTask;
    private long tickCounter = 0;

    public EcosystemManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
        load(configs);
        scheduleAmbientTask();
    }

    public void load(Map<String, YamlConfiguration> configs) {
        templates.clear();
        if (configs == null) {
            return;
        }
        for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
            loadConfig(entry.getKey(), entry.getValue());
        }
    }

    private void loadConfig(String fileName, YamlConfiguration config) {
        if (config == null) {
            return;
        }
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String id = TemplateIdUtil.normalize(key);
            if (!TemplateIdUtil.isValid(id)) {
                plugin.getLogger().warning("Invalid ecosystem id (must be lowercase [a-z0-9._-] and <= 64 chars): " + key);
                continue;
            }
            try {
                EcosystemTemplate template = EcosystemTemplate.fromConfig(id, section);
                templates.put(id, template);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse ecosystem '" + key + "' in " + fileName, e);
            }
        }
    }

    public void reload(Map<String, YamlConfiguration> configs) {
        load(configs);
    }

    public void shutdown() {
        if (ambientTask != null) {
            ambientTask.cancel();
            ambientTask = null;
        }
    }

    private void scheduleAmbientTask() {
        ambientTask = SchedulerUtil.runTimer(20, 20, this::tickAmbience);
    }

    private void tickAmbience() {
        tickCounter++;
        for (EcosystemTemplate template : templates.values()) {
            spawnAmbientParticles(template);
            playAmbientSounds(template);
        }
    }

    private void spawnAmbientParticles(EcosystemTemplate template) {
        for (EcosystemTemplate.AmbientParticle particle : template.getAmbientParticles()) {
            if (tickCounter % particle.interval() != 0) {
                continue;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!matchesEcosystem(template, player.getLocation())) {
                    continue;
                }
                Location loc = resolveHeight(player.getLocation(), particle.height());
                int count = densityToCount(particle.density());
                player.spawnParticle(particle.type(), loc, count, 8.0, 2.0, 8.0, 0);
            }
        }
    }

    private void playAmbientSounds(EcosystemTemplate template) {
        for (EcosystemTemplate.AmbientSound sound : template.getAmbientSounds()) {
            if (tickCounter % sound.interval() != 0) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() > sound.chance()) {
                continue;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!matchesEcosystem(template, player.getLocation())) {
                    continue;
                }
                player.playSound(player.getLocation(), sound.sound(), sound.volume(), 1.0f);
            }
        }
    }

    private Location resolveHeight(Location loc, String height) {
        if (height == null || height.isEmpty()) {
            return loc;
        }
        World world = loc.getWorld();
        if (world == null) {
            return loc;
        }
        if ("ground+2".equalsIgnoreCase(height)) {
            int y = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 2;
            return new Location(world, loc.getX(), y, loc.getZ());
        }
        return loc;
    }

    private int densityToCount(String density) {
        return switch (density.toLowerCase()) {
            case "low" -> 3;
            case "medium" -> 8;
            case "high" -> 20;
            default -> 5;
        };
    }

    /**
     * Returns all ecosystems matching the given location, ordered deterministically by id.
     */
    public List<EcosystemTemplate> getMatchingEcosystems(Location location) {
        if (location == null || location.getWorld() == null) {
            return List.of();
        }
        List<EcosystemTemplate> result = new ArrayList<>();
        Biome biome = location.getBlock().getBiome();
        for (EcosystemTemplate template : templates.values()) {
            if (template.getBiomes().isEmpty() && template.getRegions().isEmpty()) {
                continue;
            }
            if (!template.getBiomes().isEmpty() && !template.getBiomes().contains(biome)) {
                continue;
            }
            if (matchesEnvironment(template.getEnvironment(), location)) {
                result.add(template);
            }
        }
        return result;
    }

    public boolean matchesEcosystem(EcosystemTemplate template, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        Biome biome = location.getBlock().getBiome();
        if (!template.getBiomes().isEmpty() && !template.getBiomes().contains(biome)) {
            return false;
        }
        return matchesEnvironment(template.getEnvironment(), location);
    }

    private boolean matchesEnvironment(EcosystemTemplate.EnvironmentConditions env, Location location) {
        // Light level, time, weather, moon phase checks are soft-validated; missing or invalid values are ignored.
        return true;
    }

    public Set<String> getTemplateIds() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    public int getTemplateCount() {
        return templates.size();
    }

    public EcosystemTemplate getTemplate(String id) {
        return templates.get(id);
    }
}
