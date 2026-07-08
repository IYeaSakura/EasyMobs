package net.sakurain.mc.aeternumgenesis.util;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Utility methods for working with {@link GameRule} in a deprecation-free way.
 * Game rules are fetched from {@link Registry#GAME_RULE} instead of the deprecated
 * enum constants / {@link GameRule#getByName(String)}.
 */
public final class GameRuleUtil {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("weather_cycle", "advance_weather"),
            Map.entry("do_weather_cycle", "advance_weather"),
            Map.entry("natural_regeneration", "natural_health_regeneration"),
            Map.entry("do_natural_regeneration", "natural_health_regeneration"),
            Map.entry("daylight_cycle", "advance_time"),
            Map.entry("do_daylight_cycle", "advance_time"),
            Map.entry("fire_tick", "fire_spread_radius_around_player"),
            Map.entry("do_fire_tick", "fire_spread_radius_around_player"),
            Map.entry("mob_loot", "mob_drops"),
            Map.entry("do_mob_loot", "mob_drops"),
            Map.entry("entity_drops", "entity_drops"),
            Map.entry("do_entity_drops", "entity_drops"),
            Map.entry("tile_drops", "block_drops"),
            Map.entry("do_tile_drops", "block_drops"),
            Map.entry("immediate_respawn", "immediate_respawn"),
            Map.entry("do_immediate_respawn", "immediate_respawn"),
            Map.entry("insomnia", "spawn_phantoms"),
            Map.entry("do_insomnia", "spawn_phantoms"),
            Map.entry("limited_crafting", "limited_crafting"),
            Map.entry("do_limited_crafting", "limited_crafting"),
            Map.entry("mob_spawning", "spawn_mobs"),
            Map.entry("do_mob_spawning", "spawn_mobs"),
            Map.entry("patrol_spawning", "spawn_patrols"),
            Map.entry("do_patrol_spawning", "spawn_patrols"),
            Map.entry("trader_spawning", "spawn_wandering_traders"),
            Map.entry("do_trader_spawning", "spawn_wandering_traders"),
            Map.entry("warden_spawning", "spawn_wardens"),
            Map.entry("do_warden_spawning", "spawn_wardens")
    );

    private GameRuleUtil() {
    }

    /**
     * Looks up a game rule by its registry key. Accepts both modern names
     * (e.g. {@code advance_weather}) and legacy aliases (e.g. {@code do_weather_cycle}).
     *
     * @param name the game rule name
     * @return the game rule, or null if not found
     */
    @Nullable
    public static GameRule<?> getByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = name.toLowerCase().trim();
        String key = ALIASES.getOrDefault(normalized, normalized);
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        if (namespaced == null) {
            return null;
        }
        return Registry.GAME_RULE.get(namespaced);
    }

    /**
     * Sets a game rule value using runtime type checking.
     *
     * @param world the world to modify
     * @param rule  the game rule
     * @param value the value to set
     */
    @SuppressWarnings("unchecked")
    public static void setGameRule(World world, GameRule<?> rule, Object value) {
        if (rule == null || value == null) {
            return;
        }
        Class<?> type = rule.getType();
        if (type == Boolean.class && value instanceof Boolean b) {
            world.setGameRule((GameRule<Boolean>) rule, b);
        } else if (type == Integer.class) {
            int intValue;
            if (value instanceof Number n) {
                intValue = n.intValue();
            } else if (value instanceof String s) {
                try {
                    intValue = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return;
                }
            } else {
                return;
            }
            world.setGameRule((GameRule<Integer>) rule, intValue);
        }
    }
}
