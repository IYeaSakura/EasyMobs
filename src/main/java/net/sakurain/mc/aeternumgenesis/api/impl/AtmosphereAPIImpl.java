package net.sakurain.mc.aeternumgenesis.api.impl;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.AtmosphereAPI;
import net.sakurain.mc.aeternumgenesis.atmosphere.ActiveAtmosphere;
import net.sakurain.mc.aeternumgenesis.atmosphere.AtmosphereManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AtmosphereAPIImpl implements AtmosphereAPI {

    private final AtmosphereManager manager;

    public AtmosphereAPIImpl(@NotNull AeternumGenesisPlugin plugin) {
        this.manager = plugin.getAtmosphereManager();
    }

    @Override
    public UUID applyAtmosphere(@NotNull Location center, double radius, @NotNull String atmosphereId, long durationTicks) {
        return manager.applyAtmosphere(center, radius, atmosphereId, durationTicks);
    }

    @Override
    public boolean removeAtmosphere(@NotNull UUID instanceId) {
        return manager.removeAtmosphere(instanceId);
    }

    @Override
    public void removeAllAtmospheres() {
        manager.removeAllAtmospheres();
    }

    @Override
    public @NotNull List<ActiveAtmosphere> getAffectingAtmospheres(@NotNull Location location) {
        return manager.getAffectingAtmospheres(location);
    }

    @Override
    public ActiveAtmosphere getAtmosphere(@NotNull UUID id) {
        return manager.getAtmosphere(id);
    }

    @Override
    public @NotNull Set<String> getTemplateIds() {
        return manager.getTemplateIds();
    }

    @Override
    public int getTemplateCount() {
        return manager.getTemplateCount();
    }

    @Override
    public int getActiveCount() {
        return manager.getActiveCount();
    }
}
