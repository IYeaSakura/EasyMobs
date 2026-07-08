package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class ProjectileEffect extends AbstractSkillEffect {

    private static final NamespacedKey PROJECTILE_KEY = new NamespacedKey("genesis", "projectile");

    public ProjectileEffect() {
        super("projectile");
    }

    @Override
    public void execute(SkillContext context) {
        LivingEntity caster = context.getCaster();
        if (caster == null) {
            return;
        }
        LivingEntity target = singleTarget(context);
        String projectileType = string("projectile_type", "ARROW").toUpperCase();
        double speed = number("speed", 2.0);
        double damage = number("damage", 5.0);
        int knockback = integer("knockback", 0);
        int fireTicks = integer("fire_ticks", 0);
        boolean gravity = bool("gravity", true);

        Location eye = caster.getEyeLocation();
        Vector direction;
        if (target != null) {
            direction = target.getEyeLocation().toVector().subtract(eye.toVector());
            if (direction.lengthSquared() == 0) {
                direction = eye.getDirection();
            } else {
                direction.normalize();
            }
        } else {
            direction = eye.getDirection();
        }
        if (direction.lengthSquared() == 0) {
            return;
        }

        Projectile projectile = spawnProjectile(eye, projectileType);
        if (projectile == null) {
            return;
        }
        projectile.setShooter(caster);
        projectile.setVelocity(direction.multiply(speed));

        if (projectile instanceof AbstractArrow arrow) {
            arrow.setDamage(damage);
            // setKnockbackStrength is deprecated/no-op in modern Paper; knockback is handled on hit.
            arrow.setFireTicks(fireTicks);
            arrow.setGravity(gravity);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        } else if (projectile instanceof Trident trident) {
            trident.setDamage(damage);
        }

        String meta = "damage=" + damage + ",knockback=" + knockback + ",fire=" + fireTicks;
        projectile.getPersistentDataContainer().set(PROJECTILE_KEY, PersistentDataType.STRING, meta);
    }

    private Projectile spawnProjectile(Location location, String type) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }
        return switch (type) {
            case "ARROW" -> world.spawnArrow(location, location.getDirection(), 0.0f, 0.0f);
            case "TRIDENT" -> world.spawn(location, Trident.class);
            case "FIREBALL" -> world.spawn(location, Fireball.class);
            case "SMALL_FIREBALL" -> world.spawn(location, SmallFireball.class);
            case "WITHER_SKULL" -> world.spawn(location, WitherSkull.class);
            case "DRAGON_FIREBALL" -> world.spawn(location, DragonFireball.class);
            case "SNOWBALL" -> world.spawn(location, Snowball.class);
            default -> world.spawnArrow(location, location.getDirection(), 0.0f, 0.0f);
        };
    }
}
