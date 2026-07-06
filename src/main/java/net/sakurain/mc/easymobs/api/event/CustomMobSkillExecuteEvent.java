package net.sakurain.mc.easymobs.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomMobSkillExecuteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String skillId;
    private final String effectType;
    private final LivingEntity caster;
    private final LivingEntity target;
    private boolean cancelled;

    public CustomMobSkillExecuteEvent(@NotNull String skillId, @NotNull String effectType,
                                      @NotNull LivingEntity caster, @Nullable LivingEntity target) {
        this.skillId = skillId;
        this.effectType = effectType;
        this.caster = caster;
        this.target = target;
    }

    @NotNull
    public String getSkillId() {
        return skillId;
    }

    @NotNull
    public String getEffectType() {
        return effectType;
    }

    @NotNull
    public LivingEntity getCaster() {
        return caster;
    }

    @Nullable
    public LivingEntity getTarget() {
        return target;
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
