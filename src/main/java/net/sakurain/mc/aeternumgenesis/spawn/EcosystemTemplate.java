package net.sakurain.mc.aeternumgenesis.spawn;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.ConfigParseUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Immutable data class representing an ecosystem that binds mobs to biomes/regions.
 */
public final class EcosystemTemplate {

    private final String id;
    private final Set<Biome> biomes;
    private final List<RegionFilter> regions;
    private final EnvironmentConditions environment;
    private final List<SpawnEntry> spawnEntries;
    private final List<AmbientParticle> ambientParticles;
    private final List<AmbientSound> ambientSounds;

    public EcosystemTemplate(String id, Set<Biome> biomes, List<RegionFilter> regions,
                             EnvironmentConditions environment, List<SpawnEntry> spawnEntries,
                             List<AmbientParticle> ambientParticles, List<AmbientSound> ambientSounds) {
        this.id = id;
        this.biomes = biomes == null ? Set.of() : Set.copyOf(biomes);
        this.regions = regions == null ? List.of() : List.copyOf(regions);
        this.environment = environment == null ? EnvironmentConditions.DEFAULT : environment;
        this.spawnEntries = spawnEntries == null ? List.of() : List.copyOf(spawnEntries);
        this.ambientParticles = ambientParticles == null ? List.of() : List.copyOf(ambientParticles);
        this.ambientSounds = ambientSounds == null ? List.of() : List.copyOf(ambientSounds);
    }

    public String getId() {
        return id;
    }

    public Set<Biome> getBiomes() {
        return biomes;
    }

    public List<RegionFilter> getRegions() {
        return regions;
    }

    public EnvironmentConditions getEnvironment() {
        return environment;
    }

    public List<SpawnEntry> getSpawnEntries() {
        return spawnEntries;
    }

    public List<AmbientParticle> getAmbientParticles() {
        return ambientParticles;
    }

    public List<AmbientSound> getAmbientSounds() {
        return ambientSounds;
    }

    public static EcosystemTemplate fromConfig(String id, ConfigurationSection config) {
        Set<Biome> biomes = parseBiomes(config.getStringList("biomes"));
        List<RegionFilter> regions = parseRegions(config.getConfigurationSection("regions"), id);
        EnvironmentConditions environment = parseEnvironmentConditions(config.getConfigurationSection("environment"));
        List<SpawnEntry> spawnEntries = parseSpawnEntries(config.getConfigurationSection("spawn_rules"), id);
        List<AmbientParticle> ambientParticles = parseAmbientParticles(config.getList("ambient_particles"), id);
        List<AmbientSound> ambientSounds = parseAmbientSounds(config.getList("ambient_sounds"), id);
        return new EcosystemTemplate(id, biomes, regions, environment, spawnEntries, ambientParticles, ambientSounds);
    }

    private static Set<Biome> parseBiomes(List<String> list) {
        Set<Biome> result = new HashSet<>();
        if (list == null) {
            return result;
        }
        org.bukkit.Registry<Biome> registry = io.papermc.paper.registry.RegistryAccess
                .registryAccess().getRegistry(io.papermc.paper.registry.RegistryKey.BIOME);
        if (registry == null) {
            return result;
        }
        for (String value : list) {
            NamespacedKey key = NamespacedKey.minecraft(value.toLowerCase());
            Biome biome = registry.get(key);
            if (biome != null) {
                result.add(biome);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<RegionFilter> parseRegions(ConfigurationSection section, String ecosystemId) {
        List<RegionFilter> result = new ArrayList<>();
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationRegionEntry entry = parseRegionEntry(section.getConfigurationSection(key));
            if (entry != null) {
                result.add(new RegionFilter(entry.type(), entry.world(), entry.region(), entry.bounds()));
            }
        }
        return result;
    }

    private static ConfigurationRegionEntry parseRegionEntry(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String type = section.getString("type", "worldguard");
        String region = section.getString("region");
        String world = section.getString("world");
        String bounds = section.getString("bounds");
        return new ConfigurationRegionEntry(type, world, region, bounds);
    }

    private record ConfigurationRegionEntry(String type, String world, String region, String bounds) {
        RegionFilter toFilter() {
            return new RegionFilter(type, world, region, bounds);
        }
    }

    private static EnvironmentConditions parseEnvironmentConditions(ConfigurationSection section) {
        if (section == null) {
            return EnvironmentConditions.DEFAULT;
        }
        return new EnvironmentConditions(
                section.getString("required_light_level"),
                section.getString("required_time"),
                section.getString("required_weather"),
                section.getString("required_moon_phase")
        );
    }

    private static List<SpawnEntry> parseSpawnEntries(ConfigurationSection section, String ecosystemId) {
        List<SpawnEntry> result = new ArrayList<>();
        if (section == null) {
            return result;
        }
        for (String mobId : section.getKeys(false)) {
            ConfigurationSection mobSection = section.getConfigurationSection(mobId);
            if (mobSection == null) {
                continue;
            }
            int weight = mobSection.getInt("weight", 10);
            String groupSize = mobSection.getString("group_size", "1-1");
            int maxPerChunk = mobSection.getInt("max_per_chunk", -1);
            Map<String, String> conditions = parseConditions(mobSection.getConfigurationSection("conditions"));
            result.add(new SpawnEntry(mobId, weight, groupSize, maxPerChunk, conditions));
        }
        return result;
    }

    private static Map<String, String> parseConditions(ConfigurationSection section) {
        Map<String, String> result = new HashMap<>();
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            result.put(key, String.valueOf(section.get(key)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<AmbientParticle> parseAmbientParticles(List<?> list, String ecosystemId) {
        List<AmbientParticle> result = new ArrayList<>();
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
                log("[" + ecosystemId + "] Unknown ambient particle: " + ConfigParseUtil.getString(map, "type"));
                continue;
            }
            String density = ConfigParseUtil.getString(map, "density");
            if (density == null) {
                density = "low";
            }
            String height = ConfigParseUtil.getString(map, "height");
            if (height == null) {
                height = "ground+2";
            }
            int interval = ConfigParseUtil.toInt(map.get("interval"), 40);
            result.add(new AmbientParticle(particle, density, height, interval));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<AmbientSound> parseAmbientSounds(List<?> list, String ecosystemId) {
        List<AmbientSound> result = new ArrayList<>();
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
                log("[" + ecosystemId + "] Unknown ambient sound: " + ConfigParseUtil.getString(map, "id"));
                continue;
            }
            float volume = (float) ConfigParseUtil.toDouble(map.get("volume"), 1.0);
            int interval = ConfigParseUtil.toInt(map.get("interval"), 200);
            double chance = ConfigParseUtil.toDouble(map.get("chance"), 0.3);
            result.add(new AmbientSound(sound, volume, interval, chance));
        }
        return result;
    }

    private static void log(String message) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin != null) {
            plugin.getLogger().log(Level.WARNING, message);
        }
    }

    // ==================== Nested config records ====================

    public record RegionFilter(String type, String world, String region, String bounds) {
    }

    public record EnvironmentConditions(String requiredLightLevel, String requiredTime,
                                         String requiredWeather, String requiredMoonPhase) {
        public static final EnvironmentConditions DEFAULT = new EnvironmentConditions(null, null, null, null);
    }

    public record SpawnEntry(String mobId, int weight, String groupSize, int maxPerChunk,
                              Map<String, String> conditions) {
    }

    public record AmbientParticle(Particle type, String density, String height, int interval) {
    }

    public record AmbientSound(Sound sound, float volume, int interval, double chance) {
    }
}
