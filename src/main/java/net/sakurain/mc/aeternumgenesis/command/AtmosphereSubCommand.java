package net.sakurain.mc.aeternumgenesis.command;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.atmosphere.ActiveAtmosphere;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AtmosphereSubCommand implements SubCommand {

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.error("Usage: /genesis atmosphere <apply|remove|list|active> [args]"));
            return true;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "apply" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(MessageUtil.error("This command can only be used by a player."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.error("Usage: /genesis atmosphere apply <id> [radius] [duration_seconds]"));
                    return true;
                }
                String id = args[1];
                double radius = args.length >= 3 ? parseDouble(args[2], 30.0) : 30.0;
                long durationTicks = args.length >= 4 ? (long) (parseDouble(args[3], 60.0) * 20.0) : 1200L;
                Location center = player.getLocation();
                UUID instanceId = plugin.getAtmosphereManager().applyAtmosphere(center, radius, id, durationTicks);
                if (instanceId == null) {
                    sender.sendMessage(MessageUtil.error("Unknown atmosphere: " + id));
                } else {
                    sender.sendMessage(MessageUtil.success("Applied atmosphere " + id + " (instance: " + instanceId + ")"));
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.error("Usage: /genesis atmosphere remove <instance-id>"));
                    return true;
                }
                try {
                    UUID instanceId = UUID.fromString(args[1]);
                    if (plugin.getAtmosphereManager().removeAtmosphere(instanceId)) {
                        sender.sendMessage(MessageUtil.success("Removed atmosphere instance."));
                    } else {
                        sender.sendMessage(MessageUtil.error("Atmosphere instance not found."));
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(MessageUtil.error("Invalid UUID: " + args[1]));
                }
            }
            case "list" -> {
                sender.sendMessage(MessageUtil.success("Loaded atmosphere templates:"));
                for (String id : plugin.getAtmosphereManager().getTemplateIds()) {
                    sender.sendMessage(MessageUtil.color("  - " + id));
                }
            }
            case "active" -> {
                sender.sendMessage(MessageUtil.success("Active atmosphere instances: " + plugin.getAtmosphereManager().getActiveCount()));
            }
            default -> sender.sendMessage(MessageUtil.error("Unknown action: " + action));
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "genesis.admin";
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("apply", "remove", "list", "active");
        }
        if (args.length == 2 && "apply".equalsIgnoreCase(args[0])) {
            return new ArrayList<>(AeternumGenesisPlugin.getInstance().getAtmosphereManager().getTemplateIds());
        }
        return List.of();
    }

    private double parseDouble(String value, double def) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
