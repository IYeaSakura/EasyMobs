package net.sakurain.mc.aeternumgenesis.listener;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class ProjectileHitListener implements Listener {

    private static final NamespacedKey PROJECTILE_KEY = new NamespacedKey("genesis", "projectile");

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!projectile.getPersistentDataContainer().has(PROJECTILE_KEY, PersistentDataType.STRING)) {
            return;
        }
        String meta = projectile.getPersistentDataContainer().get(PROJECTILE_KEY, PersistentDataType.STRING);
        if (meta == null) {
            return;
        }
        double damage = parseDouble(meta, "damage");
        int knockback = parseInt(meta, "knockback");
        int fireTicks = parseInt(meta, "fire");

        if (event.getHitEntity() instanceof LivingEntity target && !target.isDead()) {
            if (damage > 0) {
                if (projectile.getShooter() instanceof LivingEntity shooter && !shooter.isDead()) {
                    target.damage(damage, shooter);
                } else {
                    target.damage(damage);
                }
            }
            if (knockback > 0) {
                Vector direction = target.getLocation().toVector().subtract(projectile.getLocation().toVector());
                if (direction.lengthSquared() > 0) {
                    direction.normalize().multiply(knockback * 0.4).setY(0.3);
                    target.setVelocity(direction);
                }
            }
            if (fireTicks > 0) {
                target.setFireTicks(fireTicks);
            }
        }
        projectile.getPersistentDataContainer().remove(PROJECTILE_KEY);
    }

    private double parseDouble(String meta, String key) {
        String value = parseValue(meta, key);
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String meta, String key) {
        String value = parseValue(meta, key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String parseValue(String meta, String key) {
        for (String part : meta.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals(key)) {
                return kv[1].trim();
            }
        }
        return null;
    }
}
