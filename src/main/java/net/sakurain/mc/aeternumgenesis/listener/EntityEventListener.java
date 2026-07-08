package net.sakurain.mc.aeternumgenesis.listener;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobDamageModifyEvent;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobDeathEvent;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobDropEvent;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import net.sakurain.mc.aeternumgenesis.skill.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (event.getDamager() instanceof LivingEntity damager) {
            if (isSameFaction(damager, victim)) {
                event.setCancelled(true);
                return;
            }
            if (MobTracker.getInstance().isCustomMob(damager)) {
                CustomMobDamageModifyEvent modifyEvent = new CustomMobDamageModifyEvent(victim, damager, event.getDamage(), true);
                Bukkit.getPluginManager().callEvent(modifyEvent);
                if (modifyEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                event.setDamage(modifyEvent.getDamage());
                triggerSkills(damager, victim, "ON_HIT", event.getFinalDamage());
            }
        }

        if (MobTracker.getInstance().isCustomMob(victim)) {
            LivingEntity damager = event.getDamager() instanceof LivingEntity living ? living : null;
            CustomMobDamageModifyEvent modifyEvent = new CustomMobDamageModifyEvent(victim, damager, event.getDamage(), false);
            Bukkit.getPluginManager().callEvent(modifyEvent);
            if (modifyEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            event.setDamage(modifyEvent.getDamage());
            triggerSkills(victim, damager, "ON_HURT", event.getFinalDamage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (MobTracker.getInstance().isCustomMob(victim)) {
            // Death handling is done in EntityDeathEvent
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!MobTracker.getInstance().isCustomMob(entity)) return;

        String templateId = MobTracker.getInstance().getMobTemplateId(entity);
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(entity);
        if (template == null) return;

        Player killer = entity.getKiller();
        Bukkit.getPluginManager().callEvent(new CustomMobDeathEvent(templateId, entity, killer));

        CustomMobTemplate.DropsConfig drops = template.getDrops();
        if (drops != null) {
            List<ItemStack> dropItems = resolveDrops(drops);
            CustomMobDropEvent dropEvent = new CustomMobDropEvent(templateId, entity, dropItems, drops.overrideVanilla() ? drops.experience() : event.getDroppedExp());
            Bukkit.getPluginManager().callEvent(dropEvent);
            if (!dropEvent.isCancelled()) {
                if (drops.overrideVanilla()) {
                    event.getDrops().clear();
                    event.setDroppedExp(dropEvent.getExperience());
                }
                event.getDrops().addAll(dropEvent.getDrops());
            }
        }

        MobTracker.getInstance().cancelTracking(entity.getUniqueId());
        net.sakurain.mc.aeternumgenesis.ai.CustomAIController.getInstance().cleanup(entity.getUniqueId());

        if (killer != null) {
            triggerSkills(entity, killer, "ON_DEATH", 0);
            if (MobTracker.getInstance().isCustomMob(killer)) {
                triggerSkills(killer, entity, "ON_KILL", 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof LivingEntity caster)) return;
        if (!MobTracker.getInstance().isCustomMob(caster)) return;
        LivingEntity target = event.getTarget() instanceof LivingEntity living ? living : null;
        if (target != null && isSameFaction(caster, target)) {
            event.setCancelled(true);
            return;
        }
        triggerSkills(caster, target, "ON_TARGET", 0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity caster)) return;
        if (!MobTracker.getInstance().isCustomMob(caster)) return;
        triggerSkills(caster, null, "ON_TELEPORT", 0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTransform(EntityTransformEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!MobTracker.getInstance().isCustomMob(entity)) return;
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(entity);
        if (template == null) return;
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.DROWNED) {
            if (!template.getWaterBehavior().convertToDrowned()) {
                event.setCancelled(true);
            }
        }
    }

    private List<ItemStack> resolveDrops(CustomMobTemplate.DropsConfig drops) {
        List<ItemStack> result = new ArrayList<>();
        for (CustomMobTemplate.DropEntry entry : drops.items()) {
            if (ThreadLocalRandom.current().nextDouble(100.0) > entry.chance()) continue;
            ItemStack item = resolveItem(entry.item(), entry.amount());
            if (item != null) result.add(item);
        }
        return result;
    }

    private ItemStack resolveItem(String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) return null;
        if (itemId.startsWith("genesis:")) {
            String templateId = itemId.substring(8);
            var template = AeternumGenesisPlugin.getInstance().getItemManager().getTemplate(templateId);
            if (template == null) return null;
            var item = net.sakurain.mc.aeternumgenesis.item.ItemBuilder.build(template);
            item.setAmount(amount);
            return item;
        }
        try {
            var item = new ItemStack(org.bukkit.Material.valueOf(itemId.toUpperCase()));
            item.setAmount(amount);
            return item;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void triggerSkills(LivingEntity caster, LivingEntity target, String trigger, double damage) {
        SkillManager manager = AeternumGenesisPlugin.getInstance().getSkillManager();
        manager.handleTrigger(trigger, caster, target, damage);
    }

    private boolean isSameFaction(LivingEntity a, LivingEntity b) {
        if (a.equals(b)) return false;
        CustomMobTemplate ta = MobTracker.getInstance().getTemplate(a);
        CustomMobTemplate tb = MobTracker.getInstance().getTemplate(b);
        if (ta == null || tb == null) return false;
        String fa = ta.getFaction();
        String fb = tb.getFaction();
        return fa != null && !fa.isEmpty() && fa.equalsIgnoreCase(fb);
    }
}
