package net.sakurain.mc.aeternumgenesis.api;

import net.sakurain.mc.aeternumgenesis.atmosphere.ActiveAtmosphere;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AtmosphereAPI {

    /**
     * Applies an atmosphere at the given location.
     *
     * @param center        center location
     * @param radius        affected radius
     * @param atmosphereId  template id
     * @param durationTicks duration in ticks, or <= 0 for permanent
     * @return the instance UUID, or null if template not found
     */
    UUID applyAtmosphere(@NotNull Location center, double radius, @NotNull String atmosphereId, long durationTicks);

    /**
     * Removes a specific atmosphere instance.
     *
     * @param instanceId the instance UUID
     * @return true if removed
     */
    boolean removeAtmosphere(@NotNull UUID instanceId);

    /**
     * Removes all active atmosphere instances.
     */
    void removeAllAtmospheres();

    /**
     * Returns all atmospheres affecting the given location, sorted by priority descending.
     *
     * @param location the location
     * @return list of active atmospheres
     */
    @NotNull
    List<ActiveAtmosphere> getAffectingAtmospheres(@NotNull Location location);

    /**
     * Returns a loaded atmosphere template by id.
     *
     * @param id template id
     * @return the active instance, or null
     */
    ActiveAtmosphere getAtmosphere(@NotNull UUID id);

    /**
     * Returns all loaded atmosphere template ids.
     *
     * @return set of ids
     */
    @NotNull
    Set<String> getTemplateIds();

    /**
     * Returns the number of loaded atmosphere templates.
     *
     * @return count
     */
    int getTemplateCount();

    /**
     * Returns the number of currently active atmosphere instances.
     *
     * @return count
     */
    int getActiveCount();
}
