package net.sakurain.mc.aeternumgenesis.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomItemSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String templateId;
    private final Player player;
    private ItemStack item;
    private boolean cancelled;

    public CustomItemSpawnEvent(@NotNull String templateId, @Nullable Player player, @NotNull ItemStack item) {
        this.templateId = templateId;
        this.player = player;
        this.item = item;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = item;
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
