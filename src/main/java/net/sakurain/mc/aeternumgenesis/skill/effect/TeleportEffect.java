package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.skill.TargetType;
import net.sakurain.mc.aeternumgenesis.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class TeleportEffect extends AbstractSkillEffect {

    public TeleportEffect() {
        super("teleport");
    }

    @Override
    public void execute(SkillContext context) {
        LivingEntity target = singleTarget(context);
        if (target == null) {
            return;
        }
        Location destination = computeDestination(context, target);
        if (destination == null || destination.getWorld() == null) {
            return;
        }
        target.teleport(destination);
    }

    private Location computeDestination(SkillContext context, LivingEntity target) {
        String to = string("to", "ORIGIN").toUpperCase();
        double radius = number("radius", 3.0);

        return switch (to) {
            case "CASTER" -> context.getCaster() != null ? context.getCaster().getLocation() : null;
            case "TARGET" -> context.getTarget() != null ? context.getTarget().getLocation() : null;
            case "ORIGIN" -> context.getOrigin();
            case "BEHIND_CASTER" -> context.getCaster() != null
                    ? LocationUtil.behindTarget(context.getCaster().getLocation(), target.getLocation(), radius)
                    : null;
            case "BEHIND_TARGET" -> context.getTarget() != null
                    ? LocationUtil.behindTarget(context.getTarget().getLocation(), target.getLocation(), radius)
                    : null;
            case "AWAY_FROM_TARGET" -> context.getTarget() != null
                    ? LocationUtil.awayFrom(target.getLocation(), context.getTarget().getLocation(), radius)
                    : null;
            case "RANDOM_NEARBY" -> LocationUtil.randomNearby(target.getLocation(), 0, radius);
            default -> context.resolveLocation(TargetType.fromString(to), radius);
        };
    }
}
