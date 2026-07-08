package net.sakurain.mc.aeternumgenesis.command;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ListSubCommand implements SubCommand {

    private static final int PAGE_SIZE = 10;

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.error("Usage: /genesis list <items|mobs|skills|spawns> [page]"));
            return true;
        }

        String type = args[0].toLowerCase();
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtil.error("Invalid page number!"));
                return true;
            }
        }

        Collection<String> ids = switch (type) {
            case "items" -> AeternumGenesisPlugin.getInstance().getItemManager().getTemplateIds();
            case "mobs" -> AeternumGenesisPlugin.getInstance().getMobManager().getTemplateIds();
            case "skills" -> AeternumGenesisPlugin.getInstance().getSkillManager().getAllSkillIds();
            case "spawns" -> AeternumGenesisPlugin.getInstance().getSpawnManager().getRules().stream()
                    .map(r -> r.getId()).collect(Collectors.toList());
            default -> {
                sender.sendMessage(MessageUtil.error("Unknown list type. Use: items, mobs, skills, spawns"));
                yield List.of();
            }
        };

        List<String> sorted = new ArrayList<>(ids);
        sorted.sort(String::compareToIgnoreCase);

        int totalPages = (sorted.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > totalPages) page = Math.max(1, totalPages);

        sender.sendMessage(MessageUtil.prefix("&e" + type + " &7(Page &f" + page + "/" + totalPages + "&7, Total: &f" + sorted.size() + "&7)"));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sorted.size());
        for (int i = start; i < end; i++) {
            sender.sendMessage(MessageUtil.color(" &7- &f" + sorted.get(i)));
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "genesis.list";
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("items", "mobs", "skills", "spawns").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
