package net.sakurain.mc.aeternumgenesis.eventchain;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Loads event chain templates, manages active instances, schedules stages,
 * and listens for natural trigger conditions.
 */
public final class EventChainManager implements Listener {

    private final AeternumGenesisPlugin plugin;
    private final Map<String, EventChainTemplate> templates = new ConcurrentHashMap<>();
    private final Map<UUID, EventChainInstance> activeInstances = new ConcurrentHashMap<>();
    private final Map<String, Long> lastTriggerTicks = new ConcurrentHashMap<>();
    private final EventActionExecutor actionExecutor;
    private final EventConditionEvaluator conditionEvaluator;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private BukkitTask triggerTask;
    private BukkitTask endCheckTask;

    public EventChainManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
        this.actionExecutor = new EventActionExecutor(plugin);
        this.conditionEvaluator = new EventConditionEvaluator();
        load(configs);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startTriggerTask();
        startEndCheckTask();
    }

    public void load(Map<String, YamlConfiguration> configs) {
        templates.clear();
        if (configs == null) {
            return;
        }
        for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
            loadConfig(entry.getKey(), entry.getValue());
        }
    }

    private void loadConfig(String fileName, YamlConfiguration config) {
        if (config == null) {
            return;
        }
        ConfigurationSection root = config.getRoot();
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                EventChainTemplate template = EventChainTemplate.fromConfig(key, section);
                templates.put(template.id(), template);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to parse event chain '" + key + "' in " + fileName, e);
            }
        }
    }

    public void reload(Map<String, YamlConfiguration> configs) {
        stopAllEvents();
        load(configs);
    }

    public void shutdown() {
        stopAllEvents();
        if (triggerTask != null) {
            triggerTask.cancel();
        }
        if (endCheckTask != null) {
            endCheckTask.cancel();
        }
    }

    public Collection<EventChainTemplate> getTemplates() {
        return Collections.unmodifiableCollection(templates.values());
    }

    public int getTemplateCount() {
        return templates.size();
    }

    public Collection<EventChainInstance> getActiveInstances() {
        return Collections.unmodifiableCollection(activeInstances.values());
    }

    public int getActiveCount() {
        return activeInstances.size();
    }

    public EventChainTemplate getTemplate(String id) {
        return templates.get(id);
    }

    public boolean hasTemplate(String id) {
        return templates.containsKey(id);
    }

    /**
     * Starts an event chain manually.
     *
     * @param id        the template id
     * @param initiator the player who initiated the event, may be null
     * @return the new instance id, or null if the template was not found
     */
    @Nullable
    public UUID startEvent(String id, @Nullable Player initiator) {
        EventChainTemplate template = templates.get(id);
        if (template == null) {
            return null;
        }
        UUID instanceId = UUID.randomUUID();
        EventChainInstance instance = new EventChainInstance(instanceId, template, initiator,
                Bukkit.getCurrentTick());
        activeInstances.put(instanceId, instance);
        advanceStage(instance);
        broadcastStart(instance);
        return instanceId;
    }

    public boolean stopEvent(UUID instanceId) {
        EventChainInstance instance = activeInstances.remove(instanceId);
        if (instance == null) {
            return false;
        }
        instance.setEnded(true);
        cleanup(instance);
        return true;
    }

    public void stopAllEvents() {
        List<UUID> ids = new ArrayList<>(activeInstances.keySet());
        for (UUID id : ids) {
            stopEvent(id);
        }
    }

    private void startTriggerTask() {
        triggerTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkTriggers();
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }

    private void startEndCheckTask() {
        endCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkEndConditions();
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private void checkTriggers() {
        long currentTick = Bukkit.getCurrentTick();
        for (EventChainTemplate template : templates.values()) {
            EventChainTemplate.Trigger trigger = template.trigger();
            if (trigger.type() == EventChainTemplate.Trigger.Type.MANUAL) {
                continue;
            }
            if (!passesCooldown(template.id(), currentTick, trigger.cooldownTicks())) {
                continue;
            }
            if (Bukkit.getOnlinePlayers().size() < trigger.minPlayers()) {
                continue;
            }
            if (trigger.timeStart() >= 0 && trigger.timeEnd() >= 0) {
                if (!isAnyWorldInTimeRange(trigger.timeStart(), trigger.timeEnd())) {
                    continue;
                }
            }
            if (trigger.type() == EventChainTemplate.Trigger.Type.RANDOM_NIGHT
                    && !isAnyWorldNight()) {
                continue;
            }
            if (trigger.type() == EventChainTemplate.Trigger.Type.RANDOM_DAY
                    && !isAnyWorldDay()) {
                continue;
            }
            if (random.nextDouble() > trigger.chance()) {
                continue;
            }
            lastTriggerTicks.put(template.id(), currentTick);
            startEvent(template.id(), null);
        }
    }

    private boolean passesCooldown(String id, long currentTick, long cooldownTicks) {
        Long last = lastTriggerTicks.get(id);
        return last == null || (currentTick - last) >= cooldownTicks;
    }

    private boolean isAnyWorldNight() {
        for (World world : Bukkit.getWorlds()) {
            long time = world.getTime();
            if (time >= 13000 && time < 23000) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnyWorldDay() {
        for (World world : Bukkit.getWorlds()) {
            long time = world.getTime();
            if (time < 13000 || time >= 23000) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnyWorldInTimeRange(int start, int end) {
        for (World world : Bukkit.getWorlds()) {
            long time = world.getTime();
            if (start <= end) {
                if (time >= start && time <= end) {
                    return true;
                }
            } else {
                if (time >= start || time <= end) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkEndConditions() {
        long currentTick = Bukkit.getCurrentTick();
        List<EventChainInstance> toFinish = new ArrayList<>();
        for (EventChainInstance instance : activeInstances.values()) {
            EventChainTemplate.EndConfig endConfig = instance.getTemplate().onEnd();
            if (endConfig == null) {
                continue;
            }
            boolean timeout = endConfig.timeoutTicks() > 0
                    && instance.getElapsedTicks(currentTick) >= endConfig.timeoutTicks();
            boolean conditionMet = !endConfig.condition().isBlank()
                    && conditionEvaluator.evaluate(endConfig.condition(), instance);
            if (conditionMet || (timeout && !instance.isAnyBossAlive())) {
                toFinish.add(instance);
                instance.setSuccess(conditionMet);
            }
        }
        for (EventChainInstance instance : toFinish) {
            finishEvent(instance);
        }
    }

    private void advanceStage(EventChainInstance instance) {
        if (instance.isEnded()) {
            return;
        }
        List<EventChainTemplate.Stage> stages = instance.getTemplate().stages();
        int nextIndex = instance.getCurrentStageIndex() + 1;
        if (nextIndex >= stages.size()) {
            // No more stages; wait for end condition or timeout.
            return;
        }
        EventChainTemplate.Stage stage = stages.get(nextIndex);
        instance.setCurrentStageIndex(nextIndex);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (instance.isEnded()) {
                    return;
                }
                if (!conditionEvaluator.evaluate(stage.condition(), instance)) {
                    advanceStage(instance);
                    return;
                }
                actionExecutor.executeAll(stage.actions(), instance);
                advanceStage(instance);
            }
        }.runTaskLater(plugin, stage.delayTicks());
    }

    private void finishEvent(EventChainInstance instance) {
        if (instance.isEnded()) {
            return;
        }
        instance.setEnded(true);
        activeInstances.remove(instance.getId());
        EventChainTemplate.EndConfig endConfig = instance.getTemplate().onEnd();
        if (endConfig == null) {
            cleanup(instance);
            return;
        }
        if (instance.isSuccess()) {
            actionExecutor.executeAll(endConfig.successActions(), instance);
        } else {
            actionExecutor.executeAll(endConfig.failActions(), instance);
        }
        cleanup(instance);
    }

    private void cleanup(EventChainInstance instance) {
        // Remove any atmospheres created by this event chain.
        for (Map.Entry<String, Object> entry : instance.getContext().entrySet()) {
            if (entry.getKey().startsWith("atmosphere_") && entry.getValue() instanceof String uuidString) {
                try {
                    plugin.getAtmosphereManager().removeAtmosphere(UUID.fromString(uuidString));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void broadcastStart(EventChainInstance instance) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(MessageUtil.success("Event started: &e" + instance.getTemplate().id()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        // Boss death may trigger end condition check on next tick.
    }
}
