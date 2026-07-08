package net.sakurain.mc.aeternumgenesis.command;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobPreSpawnEvent;
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobSpawnEvent;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobManager;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.LevelSystem;
import net.sakurain.mc.aeternumgenesis.mob.MobSpawner;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SpawnMobSubCommand implements SubCommand {

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.error("Usage: /genesis spawn <mob-id> [player|x y z]"));
            return true;
        }

        String mobId = args[0];
        CustomMobManager manager = AeternumGenesisPlugin.getInstance().getMobManager();
        CustomMobTemplate template = manager.getTemplate(mobId);
        if (template == null) {
            sender.sendMessage(MessageUtil.error("Mob '&e" + mobId + "&c' does not exist!"));
            return true;
        }

        Location location = resolveLocation(sender, args);
        if (location == null) {
            return true;
        }

        int level = 1;
        if (args.length >= 5) {
            try {
                level = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtil.error("Invalid level!"));
                return true;
            }
        }

        CustomMobPreSpawnEvent preEvent = new CustomMobPreSpawnEvent(mobId, location, level);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            sender.sendMessage(MessageUtil.error("Spawn was cancelled by another plugin."));
            return true;
        }

        LivingEntity entity = MobSpawner.spawn(template, location);
        if (entity == null) {
            sender.sendMessage(MessageUtil.error("Failed to spawn mob!"));
            return true;
        }
        LevelSystem.applyLevel(entity, preEvent.getLevel(), template);
        Bukkit.getPluginManager().callEvent(new CustomMobSpawnEvent(mobId, entity, location, preEvent.getLevel()));
        sender.sendMessage(MessageUtil.success("Spawned &e" + mobId + " &aat &e" +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));
        return true;
    }

    private Location resolveLocation(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 4) {
            Player player = sender instanceof Player ? (Player) sender : null;
            World world = player != null ? player.getWorld() : Bukkit.getWorlds().get(0);
            if (world == null) {
                sender.sendMessage(MessageUtil.error("No world available!"));
                return null;
            }
            try {
                double x = parseCoordinate(args[1], player != null ? player.getLocation().getX() : 0);
                double y = parseCoordinate(args[2], player != null ? player.getLocation().getY() : 0);
                double z = parseCoordinate(args[3], player != null ? player.getLocation().getZ() : 0);
                return new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtil.error("Invalid coordinates!"));
                return null;
            }
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MessageUtil.error("Player '&e" + args[1] + "&c' is not online!"));
                return null;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else if (sender instanceof BlockCommandSender block) {
            target = null;
            return block.getBlock().getLocation().add(0.5, 1, 0.5);
        } else {
            sender.sendMessage(MessageUtil.error("Console must specify a target player or coordinates!"));
            return null;
        }
        return target.getLocation();
    }

    private double parseCoordinate(@NotNull String input, double relative) {
        if (input.startsWith("~")) {
            String rest = input.substring(1);
            return rest.isEmpty() ? relative : relative + Double.parseDouble(rest);
        }
        return Double.parseDouble(input);
    }

    @Override
    public String getPermission() {
        return "genesis.spawn";
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        CustomMobManager manager = AeternumGenesisPlugin.getInstance().getMobManager();
        if (args.length == 1) {
            return manager.getTemplateIds().stream()
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
