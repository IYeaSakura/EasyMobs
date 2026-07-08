package net.sakurain.mc.aeternumgenesis.atmosphere;

import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

/**
 * Tracks which atmosphere is currently applied to a player and what needs cleanup.
 */
public final class AtmosphereState {

    private final UUID atmosphereInstanceId;
    private final String atmosphereId;
    private final List<PotionEffectType> potionEffectTypes;
    private final boolean clearPotionOnExit;
    private final boolean restoreWeatherOnExit;

    public AtmosphereState(ActiveAtmosphere atmosphere, List<PotionEffectType> potionEffectTypes,
                           boolean clearPotionOnExit, boolean restoreWeatherOnExit) {
        this.atmosphereInstanceId = atmosphere.getInstanceId();
        this.atmosphereId = atmosphere.getTemplateId();
        this.potionEffectTypes = potionEffectTypes == null ? List.of() : List.copyOf(potionEffectTypes);
        this.clearPotionOnExit = clearPotionOnExit;
        this.restoreWeatherOnExit = restoreWeatherOnExit;
    }

    public UUID getAtmosphereInstanceId() {
        return atmosphereInstanceId;
    }

    public String getAtmosphereId() {
        return atmosphereId;
    }

    public List<PotionEffectType> getPotionEffectTypes() {
        return potionEffectTypes;
    }

    public boolean isClearPotionOnExit() {
        return clearPotionOnExit;
    }

    public boolean isRestoreWeatherOnExit() {
        return restoreWeatherOnExit;
    }
}
