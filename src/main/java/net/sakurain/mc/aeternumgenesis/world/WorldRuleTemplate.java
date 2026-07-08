package net.sakurain.mc.aeternumgenesis.world;

import net.sakurain.mc.aeternumgenesis.util.GameRuleUtil;
import org.bukkit.GameRule;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable data class representing global world rules loaded from YAML.
 */
public final class WorldRuleTemplate {

    private final Map<GameRule<?>, Object> gameRules;
    private final Boolean keepInventory;
    private final Boolean keepExperience;
    private final String respawnWorld;
    private final String deathMessageFormat;
    private final Boolean pvp;
    private final Double fallDamageMultiplier;
    private final Double fireDamageMultiplier;
    private final Double hungerDrainRate;

    public WorldRuleTemplate(Map<GameRule<?>, Object> gameRules, Boolean keepInventory, Boolean keepExperience,
                             String respawnWorld, String deathMessageFormat, Boolean pvp,
                             Double fallDamageMultiplier, Double fireDamageMultiplier, Double hungerDrainRate) {
        this.gameRules = gameRules == null ? Map.of() : Map.copyOf(gameRules);
        this.keepInventory = keepInventory;
        this.keepExperience = keepExperience;
        this.respawnWorld = respawnWorld;
        this.deathMessageFormat = deathMessageFormat;
        this.pvp = pvp;
        this.fallDamageMultiplier = fallDamageMultiplier;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.hungerDrainRate = hungerDrainRate;
    }

    public Map<GameRule<?>, Object> getGameRules() {
        return gameRules;
    }

    public Boolean getKeepInventory() {
        return keepInventory;
    }

    public Boolean getKeepExperience() {
        return keepExperience;
    }

    public String getRespawnWorld() {
        return respawnWorld;
    }

    public String getDeathMessageFormat() {
        return deathMessageFormat;
    }

    public Boolean getPvp() {
        return pvp;
    }

    public Double getFallDamageMultiplier() {
        return fallDamageMultiplier;
    }

    public Double getFireDamageMultiplier() {
        return fireDamageMultiplier;
    }

    public Double getHungerDrainRate() {
        return hungerDrainRate;
    }

    public static WorldRuleTemplate fromConfig(ConfigurationSection config) {
        if (config == null) {
            return new WorldRuleTemplate(Map.of(), null, null, null, null, null, null, null, null);
        }
        ConfigurationSection global = config.getConfigurationSection("global");
        ConfigurationSection death = config.getConfigurationSection("death");
        ConfigurationSection player = config.getConfigurationSection("player");

        Map<GameRule<?>, Object> gameRules = parseGameRules(global);

        Boolean keepInventory = death == null ? null : parseBoolean(death.getString("keep_inventory"));
        Boolean keepExperience = death == null ? null : parseBoolean(death.getString("keep_experience"));
        String respawnWorld = death == null ? null : death.getString("respawn_world");
        String deathMessageFormat = death == null ? null : death.getString("death_message_format");

        Boolean pvp = player == null ? null : parseBoolean(player.getString("pvp"));
        Double fallDamageMultiplier = player == null ? null : parseDouble(player.getString("fall_damage_multiplier"));
        Double fireDamageMultiplier = player == null ? null : parseDouble(player.getString("fire_damage_multiplier"));
        Double hungerDrainRate = player == null ? null : parseDouble(player.getString("hunger_drain_rate"));

        // Map custom keys to Bukkit game rules where applicable.
        if (global != null) {
            addBooleanGameRule(global, "weather_cycle", "advance_weather", gameRules);
            addBooleanGameRule(global, "natural_regeneration", "natural_health_regeneration", gameRules);
            addBooleanGameRule(global, "natural_hunger", "natural_health_regeneration", gameRules);
            addBooleanGameRule(global, "mob_griefing", "mob_griefing", gameRules);
        }
        if (death != null) {
            addBooleanGameRule(death, "keep_inventory", "keep_inventory", gameRules);
        }

        return new WorldRuleTemplate(gameRules, keepInventory, keepExperience, respawnWorld,
                deathMessageFormat, pvp, fallDamageMultiplier, fireDamageMultiplier, hungerDrainRate);
    }

    private static Map<GameRule<?>, Object> parseGameRules(ConfigurationSection section) {
        Map<GameRule<?>, Object> result = new HashMap<>();
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            GameRule<?> rule = GameRuleUtil.getByName(key);
            if (rule == null) {
                continue;
            }
            Object value = section.get(key);
            result.put(rule, value);
        }
        return result;
    }

    private static void addBooleanGameRule(ConfigurationSection section, String key, String ruleName,
                                           Map<GameRule<?>, Object> target) {
        String value = section.getString(key);
        if (value == null || value.isEmpty()) {
            return;
        }
        Boolean parsed = parseBoolean(value);
        if (parsed == null) {
            return;
        }
        GameRule<?> rule = GameRuleUtil.getByName(ruleName);
        if (rule == null || rule.getType() != Boolean.class) {
            return;
        }
        target.put(rule, parsed);
    }

    private static Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
