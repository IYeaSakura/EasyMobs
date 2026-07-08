package net.sakurain.mc.aeternumgenesis.eventchain;

import net.sakurain.mc.aeternumgenesis.util.ConfigParseUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Immutable template for an event chain loaded from YAML.
 */
public record EventChainTemplate(
        String id,
        Trigger trigger,
        List<Stage> stages,
        EndConfig onEnd
) {

    public record Trigger(
            Type type,
            double chance,
            long cooldownTicks,
            int minPlayers,
            int timeStart,
            int timeEnd,
            long checkIntervalTicks
    ) {
        public enum Type {
            RANDOM_NIGHT,
            RANDOM_DAY,
            TIME,
            MANUAL
        }
    }

    public record Stage(
            String id,
            long delayTicks,
            Condition condition,
            List<Action> actions
    ) {
    }

    public record Action(
            String type,
            Map<String, Object> parameters
    ) {
    }

    public record Condition(
            String expression
    ) {
        public boolean isBlank() {
            return expression == null || expression.isBlank();
        }
    }

    public record EndConfig(
            Condition condition,
            long timeoutTicks,
            List<Action> successActions,
            List<Action> failActions
    ) {
    }

    public static EventChainTemplate fromConfig(String id, ConfigurationSection config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration section is null");
        }
        ConfigurationSection root = config.getConfigurationSection(id);
        if (root == null) {
            root = config;
        }
        Trigger trigger = parseTrigger(root.getConfigurationSection("trigger"));
        List<Stage> stages = parseStages(root.getConfigurationSection("stages"));
        EndConfig onEnd = parseEndConfig(root.getConfigurationSection("on_end"));
        return new EventChainTemplate(id, trigger, stages, onEnd);
    }

    private static Trigger parseTrigger(ConfigurationSection section) {
        if (section == null) {
            return new Trigger(Trigger.Type.MANUAL, 0.0, 0, 0, -1, -1, 1200L);
        }
        Trigger.Type type;
        try {
            type = Trigger.Type.valueOf(section.getString("type", "manual").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            type = Trigger.Type.MANUAL;
        }
        double chance = ConfigParseUtil.toDouble(section.get("chance"), 1.0);
        long cooldownTicks = parseDuration(section.getString("cooldown", "0"));
        int minPlayers = section.getInt("min_players", 1);
        int timeStart = section.getInt("time_start", -1);
        int timeEnd = section.getInt("time_end", -1);
        long checkIntervalTicks = section.getLong("check_interval_ticks", 1200L);
        return new Trigger(type, chance, cooldownTicks, minPlayers, timeStart, timeEnd, checkIntervalTicks);
    }

    private static List<Stage> parseStages(ConfigurationSection section) {
        List<Stage> result = new ArrayList<>();
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection stageSection = section.getConfigurationSection(key);
            if (stageSection == null) {
                continue;
            }
            String stageId = stageSection.getString("id", key);
            long delayTicks = stageSection.getLong("delay", 0);
            Condition condition = new Condition(stageSection.getString("condition"));
            List<Action> actions = parseActions(stageSection.getConfigurationSection("actions"));
            result.add(new Stage(stageId, delayTicks, condition, actions));
        }
        return result;
    }

    private static List<Action> parseActions(ConfigurationSection section) {
        List<Action> result = new ArrayList<>();
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection actionSection = section.getConfigurationSection(key);
            if (actionSection == null) {
                continue;
            }
            String type = actionSection.getString("type");
            if (type == null || type.isBlank()) {
                continue;
            }
            Map<String, Object> parameters = new HashMap<>();
            for (String paramKey : actionSection.getKeys(false)) {
                if ("type".equals(paramKey)) {
                    continue;
                }
                parameters.put(paramKey, actionSection.get(paramKey));
            }
            result.add(new Action(type.toLowerCase(Locale.ROOT), Map.copyOf(parameters)));
        }
        return result;
    }

    private static EndConfig parseEndConfig(ConfigurationSection section) {
        if (section == null) {
            return new EndConfig(new Condition(null), -1, List.of(), List.of());
        }
        Condition condition = new Condition(section.getString("condition"));
        long timeoutTicks = section.getLong("timeout_ticks", -1);
        List<Action> successActions = parseActions(section.getConfigurationSection("success_actions"));
        List<Action> failActions = parseActions(section.getConfigurationSection("fail_actions"));
        return new EndConfig(condition, timeoutTicks, successActions, failActions);
    }

    /**
     * Parses a simple duration string into ticks.
     * Supported units: d (days), h (hours), m (minutes), s (seconds), t (ticks).
     * A plain number is treated as ticks.
     */
    static long parseDuration(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        long total = 0;
        int i = 0;
        while (i < value.length()) {
            int start = i;
            while (i < value.length() && (Character.isDigit(value.charAt(i)) || value.charAt(i) == '.')) {
                i++;
            }
            if (i == start) {
                break;
            }
            double amount;
            try {
                amount = Double.parseDouble(value.substring(start, i));
            } catch (NumberFormatException e) {
                break;
            }
            if (i >= value.length()) {
                total += (long) amount;
                break;
            }
            char unit = value.charAt(i);
            i++;
            switch (unit) {
                case 'd' -> total += (long) (amount * 24 * 60 * 60 * 20);
                case 'h' -> total += (long) (amount * 60 * 60 * 20);
                case 'm' -> total += (long) (amount * 60 * 20);
                case 's' -> total += (long) (amount * 20);
                case 't' -> total += (long) amount;
                default -> total += (long) amount;
            }
        }
        return total;
    }
}
