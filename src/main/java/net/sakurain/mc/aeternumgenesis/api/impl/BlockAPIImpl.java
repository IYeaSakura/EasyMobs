package net.sakurain.mc.aeternumgenesis.api.impl;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.BlockAPI;
import net.sakurain.mc.aeternumgenesis.block.CustomBlockManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BlockAPIImpl implements BlockAPI {

    private final AeternumGenesisPlugin plugin;

    public BlockAPIImpl(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isCustomBlock(@NotNull Location location) {
        return plugin.getBlockManager().isCustomBlock(location);
    }

    @Override
    @Nullable
    public String getBlockTemplateId(@NotNull Location location) {
        return plugin.getBlockManager().getBlockTemplateId(location);
    }

    @Override
    public boolean hasTemplate(@NotNull String templateId) {
        return plugin.getBlockManager().hasTemplate(templateId);
    }

    @Override
    @NotNull
    public Collection<String> getAllTemplateIds() {
        return plugin.getBlockManager().getTemplateIds();
    }

    @Override
    public boolean placeCustomBlock(@NotNull Location location, @NotNull String templateId) {
        return plugin.getBlockManager().setCustomBlock(location, templateId);
    }

    @Override
    public boolean removeCustomBlock(@NotNull Location location) {
        return plugin.getBlockManager().removeCustomBlock(location);
    }

    @Override
    public boolean registerTemplate(@NotNull String templateId, @NotNull ConfigurationSection config) {
        return plugin.getBlockManager().registerTemplate(templateId, config);
    }

    @Override
    public boolean unregisterTemplate(@NotNull String templateId) {
        return plugin.getBlockManager().unregisterTemplate(templateId);
    }
}
