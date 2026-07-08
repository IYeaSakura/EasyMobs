package net.sakurain.mc.aeternumgenesis.api.event;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomMobSpawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String templateId;
    private final LivingEntity entity;
    private final Location spawnLocation;
    private final int level;

    public CustomMobSpawnEvent(@NotNull String templateId, @NotNull LivingEntity entity,
                               @NotNull Location spawnLocation, int level) {
        this.templateId = templateId;
        this.entity = entity;
        this.spawnLocation = spawnLocation;
        this.level = level;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }

    @NotNull
    public LivingEntity getEntity() {
        return entity;
    }

    @NotNull
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public int getLevel() {
        return level;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
