package net.sakurain.mc.aeternumgenesis.spawn.condition;

import net.sakurain.mc.aeternumgenesis.spawn.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class MoonPhaseCondition implements SpawnCondition {

    private int expectedPhase = -1;

    @Override
    public String getType() {
        return "moon_phase";
    }

    @Override
    public void parse(String arguments) {
        this.expectedPhase = parsePhase(arguments);
    }

    @Override
    public boolean test(Location location, EntityType originalType) {
        World world = location.getWorld();
        if (world == null || expectedPhase < 0) {
            return false;
        }
        long day = world.getFullTime() / 24000L;
        int phase = Math.floorMod(day, 8);
        return phase == expectedPhase;
    }

    private int parsePhase(String value) {
        if (value == null) {
            return -1;
        }
        return switch (value.trim().toLowerCase()) {
            case "full" -> 0;
            case "waning_gibbous" -> 1;
            case "last_quarter" -> 2;
            case "waning_crescent" -> 3;
            case "new" -> 4;
            case "waxing_crescent" -> 5;
            case "first_quarter" -> 6;
            case "waxing_gibbous" -> 7;
            default -> -1;
        };
    }
}
