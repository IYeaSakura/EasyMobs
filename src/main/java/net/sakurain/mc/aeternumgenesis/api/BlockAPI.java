package net.sakurain.mc.aeternumgenesis.api;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface BlockAPI {

    boolean isCustomBlock(@NotNull Location location);

    @Nullable
    String getBlockTemplateId(@NotNull Location location);

    boolean hasTemplate(@NotNull String templateId);

    @NotNull
    Collection<String> getAllTemplateIds();

    boolean placeCustomBlock(@NotNull Location location, @NotNull String templateId);

    boolean removeCustomBlock(@NotNull Location location);

    boolean registerTemplate(@NotNull String templateId, @NotNull ConfigurationSection config);

    boolean unregisterTemplate(@NotNull String templateId);
}
