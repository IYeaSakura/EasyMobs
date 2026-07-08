package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import net.sakurain.mc.aeternumgenesis.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class InsideCondition implements SpawnCondition {

    private boolean expected = true;

    @Override
    public String getType() {
        return "inside";
    }

    @Override
    public void parse(String arguments) {
        this.expected = parseBoolean(arguments);
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        return !LocationUtil.isOutside(location) == expected;
    }

    private boolean parseBoolean(String value) {
        return value == null || value.isBlank() || Boolean.parseBoolean(value.trim());
    }
}
