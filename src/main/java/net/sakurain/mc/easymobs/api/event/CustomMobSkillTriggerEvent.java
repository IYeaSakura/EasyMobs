package net.sakurain.mc.easymobs.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomMobSkillTriggerEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String skillId;
    private final LivingEntity caster;
    private final LivingEntity target;
    private final String triggerType;
    private boolean cancelled;

    public CustomMobSkillTriggerEvent(@NotNull String skillId, @NotNull LivingEntity caster,
                                      @Nullable LivingEntity target, @NotNull String triggerType) {
        this.skillId = skillId;
        this.caster = caster;
        this.target = target;
        this.triggerType = triggerType;
    }

    @NotNull
    public String getSkillId() {
        return skillId;
    }

    @NotNull
    public LivingEntity getCaster() {
        return caster;
    }

    @Nullable
    public LivingEntity getTarget() {
        return target;
    }

    @NotNull
    public String getTriggerType() {
        return triggerType;
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
