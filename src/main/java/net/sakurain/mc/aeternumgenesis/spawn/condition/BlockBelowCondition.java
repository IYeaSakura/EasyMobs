package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class BlockBelowCondition implements SpawnCondition {

    private final Set<String> materials = new HashSet<>();

    @Override
    public String getType() {
        return "block_below";
    }

    @Override
    public void parse(String arguments) {
        materials.clear();
        if (arguments == null || arguments.isBlank()) {
            return;
        }
        for (String part : arguments.split(",")) {
            String trimmed = part.trim().toUpperCase();
            if (!trimmed.isEmpty()) {
                materials.add(trimmed);
            }
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        if (materials.isEmpty()) {
            return true;
        }
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        Material type = world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).getType();
        return materials.contains(type.name());
    }
}
