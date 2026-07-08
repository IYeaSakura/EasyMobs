package net.sakurain.mc.aeternumgenesis.mob;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;

/**
 * Listener that cancels damage and effects based on a custom mob's immunities.
 */
public final class ImmunityHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living) || !MobTracker.getInstance().isCustomMob(living)) {
            return;
        }
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(living);
        if (template == null) {
            return;
        }
        if (isImmune(template, event.getCause())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living) || !MobTracker.getInstance().isCustomMob(living)) {
            return;
        }
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(living);
        if (template != null && template.getImmunities().fire()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living) || !MobTracker.getInstance().isCustomMob(living)) {
            return;
        }
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(living);
        if (template != null && template.getImmunities().potion()) {
            event.setCancelled(true);
        }
    }

    private boolean isImmune(CustomMobTemplate template, EntityDamageEvent.DamageCause cause) {
        CustomMobTemplate.ImmunitiesConfig immunities = template.getImmunities();
        return switch (cause) {
            case FIRE, FIRE_TICK, LAVA, HOT_FLOOR, CAMPFIRE -> immunities.fire();
            case PROJECTILE -> immunities.projectile();
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> immunities.melee();
            case MAGIC, POISON, WITHER, DRAGON_BREATH -> immunities.potion() || immunities.magic();
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> immunities.explosion();
            case FALL -> immunities.fall();
            case DROWNING -> immunities.drowning();
            case FREEZE -> immunities.freeze();
            case CONTACT, SUFFOCATION, FALLING_BLOCK, VOID, SUICIDE, STARVATION, CRAMMING, THORNS, FLY_INTO_WALL,
                 KILL, WORLD_BORDER, SONIC_BOOM, MELTING, DRYOUT -> false;
            default -> false;
        };
    }
}
