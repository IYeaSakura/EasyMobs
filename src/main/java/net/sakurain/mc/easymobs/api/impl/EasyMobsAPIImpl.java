package net.sakurain.mc.easymobs.api.impl;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.api.*;
import org.jetbrains.annotations.NotNull;

public class EasyMobsAPIImpl implements EasyMobsAPI {

    private final EasyMobsPlugin plugin;
    private final ItemAPI itemAPI;
    private final MobAPI mobAPI;
    private final SpawnAPI spawnAPI;
    private final SkillAPI skillAPI;
    private final RegistryAPI registryAPI;

    public EasyMobsAPIImpl(@NotNull EasyMobsPlugin plugin) {
        this.plugin = plugin;
        this.itemAPI = new ItemAPIImpl(plugin);
        this.mobAPI = new MobAPIImpl(plugin);
        this.spawnAPI = new SpawnAPIImpl(plugin);
        this.skillAPI = new SkillAPIImpl(plugin);
        this.registryAPI = new RegistryAPIImpl(plugin);
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
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
}
