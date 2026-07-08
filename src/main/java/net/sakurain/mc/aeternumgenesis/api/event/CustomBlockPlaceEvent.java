package net.sakurain.mc.aeternumgenesis.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Location location;
    private final String templateId;
    private final Player player;
    private boolean cancelled = false;

    public CustomBlockPlaceEvent(@NotNull Location location, @NotNull String templateId, @Nullable Player player) {
        this.location = location;
        this.templateId = templateId;
        this.player = player;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
