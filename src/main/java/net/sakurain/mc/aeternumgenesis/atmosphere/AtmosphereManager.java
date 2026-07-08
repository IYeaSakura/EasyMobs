package net.sakurain.mc.aeternumgenesis.atmosphere;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.GameRuleUtil;
import net.sakurain.mc.aeternumgenesis.util.SchedulerUtil;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Manages active atmosphere instances and applies their layered effects to players and entities.
 */
public final class AtmosphereManager {

    private final AeternumGenesisPlugin plugin;
    private final Map<String, AtmosphereTemplate> templates = new ConcurrentHashMap<>();
    private final Map<UUID, ActiveAtmosphere> activeRegions = new ConcurrentHashMap<>();
    private final Map<UUID, AtmosphereState> playerStates = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> playerBossBars = new ConcurrentHashMap<>();
    private final Set<World> activeWorlds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<World, Object> originalGameRules = new ConcurrentHashMap<>();

    private BukkitTask tickTask;
    private BukkitTask modifierTask;
    private long tickCounter = 0;

    public AtmosphereManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
        load(configs);
        scheduleTasks();
    }

    /**
     * Loads atmosphere templates from YAML configs.
     */
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
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String id = TemplateIdUtil.normalize(key);
            if (!TemplateIdUtil.isValid(id)) {
                plugin.getLogger().warning("Invalid atmosphere id (must be lowercase [a-z0-9._-] and <= 64 chars): " + key);
                continue;
            }
            try {
                AtmosphereTemplate template = AtmosphereTemplate.fromConfig(id, section);
                templates.put(id, template);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse atmosphere '" + key + "' in " + fileName, e);
            }
        }
    }

    /**
     * Reloads templates and clears all active atmospheres.
     */
    public void reload(Map<String, YamlConfiguration> configs) {
        clearAll();
        load(configs);
    }

    private void scheduleTasks() {
        long interval = Math.max(1, plugin.getConfig().getLong("atmosphere.tick-interval", 5L));
        tickTask = SchedulerUtil.runTimer(interval, interval, this::tick);
        modifierTask = SchedulerUtil.runTimer(20, 20, this::applyEntityModifiers);
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        if (modifierTask != null) {
            modifierTask.cancel();
            modifierTask = null;
        }
        clearAll();
    }

    /**
     * Applies an atmosphere instance at the given location.
     *
     * @param center        the center location
     * @param radius        the affected radius
     * @param templateId    the atmosphere template id
     * @param durationTicks duration in ticks, or <= 0 for permanent
     * @return the instance UUID
     */
    public UUID applyAtmosphere(Location center, double radius, String templateId, long durationTicks) {
        AtmosphereTemplate template = templates.get(templateId);
        if (template == null) {
            plugin.getLogger().warning("Unknown atmosphere template: " + templateId);
            return null;
        }
        if (radius <= 0) {
            radius = 30.0;
        }
        ActiveAtmosphere atmosphere = new ActiveAtmosphere(templateId, template, center, radius, durationTicks);
        activeRegions.put(atmosphere.getInstanceId(), atmosphere);
        activeWorlds.add(center.getWorld());
        applyWorldGameRules(center.getWorld(), template);
        return atmosphere.getInstanceId();
    }

    /**
     * Removes a specific atmosphere instance.
     */
    public boolean removeAtmosphere(UUID instanceId) {
        ActiveAtmosphere removed = activeRegions.remove(instanceId);
        if (removed == null) {
            return false;
        }
        removeEntityModifiersForAtmosphere(removed);
        cleanupAfterRemove(removed);
        return true;
    }

    /**
     * Removes all active atmospheres.
     */
    public void removeAllAtmospheres() {
        List<UUID> ids = new ArrayList<>(activeRegions.keySet());
        for (UUID id : ids) {
            removeAtmosphere(id);
        }
    }

    private void cleanupAfterRemove(ActiveAtmosphere atmosphere) {
        World world = atmosphere.getCenter().getWorld();
        boolean stillActive = activeRegions.values().stream()
                .anyMatch(a -> a.getCenter().getWorld().equals(world));
        if (!stillActive) {
            activeWorlds.remove(world);
            restoreWorldGameRules(world);
        }
    }

    private void removeEntityModifiersForAtmosphere(ActiveAtmosphere atmosphere) {
        Location center = atmosphere.getCenter();
        World world = center.getWorld();
        double radius = atmosphere.getRadius();
        for (org.bukkit.entity.Entity entity : world.getNearbyEntities(center, radius, radius, radius,
                e -> e instanceof LivingEntity)) {
            cleanupEntityModifiers((LivingEntity) entity, atmosphere.getTemplate());
        }
    }

    private void clearAll() {
        removeAllAtmospheres();
        for (Player player : Bukkit.getOnlinePlayers()) {
            AtmosphereState state = playerStates.remove(player.getUniqueId());
            if (state != null) {
                clearAtmosphere(player, state);
            }
            BossBar bar = playerBossBars.remove(player.getUniqueId());
            if (bar != null) {
                bar.removeAll();
            }
        }
        playerStates.clear();
        playerBossBars.clear();
        activeWorlds.clear();
    }

    public List<ActiveAtmosphere> getAffectingAtmospheres(Location location) {
        if (location == null || location.getWorld() == null) {
            return List.of();
        }
        List<ActiveAtmosphere> result = new ArrayList<>();
        for (ActiveAtmosphere atmosphere : activeRegions.values()) {
            if (atmosphere.isExpired()) {
                continue;
            }
            if (atmosphere.isInside(location)) {
                result.add(atmosphere);
            }
        }
        result.sort(Comparator.comparingInt(a -> -a.getTemplate().getPriority()));
        return result;
    }

    public ActiveAtmosphere getAtmosphere(UUID instanceId) {
        return activeRegions.get(instanceId);
    }

    public Set<String> getTemplateIds() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    public int getTemplateCount() {
        return templates.size();
    }

    public int getActiveCount() {
        return activeRegions.size();
    }

    private void tick() {
        tickCounter++;
        removeExpiredAtmospheres();
        updatePlayers();
        spawnAmbientParticles();
        playAmbientSounds();
    }

    private void removeExpiredAtmospheres() {
        List<UUID> expired = new ArrayList<>();
        for (Map.Entry<UUID, ActiveAtmosphere> entry : activeRegions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expired.add(entry.getKey());
            }
        }
        for (UUID id : expired) {
            removeAtmosphere(id);
        }
    }

    private void updatePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<ActiveAtmosphere> atmospheres = getAffectingAtmospheres(player.getLocation());
            ActiveAtmosphere highest = atmospheres.isEmpty() ? null : atmospheres.get(0);
            AtmosphereState current = playerStates.get(player.getUniqueId());

            UUID currentId = current == null ? null : current.getAtmosphereInstanceId();
            UUID newId = highest == null ? null : highest.getInstanceId();

            if (!Objects.equals(currentId, newId)) {
                if (current != null) {
                    clearAtmosphere(player, current);
                }
                if (highest != null) {
                    applyAtmosphere(player, highest);
                }
            } else if (highest != null) {
                updateDynamicUi(player, highest);
            }
        }
    }

    private void applyAtmosphere(Player player, ActiveAtmosphere atmosphere) {
        AtmosphereTemplate template = atmosphere.getTemplate();

        List<PotionEffectType> appliedTypes = new ArrayList<>();
        if (!template.getPotionEffects().isEmpty()) {
            for (PotionEffect effect : template.getPotionEffects()) {
                player.addPotionEffect(effect);
                appliedTypes.add(effect.getType());
            }
        }

        if (template.getWeather().type() != null) {
            player.setPlayerWeather(template.getWeather().type());
        }

        AtmosphereTemplate.TitleConfig title = template.getUi().title();
        if (title != null && title.text() != null) {
            Title.Times times = Title.Times.times(
                    java.time.Duration.ofMillis(title.fadeIn() * 50L),
                    java.time.Duration.ofMillis(title.stay() * 50L),
                    java.time.Duration.ofMillis(title.fadeOut() * 50L));
            Title adventureTitle = Title.title(
                    colorize(title.text()),
                    colorize(title.subtitle() == null ? "" : title.subtitle()),
                    times);
            player.showTitle(adventureTitle);
        }

        AtmosphereTemplate.BossBarConfig bossBar = template.getUi().bossBar();
        if (bossBar != null && bossBar.text() != null) {
            String legacyTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .serialize(colorize(bossBar.text()));
            BossBar bar = Bukkit.createBossBar(
                    legacyTitle,
                    bossBar.color() == null ? BarColor.RED : bossBar.color(),
                    bossBar.style() == null ? BarStyle.SOLID : bossBar.style());
            bar.addPlayer(player);
            playerBossBars.put(player.getUniqueId(), bar);
        }

        AtmosphereTemplate.TransitionConfig transition = template.getTransition();
        AtmosphereState state = new AtmosphereState(atmosphere, appliedTypes,
                transition.exitPotionClear(), transition.exitWeatherRestore());
        playerStates.put(player.getUniqueId(), state);

        updateDynamicUi(player, atmosphere);
    }

    private void clearAtmosphere(Player player, AtmosphereState state) {
        if (state.isRestoreWeatherOnExit()) {
            player.resetPlayerWeather();
        }
        if (state.isClearPotionOnExit()) {
            for (PotionEffectType type : state.getPotionEffectTypes()) {
                player.removePotionEffect(type);
            }
        }
        BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void updateDynamicUi(Player player, ActiveAtmosphere atmosphere) {
        String actionBar = atmosphere.getTemplate().getUi().actionBar();
        if (actionBar != null) {
            String text = actionBar
                    .replace("{remaining}", String.valueOf(atmosphere.getRemainingTicks() / 20))
                    .replace("{progress}", String.valueOf((int) atmosphere.getProgressPercent()));
            player.sendActionBar(colorize(text));
        }

        BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar != null) {
            double progress = atmosphere.getProgressPercent() / 100.0;
            if (progress >= 0 && progress <= 1.0) {
                bar.setProgress(1.0 - progress);
            }
        }
    }

    private void spawnAmbientParticles() {
        for (ActiveAtmosphere atmosphere : activeRegions.values()) {
            if (atmosphere.isExpired()) {
                continue;
            }
            World world = atmosphere.getCenter().getWorld();
            for (AtmosphereTemplate.ParticleLayer layer : atmosphere.getTemplate().getParticles()) {
                if (layer.interval() <= 0 || tickCounter % layer.interval() != 0) {
                    continue;
                }
                Location center = atmosphere.getCenter().clone().add(layer.offsetX(), layer.offsetY(), layer.offsetZ());
                List<Player> nearby = getNearbyPlayers(center, Math.min(layer.radius(), 64.0));
                if (nearby.isEmpty()) {
                    continue;
                }
                spawnParticleForLayer(center, layer, nearby);
            }
        }
    }

    private void spawnParticleForLayer(Location center, AtmosphereTemplate.ParticleLayer layer, List<Player> nearby) {
        Particle particle = layer.type();
        int count = Math.max(1, layer.count());
        double spreadX = layer.radius() / 2.0;
        double spreadY = layer.radius() / 4.0;
        double spreadZ = layer.radius() / 2.0;

        if (particle == Particle.DUST && layer.color() != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(layer.color(), 1.0f);
            for (Player player : nearby) {
                player.spawnParticle(particle, center, count, spreadX, spreadY, spreadZ, 0, dustOptions);
            }
        } else {
            for (Player player : nearby) {
                player.spawnParticle(particle, center, count, spreadX, spreadY, spreadZ, 0);
            }
        }
    }

    private void playAmbientSounds() {
        for (ActiveAtmosphere atmosphere : activeRegions.values()) {
            if (atmosphere.isExpired()) {
                continue;
            }
            for (AtmosphereTemplate.SoundLayer layer : atmosphere.getTemplate().getSounds()) {
                if (!"ambient".equalsIgnoreCase(layer.type())) {
                    continue;
                }
                if (layer.interval() <= 0 || tickCounter % layer.interval() != 0) {
                    continue;
                }
                if (ThreadLocalRandom.current().nextDouble() > layer.chance()) {
                    continue;
                }
                Location center = atmosphere.getCenter();
                List<Player> nearby = getNearbyPlayers(center, Math.min(64.0, atmosphere.getRadius()));
                for (Player player : nearby) {
                    player.playSound(center, layer.sound(), layer.volume(), layer.pitch());
                }
            }
        }
    }

    private void applyEntityModifiers() {
        for (ActiveAtmosphere atmosphere : activeRegions.values()) {
            if (atmosphere.isExpired()) {
                continue;
            }
            if (atmosphere.getTemplate().getEntityModifiers().isEmpty()) {
                continue;
            }
            Location center = atmosphere.getCenter();
            World world = center.getWorld();
            double radius = atmosphere.getRadius();

            for (org.bukkit.entity.Entity entity : world.getNearbyEntities(center, radius, radius, radius,
                    e -> e instanceof LivingEntity)) {
                LivingEntity living = (LivingEntity) entity;
                for (AtmosphereTemplate.EntityModifier modifier : atmosphere.getTemplate().getEntityModifiers()) {
                    if (!matchesModifierTarget(living, modifier.target())) {
                        continue;
                    }
                    applyModifierToEntity(living, modifier);
                }
            }
        }
    }

    private boolean matchesModifierTarget(LivingEntity entity, String target) {
        if (target == null || "all".equalsIgnoreCase(target)) {
            return true;
        }
        String lower = target.toLowerCase();
        return switch (lower) {
            case "players" -> entity instanceof Player;
            case "monsters", "all_monsters" -> entity instanceof org.bukkit.entity.Monster;
            case "animals" -> entity instanceof org.bukkit.entity.Animals;
            case "enemy", "enemies" -> entity instanceof org.bukkit.entity.Monster || (entity instanceof Player);
            default -> true;
        };
    }

    private void applyModifierToEntity(LivingEntity entity, AtmosphereTemplate.EntityModifier modifier) {
        for (Map.Entry<Attribute, AttributeModifier> entry : modifier.attributes().entrySet()) {
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance == null) {
                continue;
            }
            boolean has = false;
            for (AttributeModifier existing : instance.getModifiers()) {
                if (existing.getKey().equals(entry.getValue().getKey())) {
                    has = true;
                    break;
                }
            }
            if (!has) {
                instance.addModifier(entry.getValue());
            }
        }
        if (modifier.glow()) {
            entity.setGlowing(true);
        }
    }

    private void cleanupEntityModifiers(LivingEntity entity, AtmosphereTemplate template) {
        for (AtmosphereTemplate.EntityModifier modifier : template.getEntityModifiers()) {
            if (!matchesModifierTarget(entity, modifier.target())) {
                continue;
            }
            for (Map.Entry<Attribute, AttributeModifier> entry : modifier.attributes().entrySet()) {
                AttributeInstance instance = entity.getAttribute(entry.getKey());
                if (instance == null) {
                    continue;
                }
                instance.removeModifier(entry.getValue());
            }
        }
    }

    private void applyWorldGameRules(World world, AtmosphereTemplate template) {
        if (world == null) {
            return;
        }
        AtmosphereTemplate.EnvironmentLayer env = template.getEnvironment();
        if (env == AtmosphereTemplate.EnvironmentLayer.DEFAULT) {
            return;
        }
        org.bukkit.GameRule<?> rule = GameRuleUtil.getByName("mob_griefing");
        if (rule == null) {
            return;
        }
        if (!originalGameRules.containsKey(world)) {
            originalGameRules.put(world, world.getGameRuleValue(rule));
        }
        GameRuleUtil.setGameRule(world, rule, env.mobGriefing());
    }

    private void restoreWorldGameRules(World world) {
        Object original = originalGameRules.remove(world);
        if (original == null) {
            return;
        }
        org.bukkit.GameRule<?> rule = GameRuleUtil.getByName("mob_griefing");
        if (rule != null) {
            GameRuleUtil.setGameRule(world, rule, original);
        }
    }

    private List<Player> getNearbyPlayers(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) {
            return List.of();
        }
        double radiusSq = radius * radius;
        List<Player> result = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(center) <= radiusSq) {
                result.add(player);
            }
        }
        return result;
    }

    private Component colorize(String text) {
        if (text == null) {
            return Component.empty();
        }
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
