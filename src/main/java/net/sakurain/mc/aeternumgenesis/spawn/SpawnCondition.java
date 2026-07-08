package net.sakurain.mc.aeternumgenesis.spawn;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
 * A condition that must pass for a spawn rule to apply.
 */
public interface SpawnCondition {

    /**
     * @return the condition type id (e.g. "night")
     */
    String getType();

    /**
     * Parses the condition arguments from the configuration string.
     *
     * @param arguments arguments after the condition type
     */
    void parse(String arguments);

    /**
     * Tests this condition at the given location.
     *
     * @param location     location to test
     * @param originalType original entity type being replaced/denied, may be null for ADD spawns
     * @return true if the condition passes
     */
    boolean test(Location location, EntityType originalType);
}
