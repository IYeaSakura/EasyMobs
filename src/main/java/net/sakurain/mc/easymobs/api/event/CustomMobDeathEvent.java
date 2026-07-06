package net.sakurain.mc.easymobs.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomMobDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String templateId;
    private final LivingEntity entity;
    private final Player killer;

    public CustomMobDeathEvent(@NotNull String templateId, @NotNull LivingEntity entity, @Nullable Player killer) {
        this.templateId = templateId;
        this.entity = entity;
        this.killer = killer;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }

    @NotNull
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    public Player getKiller() {
        return killer;
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
