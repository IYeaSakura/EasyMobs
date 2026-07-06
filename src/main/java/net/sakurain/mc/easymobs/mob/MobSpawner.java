package net.sakurain.mc.easymobs.mob;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.ai.CustomAIController;
import net.sakurain.mc.easymobs.item.CustomItemTemplate;
import net.sakurain.mc.easymobs.item.ItemBuilder;
import net.sakurain.mc.easymobs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;

/**
 * Spawns and configures {@link LivingEntity} instances from {@link CustomMobTemplate}s.
 */
@SuppressWarnings("deprecation")
public final class MobSpawner {

    private static final NamespacedKey MOB_ID_KEY = new NamespacedKey(EasyMobsPlugin.getInstance(), "ezmobs_mob_id");

    private MobSpawner() {
    }

    /**
     * Spawns a custom mob at the given location.
     *
     * @param template the mob template
     * @param location the spawn location
     * @return the spawned living entity, or null if the entity type was not living
     */
    public static LivingEntity spawn(CustomMobTemplate template, Location location) {
        if (template == null || location == null || location.getWorld() == null) {
            return null;
        }

        World world = location.getWorld();
        if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return null;
        }

        Entity entity = world.spawnEntity(location, template.getType());
        if (!(entity instanceof LivingEntity living)) {
            entity.remove();
            return null;
        }

        applyTemplate(living, template);
        return living;
    }

    private static void applyTemplate(LivingEntity entity, CustomMobTemplate template) {
        applyDisplayName(entity, template);
        applyHealth(entity, template);
        applyAttributes(entity, template);
        applyEquipment(entity, template);
        applyGlowing(entity, template);
        applyBaby(entity, template);
        applySize(entity, template);
        applyPotionEffects(entity, template);
        applyWaterBehavior(entity, template);
        applyBreakDoor(entity, template);
        applyPdc(entity, template);
        applyBossBar(entity, template);

        if (entity instanceof org.bukkit.entity.Mob mob) {
            CustomMobTemplate.AIConfig ai = template.getAi();
            if (ai != null && ai.useCustomAi()) {
                CustomAIController.getInstance().setupAI(mob, template);
            }
        }

        MobTracker.getInstance().track(entity, template);
    }

    private static void applyDisplayName(LivingEntity entity, CustomMobTemplate template) {
        if (template.getDisplayName() != null && !template.getDisplayName().isEmpty()) {
            entity.customName(MessageUtil.color(template.getDisplayName()));
            entity.setCustomNameVisible(true);
        }
    }

    private static void applyHealth(LivingEntity entity, CustomMobTemplate template) {
        double max = template.getMaxHealth() > 0 ? template.getMaxHealth() : template.getHealth();
        if (max <= 0) {
            max = 20.0;
        }
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(max);
        }
        double health = template.getHealth() > 0 ? template.getHealth() : max;
        entity.setHealth(Math.min(health, max));
    }

    private static void applyAttributes(LivingEntity entity, CustomMobTemplate template) {
        for (CustomMobTemplate.MobAttribute mobAttribute : template.getAttributes()) {
            AttributeInstance instance = entity.getAttribute(mobAttribute.attribute());
            if (instance == null) {
                continue;
            }
            if (mobAttribute.operation() == AttributeModifier.Operation.ADD_NUMBER) {
                instance.setBaseValue(instance.getBaseValue() + mobAttribute.amount());
            } else {
                NamespacedKey key = new NamespacedKey(EasyMobsPlugin.getInstance(),
                        "ezmobs_attr_" + mobAttribute.attribute().getKey().getKey());
                instance.addModifier(new AttributeModifier(key, mobAttribute.amount(), mobAttribute.operation()));
            }
        }
    }

    private static void applyEquipment(LivingEntity entity, CustomMobTemplate template) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }

        for (Map.Entry<EquipmentSlot, CustomMobTemplate.EquipmentEntry> entry : template.getEquipment().entrySet()) {
            ItemStack item = resolveItem(entry.getValue().item());
            if (item == null || item.isEmpty()) {
                continue;
            }
            setEquipment(equipment, entry.getKey(), item);
            setDropChance(equipment, entry.getKey(), entry.getValue().dropChance());
        }

        // Equipment built by ItemBuilder already carries attributes and enchantments when applicable.
        // The equipment_effects flags are used by other systems (e.g. MobEquipmentAttackHandler for special effects).
    }

    private static ItemStack resolveItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }
        if (itemId.startsWith("ezmobs:")) {
            String id = itemId.substring(7);
            CustomItemTemplate template = EasyMobsPlugin.getInstance().getItemManager().getTemplate(id);
            if (template == null) {
                return null;
            }
            return ItemBuilder.build(template);
        }
        try {
            Material material = Material.valueOf(itemId.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void setEquipment(EntityEquipment equipment, EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HEAD -> equipment.setHelmet(item);
            case CHEST -> equipment.setChestplate(item);
            case LEGS -> equipment.setLeggings(item);
            case FEET -> equipment.setBoots(item);
            case HAND -> equipment.setItemInMainHand(item);
            case OFF_HAND -> equipment.setItemInOffHand(item);
            default -> {
                // Unsupported slot for standard mobs.
            }
        }
    }

    private static void setDropChance(EntityEquipment equipment, EquipmentSlot slot, double chance) {
        float value = (float) Math.max(0.0, Math.min(1.0, chance / 100.0));
        switch (slot) {
            case HEAD -> equipment.setHelmetDropChance(value);
            case CHEST -> equipment.setChestplateDropChance(value);
            case LEGS -> equipment.setLeggingsDropChance(value);
            case FEET -> equipment.setBootsDropChance(value);
            case HAND -> equipment.setItemInMainHandDropChance(value);
            case OFF_HAND -> equipment.setItemInOffHandDropChance(value);
            default -> {
            }
        }
    }

    private static void applyGlowing(LivingEntity entity, CustomMobTemplate template) {
        if (!template.isGlowing()) {
            return;
        }
        entity.setGlowing(true);
        if (template.getGlowingColor() != null && !template.getGlowingColor().isEmpty()) {
            applyGlowingColor(entity, template.getGlowingColor());
        }
    }

    private static void applyGlowingColor(LivingEntity entity, String colorName) {
        try {
            ChatColor color = ChatColor.valueOf(colorName.toUpperCase());
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "ezmobs_glow_" + color.name();
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                team.setColor(color);
            }
            team.addEntry(entity.getUniqueId().toString());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    private static void applyBaby(LivingEntity entity, CustomMobTemplate template) {
        if (template.isBaby() && entity instanceof Ageable ageable) {
            ageable.setBaby();
        }
    }

    private static void applySize(LivingEntity entity, CustomMobTemplate template) {
        int size = template.getSize();
        if (size <= 0) {
            return;
        }
        if (entity instanceof Slime slime) {
            slime.setSize(size);
        } else if (entity instanceof Phantom phantom) {
            phantom.setSize(size);
        }
    }

    private static void applyPotionEffects(LivingEntity entity, CustomMobTemplate template) {
        for (CustomMobTemplate.PotionEffectConfig effect : template.getPotionEffects()) {
            PotionEffectType type = effect.type();
            if (type == null) {
                continue;
            }
            entity.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, effect.amplifier(),
                    effect.ambient(), effect.particles()));
        }
    }

    private static void applyWaterBehavior(LivingEntity entity, CustomMobTemplate template) {
        CustomMobTemplate.WaterBehaviorConfig water = template.getWaterBehavior();
        if (water.canBreatheUnderwater()) {
            PotionEffectType waterBreathing = PotionEffectType.WATER_BREATHING;
            if (waterBreathing != null) {
                entity.addPotionEffect(new PotionEffect(waterBreathing, Integer.MAX_VALUE, 0, true, false));
            }
        }
        // Bukkit API has limited direct water controls; the remaining fields are stored for future NMS-free extensions.
    }

    private static void applyBreakDoor(LivingEntity entity, CustomMobTemplate template) {
        CustomMobTemplate.BreakDoorConfig breakDoor = template.getBreakDoor();
        if (!breakDoor.enabled()) {
            return;
        }
        // No direct Bukkit API for door breaking chance without NMS.
    }

    private static void applyPdc(LivingEntity entity, CustomMobTemplate template) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(MOB_ID_KEY, PersistentDataType.STRING, template.getId());
    }

    private static void applyBossBar(LivingEntity entity, CustomMobTemplate template) {
        CustomMobTemplate.BossBarConfig config = template.getBossbar();
        if (!config.enabled()) {
            return;
        }
        String title = config.title() != null ? config.title() : template.getDisplayName();
        if (title == null) {
            title = template.getId();
        }
        BossBar bossBar = Bukkit.createBossBar(
                ChatColor.translateAlternateColorCodes('&', title),
                config.color() != null ? config.color() : BarColor.RED,
                config.style() != null ? config.style() : BarStyle.SOLID
        );
        bossBar.setProgress(1.0);
        MobTracker.getInstance().registerBossBar(entity.getUniqueId(), bossBar);
    }

    /**
     * Returns the PDC key used to store the custom mob id.
     *
     * @return the mob id key
     */
    public static NamespacedKey getMobIdKey() {
        return MOB_ID_KEY;
    }
}
