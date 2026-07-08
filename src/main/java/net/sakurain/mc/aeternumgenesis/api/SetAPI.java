package net.sakurain.mc.aeternumgenesis.api;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SetAPI {

    @NotNull
    Collection<String> getAllSetIds();

    boolean hasSet(@NotNull String setId);

    boolean registerSet(@NotNull String setId, @NotNull ConfigurationSection config);

    boolean unregisterSet(@NotNull String setId);
}
