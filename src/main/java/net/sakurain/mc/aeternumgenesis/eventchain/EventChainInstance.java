package net.sakurain.mc.aeternumgenesis.eventchain;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Runtime state of an active event chain.
 */
public final class EventChainInstance {

    private final UUID id;
    private final EventChainTemplate template;
    private final Player initiator;
    private final long startTick;
    private int currentStageIndex;
    private boolean ended;
    private boolean success;
    private final Map<String, Object> context = new HashMap<>();
    private final Map<String, UUID> spawnedBosses = new HashMap<>();

    public EventChainInstance(UUID id, EventChainTemplate template, @Nullable Player initiator, long startTick) {
        this.id = id;
        this.template = template;
        this.initiator = initiator;
        this.startTick = startTick;
        this.currentStageIndex = -1;
    }

    public UUID getId() {
        return id;
    }

    public EventChainTemplate getTemplate() {
        return template;
    }

    @Nullable
    public Player getInitiator() {
        return initiator;
    }

    public long getStartTick() {
        return startTick;
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    public void setCurrentStageIndex(int index) {
        this.currentStageIndex = index;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(String key, Object value) {
        context.put(key, value);
    }

    @Nullable
    public Object getContext(String key) {
        return context.get(key);
    }

    public void registerBoss(String key, LivingEntity entity) {
        spawnedBosses.put(key, entity.getUniqueId());
    }

    public Map<String, UUID> getSpawnedBosses() {
        return Map.copyOf(spawnedBosses);
    }

    public boolean isBossAlive(String key) {
        UUID uuid = spawnedBosses.get(key);
        if (uuid == null) {
            return false;
        }
        org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(uuid);
        return entity instanceof LivingEntity living && !living.isDead();
    }

    public boolean isAnyBossAlive() {
        for (String key : spawnedBosses.keySet()) {
            if (isBossAlive(key)) {
                return true;
            }
        }
        return false;
    }

    public long getElapsedTicks(long currentTick) {
        return currentTick - startTick;
    }
}
