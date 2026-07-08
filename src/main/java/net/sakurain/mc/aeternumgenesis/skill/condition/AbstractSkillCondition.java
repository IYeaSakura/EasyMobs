package net.sakurain.mc.aeternumgenesis.skill.condition;

import java.util.List;
import java.util.Map;

public abstract class AbstractSkillCondition implements SkillCondition {

    protected final String type;
    protected Map<String, Object> parameters = Map.of();

    protected AbstractSkillCondition(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void load(Map<String, Object> parameters) {
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
    }

    protected String string(String key, String def) {
        Object value = parameters.get(key);
        return value != null ? String.valueOf(value) : def;
    }

    protected double number(String key, double def) {
        Object value = parameters.get(key);
        if (value == null) return def;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected int integer(String key, int def) {
        Object value = parameters.get(key);
        if (value == null) return def;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected boolean bool(String key, boolean def) {
        Object value = parameters.get(key);
        if (value == null) return def;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    protected List<String> stringList(String key) {
        Object value = parameters.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::toLowerCase).toList();
        }
        if (value instanceof String s) {
            return List.of(s.toLowerCase());
        }
        return List.of();
    }

    protected boolean compare(double actual, String operator, double expected) {
        return switch (operator) {
            case ">" -> actual > expected;
            case ">=" -> actual >= expected;
            case "<" -> actual < expected;
            case "<=" -> actual <= expected;
            case "!=" -> actual != expected;
            default -> Double.compare(actual, expected) == 0;
        };
    }
}
