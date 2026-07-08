package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class BiomeCondition implements SpawnCondition {

    private final Set<String> biomes = new HashSet<>();

    @Override
    public String getType() {
        return "biome";
    }

    @Override
    public void parse(String arguments) {
        biomes.clear();
        if (arguments == null || arguments.isBlank()) {
            return;
        }
        for (String part : arguments.split(",")) {
            String trimmed = part.trim().toUpperCase();
            if (!trimmed.isEmpty()) {
                biomes.add(trimmed);
            }
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        if (biomes.isEmpty()) {
            return true;
        }
        Biome biome = location.getBlock().getBiome();
        return biomes.contains(biome.getKey().getKey().toUpperCase());
    }
}
