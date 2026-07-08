package net.sakurain.mc.aeternumgenesis.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomMobDropEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String templateId;
    private final LivingEntity entity;
    private final List<ItemStack> drops;
    private int experience;
    private boolean cancelled;

    public CustomMobDropEvent(@NotNull String templateId, @NotNull LivingEntity entity,
                              @NotNull List<ItemStack> drops, int experience) {
        this.templateId = templateId;
        this.entity = entity;
        this.drops = new ArrayList<>(drops);
        this.experience = experience;
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
    public List<ItemStack> getDrops() {
        return drops;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
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
