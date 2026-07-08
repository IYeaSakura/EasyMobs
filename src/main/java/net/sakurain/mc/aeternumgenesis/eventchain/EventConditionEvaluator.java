package net.sakurain.mc.aeternumgenesis.eventchain;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates event chain condition expressions.
 *
 * <p>Supported syntax:</p>
 * <ul>
 *   <li>Comparisons: {@code player_count_online > 5}, {@code time < 13000}, {@code elapsed_ticks >= 600}</li>
 *   <li>Time keywords: {@code time == night}, {@code time == day}, {@code time == dawn}, {@code time == dusk}</li>
 *   <li>Logical operators: {@code and(...)}, {@code or(...)}, {@code not(...)}</li>
 *   <li>Boss state: {@code boss_defeated}, {@code boss_alive},
 *       {@code boss_defeated(main)}, {@code boss_alive(main)}</li>
 *   <li>Mob count: {@code mob_count_in_radius(30) < 10}</li>
 * </ul>
 */
public final class EventConditionEvaluator {

    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "^\\s*(\\w+(?:\\s*\\([^)]*\\))?)\\s*(==|!=|>=|<=|>|<)\\s*(.+?)\\s*$");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*\\(([^)]*)\\)\\s*$");

    public boolean evaluate(EventChainTemplate.Condition condition, EventChainInstance instance) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        return evaluateExpression(condition.expression().trim().toLowerCase(), instance);
    }

    private boolean evaluateExpression(String expr, EventChainInstance instance) {
        expr = expr.trim();
        System.out.println("EVAL: '" + expr + "'");
        if (expr.startsWith("and(")) {
            return evaluateLogical(expr.substring(4), instance, true);
        }
        if (expr.startsWith("or(")) {
            return evaluateLogical(expr.substring(3), instance, false);
        }
        if (expr.startsWith("not(")) {
            String inner = extractParenthesized(expr.substring(4));
            return !evaluateExpression(inner, instance);
        }
        if (expr.startsWith("(")) {
            String inner = extractParenthesized(expr);
            return evaluateExpression(inner, instance);
        }

        if ("boss_defeated".equals(expr)) {
            return !instance.isAnyBossAlive();
        }
        if ("boss_alive".equals(expr)) {
            return instance.isAnyBossAlive();
        }

        Matcher functionMatcher = FUNCTION_PATTERN.matcher(expr);
        if (functionMatcher.matches()) {
            String name = functionMatcher.group(1);
            String arg = functionMatcher.group(2).trim();
            if ("boss_alive".equals(name)) {
                return instance.isBossAlive(arg.isEmpty() ? "boss" : arg);
            }
            if ("boss_defeated".equals(name)) {
                return !instance.isBossAlive(arg.isEmpty() ? "boss" : arg);
            }
        }

        Matcher comparisonMatcher = COMPARISON_PATTERN.matcher(expr);
        if (comparisonMatcher.matches()) {
            String left = comparisonMatcher.group(1).trim();
            String operator = comparisonMatcher.group(2);
            String right = comparisonMatcher.group(3).trim();
            return evaluateComparison(left, operator, right, instance);
        }

        return false;
    }

    private boolean evaluateLogical(String remainder, EventChainInstance instance, boolean isAnd) {
        String content = extractParenthesized(remainder);
        System.out.println("LOGICAL content: '" + content + "'");
        List<String> parts = splitTopLevel(content, ',');
        for (String part : parts) {
            boolean result = evaluateExpression(part, instance);
            if (isAnd && !result) {
                return false;
            }
            if (!isAnd && result) {
                return true;
            }
        }
        return isAnd;
    }

    private boolean evaluateComparison(String left, String operator, String right, EventChainInstance instance) {
        Matcher functionMatcher = FUNCTION_PATTERN.matcher(left);
        if (functionMatcher.matches()) {
            String name = functionMatcher.group(1);
            String arg = functionMatcher.group(2).trim();
            if ("mob_count_in_radius".equals(name)) {
                double radius;
                try {
                    radius = Double.parseDouble(arg);
                } catch (NumberFormatException e) {
                    return false;
                }
                double count = countMobsInRadius(instance, radius);
                double value;
                try {
                    value = Double.parseDouble(right);
                } catch (NumberFormatException e) {
                    return false;
                }
                return compare(count, operator, value);
            }
            if ("boss_alive".equals(name)) {
                boolean alive = instance.isBossAlive(arg.isEmpty() ? "boss" : arg);
                return compareBoolean(alive, operator, right);
            }
            if ("boss_defeated".equals(name)) {
                boolean defeated = !instance.isBossAlive(arg.isEmpty() ? "boss" : arg);
                return compareBoolean(defeated, operator, right);
            }
            return false;
        }

        if ("time".equals(left)) {
            return compareTime(right, operator, instance);
        }

        double leftValue = resolveNumericVariable(left, instance);
        double rightValue;
        try {
            rightValue = Double.parseDouble(right);
        } catch (NumberFormatException e) {
            return false;
        }
        return compare(leftValue, operator, rightValue);
    }

    private boolean compareTime(String keyword, String operator, EventChainInstance instance) {
        long time = resolveWorldTime(instance);
        int target = 0;
        boolean range = false;
        int start = 0;
        int end = 0;

        switch (keyword) {
            case "day" -> { start = 0; end = 12000; range = true; }
            case "night" -> { start = 13000; end = 24000; range = true; }
            case "dawn" -> target = 0;
            case "morning" -> target = 6000;
            case "noon" -> target = 6000;
            case "afternoon" -> target = 9000;
            case "dusk" -> target = 12000;
            case "midnight" -> target = 18000;
            default -> {
                try {
                    target = Integer.parseInt(keyword);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        if (range) {
            boolean inRange = time >= start && time < end;
            return "==".equals(operator) ? inRange : "!=".equals(operator) && !inRange;
        }
        return compare(time, operator, target);
    }

    private double resolveNumericVariable(String name, EventChainInstance instance) {
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
        Player initiator = instance.getInitiator();
        if (initiator != null) {
            world = initiator.getWorld();
        }
        if (world == null) {
            for (World w : Bukkit.getWorlds()) {
                world = w;
                break;
            }
        }
        return world != null ? world.getTime() : 0;
    }

    private int countMobsInRadius(EventChainInstance instance, double radius) {
        Location center = null;
        Player initiator = instance.getInitiator();
        if (initiator != null) {
            center = initiator.getLocation();
        }
        if (center == null) {
            World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
            if (world == null) {
                return 0;
            }
            center = world.getSpawnLocation();
        }
        int count = 0;
        for (LivingEntity entity : center.getNearbyLivingEntities(radius)) {
            if (!(entity instanceof Player)) {
                count++;
            }
        }
        return count;
    }

    private boolean compareBoolean(boolean value, String operator, String right) {
        boolean target = Boolean.parseBoolean(right);
        return switch (operator) {
            case "==" -> value == target;
            case "!=" -> value != target;
            default -> false;
        };
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

    private String extractParenthesized(String text) {
        text = text.trim();
        if (!text.startsWith("(")) {
            return text;
        }
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return text.substring(1, i);
                }
            }
        }
        return text.substring(1);
    }

    private List<String> splitTopLevel(String text, char delimiter) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                depth++;
            } else if (c == ')' || c == ']' || c == '}') {
                depth--;
            } else if (c == delimiter && depth == 0) {
                result.add(text.substring(start, i));
                start = i + 1;
            }
        }
        result.add(text.substring(start));
        return result;
    }
}
