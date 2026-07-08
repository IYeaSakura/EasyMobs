package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class YBelowCondition implements SpawnCondition {

    private double maxY = 256;

    @Override
    public String getType() {
        return "y_below";
    }

    @Override
    public void parse(String arguments) {
        try {
            this.maxY = Double.parseDouble(arguments.trim());
        } catch (NumberFormatException | NullPointerException e) {
            this.maxY = 256;
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        return location.getY() < maxY;
    }
}
