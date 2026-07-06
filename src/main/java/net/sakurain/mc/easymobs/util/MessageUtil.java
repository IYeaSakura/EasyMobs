package net.sakurain.mc.easymobs.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageUtil {

    private static final String PREFIX = "&7[&cEasyMobs&7] ";

    private MessageUtil() {
    }

    public static Component color(String text) {
        if (text == null) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static Component prefix(String text) {
        return color(PREFIX + text);
    }

    public static Component error(String text) {
        return color(PREFIX + "&c" + text);
    }

    public static Component success(String text) {
        return color(PREFIX + "&a" + text);
    }

    public static Component warn(String text) {
        return color(PREFIX + "&e" + text);
    }

    public static void send(CommandSender sender, String text) {
        sender.sendMessage(prefix(text));
    }

    public static void sendError(CommandSender sender, String text) {
        sender.sendMessage(error(text));
    }

    public static void sendSuccess(CommandSender sender, String text) {
        sender.sendMessage(success(text));
    }

    public static void sendWarn(CommandSender sender, String text) {
        sender.sendMessage(warn(text));
    }

    public static void sendActionBar(Player player, String text) {
        player.sendActionBar(color(text));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.showTitle(net.kyori.adventure.title.Title.title(
                color(title),
                color(subtitle),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(fadeIn * 50L),
                        java.time.Duration.ofMillis(stay * 50L),
                        java.time.Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }
}
