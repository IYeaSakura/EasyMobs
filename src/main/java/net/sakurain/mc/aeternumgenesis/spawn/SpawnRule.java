package net.sakurain.mc.aeternumgenesis.spawn;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Immutable data class representing a single natural spawn rule loaded from YAML.
 */
public final class SpawnRule {

    private static final int MAX_LEVEL = 10_000;

    private final String id;
    private final Action action;
    private final String type;
    private final double chance;
    private final int priority;
    private final int level;
    private final int[] levelRange;
    private final Set<String> worlds;
    private final Set<String> biomes;
    private final PositionType positionType;
    private final Set<EntityType> replaceTypes;
    private final Set<EntityType> denyTypes;
    private final Set<CreatureSpawnEvent.SpawnReason> spawnReasons;
    private final List<SpawnCondition> conditions;
    private final DensityLimits densityLimits;

    public SpawnRule(String id, Action action, String type, double chance, int priority,
                     int level, int[] levelRange, Set<String> worlds, Set<String> biomes,
                     PositionType positionType, Set<EntityType> replaceTypes, Set<EntityType> denyTypes,
                     Set<CreatureSpawnEvent.SpawnReason> spawnReasons, List<SpawnCondition> conditions,
                     DensityLimits densityLimits) {
        this.id = id != null ? id.toLowerCase(Locale.ROOT) : "";
        this.action = action != null ? action : Action.ADD;
        this.type = type != null ? type.toLowerCase(Locale.ROOT) : "";
        this.chance = chance;
        this.priority = priority;
        this.level = level;
        this.levelRange = levelRange;
        this.worlds = worlds != null ? Set.copyOf(worlds) : Set.of();
        this.biomes = biomes != null ? Set.copyOf(biomes) : Set.of();
        this.positionType = positionType;
        this.replaceTypes = replaceTypes != null ? Set.copyOf(replaceTypes) : Set.of();
        this.denyTypes = denyTypes != null ? Set.copyOf(denyTypes) : Set.of();
        this.spawnReasons = spawnReasons != null ? Set.copyOf(spawnReasons) : Set.of();
        this.conditions = conditions != null ? List.copyOf(conditions) : List.of();
        this.densityLimits = densityLimits;
    }

    public String getId() {
        return id;
    }

    public Action getAction() {
        return action;
    }

    public String getType() {
        return type;
    }

    public double getChance() {
        return chance;
    }

    public int getPriority() {
        return priority;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Returns a resolved level for this spawn rule.
     * Fixed level takes precedence; otherwise the configured range is used.
     *
     * @return level, at least 1
     */
    public int getRandomLevel() {
        if (level > 0) {
            return Math.min(level, MAX_LEVEL);
        }
        if (levelRange != null && levelRange.length == 2) {
            int min = Math.max(1, Math.min(levelRange[0], levelRange[1]));
            int max = Math.max(1, Math.max(levelRange[0], levelRange[1]));
            min = Math.min(min, MAX_LEVEL);
            max = Math.min(max, MAX_LEVEL);
            if (max >= min) {
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            }
        }
        return 1;
    }

    public Set<String> getWorlds() {
        return worlds;
    }

    public Set<String> getBiomes() {
        return biomes;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public Set<EntityType> getReplaceTypes() {
        return replaceTypes;
    }

    public Set<EntityType> getDenyTypes() {
        return denyTypes;
    }

    public Set<CreatureSpawnEvent.SpawnReason> getSpawnReasons() {
        return spawnReasons;
    }

    public List<SpawnCondition> getConditions() {
        return conditions;
    }

    public DensityLimits getDensityLimits() {
        return densityLimits;
    }

    /**
     * Parses a rule from a configuration section.
     *
     * @param id      rule id
     * @param section configuration section
     * @return parsed rule, or null if invalid
     */
    public static SpawnRule fromConfig(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        Action action = parseEnum(Action.class, section.getString("action"), Action.ADD);
        String type = section.getString("type", "").trim();
        if (type.isEmpty()) {
            log("Spawn rule '" + id + "' is missing required field 'type'.");
            return null;
        }

        double chance = section.getDouble("chance", 0.0);
        int priority = section.getInt("priority", 10);
        int level = section.getInt("level", 0);
        int[] levelRange = parseLevelRange(section.getString("level_range"));

        Set<String> worlds = parseStringSet(section.getString("worlds"));
        Set<String> biomes = parseStringSetUpper(section.getString("biomes"));
        PositionType positionType = parseEnum(PositionType.class, section.getString("position_type"), null);

        Set<EntityType> replaceTypes = parseEntityTypeSet(section.getString("replace_types"));
        Set<EntityType> denyTypes = parseEntityTypeSet(section.getString("deny_types"));
        Set<CreatureSpawnEvent.SpawnReason> spawnReasons = parseSpawnReasonSet(section.getString("spawn_reasons"));

        List<SpawnCondition> conditions = new ArrayList<>();
        if (section.isList("conditions")) {
            for (String line : section.getStringList("conditions")) {
                SpawnCondition condition = SpawnConditionParser.parse(line);
                if (condition != null) {
                    conditions.add(condition);
                }
            }
        }

        DensityLimits densityLimits = parseDensityLimits(section.getConfigurationSection("density_limits"));

        return new SpawnRule(id, action, type, chance, priority, level, levelRange,
                worlds, biomes, positionType, replaceTypes, denyTypes, spawnReasons,
                conditions, densityLimits);
    }

    private static DensityLimits parseDensityLimits(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        int maxPerChunk = section.getInt("max_per_chunk", 0);
        MaxPerRadius maxPerRadius = null;
        ConfigurationSection radiusSection = section.getConfigurationSection("max_per_radius");
        if (radiusSection != null) {
            String template = radiusSection.getString("template");
            int amount = radiusSection.getInt("amount", 0);
            double radius = radiusSection.getDouble("radius", 0.0);
            if (template != null && !template.isEmpty() && amount > 0 && radius > 0) {
                maxPerRadius = new MaxPerRadius(template.toLowerCase(Locale.ROOT), amount, radius);
            }
        }
        return new DensityLimits(maxPerChunk, maxPerRadius);
    }

    private static int[] parseLevelRange(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.split("-", 2);
        if (parts.length != 2) {
            return null;
        }
        try {
            int min = Integer.parseInt(parts[0].trim());
            int max = Integer.parseInt(parts[1].trim());
            return new int[]{min, max};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Set<String> parseStringSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<String> set = new HashSet<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }

    private static Set<String> parseStringSetUpper(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<String> set = new HashSet<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim().toUpperCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }

    private static Set<EntityType> parseEntityTypeSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<EntityType> set = EnumSet.noneOf(EntityType.class);
        for (String part : value.split(",")) {
            String trimmed = part.trim().toUpperCase(Locale.ROOT);
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                set.add(EntityType.valueOf(trimmed));
            } catch (IllegalArgumentException e) {
                log("Unknown entity type in spawn rule: " + trimmed);
            }
        }
        return set;
    }

    private static Set<CreatureSpawnEvent.SpawnReason> parseSpawnReasonSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<CreatureSpawnEvent.SpawnReason> set = EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class);
        for (String part : value.split(",")) {
            String trimmed = part.trim().toUpperCase(Locale.ROOT);
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                set.add(CreatureSpawnEvent.SpawnReason.valueOf(trimmed));
            } catch (IllegalArgumentException e) {
                log("Unknown spawn reason in spawn rule: " + trimmed);
            }
        }
        return set;
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> clazz, String value, T def) {
        if (value == null || value.isBlank()) {
            return def;
        }
        try {
            return Enum.valueOf(clazz, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    private static void log(String message) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, message);
        }
    }

    public enum Action {
        ADD, REPLACE, DENY
    }

    public enum PositionType {
        LAND, SEA, GROUND, AIR, UNDERGROUND
    }

    /**
     * Density limits for a spawn rule.
     */
    public record DensityLimits(int maxPerChunk, MaxPerRadius maxPerRadius) {
    }

    /**
     * Radius-based density limit for a specific mob template.
     */
    public record MaxPerRadius(String template, int amount, double radius) {
    }
}
