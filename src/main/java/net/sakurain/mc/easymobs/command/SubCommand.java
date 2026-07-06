package net.sakurain.mc.easymobs.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

    boolean execute(CommandSender sender, String[] args);

    default String getPermission() {
        return null;
    }

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
