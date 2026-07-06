package net.sakurain.mc.easymobs.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomMobDamageModifyEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final LivingEntity entity;
    private final LivingEntity damager;
    private double damage;
    private final boolean isDealing;
    private boolean cancelled;

    public CustomMobDamageModifyEvent(@Nullable LivingEntity entity, @Nullable LivingEntity damager,
                                      double damage, boolean isDealing) {
        this.entity = entity;
        this.damager = damager;
        this.damage = damage;
        this.isDealing = isDealing;
    }

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    public LivingEntity getDamager() {
        return damager;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public boolean isDealing() {
        return isDealing;
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
