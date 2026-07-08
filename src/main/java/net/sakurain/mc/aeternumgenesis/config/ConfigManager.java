package net.sakurain.mc.aeternumgenesis.config;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final AeternumGenesisPlugin plugin;

    private final Map<String, YamlConfiguration> itemConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> mobConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> skillConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> spawnConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> setConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> blockConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> atmosphereConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> ecosystemConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> worldRuleConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> eventChainConfigs = new HashMap<>();

    public ConfigManager(AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        loadConfigs("items", itemConfigs);
        loadConfigs("mobs", mobConfigs);
        loadConfigs("skills", skillConfigs);
        loadConfigs("spawns", spawnConfigs);
        loadConfigs("sets", setConfigs);
        loadConfigs("blocks", blockConfigs);
        loadConfigs("atmospheres", atmosphereConfigs);
        loadConfigs("ecosystems", ecosystemConfigs);
        loadConfigs("worlds", worldRuleConfigs);
        loadConfigs("events", eventChainConfigs);
    }

    public void reloadAll() {
        itemConfigs.clear();
        mobConfigs.clear();
        skillConfigs.clear();
        spawnConfigs.clear();
        setConfigs.clear();
        blockConfigs.clear();
        atmosphereConfigs.clear();
        ecosystemConfigs.clear();
        worldRuleConfigs.clear();
        eventChainConfigs.clear();
        loadAll();
    }

    private void loadConfigs(String dirName, Map<String, YamlConfiguration> target) {
        File dir = new File(plugin.getDataFolder(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        loadConfigsRecursive(dir, dirName, target);
    }

    private void loadConfigsRecursive(File dir, String pathPrefix, Map<String, YamlConfiguration> target) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            if (name.contains("..") || name.contains(File.separator)) {
                continue;
            }
            if (file.isDirectory()) {
                loadConfigsRecursive(file, pathPrefix + "/" + name, target);
            } else if (name.endsWith(".yml")) {
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(file);
                    target.put(pathPrefix + "/" + name, config);
                } catch (IOException | InvalidConfigurationException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load " + pathPrefix + "/" + name, e);
                }
            }
        }
    }

    public Map<String, YamlConfiguration> getItemConfigs() {
        return Map.copyOf(itemConfigs);
    }

    public Map<String, YamlConfiguration> getMobConfigs() {
        return Map.copyOf(mobConfigs);
    }

    public Map<String, YamlConfiguration> getSkillConfigs() {
        return Map.copyOf(skillConfigs);
    }

    public Map<String, YamlConfiguration> getSpawnConfigs() {
        return Map.copyOf(spawnConfigs);
    }

    public Map<String, YamlConfiguration> getSetConfigs() {
        return Map.copyOf(setConfigs);
    }

    public Map<String, YamlConfiguration> getBlockConfigs() {
        return Map.copyOf(blockConfigs);
    }

    public Map<String, YamlConfiguration> getAtmosphereConfigs() {
        return Map.copyOf(atmosphereConfigs);
    }

    public Map<String, YamlConfiguration> getEcosystemConfigs() {
        return Map.copyOf(ecosystemConfigs);
    }

    public Map<String, YamlConfiguration> getWorldRuleConfigs() {
        return Map.copyOf(worldRuleConfigs);
    }

    public Map<String, YamlConfiguration> getEventChainConfigs() {
        return Map.copyOf(eventChainConfigs);
    }
}
