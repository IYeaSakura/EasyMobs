package net.sakurain.mc.aeternumgenesis.mob;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.item.ItemEffectParser;
import net.sakurain.mc.aeternumgenesis.skill.SkillBinding;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Immutable data class representing a custom mob template loaded from YAML.
 */
public final class CustomMobTemplate {

    private final String id;
    private final EntityType type;
    private final String displayName;
    private final double health;
    private final double maxHealth;
    private final List<MobAttribute> attributes;
    private final Map<EquipmentSlot, EquipmentEntry> equipment;
    private final EquipmentEffects equipmentEffects;
    private final boolean glowing;
    private final String glowingColor;
    private final int size;
    private final boolean baby;
    private final BossBarConfig bossbar;
    private final List<ParticleConfig> particles;
    private final AmbientSoundConfig ambientSound;
    private final List<PotionEffectConfig> potionEffects;
    private final SensesConfig senses;
    private final WaterBehaviorConfig waterBehavior;
    private final ImmunitiesConfig immunities;
    private final BreakDoorConfig breakDoor;
    private final String faction;
    private final AIConfig ai;
    private final DropsConfig drops;
    private final List<SkillBinding> skills;

    public CustomMobTemplate(String id, EntityType type, String displayName, double health, double maxHealth,
                             List<MobAttribute> attributes, Map<EquipmentSlot, EquipmentEntry> equipment,
                             EquipmentEffects equipmentEffects, boolean glowing, String glowingColor, int size,
                             boolean baby, BossBarConfig bossbar, List<ParticleConfig> particles,
                             AmbientSoundConfig ambientSound, List<PotionEffectConfig> potionEffects,
                             SensesConfig senses, WaterBehaviorConfig waterBehavior, ImmunitiesConfig immunities,
                             BreakDoorConfig breakDoor, String faction, AIConfig ai, DropsConfig drops,
                             List<SkillBinding> skills) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.health = health;
        this.maxHealth = maxHealth;
        this.attributes = attributes == null ? List.of() : List.copyOf(attributes);
        this.equipment = equipment == null ? Map.of() : Map.copyOf(equipment);
        this.equipmentEffects = equipmentEffects == null ? EquipmentEffects.DEFAULT : equipmentEffects;
        this.glowing = glowing;
        this.glowingColor = glowingColor;
        this.size = size;
        this.baby = baby;
        this.bossbar = bossbar == null ? BossBarConfig.DEFAULT : bossbar;
        this.particles = particles == null ? List.of() : List.copyOf(particles);
        this.ambientSound = ambientSound == null ? AmbientSoundConfig.DEFAULT : ambientSound;
        this.potionEffects = potionEffects == null ? List.of() : List.copyOf(potionEffects);
        this.senses = senses == null ? SensesConfig.DEFAULT : senses;
        this.waterBehavior = waterBehavior == null ? WaterBehaviorConfig.DEFAULT : waterBehavior;
        this.immunities = immunities == null ? ImmunitiesConfig.DEFAULT : immunities;
        this.breakDoor = breakDoor == null ? BreakDoorConfig.DEFAULT : breakDoor;
        this.faction = faction == null ? "" : faction;
        this.ai = ai == null ? AIConfig.DEFAULT : ai;
        this.drops = drops == null ? DropsConfig.DEFAULT : drops;
        this.skills = skills == null ? List.of() : List.copyOf(skills);
    }

    public String getId() {
        return id;
    }

    public EntityType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public List<MobAttribute> getAttributes() {
        return attributes;
    }

    public Map<EquipmentSlot, EquipmentEntry> getEquipment() {
        return equipment;
    }

    public EquipmentEffects getEquipmentEffects() {
        return equipmentEffects;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public String getGlowingColor() {
        return glowingColor;
    }

    public int getSize() {
        return size;
    }

    public boolean isBaby() {
        return baby;
    }

    public BossBarConfig getBossbar() {
        return bossbar;
    }

    public List<ParticleConfig> getParticles() {
        return particles;
    }

    public AmbientSoundConfig getAmbientSound() {
        return ambientSound;
    }

    public List<PotionEffectConfig> getPotionEffects() {
        return potionEffects;
    }

    public SensesConfig getSenses() {
        return senses;
    }

    public WaterBehaviorConfig getWaterBehavior() {
        return waterBehavior;
    }

    public ImmunitiesConfig getImmunities() {
        return immunities;
    }

    public BreakDoorConfig getBreakDoor() {
        return breakDoor;
    }

    public AIConfig getAi() {
        return ai;
    }

    public String getFaction() {
        return faction;
    }

    public DropsConfig getDrops() {
        return drops;
    }

    public List<SkillBinding> getSkills() {
        return skills;
    }

    /**
     * Builds a template from a configuration section.
     *
     * @param id     the template id
     * @param config the configuration section
     * @return the parsed template
     */
    @SuppressWarnings("unchecked")
    public static CustomMobTemplate fromConfig(String id, ConfigurationSection config) {
        EntityType type = parseEntityType(config.getString("type", "ZOMBIE"));
        String displayName = config.getString("display_name");
        double health = config.getDouble("health", 20.0);
        double maxHealth = config.contains("max_health") ? config.getDouble("max_health") : health;

        List<MobAttribute> attributes = parseAttributes(config.getMapList("attributes"), id);
        Map<EquipmentSlot, EquipmentEntry> equipment = parseEquipment(config.getConfigurationSection("equipment"), id);
        EquipmentEffects equipmentEffects = parseEquipmentEffects(config.getConfigurationSection("equipment_effects"));
        boolean glowing = config.getBoolean("glowing", false);
        String glowingColor = config.getString("glowing_color");
        int size = config.getInt("size", 0);
        boolean baby = config.getBoolean("baby", false);
        BossBarConfig bossbar = parseBossBar(config.getConfigurationSection("bossbar"));
        List<ParticleConfig> particles = parseParticles(config.getMapList("particles"), id);
        AmbientSoundConfig ambientSound = parseAmbientSound(config.getConfigurationSection("ambient_sound"), id);
        List<PotionEffectConfig> potionEffects = parsePotionEffects(config.getMapList("potion_effects"), id);
        SensesConfig senses = parseSenses(config.getConfigurationSection("senses"));
        WaterBehaviorConfig waterBehavior = parseWaterBehavior(config.getConfigurationSection("water_behavior"));
        ImmunitiesConfig immunities = parseImmunities(config.getConfigurationSection("immunities"));
        BreakDoorConfig breakDoor = parseBreakDoor(config.getConfigurationSection("break_door"));
        String faction = config.getString("faction");
        AIConfig ai = parseAI(config.getConfigurationSection("ai"));
        DropsConfig drops = parseDrops(config.getConfigurationSection("drops"), id);
        List<SkillBinding> skills = parseSkills(config.getMapList("skills"));

        return new CustomMobTemplate(id, type, displayName, health, maxHealth, attributes, equipment,
                equipmentEffects, glowing, glowingColor, size, baby, bossbar, particles, ambientSound,
                potionEffects, senses, waterBehavior, immunities, breakDoor, faction, ai, drops, skills);
    }

    private static EntityType parseEntityType(String value) {
        if (value == null) {
            return EntityType.ZOMBIE;
        }
        try {
            return EntityType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log("Unknown entity type: " + value);
            return EntityType.ZOMBIE;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<MobAttribute> parseAttributes(List<?> list, String mobId) {
        List<MobAttribute> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            Attribute attribute = parseAttribute(getString(map, "type"));
            if (attribute == null) {
                log("[" + mobId + "] Unknown attribute: " + getString(map, "type"));
                continue;
            }
            double amount = toDouble(map.get("amount"), 0.0);
            AttributeModifier.Operation operation = parseOperation(getString(map, "operation"));
            result.add(new MobAttribute(attribute, amount, operation));
        }
        return result;
    }

    private static Attribute parseAttribute(String value) {
        return ItemEffectParser.parseAttribute(value);
    }

    private static AttributeModifier.Operation parseOperation(String value) {
        if (value == null) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
        try {
            return AttributeModifier.Operation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
    }

    private static Map<EquipmentSlot, EquipmentEntry> parseEquipment(ConfigurationSection section, String mobId) {
        Map<EquipmentSlot, EquipmentEntry> result = new EnumMap<>(EquipmentSlot.class);
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            EquipmentSlot slot = parseEquipmentSlot(key);
            if (slot == null) {
                log("[" + mobId + "] Unknown equipment slot: " + key);
                continue;
            }
            ConfigurationSlotEntry entry = parseSlotEntry(section.getConfigurationSection(key));
            if (entry != null) {
                result.put(slot, entry.toEquipmentEntry());
            }
        }
        return result;
    }

    private record ConfigurationSlotEntry(String item, double dropChance) {
        EquipmentEntry toEquipmentEntry() {
            return new EquipmentEntry(item, dropChance);
        }
    }

    private static ConfigurationSlotEntry parseSlotEntry(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String item = section.getString("item");
        if (item == null || item.isEmpty()) {
            return null;
        }
        double dropChance = section.getDouble("drop_chance", 0.0);
        return new ConfigurationSlotEntry(item, dropChance);
    }

    private static EquipmentSlot parseEquipmentSlot(String value) {
        if (value == null) {
            return null;
        }
        String upper = value.toUpperCase();
        try {
            return EquipmentSlot.valueOf(upper);
        } catch (IllegalArgumentException ignored) {
        }
        return switch (upper) {
            case "HELMET" -> EquipmentSlot.HEAD;
            case "CHESTPLATE" -> EquipmentSlot.CHEST;
            case "LEGGINGS" -> EquipmentSlot.LEGS;
            case "BOOTS" -> EquipmentSlot.FEET;
            case "MAIN_HAND" -> EquipmentSlot.HAND;
            case "OFF_HAND" -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    private static EquipmentEffects parseEquipmentEffects(ConfigurationSection section) {
        if (section == null) {
            return EquipmentEffects.DEFAULT;
        }
        return new EquipmentEffects(
                section.getBoolean("enabled", true),
                section.getBoolean("apply_attributes", true),
                section.getBoolean("apply_enchantments", true),
                section.getBoolean("apply_special", true)
        );
    }

    private static BossBarConfig parseBossBar(ConfigurationSection section) {
        if (section == null || !section.getBoolean("enabled", false)) {
            return BossBarConfig.DEFAULT;
        }
        return new BossBarConfig(
                true,
                section.getString("title"),
                parseEnum(BarColor.class, section.getString("color"), BarColor.RED),
                parseEnum(BarStyle.class, section.getString("style"), BarStyle.SOLID),
                section.getBoolean("show_to_all", false),
                section.getDouble("range", 32.0)
        );
    }

    @SuppressWarnings("unchecked")
    private static List<ParticleConfig> parseParticles(List<?> list, String mobId) {
        List<ParticleConfig> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            Particle particle = parseParticle(getString(map, "type"));
            if (particle == null) {
                log("[" + mobId + "] Unknown particle: " + getString(map, "type"));
                continue;
            }
            result.add(new ParticleConfig(
                    particle,
                    parseEnum(ParticleLocation.class, getString(map, "location"), ParticleLocation.CENTER),
                    toInt(map.get("count"), 1),
                    toDouble(map.get("offset_x"), 0.5),
                    toDouble(map.get("offset_y"), 0.5),
                    toDouble(map.get("offset_z"), 0.5),
                    getString(map, "color"),
                    toDouble(map.get("size"), 1.0),
                    toInt(map.get("interval"), 20)
            ));
        }
        return result;
    }

    private static Particle parseParticle(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.PARTICLE_TYPE.get(key);
    }

    private static AmbientSoundConfig parseAmbientSound(ConfigurationSection section, String mobId) {
        if (section == null) {
            return AmbientSoundConfig.DEFAULT;
        }
        Sound sound = parseSound(section.getString("sound"));
        if (sound == null) {
            log("[" + mobId + "] Unknown ambient sound: " + section.getString("sound"));
            return AmbientSoundConfig.DEFAULT;
        }
        return new AmbientSoundConfig(
                sound,
                section.getInt("interval", 100),
                (float) section.getDouble("volume", 1.0),
                (float) section.getDouble("pitch", 1.0)
        );
    }

    private static Sound parseSound(String value) {
        if (value == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return Registry.SOUNDS.get(key);
    }

    @SuppressWarnings("unchecked")
    private static List<PotionEffectConfig> parsePotionEffects(List<?> list, String mobId) {
        List<PotionEffectConfig> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            PotionEffectType type = parsePotionEffectType(getString(map, "type"));
            if (type == null) {
                log("[" + mobId + "] Unknown potion effect: " + getString(map, "type"));
                continue;
            }
            result.add(new PotionEffectConfig(
                    type,
                    toInt(map.get("amplifier"), 0),
                    toBoolean(map.get("ambient"), false),
                    toBoolean(map.get("particles"), true)
            ));
        }
        return result;
    }

    private static PotionEffectType parsePotionEffectType(String value) {
        return net.sakurain.mc.aeternumgenesis.item.ItemEffectParser.parsePotionEffectType(value);
    }

    private static SensesConfig parseSenses(ConfigurationSection section) {
        if (section == null) {
            return SensesConfig.DEFAULT;
        }
        return new SensesConfig(
                section.getBoolean("vision", true),
                section.getBoolean("hearing", true),
                section.getBoolean("smell", false),
                section.getDouble("vision_range", 32.0),
                section.getDouble("hearing_range", 16.0),
                section.getDouble("smell_range", 8.0)
        );
    }

    private static WaterBehaviorConfig parseWaterBehavior(ConfigurationSection section) {
        if (section == null) {
            return WaterBehaviorConfig.DEFAULT;
        }
        return new WaterBehaviorConfig(
                section.getBoolean("float_on_water", true),
                section.getDouble("water_movement_speed", 0.5),
                section.getDouble("surface_movement_speed", 0.7),
                section.getBoolean("can_breathe_underwater", false),
                section.getDouble("drown_damage", 2.0),
                section.getBoolean("convert_to_drowned", true),
                section.getBoolean("surface_seeking", false)
        );
    }

    private static ImmunitiesConfig parseImmunities(ConfigurationSection section) {
        if (section == null) {
            return ImmunitiesConfig.DEFAULT;
        }
        return new ImmunitiesConfig(
                section.getBoolean("fire", false),
                section.getBoolean("projectile", false),
                section.getBoolean("melee", false),
                section.getBoolean("potion", false),
                section.getBoolean("sunlight", false),
                section.getBoolean("explosion", false),
                section.getBoolean("fall", false),
                section.getBoolean("drowning", false),
                section.getBoolean("magic", false),
                section.getBoolean("freeze", false)
        );
    }

    private static BreakDoorConfig parseBreakDoor(ConfigurationSection section) {
        if (section == null) {
            return BreakDoorConfig.DEFAULT;
        }
        return new BreakDoorConfig(
                section.getBoolean("enabled", false),
                section.getDouble("chance", 0.0)
        );
    }

    private static AIConfig parseAI(ConfigurationSection section) {
        if (section == null) {
            return AIConfig.DEFAULT;
        }
        return new AIConfig(
                section.getBoolean("use_custom_ai", false),
                section.getBoolean("remove_default_goals", false),
                section.getDouble("target_range", 32.0),
                section.getBoolean("always_aggressive", false),
                section.getStringList("targets"),
                parseTargetingStrategy(section.getConfigurationSection("targeting_strategy")),
                parsePathfinding(section.getConfigurationSection("pathfinding")),
                parseBehavior(section.getConfigurationSection("behavior"))
        );
    }

    private static TargetingStrategy parseTargetingStrategy(ConfigurationSection section) {
        if (section == null) {
            return TargetingStrategy.DEFAULT;
        }
        return new TargetingStrategy(
                section.getString("type", "nearest"),
                section.getInt("switch_interval", 200),
                section.getDouble("switch_threshold", 0.0),
                section.getBoolean("prefer_players", true),
                section.getInt("max_targets_memory", 1)
        );
    }

    private static PathfindingConfig parsePathfinding(ConfigurationSection section) {
        if (section == null) {
            return PathfindingConfig.DEFAULT;
        }
        return new PathfindingConfig(
                section.getBoolean("can_open_doors", false),
                section.getBoolean("can_pass_doors", true),
                section.getBoolean("can_float", false),
                section.getBoolean("avoid_water", false),
                section.getBoolean("avoid_sun", false),
                section.getBoolean("avoid_fire", false),
                section.getDouble("walk_speed", 0.25)
        );
    }

    private static BehaviorConfig parseBehavior(ConfigurationSection section) {
        if (section == null) {
            return BehaviorConfig.DEFAULT;
        }
        return new BehaviorConfig(
                section.getDouble("leash_range", 32.0),
                section.getDouble("reaggro_range", 16.0),
                section.getInt("attack_cooldown", 20),
                section.getBoolean("circle_target", false),
                section.getDouble("circle_radius", 3.0),
                section.getBoolean("keep_distance", false),
                section.getBoolean("strafe", false)
        );
    }

    private static DropsConfig parseDrops(ConfigurationSection section, String mobId) {
        if (section == null) {
            return DropsConfig.DEFAULT;
        }
        return new DropsConfig(
                section.getBoolean("override_vanilla", false),
                section.getInt("experience", 0),
                parseDropEntries(section.getMapList("items"), mobId)
        );
    }

    @SuppressWarnings("unchecked")
    private static List<DropEntry> parseDropEntries(List<?> list, String mobId) {
        List<DropEntry> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            String item = getString(map, "item");
            if (item == null || item.isEmpty()) {
                continue;
            }
            result.add(new DropEntry(
                    item,
                    toInt(map.get("amount"), 1),
                    toDouble(map.get("chance"), 100.0)
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<SkillBinding> parseSkills(List<?> list) {
        List<SkillBinding> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            String skillId = getString(map, "skill_id");
            if (skillId == null) {
                skillId = getString(map, "skill");
            }
            String trigger = getString(map, "trigger");
            double chance = toDouble(map.get("chance"), 100.0);
            double cooldown = toDouble(map.get("cooldown"), -1.0);
            int level = toInt(map.get("level"), 1);
            result.add(new SkillBinding(skillId, trigger, chance, cooldown, level));
        }
        return result;
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> clazz, String value, T def) {
        if (value == null) {
            return def;
        }
        try {
            return Enum.valueOf(clazz, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private static int toInt(Object value, int def) {
        if (value == null) {
            return def;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double toDouble(Object value, double def) {
        if (value == null) {
            return def;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean toBoolean(Object value, boolean def) {
        if (value == null) {
            return def;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private static void log(String message) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, message);
        }
    }

    // ==================== Nested config records ====================

    public record MobAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation) {
    }

    public record EquipmentEntry(String item, double dropChance) {
    }

    public record EquipmentEffects(boolean enabled, boolean applyAttributes, boolean applyEnchantments,
                                    boolean applySpecial) {
        public static final EquipmentEffects DEFAULT = new EquipmentEffects(true, true, true, true);
    }

    public record BossBarConfig(boolean enabled, String title, BarColor color, BarStyle style, boolean showToAll,
                                 double range) {
        public static final BossBarConfig DEFAULT = new BossBarConfig(false, null, BarColor.RED, BarStyle.SOLID, false, 32.0);
    }

    public enum ParticleLocation {
        FEET, HEAD, CENTER
    }

    public record ParticleConfig(Particle type, ParticleLocation location, int count, double offsetX, double offsetY,
                                  double offsetZ, String color, double size, int interval) {
    }

    public record AmbientSoundConfig(Sound sound, int interval, float volume, float pitch) {
        public static final AmbientSoundConfig DEFAULT = new AmbientSoundConfig(null, 100, 1.0f, 1.0f);
    }

    public record PotionEffectConfig(PotionEffectType type, int amplifier, boolean ambient, boolean particles) {
    }

    public record SensesConfig(boolean vision, boolean hearing, boolean smell, double visionRange,
                                double hearingRange, double smellRange) {
        public static final SensesConfig DEFAULT = new SensesConfig(true, true, false, 32.0, 16.0, 8.0);
    }

    public record WaterBehaviorConfig(boolean floatOnWater, double waterMovementSpeed, double surfaceMovementSpeed,
                                       boolean canBreatheUnderwater, double drownDamage, boolean convertToDrowned,
                                       boolean surfaceSeeking) {
        public static final WaterBehaviorConfig DEFAULT = new WaterBehaviorConfig(true, 0.5, 0.7, false, 2.0, true, false);
    }

    public record ImmunitiesConfig(boolean fire, boolean projectile, boolean melee, boolean potion, boolean sunlight,
                                    boolean explosion, boolean fall, boolean drowning, boolean magic, boolean freeze) {
        public static final ImmunitiesConfig DEFAULT = new ImmunitiesConfig(false, false, false, false, false,
                false, false, false, false, false);
    }

    public record BreakDoorConfig(boolean enabled, double chance) {
        public static final BreakDoorConfig DEFAULT = new BreakDoorConfig(false, 0.0);
    }

    public record AIConfig(boolean useCustomAi, boolean removeDefaultGoals, double targetRange,
                           boolean alwaysAggressive, List<String> targets, TargetingStrategy targetingStrategy,
                           PathfindingConfig pathfinding, BehaviorConfig behavior) {
        public static final AIConfig DEFAULT = new AIConfig(false, false, 32.0, false, List.of(),
                TargetingStrategy.DEFAULT, PathfindingConfig.DEFAULT, BehaviorConfig.DEFAULT);
    }

    public record TargetingStrategy(String type, int switchInterval, double switchThreshold, boolean preferPlayers,
                                     int maxTargetsMemory) {
        public static final TargetingStrategy DEFAULT = new TargetingStrategy("nearest", 200, 0.0, true, 1);
    }

    public record PathfindingConfig(boolean canOpenDoors, boolean canPassDoors, boolean canFloat, boolean avoidWater,
                                     boolean avoidSun, boolean avoidFire, double walkSpeed) {
        public static final PathfindingConfig DEFAULT = new PathfindingConfig(false, true, false, false, false, false, 0.25);
    }

    public record BehaviorConfig(double leashRange, double reaggroRange, int attackCooldown, boolean circleTarget,
                                  double circleRadius, boolean keepDistance, boolean strafe) {
        public static final BehaviorConfig DEFAULT = new BehaviorConfig(32.0, 16.0, 20, false, 3.0, false, false);
    }

    public record DropEntry(String item, int amount, double chance) {
    }

    public record DropsConfig(boolean overrideVanilla, int experience, List<DropEntry> items) {
        public static final DropsConfig DEFAULT = new DropsConfig(false, 0, Collections.emptyList());
    }
}
