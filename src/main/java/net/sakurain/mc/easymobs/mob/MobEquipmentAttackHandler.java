package net.sakurain.mc.easymobs.mob;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.item.CustomItemTemplate;
import net.sakurain.mc.easymobs.item.ItemIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener that executes special attack effects from a custom mob's held custom item.
 */
public final class MobEquipmentAttackHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity livingDamager) || !MobTracker.getInstance().isCustomMob(livingDamager)) {
            return;
        }
        CustomMobTemplate template = MobTracker.getInstance().getTemplate(livingDamager);
        if (template == null) {
            return;
        }
        CustomMobTemplate.EquipmentEffects effects = template.getEquipmentEffects();
        if (!effects.enabled() || !effects.applySpecial()) {
            return;
        }

        ItemStack weapon = livingDamager.getEquipment().getItemInMainHand();
        CustomItemTemplate itemTemplate = ItemIdentifier.getTemplate(weapon);
        if (itemTemplate == null) {
            return;
        }

        Entity victim = event.getEntity();
        for (CustomItemTemplate.ItemAttackEffect attackEffect : itemTemplate.getAttackEffects()) {
            if (attackEffect.getChance() <= 0) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() * 100.0 > attackEffect.getChance()) {
                continue;
            }
            Collection<LivingEntity> targets = resolveTargets(livingDamager, victim, attackEffect.getTarget());
            for (LivingEntity target : targets) {
                for (CustomItemTemplate.EffectEntry entry : attackEffect.getEffects()) {
                    applyEffect(target, entry);
                }
            }
        }
    }

    private Collection<LivingEntity> resolveTargets(LivingEntity attacker, Entity victim, CustomItemTemplate.TargetType targetType) {
        return switch (targetType) {
            case SELF -> List.of(attacker);
            case VICTIM -> victim instanceof LivingEntity living ? List.of(living) : Collections.emptyList();
            case AREA -> {
                double radius = 3.0;
                List<LivingEntity> list = new ArrayList<>();
                for (Entity entity : victim.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof LivingEntity living && !entity.equals(attacker)) {
                        list.add(living);
                    }
                }
                if (victim instanceof LivingEntity living && !victim.equals(attacker)) {
                    list.add(living);
                }
                yield list;
            }
        };
    }

    private void applyEffect(LivingEntity target, CustomItemTemplate.EffectEntry entry) {
        switch (entry.getType()) {
            case POTION -> {
                if (entry.getPotionType() != null) {
                    target.addPotionEffect(new PotionEffect(entry.getPotionType(), entry.getDuration(),
                            entry.getAmplifier(), entry.isAmbient(), entry.isParticles(), entry.isIcon()));
                }
            }
            case ATTRIBUTE -> {
                if (entry.getAttribute() == null) {
                    break;
                }
                AttributeInstance instance = target.getAttribute(entry.getAttribute());
                if (instance == null) {
                    break;
                }
                NamespacedKey key = new NamespacedKey(EasyMobsPlugin.getInstance(),
                        "ezmobs_mob_attack_" + entry.getAttribute().getKey().getKey() + "_" + entry.getAttributeSlot());
                instance.getModifiers().stream()
                        .filter(m -> m.getKey().equals(key))
                        .findFirst()
                        .ifPresent(instance::removeModifier);
                instance.addModifier(new AttributeModifier(key, entry.getAttributeAmount(),
                        entry.getAttributeOperation(), toSlotGroup(entry.getAttributeSlot())));
            }
            case PARTICLE -> {
                if (entry.getParticle() != null) {
                    target.getWorld().spawnParticle(entry.getParticle(), target.getLocation().clone().add(0, 1, 0),
                            entry.getParticleCount(), entry.getParticleOffsetX(), entry.getParticleOffsetY(),
                            entry.getParticleOffsetZ());
                }
            }
            case SOUND -> {
                if (entry.getSound() != null) {
                    target.getWorld().playSound(target.getLocation(), entry.getSound(), entry.getSoundVolume(),
                            entry.getSoundPitch());
                }
            }
        }
    }

    private static EquipmentSlotGroup toSlotGroup(org.bukkit.inventory.EquipmentSlot slot) {
        if (slot == null) {
            return EquipmentSlotGroup.ANY;
        }
        return switch (slot) {
            case HAND -> EquipmentSlotGroup.MAINHAND;
            case OFF_HAND -> EquipmentSlotGroup.OFFHAND;
            case FEET -> EquipmentSlotGroup.FEET;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case HEAD -> EquipmentSlotGroup.HEAD;
            case BODY -> EquipmentSlotGroup.BODY;
            case SADDLE -> EquipmentSlotGroup.SADDLE;
        };
    }
}
