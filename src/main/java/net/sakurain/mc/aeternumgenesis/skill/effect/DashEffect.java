package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dashes the caster forward, damaging and knocking back entities in the path.
 */
public class DashEffect extends AbstractSkillEffect {

    public DashEffect() {
        super("dash");
    }

    @Override
    public void execute(SkillContext context) {
        LivingEntity caster = context.getCaster();
        if (caster == null) {
            return;
        }
        double distance = number("distance", 5.0);
        double damage = number("damage_on_pass", 0.0);
        double knockback = number("knockback", 0.5);
        double hitRadius = number("hit_radius", 1.5);
        boolean stopOnBlock = bool("stop_on_block", true);

        Vector direction = caster.getLocation().getDirection().setY(0).normalize();
        if (direction.lengthSquared() < 0.001) {
            direction = new Vector(0, 0, 1);
        }
        Location start = caster.getLocation().clone();
        World world = start.getWorld();
        if (world == null) {
            return;
        }

        Set<LivingEntity> hit = new HashSet<>();
        Location current = start.clone();
        double step = 0.5;
        double traveled = 0;

        while (traveled < distance) {
            current.add(direction.clone().multiply(step));
            traveled += step;
            if (stopOnBlock && !current.getBlock().isPassable()) {
                current.subtract(direction.clone().multiply(step));
                break;
            }
            spawnDashParticle(current);
            for (LivingEntity entity : world.getNearbyLivingEntities(current, hitRadius, hitRadius, hitRadius, e -> !e.equals(caster))) {
                if (hit.add(entity)) {
                    if (damage > 0) {
                        entity.damage(damage, caster);
                    }
                    if (knockback > 0) {
                        Vector kb = entity.getLocation().toVector().subtract(current.toVector()).normalize();
                        kb.setY(0.4);
                        entity.setVelocity(entity.getVelocity().add(kb.multiply(knockback)));
                    }
                }
            }
        }

        caster.teleport(current.setDirection(start.getDirection()));
        world.playSound(current, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    private void spawnDashParticle(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        String particleName = string("particle", "ASH");
        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            particle = Particle.ASH;
        }
        world.spawnParticle(particle, location, 3, 0.3, 0.3, 0.3, 0);
    }
}
