package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class RandomChanceCondition implements SpawnCondition {

    private double chance = 1.0;

    @Override
    public String getType() {
        return "random_chance";
    }

    @Override
    public void parse(String arguments) {
        try {
            this.chance = Double.parseDouble(arguments.trim());
        } catch (NumberFormatException | NullPointerException e) {
            this.chance = 1.0;
        }
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        return Math.random() < chance;
    }
}
