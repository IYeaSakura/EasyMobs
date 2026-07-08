package net.sakurain.mc.aeternumgenesis.command;

import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class GenesisCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public GenesisCommand() {
        subCommands.put("give", new GiveItemSubCommand());
        subCommands.put("spawn", new SpawnMobSubCommand());
        subCommands.put("reload", new ReloadSubCommand());
        subCommands.put("list", new ListSubCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageUtil.error("Usage: /genesis <subcommand>"));
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage(MessageUtil.error("Unknown subcommand: " + args[0]));
            return true;
        }

        String permission = sub.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(MessageUtil.error("You don't have permission to use this command."));
            return true;
        }

        return sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            return List.of();
        }

        String permission = sub.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            return List.of();
        }

        return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }
}
