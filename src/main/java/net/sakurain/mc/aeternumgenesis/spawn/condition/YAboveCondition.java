package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class YAboveCondition implements SpawnCondition {

    private double minY = 0;

    @Override
    public String getType() {
        return "y_above";
    }

    @Override
    public void parse(String arguments) {
        try {
            this.minY = Double.parseDouble(arguments.trim());
        } catch (NumberFormatException | NullPointerException e) {
            this.minY = 0;
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        return location.getY() > minY;
    }
}
