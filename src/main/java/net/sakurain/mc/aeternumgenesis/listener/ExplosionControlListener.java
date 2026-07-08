package net.sakurain.mc.aeternumgenesis.listener;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Controls whether mob explosions destroy terrain based on global and per-mob settings.
 */
public final class ExplosionControlListener implements Listener {

    private final AeternumGenesisPlugin plugin;

    public ExplosionControlListener(AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity source = event.getEntity();
        if (!(source instanceof LivingEntity)) {
            return;
        }

        // Global switch: when explicitly disabled, no mob explosion breaks blocks.
        boolean globalDisable = plugin.getConfig().getBoolean("explosions.disable-mob-terrain-damage", false);

        // Per-mob override via template configuration.
        CustomMobTemplate template = MobTracker.getInstance().getTemplate((LivingEntity) source);
        if (template != null) {
            CustomMobTemplate.ExplosionConfig config = template.getExplosion();
            if (config.destroyTerrain() != null) {
                if (!config.destroyTerrain()) {
                    event.blockList().clear();
                }
                return;
            }
        }

        // No per-mob setting: fall back to global config.
        if (globalDisable) {
            event.blockList().clear();
        }
    }
}
