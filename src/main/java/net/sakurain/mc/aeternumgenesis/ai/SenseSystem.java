package net.sakurain.mc.aeternumgenesis.ai;

import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class SenseSystem {

    private SenseSystem() {
    }

    public static boolean canSense(Mob mob, LivingEntity target, CustomMobTemplate.SensesConfig senses) {
        if (senses == null) return true;
        double distance = mob.getLocation().distance(target.getLocation());

        if (senses.vision() && distance <= senses.visionRange()) {
            if (hasLineOfSight(mob, target)) return true;
        }
        if (senses.hearing() && distance <= senses.hearingRange()) {
            if (isMakingNoise(target)) return true;
        }
        return senses.smell() && distance <= senses.smellRange();
    }

    private static boolean hasLineOfSight(Mob mob, LivingEntity target) {
        Location eye = mob.getEyeLocation();
        Location targetEye = target.getEyeLocation();
        Vector direction = targetEye.toVector().subtract(eye.toVector()).normalize();
        RayTraceResult result = mob.getWorld().rayTraceBlocks(eye, direction, eye.distance(targetEye));
        return result == null;
    }

    private static boolean isMakingNoise(LivingEntity target) {
        if (target instanceof Player player) {
            return player.isSprinting() || player.isSwimming() || player.isBlocking();
        }
        return target.getVelocity().lengthSquared() > 0.01;
    }
}
