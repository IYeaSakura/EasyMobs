package net.sakurain.mc.aeternumgenesis.api;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SpawnCondition {

    @NotNull
    String getType();

    void parse(@NotNull String arguments);

    boolean test(@NotNull Location location, @Nullable EntityType originalType);
}
