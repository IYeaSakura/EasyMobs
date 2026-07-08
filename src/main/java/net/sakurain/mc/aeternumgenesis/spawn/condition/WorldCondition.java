package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class WorldCondition implements SpawnCondition {

    private final Set<String> worlds = new HashSet<>();

    @Override
    public String getType() {
        return "world";
    }

    @Override
    public void parse(String arguments) {
        worlds.clear();
        if (arguments == null || arguments.isBlank()) {
            return;
        }
        for (String part : arguments.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                worlds.add(trimmed);
            }
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        if (worlds.isEmpty()) {
            return true;
        }
        World world = location.getWorld();
        return world != null && worlds.contains(world.getName());
    }
}
