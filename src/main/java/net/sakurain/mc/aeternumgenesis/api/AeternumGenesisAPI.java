package net.sakurain.mc.aeternumgenesis.api;

import net.sakurain.mc.aeternumgenesis.api.AtmosphereAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;

public interface AeternumGenesisAPI {

    static AeternumGenesisAPI getInstance() {
        ServicesManager sm = Bukkit.getServer().getServicesManager();
        AeternumGenesisAPI api = sm.load(AeternumGenesisAPI.class);
        if (api == null) {
            throw new IllegalStateException("AeternumGenesis API is not available. Ensure AeternumGenesis plugin is loaded.");
        }
        return api;
    }

    static boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AeternumGenesis");
        return plugin != null && plugin.isEnabled();
    }

    @NotNull
    ItemAPI getItemAPI();

    @NotNull
    MobAPI getMobAPI();

    @NotNull
    SpawnAPI getSpawnAPI();

    @NotNull
    SkillAPI getSkillAPI();

    @NotNull
    RegistryAPI getRegistryAPI();

    @NotNull
    SetAPI getSetAPI();

    @NotNull
    BlockAPI getBlockAPI();

    @NotNull
    AtmosphereAPI getAtmosphereAPI();

    @NotNull
    String getVersion();
}
