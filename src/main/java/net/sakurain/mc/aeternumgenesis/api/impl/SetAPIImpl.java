package net.sakurain.mc.aeternumgenesis.api.impl;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.SetAPI;
import net.sakurain.mc.aeternumgenesis.item.set.ItemSetManager;
import net.sakurain.mc.aeternumgenesis.item.set.ItemSetTemplate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SetAPIImpl implements SetAPI {

    private final AeternumGenesisPlugin plugin;

    public SetAPIImpl(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public Collection<String> getAllSetIds() {
        return plugin.getItemSetManager().getSetIds();
    }

    @Override
    public boolean hasSet(@NotNull String setId) {
        return plugin.getItemSetManager().getSet(setId) != null;
    }

    @Override
    public boolean registerSet(@NotNull String setId, @NotNull ConfigurationSection config) {
        ItemSetTemplate set = ItemSetManager.parseTemplate(setId, config);
        if (set == null) {
            return false;
        }
        return plugin.getItemSetManager().registerSet(set);
    }

    @Override
    public boolean unregisterSet(@NotNull String setId) {
        return plugin.getItemSetManager().unregisterSet(setId);
    }
}
