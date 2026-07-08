package net.sakurain.mc.aeternumgenesis.spawn;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.SchedulerUtil;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Loads, stores, and queries {@link SpawnRule}s from YAML configurations.
 */
public final class SpawnManager {

    private final AeternumGenesisPlugin plugin;
    private Map<String, YamlConfiguration> configs = Map.of();
    private List<SpawnRule> rules = List.of();
    private BukkitTask addTask;
    private final Map<String, Supplier<SpawnCondition>> conditionRegistry = new HashMap<>();

    public SpawnManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
        this.configs = configs == null ? Map.of() : Map.copyOf(configs);
        registerDefaultConditions();
        load();
        scheduleAddTask();
    }

    private void registerDefaultConditions() {
        // Default spawn conditions are created directly by SpawnConditionParser.
        // This registry is exposed for external plugins via the API.
    }

    public void registerCondition(String type, Supplier<SpawnCondition> supplier) {
        conditionRegistry.put(type.toLowerCase(), supplier);
    }

    public void unregisterCondition(String type) {
        conditionRegistry.remove(type.toLowerCase());
    }

    public Set<String> getRegisteredConditionTypes() {
        return Collections.unmodifiableSet(conditionRegistry.keySet());
    }

    /**
     * Loads or reloads all spawn rules from the current configs.
     */
    public void load() {
        List<SpawnRule> loaded = new ArrayList<>();
        for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
            loaded.addAll(loadConfig(entry.getKey(), entry.getValue()));
        }
        loaded.sort(Comparator.comparingInt(SpawnRule::getPriority).reversed());
        this.rules = List.copyOf(loaded);
    }

    private List<SpawnRule> loadConfig(String fileName, YamlConfiguration config) {
        List<SpawnRule> result = new ArrayList<>();
        if (config == null) {
            return result;
        }
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String id = TemplateIdUtil.normalize(key);
            if (!TemplateIdUtil.isValid(id)) {
                plugin.getLogger().warning("Invalid spawn rule id (must be lowercase [a-z0-9._-] and <= 64 chars): " + key);
                continue;
            }
            try {
                SpawnRule rule = SpawnRule.fromConfig(id, section);
                if (rule != null) {
                    result.add(rule);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse spawn rule '" + key + "' in " + fileName, e);
            }
        }
        return result;
    }

    /**
     * Reloads rules from the plugin's current spawn configs and restarts the ADD task.
     */
    public void reload() {
        this.configs = Map.copyOf(plugin.getConfigManager().getSpawnConfigs());
        load();
        scheduleAddTask();
    }

    private void scheduleAddTask() {
        if (addTask != null) {
            addTask.cancel();
            addTask = null;
        }
        if (!plugin.getConfig().getBoolean("spawning.generate-spawn-points", true)) {
            return;
        }
        long interval = plugin.getConfig().getLong("spawning.add-task-interval", 100L);
        if (interval <= 0) {
            interval = 100L;
        }
        addTask = SchedulerUtil.runTimer(interval, interval, new NaturalSpawnTask());
    }

    /**
     * @return an unmodifiable list of all loaded rules sorted by priority descending
     */
    public List<SpawnRule> getRules() {
        return rules;
    }

    /**
     * Returns matching REPLACE rules for the given original entity type and world.
     *
     * @param originalType original entity type
     * @param world        world name
     * @return matching rules sorted by priority descending
     */
    public List<SpawnRule> getReplaceRules(EntityType originalType, String world) {
        if (originalType == null || world == null) {
            return List.of();
        }
        List<SpawnRule> result = new ArrayList<>();
        for (SpawnRule rule : rules) {
            if (rule.getAction() != SpawnRule.Action.REPLACE) {
                continue;
            }
            if (!rule.getReplaceTypes().contains(originalType)) {
                continue;
            }
            if (matchesWorld(rule, world)) {
                result.add(rule);
            }
        }
        return result;
    }

    /**
     * Returns matching DENY rules for the given original entity type and world.
     *
     * @param originalType original entity type
     * @param world        world name
     * @return matching rules sorted by priority descending
     */
    public List<SpawnRule> getDenyRules(EntityType originalType, String world) {
        if (originalType == null || world == null) {
            return List.of();
        }
        List<SpawnRule> result = new ArrayList<>();
        for (SpawnRule rule : rules) {
            if (rule.getAction() != SpawnRule.Action.DENY) {
                continue;
            }
            if (!rule.getDenyTypes().contains(originalType)) {
                continue;
            }
            if (matchesWorld(rule, world)) {
                result.add(rule);
            }
        }
        return result;
    }

    /**
     * Returns all ADD rules applicable to the given world.
     *
     * @param world world name
     * @return matching rules sorted by priority descending
     */
    public List<SpawnRule> getAddRules(String world) {
        if (world == null) {
            return List.of();
        }
        List<SpawnRule> result = new ArrayList<>();
        for (SpawnRule rule : rules) {
            if (rule.getAction() != SpawnRule.Action.ADD) {
                continue;
            }
            if (matchesWorld(rule, world)) {
                result.add(rule);
            }
        }
        return result;
    }

    private boolean matchesWorld(SpawnRule rule, String world) {
        Set<String> worlds = rule.getWorlds();
        return worlds.isEmpty() || worlds.contains(world);
    }

    public int getRuleCount() {
        return rules.size();
    }

    public boolean addRule(SpawnRule rule) {
        if (rule == null) {
            return false;
        }
        List<SpawnRule> updated = new ArrayList<>(this.rules);
        updated.removeIf(r -> r.getId().equalsIgnoreCase(rule.getId()));
        updated.add(rule);
        updated.sort(Comparator.comparingInt(SpawnRule::getPriority).reversed());
        this.rules = List.copyOf(updated);
        return true;
    }

    public boolean removeRule(String id) {
        if (id == null) {
            return false;
        }
        List<SpawnRule> updated = new ArrayList<>(this.rules);
        boolean removed = updated.removeIf(r -> r.getId().equalsIgnoreCase(id));
        if (removed) {
            this.rules = List.copyOf(updated);
        }
        return removed;
    }
}
