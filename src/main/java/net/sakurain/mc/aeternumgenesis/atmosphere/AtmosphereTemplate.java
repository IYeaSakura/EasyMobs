package net.sakurain.mc.aeternumgenesis.atmosphere;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.ConfigParseUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Immutable data class representing an atmosphere template loaded from YAML.
 */
public final class AtmosphereTemplate {

    private final String id;
    private final int priority;
    private final boolean stackable;
    private final WeatherLayer weather;
    private final List<PotionEffect> potionEffects;
    private final List<ParticleLayer> particles;
    private final List<SoundLayer> sounds;
    private final UiLayer ui;
    private final List<EntityModifier> entityModifiers;
    private final EnvironmentLayer environment;
    private final TransitionConfig transition;

    public AtmosphereTemplate(String id, int priority, boolean stackable, WeatherLayer weather,
                              List<PotionEffect> potionEffects, List<ParticleLayer> particles,
                              List<SoundLayer> sounds, UiLayer ui, List<EntityModifier> entityModifiers,
                              EnvironmentLayer environment, TransitionConfig transition) {
        this.id = id;
        this.priority = priority;
        this.stackable = stackable;
        this.weather = weather == null ? WeatherLayer.DEFAULT : weather;
        this.potionEffects = potionEffects == null ? List.of() : List.copyOf(potionEffects);
        this.particles = particles == null ? List.of() : List.copyOf(particles);
        this.sounds = sounds == null ? List.of() : List.copyOf(sounds);
        this.ui = ui == null ? UiLayer.DEFAULT : ui;
        this.entityModifiers = entityModifiers == null ? List.of() : List.copyOf(entityModifiers);
        this.environment = environment == null ? EnvironmentLayer.DEFAULT : environment;
        this.transition = transition == null ? TransitionConfig.DEFAULT : transition;
    }

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isStackable() {
        return stackable;
    }

    public WeatherLayer getWeather() {
        return weather;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public List<ParticleLayer> getParticles() {
        return particles;
    }

    public List<SoundLayer> getSounds() {
        return sounds;
    }

    public UiLayer getUi() {
        return ui;
    }

    public List<EntityModifier> getEntityModifiers() {
        return entityModifiers;
    }

    public EnvironmentLayer getEnvironment() {
        return environment;
    }

    public TransitionConfig getTransition() {
        return transition;
    }

    public static AtmosphereTemplate fromConfig(String id, ConfigurationSection config) {
        int priority = config.getInt("priority", 0);
        boolean stackable = config.getBoolean("stackable", false);

        ConfigurationSection layers = config.getConfigurationSection("layers");
        if (layers == null) {
            layers = config;
        }

        WeatherLayer weather = parseWeatherLayer(layers.getConfigurationSection("weather"));
        List<PotionEffect> potionEffects = ConfigParseUtil.parsePotionEffects(
                layers.getList("potion_effects"), "[" + id + "] potion_effects");
        List<ParticleLayer> particles = parseParticleLayers(layers.getList("particles"), id);
        List<SoundLayer> sounds = parseSoundLayers(layers.getList("sounds"), id);
        UiLayer ui = parseUiLayer(layers.getConfigurationSection("ui"));
        List<EntityModifier> entityModifiers = parseEntityModifiers(layers.getList("entity_modifiers"), id);
        EnvironmentLayer environment = parseEnvironmentLayer(layers.getConfigurationSection("environment"));
        TransitionConfig transition = parseTransitionConfig(config.getConfigurationSection("transition"));

        return new AtmosphereTemplate(id, priority, stackable, weather, potionEffects, particles,
                sounds, ui, entityModifiers, environment, transition);
    }

    private static WeatherLayer parseWeatherLayer(ConfigurationSection section) {
        if (section == null) {
            return WeatherLayer.DEFAULT;
        }
        WeatherType type = parseWeatherType(section.getString("type"));
        return new WeatherLayer(type);
    }

    private static WeatherType parseWeatherType(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "CLEAR", "SUNNY" -> WeatherType.CLEAR;
            case "DOWNFALL", "RAIN", "RAINING" -> WeatherType.DOWNFALL;
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private static List<ParticleLayer> parseParticleLayers(List<?> list, String atmosphereId) {
        List<ParticleLayer> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            Particle particle = ConfigParseUtil.parseParticle(ConfigParseUtil.getString(map, "type"));
            if (particle == null) {
                log("[" + atmosphereId + "] Unknown particle: " + ConfigParseUtil.getString(map, "type"));
                continue;
            }
            String density = ConfigParseUtil.getString(map, "density");
            if (density == null) {
                density = "medium";
            }
            String pattern = ConfigParseUtil.getString(map, "pattern");
            if (pattern == null) {
                pattern = "ambient";
            }
            double radius = ConfigParseUtil.toDouble(map.get("radius"), 10.0);
            double[] offset = ConfigParseUtil.parseOffset(ConfigParseUtil.getString(map, "offset"));
            int interval = ConfigParseUtil.toInt(map.get("interval"), 20);
            String colorHex = ConfigParseUtil.getString(map, "color");
            int count = ConfigParseUtil.toInt(map.get("count"), densityToCount(density));
            result.add(new ParticleLayer(particle, density, pattern, radius, offset[0], offset[1], offset[2],
                    interval, ConfigParseUtil.parseColor(colorHex), count));
        }
        return result;
    }

    private static int densityToCount(String density) {
        return switch (density.toLowerCase()) {
            case "low" -> 5;
            case "high" -> 30;
            case "medium" -> 15;
            default -> 10;
        };
    }

    @SuppressWarnings("unchecked")
    private static List<SoundLayer> parseSoundLayers(List<?> list, String atmosphereId) {
        List<SoundLayer> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            Sound sound = ConfigParseUtil.parseSound(ConfigParseUtil.getString(map, "id"));
            if (sound == null) {
                sound = ConfigParseUtil.parseSound(ConfigParseUtil.getString(map, "sound"));
            }
            if (sound == null) {
                log("[" + atmosphereId + "] Unknown sound: " + ConfigParseUtil.getString(map, "id"));
                continue;
            }
            String type = ConfigParseUtil.getString(map, "type");
            if (type == null) {
                type = "ambient";
            }
            float volume = (float) ConfigParseUtil.toDouble(map.get("volume"), 1.0);
            float pitch = (float) ConfigParseUtil.toDouble(map.get("pitch"), 1.0);
            int interval = ConfigParseUtil.toInt(map.get("interval"), 100);
            double chance = ConfigParseUtil.toDouble(map.get("chance"), 1.0);
            result.add(new SoundLayer(sound, type, volume, pitch, interval, chance));
        }
        return result;
    }

    private static UiLayer parseUiLayer(ConfigurationSection section) {
        if (section == null) {
            return UiLayer.DEFAULT;
        }
        String actionBar = section.getString("action_bar");
        TitleConfig title = parseTitleConfig(section.getConfigurationSection("title"));
        BossBarConfig bossBar = parseBossBarConfig(section.getConfigurationSection("boss_bar"));
        return new UiLayer(actionBar, title, bossBar);
    }

    private static TitleConfig parseTitleConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        return new TitleConfig(
                section.getString("text"),
                section.getString("subtitle"),
                section.getInt("fade_in", 10),
                section.getInt("stay", 70),
                section.getInt("fade_out", 20)
        );
    }

    private static BossBarConfig parseBossBarConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        BarColor color = parseEnum(BarColor.class, section.getString("color"), BarColor.RED);
        BarStyle style = parseEnum(BarStyle.class, section.getString("style"), BarStyle.SOLID);
        return new BossBarConfig(
                section.getString("text"),
                color,
                style,
                section.getString("progress_source")
        );
    }

    @SuppressWarnings("unchecked")
    private static List<EntityModifier> parseEntityModifiers(List<?> list, String atmosphereId) {
        List<EntityModifier> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            String target = ConfigParseUtil.getString(map, "target");
            if (target == null) {
                target = "all";
            }
            Map<Attribute, AttributeModifier> attributes = parseAttributeModifiers(
                    (List<?>) map.get("attributes"), atmosphereId);
            boolean glow = ConfigParseUtil.toBoolean(map.get("glow"), false);
            String glowColor = ConfigParseUtil.getString(map, "glow_color");
            result.add(new EntityModifier(target, attributes, glow, glowColor));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<Attribute, AttributeModifier> parseAttributeModifiers(List<?> list, String atmosphereId) {
        Map<Attribute, AttributeModifier> result = new HashMap<>();
        if (list == null) {
            return result;
        }
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            String typeName = ConfigParseUtil.getString(map, "type");
            Attribute attribute = parseAttribute(typeName);
            if (attribute == null) {
                log("[" + atmosphereId + "] Unknown attribute: " + typeName);
                continue;
            }
            double amount = ConfigParseUtil.toDouble(map.get("amount"), 0.0);
            String operationName = ConfigParseUtil.getString(map, "operation");
            AttributeModifier.Operation operation = parseOperation(operationName);
            NamespacedKey key = new NamespacedKey("genesis", "atmosphere_attr_" + attribute.getKey().getKey());
            result.put(attribute, new AttributeModifier(key, amount, operation));
        }
        return result;
    }

    private static Attribute parseAttribute(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
        return org.bukkit.Registry.ATTRIBUTE.get(key);
    }

    private static AttributeModifier.Operation parseOperation(String value) {
        if (value == null || value.isEmpty()) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
        try {
            return AttributeModifier.Operation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
    }

    private static EnvironmentLayer parseEnvironmentLayer(ConfigurationSection section) {
        if (section == null) {
            return EnvironmentLayer.DEFAULT;
        }
        return new EnvironmentLayer(
                section.getBoolean("fire_spread", true),
                section.getBoolean("block_melting", true),
                section.getBoolean("mob_griefing", true)
        );
    }

    private static TransitionConfig parseTransitionConfig(ConfigurationSection section) {
        if (section == null) {
            return TransitionConfig.DEFAULT;
        }
        return new TransitionConfig(
                section.getInt("enter_duration", 0),
                section.getInt("exit_duration", 0),
                section.getBoolean("exit_potion_clear", true),
                section.getBoolean("exit_weather_restore", true)
        );
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

    private static void log(String message) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, message);
        }
    }

    // ==================== Nested config records ====================

    public record WeatherLayer(WeatherType type) {
        public static final WeatherLayer DEFAULT = new WeatherLayer(null);
    }

    public record ParticleLayer(Particle type, String density, String pattern, double radius,
                                 double offsetX, double offsetY, double offsetZ, int interval,
                                 Color color, int count) {
    }

    public record SoundLayer(Sound sound, String type, float volume, float pitch, int interval, double chance) {
    }

    public record UiLayer(String actionBar, TitleConfig title, BossBarConfig bossBar) {
        public static final UiLayer DEFAULT = new UiLayer(null, null, null);
    }

    public record TitleConfig(String text, String subtitle, int fadeIn, int stay, int fadeOut) {
    }

    public record BossBarConfig(String text, BarColor color, BarStyle style, String progressSource) {
    }

    public record EntityModifier(String target, Map<Attribute, AttributeModifier> attributes,
                                  boolean glow, String glowColor) {
    }

    public record EnvironmentLayer(boolean fireSpread, boolean blockMelting, boolean mobGriefing) {
        public static final EnvironmentLayer DEFAULT = new EnvironmentLayer(true, true, true);
    }

    public record TransitionConfig(int enterDuration, int exitDuration, boolean exitPotionClear,
                                    boolean exitWeatherRestore) {
        public static final TransitionConfig DEFAULT = new TransitionConfig(0, 0, true, true);
    }
}
