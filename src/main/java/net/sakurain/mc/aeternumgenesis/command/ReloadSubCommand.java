package net.sakurain.mc.aeternumgenesis.command;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadSubCommand implements SubCommand {

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        plugin.reloadConfig();
        plugin.getConfigManager().reloadAll();
        plugin.getItemSetManager().load(plugin.getConfigManager().getSetConfigs());
        plugin.getItemManager().reload();
        plugin.getMobManager().reload();
        plugin.getSkillManager().load(plugin.getConfigManager().getSkillConfigs());
        plugin.getSpawnManager().reload();
        plugin.getBlockManager().loadConfigs(plugin.getConfigManager().getBlockConfigs());
        plugin.getAtmosphereManager().reload(plugin.getConfigManager().getAtmosphereConfigs());
        plugin.getEcosystemManager().reload(plugin.getConfigManager().getEcosystemConfigs());
        plugin.getWorldRuleManager().reload(plugin.getConfigManager().getWorldRuleConfigs());
        sender.sendMessage(MessageUtil.success("Configuration reloaded!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "genesis.reload";
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
