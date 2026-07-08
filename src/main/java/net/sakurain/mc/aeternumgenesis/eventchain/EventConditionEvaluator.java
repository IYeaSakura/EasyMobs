package net.sakurain.mc.aeternumgenesis.eventchain;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates simple condition expressions for event chains.
 */
public final class EventConditionEvaluator {

    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*(==|!=|>=|<=|>|<)\\s*(.+?)\\s*$");

    public boolean evaluate(EventChainTemplate.Condition condition, EventChainInstance instance) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        String expr = condition.expression().trim().toLowerCase();

        // Special: boss_defeated (no registered bosses alive)
        if ("boss_defeated".equals(expr)) {
            return !instance.isAnyBossAlive();
        }
        // Special: boss_alive (any registered boss alive)
        if ("boss_alive".equals(expr)) {
            return instance.isAnyBossAlive();
        }

        Matcher matcher = COMPARISON_PATTERN.matcher(expr);
        if (!matcher.matches()) {
            return false;
        }
        String left = matcher.group(1);
        String operator = matcher.group(2);
        String right = matcher.group(3).trim();

        double leftValue = resolveVariable(left, instance);
        double rightValue;
        try {
            rightValue = Double.parseDouble(right);
        } catch (NumberFormatException e) {
            return false;
        }

        return compare(leftValue, operator, rightValue);
    }

    private double resolveVariable(String name, EventChainInstance instance) {
        return switch (name) {
            case "player_count_online" -> Bukkit.getOnlinePlayers().size();
            case "time" -> resolveWorldTime(instance);
            case "elapsed_ticks" -> instance.getElapsedTicks(Bukkit.getCurrentTick());
            default -> {
                Object context = instance.getContext(name);
                if (context instanceof Number n) {
                    yield n.doubleValue();
                }
                yield 0;
            }
        };
    }

    private long resolveWorldTime(EventChainInstance instance) {
        World world = null;
        if (instance.getInitiator() != null) {
            world = instance.getInitiator().getWorld();
        }
        if (world == null) {
            for (World w : Bukkit.getWorlds()) {
                world = w;
                break;
            }
        }
        return world != null ? world.getTime() : 0;
    }

    private boolean compare(double left, String operator, double right) {
        return switch (operator) {
            case "==" -> Double.compare(left, right) == 0;
            case "!=" -> Double.compare(left, right) != 0;
            case ">" -> left > right;
            case ">=" -> left >= right;
            case "<" -> left < right;
            case "<=" -> left <= right;
            default -> false;
        };
    }
}
