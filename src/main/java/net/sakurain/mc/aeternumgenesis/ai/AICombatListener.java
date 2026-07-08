package net.sakurain.mc.aeternumgenesis.ai;

import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AICombatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Mob mob && MobTracker.getInstance().isCustomMob(mob)) {
            if (event.getDamager() instanceof LivingEntity attacker) {
                CustomAIController.getInstance().recordDamage(mob, attacker, event.getFinalDamage());
            }
        }
        if (event.getDamager() instanceof Mob mob && MobTracker.getInstance().isCustomMob(mob)) {
            if (event.getEntity() instanceof LivingEntity victim) {
                CustomAIController.getInstance().recordDamage(mob, victim, 0);
            }
        }
    }
}
