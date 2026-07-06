package net.sakurain.mc.easymobs.config;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
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

    private final EasyMobsPlugin plugin;

    private final Map<String, YamlConfiguration> itemConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> mobConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> skillConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> spawnConfigs = new HashMap<>();
    private final Map<String, YamlConfiguration> setConfigs = new HashMap<>();

    public ConfigManager(EasyMobsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        loadConfigs("items", itemConfigs);
        loadConfigs("mobs", mobConfigs);
        loadConfigs("skills", skillConfigs);
        loadConfigs("spawns", spawnConfigs);
        loadConfigs("sets", setConfigs);
    }

    public void reloadAll() {
        itemConfigs.clear();
        mobConfigs.clear();
        skillConfigs.clear();
        spawnConfigs.clear();
        setConfigs.clear();
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
            if (file.isDirectory()) {
                loadConfigsRecursive(file, pathPrefix + "/" + file.getName(), target);
            } else if (file.getName().endsWith(".yml")) {
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(file);
                    target.put(pathPrefix + "/" + file.getName(), config);
                } catch (IOException | InvalidConfigurationException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load " + pathPrefix + "/" + file.getName(), e);
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
}
