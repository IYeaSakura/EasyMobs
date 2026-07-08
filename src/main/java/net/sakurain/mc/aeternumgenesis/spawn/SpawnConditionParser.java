package net.sakurain.mc.aeternumgenesis.spawn;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.spawn.condition.*;

import java.util.logging.Level;

/**
 * Parses a condition line such as "night true" into a {@link SpawnCondition} instance.
 */
public final class SpawnConditionParser {

    private SpawnConditionParser() {
    }

    /**
     * Parses a condition line.
     *
     * @param line condition text
     * @return parsed condition, or null if unknown/invalid
     */
    public static SpawnCondition parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String trimmed = line.trim();
        String type;
        String args;
        int space = trimmed.indexOf(' ');
        if (space < 0) {
            type = trimmed.toLowerCase();
            args = "";
        } else {
            type = trimmed.substring(0, space).toLowerCase();
            args = stripQuotes(trimmed.substring(space + 1).trim());
        }

        SpawnCondition condition = createCondition(type);
        if (condition == null) {
            log("Unknown spawn condition type: " + type);
            return null;
        }
        condition.parse(args);
        return condition;
    }

    private static SpawnCondition createCondition(String type) {
        return switch (type) {
            case "night" -> new NightCondition();
            case "day" -> new DayCondition();
            case "raining" -> new RainingCondition();
            case "thundering" -> new ThunderingCondition();
            case "outside" -> new OutsideCondition();
            case "inside" -> new InsideCondition();
            case "moon_phase" -> new MoonPhaseCondition();
            case "y_above" -> new YAboveCondition();
            case "y_below" -> new YBelowCondition();
            case "y_range" -> new YRangeCondition();
            case "light_level" -> new LightLevelCondition();
            case "block_below" -> new BlockBelowCondition();
            case "time_range" -> new TimeRangeCondition();
            case "biome" -> new BiomeCondition();
            case "world" -> new WorldCondition();
            case "random_chance" -> new RandomChanceCondition();
            default -> null;
        };
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static void log(String message) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, message);
        }
    }
}
