package net.sakurain.mc.aeternumgenesis.eventchain;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.item.CustomItemTemplate;
import net.sakurain.mc.aeternumgenesis.item.ItemBuilder;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobManager;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.LevelSystem;
import net.sakurain.mc.aeternumgenesis.mob.MobSpawner;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import net.sakurain.mc.aeternumgenesis.util.ConfigParseUtil;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Executes actions defined in event chain stages and end handlers.
 */
public final class EventActionExecutor {

    private final AeternumGenesisPlugin plugin;

    public EventActionExecutor(AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(EventChainTemplate.Action action, EventChainInstance instance) {
        if (action == null) {
            return;
        }
        switch (action.type()) {
            case "broadcast" -> executeBroadcast(action.parameters(), instance);
            case "title_all" -> executeTitleAll(action.parameters(), instance);
            case "play_sound_all" -> executePlaySoundAll(action.parameters(), instance);
            case "apply_atmosphere" -> executeApplyAtmosphere(action.parameters(), instance);
            case "remove_atmosphere" -> executeRemoveAtmosphere(action.parameters(), instance);
            case "spawn_around_players" -> executeSpawnAroundPlayers(action.parameters(), instance);
            case "spawn_boss" -> executeSpawnBoss(action.parameters(), instance);
            case "reward_all" -> executeRewardAll(action.parameters(), instance);
            case "punish_all" -> executePunishAll(action.parameters(), instance);
            case "command_console" -> executeCommandConsole(action.parameters(), instance);
            case "command_player" -> executeCommandPlayer(action.parameters(), instance);
            case "kill_mobs" -> executeKillMobs(action.parameters(), instance);
            case "despawn_mobs" -> executeDespawnMobs(action.parameters(), instance);
            default -> plugin.getLogger().warning("Unknown event action type: " + action.type());
        }
    }

    public void executeAll(List<EventChainTemplate.Action> actions, EventChainInstance instance) {
        if (actions == null) {
            return;
        }
        for (EventChainTemplate.Action action : actions) {
            execute(action, instance);
        }
    }

    private void executeBroadcast(Map<String, Object> params, EventChainInstance instance) {
        String message = getString(params, "message");
        if (message == null || message.isBlank()) {
            return;
        }
        Component component = MessageUtil.prefix(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
        Bukkit.getConsoleSender().sendMessage(component);
    }

    private void executeTitleAll(Map<String, Object> params, EventChainInstance instance) {
        String title = getString(params, "title");
        String subtitle = getString(params, "subtitle");
        int fadeIn = getInt(params, "fade_in", 10);
        int stay = getInt(params, "stay", 70);
        int fadeOut = getInt(params, "fade_out", 10);
        if (title == null && subtitle == null) {
            return;
        }
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L));
        Title adventureTitle = Title.title(
                MessageUtil.color(title == null ? "" : title),
                MessageUtil.color(subtitle == null ? "" : subtitle),
                times);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(adventureTitle);
        }
    }

    private void executePlaySoundAll(Map<String, Object> params, EventChainInstance instance) {
        String soundName = getString(params, "sound");
        if (soundName == null) {
            return;
        }
        Sound sound = ConfigParseUtil.parseSound(soundName);
        if (sound == null) {
            return;
        }
        float volume = getFloat(params, "volume", 1.0f);
        float pitch = getFloat(params, "pitch", 1.0f);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private void executeApplyAtmosphere(Map<String, Object> params, EventChainInstance instance) {
        String atmosphereId = getString(params, "atmosphere_id");
        if (atmosphereId == null) {
            return;
        }
        long duration = EventChainTemplate.parseDuration(getString(params, "duration", "6000"));
        double radius = getDouble(params, "radius", 64.0);
        Location center = resolveCenter(params, instance);
        if (center == null) {
            return;
        }
        UUID uuid = plugin.getAtmosphereManager().applyAtmosphere(center, radius, atmosphereId, duration);
        if (uuid != null) {
            instance.setContext("atmosphere_" + atmosphereId, uuid.toString());
        }
    }

    private void executeRemoveAtmosphere(Map<String, Object> params, EventChainInstance instance) {
        String atmosphereId = getString(params, "atmosphere_id");
        if (atmosphereId == null) {
            return;
        }
        Object stored = instance.getContext("atmosphere_" + atmosphereId);
        if (stored instanceof String uuidString) {
            try {
                plugin.getAtmosphereManager().removeAtmosphere(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid atmosphere UUID in event chain context: " + uuidString);
            }
        }
    }

    private void executeSpawnAroundPlayers(Map<String, Object> params, EventChainInstance instance) {
        String mobId = getString(params, "mob");
        if (mobId == null) {
            return;
        }
        CustomMobManager mobManager = plugin.getMobManager();
        CustomMobTemplate template = mobManager.getTemplate(mobId);
        if (template == null) {
            plugin.getLogger().warning("Event chain spawn_around_players references unknown mob: " + mobId);
            return;
        }
        int count = getInt(params, "count", 5);
        double radius = getDouble(params, "radius", 40.0);
        int perPlayerCap = getInt(params, "per_player_cap", 5);
        int minDistance = getInt(params, "min_distance", 16);
        int maxDistance = getInt(params, "max_distance", 48);
        int globalCap = getInt(params, "global_cap", -1);

        if (globalCap > 0) {
            int existing = MobTracker.getInstance().countGlobalMobs(mobId);
            if (existing >= globalCap) {
                return;
            }
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int spawnedTotal = 0;
        for (Player player : players) {
            int spawnedForPlayer = 0;
            for (int i = 0; i < count && spawnedForPlayer < perPlayerCap; i++) {
                if (globalCap > 0 && spawnedTotal >= globalCap) {
                    break;
                }
                Location point = selectSpawnPoint(player.getLocation(), minDistance, maxDistance);
                if (point == null) {
                    continue;
                }
                LivingEntity entity = MobSpawner.spawn(template, point);
                if (entity == null) {
                    continue;
                }
                LevelSystem.applyLevel(entity, 1, template);
                spawnedForPlayer++;
                spawnedTotal++;
            }
            if (globalCap > 0 && spawnedTotal >= globalCap) {
                break;
            }
        }
        plugin.getLogger().fine("Event chain spawned " + spawnedTotal + " " + mobId);
    }

    private void executeSpawnBoss(Map<String, Object> params, EventChainInstance instance) {
        String mobId = getString(params, "mob");
        if (mobId == null) {
            return;
        }
        CustomMobTemplate template = plugin.getMobManager().getTemplate(mobId);
        if (template == null) {
            plugin.getLogger().warning("Event chain spawn_boss references unknown mob: " + mobId);
            return;
        }
        Location location = resolveLocation(params, instance);
        if (location == null) {
            return;
        }
        LivingEntity entity = MobSpawner.spawn(template, location);
        if (entity == null) {
            return;
        }
        int level = getInt(params, "level", 1);
        if (level > 0) {
            LevelSystem.applyLevel(entity, level, template);
        }
        String key = getString(params, "boss_key", "boss");
        instance.registerBoss(key, entity);
    }

    private void executeRewardAll(Map<String, Object> params, EventChainInstance instance) {
        String itemId = getString(params, "item");
        if (itemId == null) {
            return;
        }
        int amount = getInt(params, "amount", 1);
        CustomItemTemplate template = plugin.getItemManager().getTemplate(itemId);
        if (template == null) {
            plugin.getLogger().warning("Event chain reward_all references unknown item: " + itemId);
            return;
        }
        ItemStack stack = ItemBuilder.build(template);
        if (stack == null) {
            return;
        }
        stack.setAmount(Math.max(1, amount));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().addItem(stack).forEach((slot, leftover) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    private void executePunishAll(Map<String, Object> params, EventChainInstance instance) {
        String effectName = getString(params, "effect");
        if (effectName == null) {
            return;
        }
        PotionEffectType type = ConfigParseUtil.parsePotionEffectType(effectName);
        if (type == null) {
            return;
        }
        int duration = (int) EventChainTemplate.parseDuration(getString(params, "duration", "6000"));
        int amplifier = getInt(params, "amplifier", 0);
        boolean particles = getBoolean(params, "show_particles", false);
        boolean icon = getBoolean(params, "show_icon", false);
        PotionEffect effect = new PotionEffect(type, Math.max(1, duration), Math.max(0, amplifier), false, particles, icon);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(effect);
        }
    }

    private void executeCommandConsole(Map<String, Object> params, EventChainInstance instance) {
        String command = getString(params, "command");
        if (command == null || command.isBlank()) {
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), substituteVariables(command, instance));
    }

    private void executeCommandPlayer(Map<String, Object> params, EventChainInstance instance) {
        String command = getString(params, "command");
        if (command == null || command.isBlank()) {
            return;
        }
        Player initiator = instance.getInitiator();
        if (initiator != null) {
            Bukkit.dispatchCommand(initiator, substituteVariables(command, instance));
        }
    }

    private void executeKillMobs(Map<String, Object> params, EventChainInstance instance) {
        removeMobs(params, instance, true);
    }

    private void executeDespawnMobs(Map<String, Object> params, EventChainInstance instance) {
        removeMobs(params, instance, false);
    }

    private void removeMobs(Map<String, Object> params, EventChainInstance instance, boolean kill) {
        String mobTemplateId = getString(params, "mob_template");
        String faction = getString(params, "faction");
        double radius = getDouble(params, "radius", -1.0);
        int limit = getInt(params, "limit", -1);

        Location center = null;
        if (radius > 0) {
            center = resolveCenter(params, instance);
        }

        World world = resolveWorld(params, instance);

        List<LivingEntity> targets = new ArrayList<>();
        MobTracker tracker = MobTracker.getInstance();
        for (LivingEntity entity : tracker.getTrackedMobs()) {
            if (entity.isDead()) {
                continue;
            }
            if (world != null && !world.equals(entity.getWorld())) {
                continue;
            }
            if (center != null && !center.getWorld().equals(entity.getWorld())) {
                continue;
            }
            if (center != null && center.distanceSquared(entity.getLocation()) > radius * radius) {
                continue;
            }
            CustomMobTemplate template = tracker.getTemplate(entity);
            if (mobTemplateId != null && !mobTemplateId.isBlank()) {
                if (template == null || !template.getId().equalsIgnoreCase(mobTemplateId)) {
                    continue;
                }
            }
            if (faction != null && !faction.isBlank()) {
                if (template == null || !faction.equalsIgnoreCase(template.getFaction())) {
                    continue;
                }
            }
            targets.add(entity);
            if (limit > 0 && targets.size() >= limit) {
                break;
            }
        }

        for (LivingEntity entity : targets) {
            if (kill) {
                entity.setHealth(0.0);
            } else {
                entity.remove();
            }
        }
    }

    private Location resolveCenter(Map<String, Object> params, EventChainInstance instance) {
        String locationType = getString(params, "location", "initiator");
        return resolveLocation(locationType, params, instance);
    }

    private Location resolveLocation(Map<String, Object> params, EventChainInstance instance) {
        String locationType = getString(params, "location", "highest_player");
        return resolveLocation(locationType, params, instance);
    }

    private Location resolveLocation(String type, Map<String, Object> params, EventChainInstance instance) {
        return switch (type.toLowerCase()) {
            case "initiator" -> {
                Player initiator = instance.getInitiator();
                yield initiator != null ? initiator.getLocation() : null;
            }
            case "highest_player" -> {
                Player highest = null;
                double maxY = Double.NEGATIVE_INFINITY;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double y = player.getLocation().getY();
                    if (y > maxY) {
                        maxY = y;
                        highest = player;
                    }
                }
                yield highest != null ? highest.getLocation() : null;
            }
            case "world_spawn" -> {
                World world = resolveWorld(params, instance);
                yield world != null ? world.getSpawnLocation() : null;
            }
            default -> {
                World world = resolveWorld(params, instance);
                yield world != null ? world.getSpawnLocation() : null;
            }
        };
    }

    private World resolveWorld(Map<String, Object> params, EventChainInstance instance) {
        String worldName = getString(params, "world");
        if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return world;
            }
        }
        Player initiator = instance.getInitiator();
        if (initiator != null) {
            return initiator.getWorld();
        }
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        return players.isEmpty() ? null : players.get(0).getWorld();
    }

    private Location selectSpawnPoint(Location center, int minDistance, int maxDistance) {
        World world = center.getWorld();
        if (world == null) {
            return null;
        }
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = minDistance + ThreadLocalRandom.current().nextDouble() * (maxDistance - minDistance);
            int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            int y = world.getHighestBlockYAt(x, z);
            Location point = new Location(world, x + 0.5, y + 1.0, z + 0.5);
            if (point.getBlock().getType().isSolid()) {
                continue;
            }
            return point;
        }
        return null;
    }

    private String substituteVariables(String command, EventChainInstance instance) {
        Player initiator = instance.getInitiator();
        String result = command;
        if (initiator != null) {
            result = result.replace("{initiator}", initiator.getName());
            result = result.replace("{world}", initiator.getWorld().getName());
        }
        result = result.replace("{event_id}", instance.getTemplate().id());
        return result;
    }

    private static String getString(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    private static String getString(Map<String, Object> params, String key, String def) {
        Object value = params.get(key);
        return value == null ? def : value.toString();
    }

    private static int getInt(Map<String, Object> params, String key, int def) {
        Object value = params.get(key);
        return ConfigParseUtil.toInt(value, def);
    }

    private static double getDouble(Map<String, Object> params, String key, double def) {
        Object value = params.get(key);
        return ConfigParseUtil.toDouble(value, def);
    }

    private static float getFloat(Map<String, Object> params, String key, float def) {
        Object value = params.get(key);
        return value instanceof Number n ? n.floatValue() : def;
    }

    private static boolean getBoolean(Map<String, Object> params, String key, boolean def) {
        Object value = params.get(key);
        return ConfigParseUtil.toBoolean(value, def);
    }
}
