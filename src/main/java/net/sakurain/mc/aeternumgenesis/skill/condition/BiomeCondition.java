package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;

import java.util.List;

public class BiomeCondition extends AbstractSkillCondition {

    public BiomeCondition() {
        super("biome");
    }

    @Override
    public boolean test(SkillContext context) {
        Location loc = context.getCaster() != null ? context.getCaster().getLocation() : context.getOrigin();
        if (loc == null || loc.getWorld() == null) return false;
        List<String> biomes = stringList("biomes");
        if (biomes.isEmpty()) return true;
        String biomeKey = loc.getBlock().getBiome().getKey().toString().toLowerCase();
        String biomeName = loc.getBlock().getBiome().getKey().getKey().toLowerCase();
        return biomes.contains(biomeKey) || biomes.contains(biomeName);
    }
}
