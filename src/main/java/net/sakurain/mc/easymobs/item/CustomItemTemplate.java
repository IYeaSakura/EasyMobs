package net.sakurain.mc.easymobs.item;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

/**
 * Immutable data class representing a custom item template loaded from YAML.
 */
public final class CustomItemTemplate {

    private final String id;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int amount;
    private final Integer customModelData;
    private final boolean glow;
    private final Map<Enchantment, Integer> enchantments;
    private final boolean hideEnchants;
    private final List<ItemAttribute> attributes;
    private final List<ItemPassiveEffect> passiveEffects;
    private final List<ItemAttackEffect> attackEffects;
    private final String setId;
    private final boolean unbreakable;
    private final List<String> itemFlags;

    public CustomItemTemplate(String id, Material material, String name, List<String> lore, int amount,
                              Integer customModelData, boolean glow, Map<Enchantment, Integer> enchantments,
                              boolean hideEnchants, List<ItemAttribute> attributes,
                              List<ItemPassiveEffect> passiveEffects, List<ItemAttackEffect> attackEffects,
                              String setId, boolean unbreakable, List<String> itemFlags) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
        this.amount = amount;
        this.customModelData = customModelData;
        this.glow = glow;
        this.enchantments = enchantments == null ? Map.of() : Map.copyOf(enchantments);
        this.hideEnchants = hideEnchants;
        this.attributes = attributes == null ? List.of() : List.copyOf(attributes);
        this.passiveEffects = passiveEffects == null ? List.of() : List.copyOf(passiveEffects);
        this.attackEffects = attackEffects == null ? List.of() : List.copyOf(attackEffects);
        this.setId = setId;
        this.unbreakable = unbreakable;
        this.itemFlags = itemFlags == null ? List.of() : List.copyOf(itemFlags);
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getAmount() {
        return amount;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public boolean isGlow() {
        return glow;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public boolean isHideEnchants() {
        return hideEnchants;
    }

    public List<ItemAttribute> getAttributes() {
        return attributes;
    }

    public List<ItemPassiveEffect> getPassiveEffects() {
        return passiveEffects;
    }

    public List<ItemAttackEffect> getAttackEffects() {
        return attackEffects;
    }

    public String getSetId() {
        return setId;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public List<String> getItemFlags() {
        return itemFlags;
    }

    public enum TriggerType {
        HOLD, WEAR, BOTH
    }

    public enum EffectType {
        POTION, ATTRIBUTE, PARTICLE, SOUND
    }

    public enum TargetType {
        VICTIM, SELF, AREA
    }

    /**
     * Represents a single attribute modifier on an item.
     */
    public static final class ItemAttribute {

        private final Attribute attribute;
        private final double amount;
        private final Operation operation;
        private final EquipmentSlot slot;

        public ItemAttribute(Attribute attribute, double amount, Operation operation, EquipmentSlot slot) {
            this.attribute = attribute;
            this.amount = amount;
            this.operation = operation;
            this.slot = slot;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public double getAmount() {
            return amount;
        }

        public Operation getOperation() {
            return operation;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    /**
     * Represents a passive effect granted while an item is held or worn.
     */
    public static final class ItemPassiveEffect {

        private final TriggerType trigger;
        private final List<EffectEntry> effects;

        public ItemPassiveEffect(TriggerType trigger, List<EffectEntry> effects) {
            this.trigger = trigger;
            this.effects = effects == null ? List.of() : List.copyOf(effects);
        }

        public TriggerType getTrigger() {
            return trigger;
        }

        public List<EffectEntry> getEffects() {
            return effects;
        }
    }

    /**
     * Represents a single effect entry of a specific type.
     */
    public static final class EffectEntry {

        private final EffectType type;
        private final PotionEffectType potionType;
        private final int duration;
        private final int amplifier;
        private final boolean ambient;
        private final boolean particles;
        private final boolean icon;
        private final Attribute attribute;
        private final double attributeAmount;
        private final Operation attributeOperation;
        private final EquipmentSlot attributeSlot;
        private final org.bukkit.Particle particle;
        private final int particleCount;
        private final double particleOffsetX;
        private final double particleOffsetY;
        private final double particleOffsetZ;
        private final org.bukkit.Sound sound;
        private final float soundVolume;
        private final float soundPitch;
        private final double radius;

        public EffectEntry(EffectType type,
                           PotionEffectType potionType, int duration, int amplifier, boolean ambient, boolean particles, boolean icon,
                           Attribute attribute, double attributeAmount, Operation attributeOperation, EquipmentSlot attributeSlot,
                           org.bukkit.Particle particle, int particleCount, double particleOffsetX, double particleOffsetY, double particleOffsetZ,
                           org.bukkit.Sound sound, float soundVolume, float soundPitch,
                           double radius) {
            this.type = type;
            this.potionType = potionType;
            this.duration = duration;
            this.amplifier = amplifier;
            this.ambient = ambient;
            this.particles = particles;
            this.icon = icon;
            this.attribute = attribute;
            this.attributeAmount = attributeAmount;
            this.attributeOperation = attributeOperation;
            this.attributeSlot = attributeSlot;
            this.particle = particle;
            this.particleCount = particleCount;
            this.particleOffsetX = particleOffsetX;
            this.particleOffsetY = particleOffsetY;
            this.particleOffsetZ = particleOffsetZ;
            this.sound = sound;
            this.soundVolume = soundVolume;
            this.soundPitch = soundPitch;
            this.radius = radius;
        }

        public EffectType getType() {
            return type;
        }

        public PotionEffectType getPotionType() {
            return potionType;
        }

        public int getDuration() {
            return duration;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public boolean isAmbient() {
            return ambient;
        }

        public boolean isParticles() {
            return particles;
        }

        public boolean isIcon() {
            return icon;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public double getAttributeAmount() {
            return attributeAmount;
        }

        public Operation getAttributeOperation() {
            return attributeOperation;
        }

        public EquipmentSlot getAttributeSlot() {
            return attributeSlot;
        }

        public org.bukkit.Particle getParticle() {
            return particle;
        }

        public int getParticleCount() {
            return particleCount;
        }

        public double getParticleOffsetX() {
            return particleOffsetX;
        }

        public double getParticleOffsetY() {
            return particleOffsetY;
        }

        public double getParticleOffsetZ() {
            return particleOffsetZ;
        }

        public org.bukkit.Sound getSound() {
            return sound;
        }

        public float getSoundVolume() {
            return soundVolume;
        }

        public float getSoundPitch() {
            return soundPitch;
        }

        public double getRadius() {
            return radius;
        }
    }

    /**
     * Represents an attack-triggered effect with a chance and target type.
     */
    public static final class ItemAttackEffect {

        private final double chance;
        private final TargetType target;
        private final List<EffectEntry> effects;

        public ItemAttackEffect(double chance, TargetType target, List<EffectEntry> effects) {
            this.chance = chance;
            this.target = target;
            this.effects = effects == null ? List.of() : List.copyOf(effects);
        }

        public double getChance() {
            return chance;
        }

        public TargetType getTarget() {
            return target;
        }

        public List<EffectEntry> getEffects() {
            return effects;
        }
    }
}
