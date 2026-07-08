package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class YRangeCondition implements SpawnCondition {

    private double minY = 0;
    private double maxY = 0;

    @Override
    public String getType() {
        return "y_range";
    }

    @Override
    public void parse(String arguments) {
        String[] parts = arguments.split("-", 2);
        if (parts.length != 2) {
            return;
        }
        try {
            this.minY = Double.parseDouble(parts[0].trim());
            this.maxY = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            this.minY = 0;
            this.maxY = 0;
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        double y = location.getY();
        return y >= Math.min(minY, maxY) && y <= Math.max(minY, maxY);
    }
}
