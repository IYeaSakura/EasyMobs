package net.sakurain.mc.aeternumgenesis.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomItemBuildEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String templateId;
    private ItemStack item;
    private boolean cancelled;

    public CustomItemBuildEvent(@NotNull String templateId, @NotNull ItemStack item) {
        this.templateId = templateId;
        this.item = item;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
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
