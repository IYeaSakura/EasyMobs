package net.sakurain.mc.easymobs.mob;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.util.MessageUtil;
import net.sakurain.mc.easymobs.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton tracker for custom mobs. Manages periodic particle, sound, bossbar and AI tasks.
 */
public final class MobTracker {

    private static MobTracker instance;

    private static final NamespacedKey MOB_ID_KEY = new NamespacedKey(EasyMobsPlugin.getInstance(), "ezmobs_mob_id");
    private static final long BOSSBAR_UPDATE_INTERVAL = 5L;
    private static final long AI_UPDATE_INTERVAL = 10L;

    private final EasyMobsPlugin plugin;
    private final Map<UUID, CustomMobTemplate> trackedMobs = new HashMap<>();
    private final Map<UUID, List<BukkitTask>> activeTasks = new HashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    private MobTracker() {
        this.plugin = EasyMobsPlugin.getInstance();
    }

    /**
     * Returns the singleton instance, creating it if necessary.
     *
     * @return the tracker instance
     */
    public static synchronized MobTracker getInstance() {
        if (instance == null) {
            instance = new MobTracker();
        }
        return instance;
    }

    /**
     * Tracks a spawned custom mob and starts its periodic tasks.
     *
     * @param entity   the living entity
     * @param template the mob template
     */
    public void track(LivingEntity entity, CustomMobTemplate template) {
        if (entity == null || template == null) {
            return;
        }
        UUID uuid = entity.getUniqueId();
        cancelTracking(uuid);
        trackedMobs.put(uuid, template);

        List<BukkitTask> tasks = new ArrayList<>();
        startParticleTasks(uuid, template, tasks);
        startSoundTask(uuid, template, tasks);
        startBossBarTask(uuid, template, tasks);
        startAITask(uuid, template, tasks);
        activeTasks.put(uuid, tasks);
    }

    private void startParticleTasks(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks) {
        for (CustomMobTemplate.ParticleConfig particle : template.getParticles()) {
            if (particle.interval() <= 0) {
                continue;
            }
            tasks.add(SchedulerUtil.runTimer(particle.interval(), particle.interval(), () -> {
                LivingEntity entity = getLivingEntity(uuid);
                if (entity == null) {
                    return;
                }
                spawnParticle(entity, particle);
            }));
        }
    }

    private void spawnParticle(LivingEntity entity, CustomMobTemplate.ParticleConfig config) {
        Location location = resolveParticleLocation(entity, config.location());
        Particle particle = config.type();
        if (particle == null || location.getWorld() == null) {
            return;
        }

        Object data = null;
        if (particle == Particle.DUST) {
            Color color = parseColor(config.color());
            if (color != null) {
                data = new Particle.DustOptions(color, (float) Math.max(0.1, config.size()));
            }
        }

        if (data != null) {
            location.getWorld().spawnParticle(particle, location, config.count(), config.offsetX(), config.offsetY(), config.offsetZ(), 0.0, data);
        } else {
            location.getWorld().spawnParticle(particle, location, config.count(), config.offsetX(), config.offsetY(), config.offsetZ(), 0.0);
        }
    }

    private static Location resolveParticleLocation(LivingEntity entity, CustomMobTemplate.ParticleLocation location) {
        return switch (location) {
            case FEET -> entity.getLocation();
            case HEAD -> entity.getEyeLocation();
            case CENTER -> entity.getLocation().add(0, entity.getHeight() / 2.0, 0);
        };
    }

    private static Color parseColor(String value) {
        if (value == null || value.isEmpty()) {
            return Color.RED;
        }
        if (value.contains(",")) {
            String[] parts = value.split(",");
            try {
                int r = Integer.parseInt(parts[0].trim());
                int g = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                int b = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
                return Color.fromRGB(clamp(r), clamp(g), clamp(b));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                return Color.RED;
            }
        }
        return switch (value.toLowerCase()) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> Color.YELLOW;
            case "aqua" -> Color.AQUA;
            case "black" -> Color.BLACK;
            case "fuchsia" -> Color.FUCHSIA;
            case "gray", "grey" -> Color.GRAY;
            case "lime" -> Color.LIME;
            case "maroon" -> Color.MAROON;
            case "navy" -> Color.NAVY;
            case "olive" -> Color.OLIVE;
            case "orange" -> Color.ORANGE;
            case "purple" -> Color.PURPLE;
            case "silver" -> Color.SILVER;
            case "teal" -> Color.TEAL;
            case "white" -> Color.WHITE;
            default -> Color.RED;
        };
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void startSoundTask(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks) {
        CustomMobTemplate.AmbientSoundConfig sound = template.getAmbientSound();
        if (sound.sound() == null || sound.interval() <= 0) {
            return;
        }
        tasks.add(SchedulerUtil.runTimer(sound.interval(), sound.interval(), () -> {
            LivingEntity entity = getLivingEntity(uuid);
            if (entity == null) {
                return;
            }
            entity.getWorld().playSound(entity.getLocation(), sound.sound(), sound.volume(), sound.pitch());
        }));
    }

    private void startBossBarTask(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks) {
        CustomMobTemplate.BossBarConfig config = template.getBossbar();
        if (!config.enabled()) {
            return;
        }
        BossBar bossBar = activeBossBars.get(uuid);
        if (bossBar == null) {
            return;
        }
        tasks.add(SchedulerUtil.runTimer(BOSSBAR_UPDATE_INTERVAL, BOSSBAR_UPDATE_INTERVAL, () -> {
            LivingEntity entity = getLivingEntity(uuid);
            if (entity == null) {
                return;
            }
            updateBossBar(bossBar, entity, config);
        }));
    }

    private void updateBossBar(BossBar bossBar, LivingEntity entity, CustomMobTemplate.BossBarConfig config) {
        double max = getMaxHealth(entity);
        double progress = max > 0 ? entity.getHealth() / max : 0.0;
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

        List<Player> currentViewers = new ArrayList<>(bossBar.getPlayers());
        if (config.showToAll()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(entity.getWorld())
                        && player.getLocation().distanceSquared(entity.getLocation()) <= config.range() * config.range()) {
                    if (!currentViewers.contains(player)) {
                        bossBar.addPlayer(player);
                    }
                    currentViewers.remove(player);
                }
            }
        } else {
            // Show to nearby players as default behavior when show_to_all is false.
            for (Player player : entity.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(entity.getLocation()) <= config.range() * config.range()) {
                    if (!currentViewers.contains(player)) {
                        bossBar.addPlayer(player);
                    }
                    currentViewers.remove(player);
                }
            }
        }
        for (Player player : currentViewers) {
            bossBar.removePlayer(player);
        }
    }

    private double getMaxHealth(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attribute.MAX_HEALTH);
        return instance != null ? instance.getBaseValue() : entity.getHealth();
    }

    private void startAITask(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks) {
        CustomMobTemplate.AIConfig ai = template.getAi();
        // The custom AI goal system handles targeting when useCustomAi is enabled.
        if (ai.useCustomAi() || !ai.alwaysAggressive()) {
            return;
        }
        tasks.add(SchedulerUtil.runTimer(AI_UPDATE_INTERVAL, AI_UPDATE_INTERVAL, () -> {
            LivingEntity entity = getLivingEntity(uuid);
            if (entity == null) {
                return;
            }
            updateAI(entity, ai);
        }));
    }

    private void updateAI(LivingEntity entity, CustomMobTemplate.AIConfig ai) {
        if (!ai.alwaysAggressive()) {
            return;
        }
        LivingEntity target = findNearestTarget(entity, ai.targetRange(), ai.targetingStrategy().preferPlayers());
        if (target != null && entity instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(target);
        }
    }

    private LivingEntity findNearestTarget(LivingEntity entity, double range, boolean preferPlayers) {
        double rangeSq = range * range;
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity nearby : entity.getNearbyEntities(range, range, range)) {
            if (!(nearby instanceof LivingEntity living) || nearby.equals(entity)) {
                continue;
            }
            if (living.isDead()) {
                continue;
            }
            double dist = entity.getLocation().distanceSquared(living.getLocation());
            if (dist > rangeSq) {
                continue;
            }
            if (preferPlayers && !(living instanceof Player)) {
                continue;
            }
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = living;
            }
        }
        return nearest;
    }

    private LivingEntity getLivingEntity(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (!(entity instanceof LivingEntity living) || living.isDead()) {
            cancelTracking(uuid);
            return null;
        }
        return living;
    }

    /**
     * Registers a bossbar for the given mob UUID. The tracker will manage viewers and updates.
     *
     * @param uuid    the mob uuid
     * @param bossBar the bossbar
     */
    public void registerBossBar(UUID uuid, BossBar bossBar) {
        if (uuid == null || bossBar == null) {
            return;
        }
        BossBar previous = activeBossBars.put(uuid, bossBar);
        if (previous != null) {
            previous.removeAll();
        }
    }

    /**
     * Stops tracking the given mob and cancels all of its tasks.
     *
     * @param uuid the mob uuid
     */
    public void cancelTracking(UUID uuid) {
        trackedMobs.remove(uuid);
        List<BukkitTask> tasks = activeTasks.remove(uuid);
        if (tasks != null) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
        BossBar bossBar = activeBossBars.remove(uuid);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    /**
     * Stops tracking all mobs and cancels every active task.
     */
    public void cancelAll() {
        for (UUID uuid : new ArrayList<>(trackedMobs.keySet())) {
            cancelTracking(uuid);
        }
        trackedMobs.clear();
        activeTasks.clear();
        for (BossBar bossBar : activeBossBars.values()) {
            bossBar.removeAll();
        }
        activeBossBars.clear();
    }

    /**
     * Returns true if the entity is a tracked custom mob.
     *
     * @param entity the entity
     * @return true if custom mob
     */
    public boolean isCustomMob(Entity entity) {
        if (entity == null) {
            return false;
        }
        return getMobTemplateId(entity) != null;
    }

    /**
     * Returns the stored mob template id, or null.
     *
     * @param entity the entity
     * @return template id or null
     */
    public String getMobTemplateId(Entity entity) {
        if (entity == null) {
            return null;
        }
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.get(MOB_ID_KEY, PersistentDataType.STRING);
    }

    /**
     * Returns the loaded template for the entity, or null.
     *
     * @param entity the entity
     * @return template or null
     */
    public CustomMobTemplate getTemplate(Entity entity) {
        String id = getMobTemplateId(entity);
        if (id == null) {
            return null;
        }
        return plugin.getMobManager().getTemplate(id);
    }

    /**
     * Returns the PDC key used to identify custom mobs.
     *
     * @return the mob id key
     */
    public static NamespacedKey getMobIdKey() {
        return MOB_ID_KEY;
    }

    /**
     * Returns all currently tracked custom mobs that are still alive and loaded.
     *
     * @return list of tracked living entities
     */
    public List<LivingEntity> getTrackedMobs() {
        List<LivingEntity> result = new ArrayList<>();
        for (UUID uuid : new ArrayList<>(trackedMobs.keySet())) {
            org.bukkit.entity.Entity entity = Bukkit.getEntity(uuid);
            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }
            result.add(living);
        }
        return result;
    }

    /**
     * Counts the number of tracked custom mobs inside the given chunk.
     *
     * @param chunk the chunk
     * @return number of custom mobs in the chunk
     */
    public int countMobsInChunk(org.bukkit.Chunk chunk) {
        if (chunk == null) {
            return 0;
        }
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        org.bukkit.World world = chunk.getWorld();
        int count = 0;
        for (java.util.Map.Entry<UUID, CustomMobTemplate> entry : trackedMobs.entrySet()) {
            org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }
            if (!world.equals(entity.getWorld())) {
                continue;
            }
            Location loc = entity.getLocation();
            if ((loc.getBlockX() >> 4) == chunkX && (loc.getBlockZ() >> 4) == chunkZ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the number of tracked custom mobs of the given template within a radius.
     *
     * @param center     center location
     * @param radius     search radius in blocks
     * @param templateId template id to match
     * @return number of matching custom mobs nearby
     */
    public int countNearbyMobs(Location center, double radius, String templateId) {
        if (center == null || center.getWorld() == null || radius <= 0 || templateId == null) {
            return 0;
        }
        double radiusSq = radius * radius;
        String targetId = templateId.toLowerCase();
        int count = 0;
        for (java.util.Map.Entry<UUID, CustomMobTemplate> entry : trackedMobs.entrySet()) {
            if (!entry.getValue().getId().equals(targetId)) {
                continue;
            }
            org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }
            if (!center.getWorld().equals(entity.getWorld())) {
                continue;
            }
            if (center.distanceSquared(entity.getLocation()) <= radiusSq) {
                count++;
            }
        }
        return count;
    }
}
