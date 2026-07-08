package net.sakurain.mc.aeternumgenesis.api.impl;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.*;
import net.sakurain.mc.aeternumgenesis.api.AtmosphereAPI;
import net.sakurain.mc.aeternumgenesis.api.BlockAPI;
import org.jetbrains.annotations.NotNull;

public class AeternumGenesisAPIImpl implements AeternumGenesisAPI {

    private final AeternumGenesisPlugin plugin;
    private final ItemAPI itemAPI;
    private final MobAPI mobAPI;
    private final SpawnAPI spawnAPI;
    private final SkillAPI skillAPI;
    private final RegistryAPI registryAPI;
    private final SetAPI setAPI;
    private final BlockAPI blockAPI;
    private final AtmosphereAPI atmosphereAPI;

    public AeternumGenesisAPIImpl(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
        this.itemAPI = new ItemAPIImpl(plugin);
        this.mobAPI = new MobAPIImpl(plugin);
        this.spawnAPI = new SpawnAPIImpl(plugin);
        this.skillAPI = new SkillAPIImpl(plugin);
        this.registryAPI = new RegistryAPIImpl(plugin);
        this.setAPI = new SetAPIImpl(plugin);
        this.blockAPI = new BlockAPIImpl(plugin);
        this.atmosphereAPI = new AtmosphereAPIImpl(plugin);
    }

    @Override
    @NotNull
    public ItemAPI getItemAPI() {
        return itemAPI;
    }

    @Override
    @NotNull
    public MobAPI getMobAPI() {
        return mobAPI;
    }

    @Override
    @NotNull
    public SpawnAPI getSpawnAPI() {
        return spawnAPI;
    }

    @Override
    @NotNull
    public SkillAPI getSkillAPI() {
        return skillAPI;
    }

    @Override
    @NotNull
    public RegistryAPI getRegistryAPI() {
        return registryAPI;
    }

    @Override
    @NotNull
    public SetAPI getSetAPI() {
        return setAPI;
    }

    @Override
    @NotNull
    public BlockAPI getBlockAPI() {
        return blockAPI;
    }

    @Override
    @NotNull
    public AtmosphereAPI getAtmosphereAPI() {
        return atmosphereAPI;
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
}
