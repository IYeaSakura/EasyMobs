package net.sakurain.mc.aeternumgenesis.mob;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import net.sakurain.mc.aeternumgenesis.util.SchedulerUtil;
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
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Singleton tracker for custom mobs. Manages periodic particle, sound, bossbar and AI tasks.
 */
public final class MobTracker {

    private static MobTracker instance;

    private static final NamespacedKey MOB_ID_KEY = new NamespacedKey("genesis", "genesis_mob_id");
    private static final long BOSSBAR_UPDATE_INTERVAL = 5L;
    private static final long AI_UPDATE_INTERVAL = 10L;
    private static final long TIMER_SKILL_INTERVAL = 20L;

    private final AeternumGenesisPlugin plugin;
    private final Map<UUID, CustomMobTemplate> trackedMobs = new HashMap<>();
    private final Map<UUID, List<BukkitTask>> activeTasks = new HashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();
    private final Map<UUID, Map<String, Long>> lastTimerTicks = new HashMap<>();

    private MobTracker() {
        this.plugin = AeternumGenesisPlugin.getInstance();
        startGlobalTimerTask();
    }

    private void startGlobalTimerTask() {
        SchedulerUtil.runTimer(TIMER_SKILL_INTERVAL, TIMER_SKILL_INTERVAL, () -> {
            long currentTick = Bukkit.getCurrentTick();
            for (UUID uuid : new ArrayList<>(trackedMobs.keySet())) {
                LivingEntity entity = getLivingEntity(uuid);
                if (entity == null || entity.isDead()) {
                    continue;
                }
                CustomMobTemplate template = trackedMobs.get(uuid);
                if (template == null) {
                    continue;
                }
                checkTimerSkills(entity, template, currentTick);
            }
        });
    }

    private void checkTimerSkills(LivingEntity entity, CustomMobTemplate template, long currentTick) {
        boolean hasTimer = false;
        for (net.sakurain.mc.aeternumgenesis.skill.SkillBinding binding : template.getSkills()) {
            if (!"on_timer".equalsIgnoreCase(binding.trigger())) {
                continue;
            }
            hasTimer = true;
            int interval = binding.interval();
            if (interval <= 0) {
                interval = 100;
            }
            Map<String, Long> map = lastTimerTicks.computeIfAbsent(entity.getUniqueId(), k -> new HashMap<>());
            Long last = map.get(binding.skillId());
            if (last != null && (currentTick - last) < interval) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() * 100.0 >= binding.chance()) {
                map.put(binding.skillId(), currentTick);
                continue;
            }
            map.put(binding.skillId(), currentTick);
            plugin.getSkillManager().triggerBinding(entity, entity, 0.0, binding);
        }
        if (!hasTimer) {
            lastTimerTicks.remove(entity.getUniqueId());
        }
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
        startBossBarTask(uuid, template, tasks, true);
        startWaterBehaviorTask(uuid, template, tasks);
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
        startBossBarTask(uuid, template, tasks, false);
    }

    private void startBossBarTask(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks, boolean allowLateRegistration) {
        CustomMobTemplate.BossBarConfig config = template.getBossbar();
        if (!config.enabled()) {
            return;
        }
        BossBar bossBar = activeBossBars.get(uuid);
        if (bossBar == null) {
            if (allowLateRegistration) {
                return;
            }
            throw new IllegalStateException("BossBar was not registered before track started for " + uuid);
        }
        tasks.add(SchedulerUtil.runTimer(BOSSBAR_UPDATE_INTERVAL, BOSSBAR_UPDATE_INTERVAL, () -> {
            LivingEntity entity = getLivingEntity(uuid);
            if (entity == null) {
                return;
            }
            updateBossBar(bossBar, entity, config);
        }));
    }

    /**
     * Starts the bossbar update task for a mob whose bossbar was registered after {@link #track(LivingEntity, CustomMobTemplate)}.
     *
     * @param uuid the mob uuid
     */
    public void startBossBarTask(UUID uuid) {
        CustomMobTemplate template = trackedMobs.get(uuid);
        if (template == null) {
            return;
        }
        List<BukkitTask> tasks = activeTasks.computeIfAbsent(uuid, k -> new ArrayList<>());
        startBossBarTask(uuid, template, tasks, true);
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

    private void startWaterBehaviorTask(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks) {
        CustomMobTemplate.WaterBehaviorConfig water = template.getWaterBehavior();
        if (water == null) {
            return;
        }
        tasks.add(SchedulerUtil.runTimer(5L, 5L, () -> {
            LivingEntity entity = getLivingEntity(uuid);
            if (entity == null) {
                return;
            }
            handleWaterBehavior(entity, water);
        }));
    }

    private void handleWaterBehavior(LivingEntity entity, CustomMobTemplate.WaterBehaviorConfig water) {
        if (!entity.isInWater()) {
            return;
        }

        // Breathing / drowning
        if (water.canBreatheUnderwater()) {
            PotionEffectType waterBreathing = PotionEffectType.WATER_BREATHING;
            if (waterBreathing != null && !entity.hasPotionEffect(waterBreathing)) {
                entity.addPotionEffect(new PotionEffect(waterBreathing, Integer.MAX_VALUE, 0, true, false));
            }
        } else if (entity instanceof org.bukkit.entity.Zombie && !water.convertToDrowned()) {
            int air = entity.getRemainingAir();
            if (air > 0) {
                entity.setRemainingAir(Math.max(0, air - 5));
            } else {
                entity.damage(water.drownDamage(), org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.DROWN).build());
            }
        }

        // Swimming behavior: try to chase target in 3D like Drowned
        if (entity instanceof Mob mob && (water.surfaceSeeking() || water.floatOnWater())) {
            handleSwimming(mob, water);
            return;
        }

        // Legacy gentle buoyancy (only if not using swim logic)
        if (water.floatOnWater()) {
            applyBuoyancy(entity, 0.08);
        }
    }

    private void handleSwimming(Mob mob, CustomMobTemplate.WaterBehaviorConfig water) {
        LivingEntity target = mob.getTarget();
        Location loc = mob.getLocation();
        org.bukkit.block.Block feetBlock = loc.getBlock();
        boolean feetInWater = feetBlock.getType().name().contains("WATER");

        if (target != null && target.isValid()) {
            Location targetLoc = target.getLocation();

            // Direct velocity-based swimming: avoids vanilla pathfinder spinning in water.
            org.bukkit.util.Vector direction = targetLoc.toVector().subtract(loc.toVector());
            double distance = direction.length();
            if (distance < 0.1) {
                return;
            }
            direction.normalize();

            // Rotate body to face the movement direction (lookAt only rotates the head).
            Location look = loc.clone().setDirection(direction);
            mob.setRotation(look.getYaw(), look.getPitch());

            double baseSpeed = Math.max(0.1, water.waterMovementSpeed());
            // Slow down when very close to avoid overshooting/oscillation.
            double speed = distance < 2.0 ? baseSpeed * (distance / 2.0) : baseSpeed;

            org.bukkit.util.Vector velocity = direction.multiply(speed);
            mob.setVelocity(velocity);

            // Stop any pending path so vanilla pathfinder does not fight the velocity.
            mob.getPathfinder().stopPathfinding();
            return;
        }

        // No target: surface seeking / idle floating
        if (water.surfaceSeeking()) {
            seekSurface(mob, loc, feetInWater, water);
        } else if (water.floatOnWater()) {
            applyBuoyancy(mob, 0.08);
        }
    }

    private void seekSurface(Mob mob, Location loc, boolean feetInWater, CustomMobTemplate.WaterBehaviorConfig water) {
        if (loc.getWorld() == null) {
            return;
        }
        double headY = loc.getY() + mob.getHeight() * 0.85;
        org.bukkit.block.Block headBlock = loc.getWorld().getBlockAt((int) Math.floor(loc.getX()), (int) Math.floor(headY), (int) Math.floor(loc.getZ()));
        org.bukkit.block.Block blockAbove = headBlock.getRelative(org.bukkit.block.BlockFace.UP);
        org.bukkit.util.Vector velocity = mob.getVelocity();

        if (!headBlock.getType().name().contains("WATER") || blockAbove.getType().isAir()) {
            // Head is already out of water or just below air: maintain gentle buoyancy to stay afloat
            if (feetInWater && velocity.getY() < 0.05) {
                mob.setVelocity(velocity.setY(0.05));
            }
            return;
        }

        // Fully submerged: push up more strongly
        double speed = Math.max(0.1, water.surfaceMovementSpeed() * 0.2);
        if (velocity.getY() < speed) {
            mob.setVelocity(velocity.setY(speed));
        }
    }

    private void applyBuoyancy(LivingEntity entity, double minY) {
        Location eyeLoc = entity.getEyeLocation();
        if (eyeLoc.getBlock().getType().isAir()) {
            return;
        }
        org.bukkit.util.Vector velocity = entity.getVelocity();
        if (velocity.getY() < minY) {
            entity.setVelocity(velocity.setY(minY));
        }
    }

    private void startAITask(UUID uuid, CustomMobTemplate template, List<BukkitTask> tasks) {
        CustomMobTemplate.AIConfig ai = template.getAi();
        // The custom AI goal system handles targeting when useCustomAi is enabled.
        boolean hasCustomTargets = ai.targets() != null && !ai.targets().isEmpty();
        if (ai.useCustomAi() || (!ai.alwaysAggressive() && !hasCustomTargets)) {
            return;
        }
        tasks.add(SchedulerUtil.runTimer(AI_UPDATE_INTERVAL, AI_UPDATE_INTERVAL, () -> {
            LivingEntity entity = getLivingEntity(uuid);
            if (entity == null) {
                return;
            }
            updateAI(entity, template);
        }));
    }

    private void updateAI(LivingEntity entity, CustomMobTemplate template) {
        CustomMobTemplate.AIConfig ai = template.getAi();
        if (ai == null) {
            return;
        }

        // Custom target list takes precedence over the simple always-aggressive logic.
        if (ai.targets() != null && !ai.targets().isEmpty()) {
            LivingEntity target = findNearestMatchingTarget(entity, ai.targetRange(), ai.targets());
            if (target != null && entity instanceof org.bukkit.entity.Mob mob) {
                mob.setTarget(target);
            }
            return;
        }

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
            if (living.isDead() || isSameFaction(entity, living)) {
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

    private LivingEntity findNearestMatchingTarget(LivingEntity entity, double range, List<String> targets) {
        double rangeSq = range * range;
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity nearby : entity.getNearbyEntities(range, range, range)) {
            if (!(nearby instanceof LivingEntity living) || nearby.equals(entity)) {
                continue;
            }
            if (living.isDead() || isSameFaction(entity, living)) {
                continue;
            }
            if (!matchesTarget(living, targets)) {
                continue;
            }
            double dist = entity.getLocation().distanceSquared(living.getLocation());
            if (dist <= rangeSq && dist < nearestDist) {
                nearestDist = dist;
                nearest = living;
            }
        }
        return nearest;
    }

    private boolean matchesTarget(LivingEntity candidate, List<String> targets) {
        for (String target : targets) {
            String lower = target.toLowerCase();
            switch (lower) {
                case "players" -> {
                    if (candidate instanceof Player) return true;
                }
                case "mobs" -> {
                    if (candidate instanceof Mob) return true;
                }
                default -> {
                    if (lower.startsWith("faction:")) {
                        String faction = lower.substring(8);
                        CustomMobTemplate t = getTemplate(candidate);
                        if (t != null && faction.equalsIgnoreCase(t.getFaction())) {
                            return true;
                        }
                    } else {
                        try {
                            org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf(target.toUpperCase());
                            if (candidate.getType() == type) return true;
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSameFaction(LivingEntity a, LivingEntity b) {
        CustomMobTemplate ta = getTemplate(a);
        CustomMobTemplate tb = getTemplate(b);
        if (ta == null || tb == null) return false;
        String fa = ta.getFaction();
        String fb = tb.getFaction();
        return fa != null && !fa.isEmpty() && fa.equalsIgnoreCase(fb);
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
        lastTimerTicks.remove(uuid);
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
        lastTimerTicks.clear();
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

    /**
     * Counts the number of tracked custom mobs of the given template across all loaded worlds.
     *
     * @param templateId template id to match
     * @return number of matching custom mobs
     */
    public int countGlobalMobs(String templateId) {
        if (templateId == null) {
            return 0;
        }
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
            count++;
        }
        return count;
    }
}
