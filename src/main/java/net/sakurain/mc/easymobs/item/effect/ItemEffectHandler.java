package net.sakurain.mc.easymobs.item.effect;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.item.CustomItemTemplate;
import net.sakurain.mc.easymobs.item.ItemIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener that applies and refreshes passive item effects and handles attack effects.
 */
public final class ItemEffectHandler implements Listener {

    private static final int REFRESH_INTERVAL = 20; // ticks

    private final EasyMobsPlugin plugin;
    private final NamespacedKey activeEffectsKey;
    private final NamespacedKey activeAttrsKey;
    private final NamespacedKey modifierNamespaceKey;
    private BukkitTask refreshTask;

    public ItemEffectHandler(EasyMobsPlugin plugin) {
        this.plugin = plugin;
        this.activeEffectsKey = new NamespacedKey(plugin, "ezmobs_active_effects");
        this.activeAttrsKey = new NamespacedKey(plugin, "ezmobs_active_attrs");
        this.modifierNamespaceKey = new NamespacedKey(plugin, "modifier");
        startRefreshTask();
    }

    private void startRefreshTask() {
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    refreshPlayer(player);
                }
            }
        }.runTaskTimer(plugin, REFRESH_INTERVAL, REFRESH_INTERVAL);
    }

    private void refreshPlayer(Player player) {
        removePreviousEffects(player);
        removePreviousAttributes(player);

        collectPassiveEffects(player, CustomItemTemplate.TriggerType.HOLD,
                player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand());
        collectPassiveEffects(player, CustomItemTemplate.TriggerType.WEAR,
                player.getInventory().getArmorContents());
    }

    private void collectPassiveEffects(Player player, CustomItemTemplate.TriggerType requiredTrigger, ItemStack... items) {
        for (ItemStack item : items) {
            if (item == null || item.isEmpty()) {
                continue;
            }
            CustomItemTemplate template = ItemIdentifier.getTemplate(item);
            if (template == null) {
                continue;
            }
            for (CustomItemTemplate.ItemPassiveEffect passive : template.getPassiveEffects()) {
                CustomItemTemplate.TriggerType trigger = passive.getTrigger();
                if (trigger == CustomItemTemplate.TriggerType.BOTH || trigger == requiredTrigger) {
                    for (CustomItemTemplate.EffectEntry entry : passive.getEffects()) {
                        switch (entry.getType()) {
                            case POTION -> applyPotionEffect(player, entry);
                            case ATTRIBUTE -> applyAttributeEffect(player, entry);
                            case PARTICLE -> spawnParticle(player, entry);
                            case SOUND -> playSound(player, entry);
                        }
                    }
                }
            }
        }
    }

    private void applyPotionEffect(Player player, CustomItemTemplate.EffectEntry entry) {
        PotionEffectType type = entry.getPotionType();
        if (type == null) {
            return;
        }
        PotionEffect effect = new PotionEffect(type,
                Math.max(REFRESH_INTERVAL + 10, entry.getDuration()),
                entry.getAmplifier(),
                entry.isAmbient(),
                entry.isParticles(),
                entry.isIcon());
        player.addPotionEffect(effect);
        appendActiveEffect(player, type.getKey().toString());
    }

    private void applyAttributeEffect(Player player, CustomItemTemplate.EffectEntry entry) {
        Attribute attribute = entry.getAttribute();
        if (attribute == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        NamespacedKey key = new NamespacedKey(modifierNamespaceKey.getNamespace(),
                attribute.getKey().getKey() + "_" + entry.getAttributeSlot());
        instance.getModifiers().stream()
                .filter(m -> m.getKey().equals(key))
                .findFirst()
                .ifPresent(instance::removeModifier);
        AttributeModifier modifier = new AttributeModifier(key,
                entry.getAttributeAmount(),
                entry.getAttributeOperation(),
                toSlotGroup(entry.getAttributeSlot()));
        instance.addModifier(modifier);
        appendActiveAttribute(player, attribute.getKey().toString());
    }

    private void spawnParticle(Player player, CustomItemTemplate.EffectEntry entry) {
        if (entry.getParticle() == null) {
            return;
        }
        player.getWorld().spawnParticle(entry.getParticle(), player.getLocation().clone().add(0, 1, 0),
                entry.getParticleCount(), entry.getParticleOffsetX(), entry.getParticleOffsetY(), entry.getParticleOffsetZ());
    }

    private void playSound(Player player, CustomItemTemplate.EffectEntry entry) {
        if (entry.getSound() == null) {
            return;
        }
        player.playSound(player.getLocation(), entry.getSound(), entry.getSoundVolume(), entry.getSoundPitch());
    }

    private void removePreviousEffects(Player player) {
        Set<String> previous = readSet(player, activeEffectsKey);
        for (String name : previous) {
            PotionEffectType type = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.fromString(name));
            if (type != null) {
                player.removePotionEffect(type);
            }
        }
        player.getPersistentDataContainer().remove(activeEffectsKey);
    }

    private void removePreviousAttributes(Player player) {
        Set<String> previous = readSet(player, activeAttrsKey);
        for (String name : previous) {
            Attribute attribute = org.bukkit.Registry.ATTRIBUTE.get(org.bukkit.NamespacedKey.fromString(name));
            if (attribute == null) {
                continue;
            }
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            Set<AttributeModifier> toRemove = new HashSet<>();
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (modifier.getKey().getNamespace().equals(modifierNamespaceKey.getNamespace())) {
                    toRemove.add(modifier);
                }
            }
            for (AttributeModifier modifier : toRemove) {
                instance.removeModifier(modifier);
            }
        }
        player.getPersistentDataContainer().remove(activeAttrsKey);
    }

    private void appendActiveEffect(Player player, String effectId) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String existing = pdc.getOrDefault(activeEffectsKey, PersistentDataType.STRING, "");
        Set<String> set = new HashSet<>(Arrays.asList(existing.split(";")));
        set.add(effectId);
        pdc.set(activeEffectsKey, PersistentDataType.STRING, String.join(";", set));
    }

    private void appendActiveAttribute(Player player, String attributeId) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String existing = pdc.getOrDefault(activeAttrsKey, PersistentDataType.STRING, "");
        Set<String> set = new HashSet<>(Arrays.asList(existing.split(";")));
        set.add(attributeId);
        pdc.set(activeAttrsKey, PersistentDataType.STRING, String.join(";", set));
    }

    private Set<String> readSet(Player player, NamespacedKey key) {
        String value = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "");
        if (value.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(value.split(";")));
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        CustomItemTemplate template = ItemIdentifier.getTemplate(weapon);
        if (template == null) {
            return;
        }
        Entity victim = event.getEntity();
        for (CustomItemTemplate.ItemAttackEffect attackEffect : template.getAttackEffects()) {
            if (attackEffect.getChance() <= 0) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() * 100 > attackEffect.getChance()) {
                continue;
            }
            Collection<LivingEntity> targets = resolveTargets(player, victim, attackEffect.getTarget());
            for (LivingEntity target : targets) {
                for (CustomItemTemplate.EffectEntry entry : attackEffect.getEffects()) {
                    applyAttackEffect(target, entry);
                }
            }
        }
    }

    private Collection<LivingEntity> resolveTargets(Player attacker, Entity victim, CustomItemTemplate.TargetType targetType) {
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

    private void applyAttackEffect(LivingEntity target, CustomItemTemplate.EffectEntry entry) {
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
                NamespacedKey key = new NamespacedKey(modifierNamespaceKey.getNamespace(),
                        UUID.randomUUID().toString());
                instance.addModifier(new AttributeModifier(key, entry.getAttributeAmount(),
                        entry.getAttributeOperation(), toSlotGroup(entry.getAttributeSlot())));
            }
            case PARTICLE -> {
                if (entry.getParticle() != null) {
                    target.getWorld().spawnParticle(entry.getParticle(), target.getLocation().clone().add(0, 1, 0),
                            entry.getParticleCount(), entry.getParticleOffsetX(), entry.getParticleOffsetY(), entry.getParticleOffsetZ());
                }
            }
            case SOUND -> {
                if (entry.getSound() != null) {
                    target.getWorld().playSound(target.getLocation(), entry.getSound(), entry.getSoundVolume(), entry.getSoundPitch());
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

    /**
     * Removes all tracked effects and attributes from every online player and cancels the refresh task.
     */
    public void clearAllEffects() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            removePreviousEffects(player);
            removePreviousAttributes(player);
        }
    }
}
