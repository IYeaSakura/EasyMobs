package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class TimeRangeCondition implements SpawnCondition {

    private long min = 0;
    private long max = 24000;

    @Override
    public String getType() {
        return "time_range";
    }

    @Override
    public void parse(String arguments) {
        String[] parts = arguments.split("-", 2);
        if (parts.length != 2) {
            return;
        }
        try {
            this.min = Long.parseLong(parts[0].trim());
            this.max = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException e) {
            this.min = 0;
            this.max = 24000;
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        long time = world.getTime();
        if (min <= max) {
            return time >= min && time <= max;
        }
        return time >= min || time <= max;
    }
}
