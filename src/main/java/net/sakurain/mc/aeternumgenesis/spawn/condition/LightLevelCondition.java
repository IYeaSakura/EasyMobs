package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class LightLevelCondition implements SpawnCondition {

    private int min = 0;
    private int max = 15;

    @Override
    public String getType() {
        return "light_level";
    }

    @Override
    public void parse(String arguments) {
        String[] parts = arguments.split("-", 2);
        if (parts.length == 2) {
            try {
                this.min = Integer.parseInt(parts[0].trim());
                this.max = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                this.min = 0;
                this.max = 15;
            }
        } else {
            try {
                this.min = this.max = Integer.parseInt(arguments.trim());
            } catch (NumberFormatException e) {
                this.min = 0;
                this.max = 15;
            }
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        if (location.getWorld() == null) {
            return false;
        }
        int light = location.getBlock().getLightLevel();
        return light >= Math.min(min, max) && light <= Math.max(min, max);
    }
}
