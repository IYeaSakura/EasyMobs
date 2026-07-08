package net.sakurain.mc.aeternumgenesis.world;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.GameRuleUtil;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads world rules from config and applies them to all worlds.
 */
public final class WorldRuleManager implements Listener {

    private final AeternumGenesisPlugin plugin;
    private WorldRuleTemplate template;
    private final Map<World, Map<GameRule<?>, Object>> originalGameRules = new HashMap<>();

    public WorldRuleManager(Map<String, YamlConfiguration> configs) {
        this.plugin = AeternumGenesisPlugin.getInstance();
        load(configs);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        applyToWorlds();
    }

    public void load(Map<String, YamlConfiguration> configs) {
        template = null;
        if (configs == null || configs.isEmpty()) {
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
        ConfigurationSection section = config.getConfigurationSection("world_rules");
        if (section == null) {
            section = config;
        }
        try {
            template = WorldRuleTemplate.fromConfig(section);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse world rules in " + fileName, e);
        }
    }

    public void reload(Map<String, YamlConfiguration> configs) {
        restoreOriginalGameRules();
        load(configs);
        applyToWorlds();
    }

    public void shutdown() {
        restoreOriginalGameRules();
    }

    public WorldRuleTemplate getTemplate() {
        return template;
    }

    private void applyToWorlds() {
        if (template == null) {
            return;
        }
        for (World world : plugin.getServer().getWorlds()) {
            applyGameRules(world);
            applyPvp(world);
        }
    }

    private void applyGameRules(World world) {
        if (template == null) {
            return;
        }
        Map<GameRule<?>, Object> original = originalGameRules.computeIfAbsent(world, w -> new HashMap<>());
        for (Map.Entry<GameRule<?>, Object> entry : template.getGameRules().entrySet()) {
            GameRule<?> rule = entry.getKey();
            Object value = entry.getValue();
            if (!original.containsKey(rule)) {
                Object current = world.getGameRuleValue(rule);
                original.put(rule, current);
            }
            setGameRule(world, rule, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void setGameRule(World world, GameRule<T> rule, Object value) {
        if (value instanceof Boolean b && rule.getType() == Boolean.class) {
            world.setGameRule((GameRule<Boolean>) rule, b);
        } else if (value instanceof Number n && rule.getType() == Integer.class) {
            world.setGameRule((GameRule<Integer>) rule, n.intValue());
        } else if (value instanceof String s) {
            if (rule.getType() == Boolean.class) {
                world.setGameRule((GameRule<Boolean>) rule, Boolean.parseBoolean(s));
            } else if (rule.getType() == Integer.class) {
                try {
                    world.setGameRule((GameRule<Integer>) rule, Integer.parseInt(s));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private void applyPvp(World world) {
        if (template == null || template.getPvp() == null) {
            return;
        }
        org.bukkit.GameRule<?> rule = GameRuleUtil.getByName("pvp");
        if (rule != null) {
            GameRuleUtil.setGameRule(world, rule, template.getPvp());
        }
    }

    private void restoreOriginalGameRules() {
        for (Map.Entry<World, Map<GameRule<?>, Object>> entry : originalGameRules.entrySet()) {
            World world = entry.getKey();
            for (Map.Entry<GameRule<?>, Object> ruleEntry : entry.getValue().entrySet()) {
                setGameRule(world, (GameRule<Object>) ruleEntry.getKey(), ruleEntry.getValue());
            }
        }
        originalGameRules.clear();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (template == null) {
            return;
        }
        if (template.getKeepInventory() != null && template.getKeepInventory()) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }
        if (template.getKeepExperience() != null && template.getKeepExperience()) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
        if (template.getDeathMessageFormat() != null && !template.getDeathMessageFormat().isEmpty()) {
            String message = template.getDeathMessageFormat()
                    .replace("{player}", event.getPlayer().getName())
                    .replace("{world}", event.getPlayer().getWorld().getName());
            event.deathMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (template == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Double multiplier = null;
            switch (event.getCause()) {
                case FALL -> multiplier = template.getFallDamageMultiplier();
                case FIRE, FIRE_TICK, LAVA, HOT_FLOOR -> multiplier = template.getFireDamageMultiplier();
            }
            if (multiplier != null) {
                event.setDamage(event.getDamage() * multiplier);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (template == null || template.getHungerDrainRate() == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        int oldFood = ((Player) event.getEntity()).getFoodLevel();
        int newFood = event.getFoodLevel();
        int delta = newFood - oldFood;
        if (delta < 0) {
            event.setFoodLevel(oldFood + (int) (delta * template.getHungerDrainRate()));
        }
    }
}
