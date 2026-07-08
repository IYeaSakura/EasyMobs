package net.sakurain.mc.aeternumgenesis.mob;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.skill.SkillBinding;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Loads and stores {@link CustomMobTemplate}s from YAML configurations.
 */
public final class CustomMobManager {

    private final AeternumGenesisPlugin plugin;
    private Map<String, YamlConfiguration> configs = Map.of();
    private final Map<String, CustomMobTemplate> templates = new HashMap<>();

    public CustomMobManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
        this.configs = configs == null ? Map.of() : Map.copyOf(configs);
        load();
    }

    /**
     * Loads or reloads all mob templates from the current configs.
     */
    public void load() {
        templates.clear();
        plugin.getSkillManager().clearBindings();
        for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
            loadConfig(entry.getKey(), entry.getValue());
        }
    }

    private void loadConfig(String fileName, YamlConfiguration config) {
        String fileId = stripExtension(fileName);

        // If the root contains a type field, treat the whole file as one mob template.
        if (config.contains("type")) {
            String id = config.getString("id", fileId);
            loadTemplate(fileName, id, config);
            return;
        }

        // Otherwise each top-level section is a separate mob template.
        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) {
                continue;
            }
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String id = section.getString("id", key);
            loadTemplate(fileName, id, section);
        }
    }

    private void loadTemplate(String fileName, String rawId, ConfigurationSection section) {
        String id = TemplateIdUtil.normalize(rawId);
        if (!TemplateIdUtil.isValid(id)) {
            plugin.getLogger().warning("Invalid mob template id (must be lowercase [a-z0-9._-] and <= 64 chars): " + rawId);
            return;
        }
        try {
            CustomMobTemplate template = CustomMobTemplate.fromConfig(id, section);
            templates.put(template.getId(), template);

            // Register skill bindings with the global skill manager.
            for (SkillBinding binding : template.getSkills()) {
                plugin.getSkillManager().addBinding(binding);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse mob '" + id + "' in " + fileName, e);
        }
    }

    private String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index > 0 ? name.substring(0, index) : name;
    }

    /**
     * Reloads templates from the plugin's current mob configs.
     */
    public void reload() {
        this.configs = Map.copyOf(plugin.getConfigManager().getMobConfigs());
        load();
    }

    public CustomMobTemplate getTemplate(String id) {
        return id != null ? templates.get(id.toLowerCase(java.util.Locale.ROOT)) : null;
    }

    public boolean hasTemplate(String id) {
        return id != null && templates.containsKey(id.toLowerCase(java.util.Locale.ROOT));
    }

    public Set<String> getTemplateIds() {
        return Set.copyOf(templates.keySet());
    }

    public int getTemplateCount() {
        return templates.size();
    }

    public boolean addTemplate(String id, CustomMobTemplate template) {
        if (id == null || template == null) {
            return false;
        }
        String normalized = TemplateIdUtil.normalize(id);
        if (!TemplateIdUtil.isValid(normalized)) {
            plugin.getLogger().warning("Invalid mob template id: " + id);
            return false;
        }
        templates.put(normalized, template);
        return true;
    }

    public boolean removeTemplate(String id) {
        return id != null && templates.remove(id.toLowerCase(java.util.Locale.ROOT)) != null;
    }
}
