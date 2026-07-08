package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Applies a temporary atmosphere at the target location.
 */
public class AtmosphereSkillEffect extends AbstractSkillEffect {

    public AtmosphereSkillEffect() {
        super("atmosphere");
    }

    @Override
    public void execute(SkillContext context) {
        String atmosphereId = string("atmosphere_id", null);
        if (atmosphereId == null || atmosphereId.isBlank()) {
            return;
        }
        double radius = number("radius", 15.0);
        long duration = (long) (number("duration", 300.0) * 20.0);
        Location center = resolveCenter(context);
        if (center == null || center.getWorld() == null) {
            return;
        }
        AeternumGenesisPlugin plugin = context.getPlugin();
        UUID instanceId = plugin.getAtmosphereManager().applyAtmosphere(center, radius, atmosphereId, duration);
        if (instanceId == null && plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().warning("[Skill] Unknown atmosphere id: " + atmosphereId);
        }
    }

    private Location resolveCenter(SkillContext context) {
        String centerType = string("center", "caster");
        return switch (centerType.toLowerCase()) {
            case "target" -> context.getTarget() != null ? context.getTarget().getLocation() : null;
            case "origin" -> context.getOrigin();
            default -> context.getCaster().getLocation();
        };
    }
}
