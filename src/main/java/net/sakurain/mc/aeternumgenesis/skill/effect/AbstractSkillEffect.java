package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.skill.TargetType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public abstract class AbstractSkillEffect implements SkillEffect {

    protected final String type;
    protected Map<String, Object> parameters = Map.of();

    protected AbstractSkillEffect(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void loadParameters(Map<String, Object> parameters) {
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
    }

    protected Map<String, Object> parameters() {
        return parameters;
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

    protected TargetType target(String def) {
        return TargetType.fromString(string("target", def));
    }

    protected Location location(SkillContext context) {
        String loc = string("location", "");
        if (!loc.isEmpty()) {
            try {
                return context.resolveLocation(TargetType.fromString(loc), number("radius", 3.0));
            } catch (Exception e) {
                AeternumGenesisPlugin.getInstance().getLogger().fine("Failed to resolve location '" + loc + "' for skill effect " + type + ": " + e.getMessage());
            }
        }
        return context.getOrigin() != null ? context.getOrigin() : context.getCaster().getLocation();
    }

    protected LivingEntity singleTarget(SkillContext context) {
        String targetName = string("target", "TARGET");
        TargetType type = TargetType.fromString(targetName);
        if (type == TargetType.TARGET) {
            return context.getTarget();
        }
        return context.resolveSingleTarget(type);
    }

    protected List<LivingEntity> targets(SkillContext context, double radius) {
        return context.resolveTargets(target("TARGET"), radius);
    }
}
