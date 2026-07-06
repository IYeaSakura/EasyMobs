package net.sakurain.mc.easymobs.api.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomMobPreSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String templateId;
    private final Location location;
    private int level;
    private boolean cancelled;

    public CustomMobPreSpawnEvent(@NotNull String templateId, @NotNull Location location, int level) {
        this.templateId = templateId;
        this.location = location;
        this.level = level;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
