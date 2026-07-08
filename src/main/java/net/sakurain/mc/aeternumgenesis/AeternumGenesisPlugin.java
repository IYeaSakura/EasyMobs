package net.sakurain.mc.aeternumgenesis;

import net.sakurain.mc.aeternumgenesis.api.AeternumGenesisAPI;
import net.sakurain.mc.aeternumgenesis.api.impl.AeternumGenesisAPIImpl;
import net.sakurain.mc.aeternumgenesis.command.GenesisCommand;
import net.sakurain.mc.aeternumgenesis.config.ConfigManager;
import net.sakurain.mc.aeternumgenesis.item.CustomItemManager;
import net.sakurain.mc.aeternumgenesis.item.effect.ItemEffectHandler;
import net.sakurain.mc.aeternumgenesis.item.set.ItemSetManager;
import net.sakurain.mc.aeternumgenesis.listener.CreatureSpawnListener;
import net.sakurain.mc.aeternumgenesis.listener.EntityEventListener;
import net.sakurain.mc.aeternumgenesis.listener.SkillTriggerListener;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobManager;
import net.sakurain.mc.aeternumgenesis.mob.ImmunityHandler;
import net.sakurain.mc.aeternumgenesis.mob.MobEquipmentAttackHandler;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;
import net.sakurain.mc.aeternumgenesis.skill.SkillManager;
import net.sakurain.mc.aeternumgenesis.spawn.SpawnManager;
import net.sakurain.mc.aeternumgenesis.ai.AICombatListener;
import net.sakurain.mc.aeternumgenesis.ai.CustomAIController;
import net.sakurain.mc.aeternumgenesis.block.CustomBlockManager;
import net.sakurain.mc.aeternumgenesis.listener.CustomBlockListener;
import net.sakurain.mc.aeternumgenesis.listener.ProjectileHitListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class AeternumGenesisPlugin extends JavaPlugin {

    private static volatile AeternumGenesisPlugin instance;

    private ConfigManager configManager;
    private CustomItemManager itemManager;
    private ItemSetManager itemSetManager;
    private CustomMobManager mobManager;
    private SkillManager skillManager;
    private SpawnManager spawnManager;
    private CustomBlockManager blockManager;
    private ItemEffectHandler itemEffectHandler;
    private AeternumGenesisAPIImpl api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        configManager.loadAll();

        this.skillManager = new SkillManager(configManager.getSkillConfigs());

        this.itemSetManager = new ItemSetManager(this);
        itemSetManager.load(configManager.getSetConfigs());

        this.itemManager = new CustomItemManager(configManager.getItemConfigs());
        this.mobManager = new CustomMobManager(configManager.getMobConfigs());
        this.spawnManager = new SpawnManager(configManager.getSpawnConfigs());
        this.blockManager = new CustomBlockManager(this);
        this.blockManager.loadConfigs(configManager.getBlockConfigs());
        this.blockManager.respawnAllHolograms();

        registerListeners();
        registerCommands();
        registerAPI();

        getLogger().info("AeternumGenesis enabled! Items: " + itemManager.getTemplateCount()
                + ", Mobs: " + mobManager.getTemplateCount()
                + ", Skills: " + skillManager.getTemplateCount()
                + ", SpawnRules: " + spawnManager.getRuleCount());
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        MobTracker.getInstance().cancelAll();
        if (itemEffectHandler != null) {
            itemEffectHandler.clearAllEffects();
        }
        if (api != null) {
            Bukkit.getServer().getServicesManager().unregister(api);
        }
        if (blockManager != null) {
            blockManager.saveStorage();
        }
        instance = null;
    }

    private void registerListeners() {
        this.itemEffectHandler = new ItemEffectHandler(this);
        getServer().getPluginManager().registerEvents(itemEffectHandler, this);
        getServer().getPluginManager().registerEvents(new EntityEventListener(), this);
        getServer().getPluginManager().registerEvents(new SkillTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new ImmunityHandler(), this);
        getServer().getPluginManager().registerEvents(new AICombatListener(), this);
        getServer().getPluginManager().registerEvents(new MobEquipmentAttackHandler(), this);
        getServer().getPluginManager().registerEvents(new CustomBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
    }

    private void registerCommands() {
        GenesisCommand command = new GenesisCommand();
        org.bukkit.command.PluginCommand cmd = getCommand("genesis");
        if (cmd == null) {
            getLogger().severe("Command 'genesis' not registered in plugin.yml");
            return;
        }
        cmd.setExecutor(command);
        cmd.setTabCompleter(command);
    }

    private void registerAPI() {
        this.api = new AeternumGenesisAPIImpl(this);
        Bukkit.getServer().getServicesManager().register(AeternumGenesisAPI.class, api, this, ServicePriority.Normal);
    }

    public static AeternumGenesisPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CustomItemManager getItemManager() {
        return itemManager;
    }

    public ItemSetManager getItemSetManager() {
        return itemSetManager;
    }

    public CustomMobManager getMobManager() {
        return mobManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public ItemEffectHandler getItemEffectHandler() {
        return itemEffectHandler;
    }

    public CustomBlockManager getBlockManager() {
        return blockManager;
    }

    public AeternumGenesisAPI getAPI() {
        return api;
    }

    public net.sakurain.mc.aeternumgenesis.api.ItemAPI getItemAPI() {
        return api.getItemAPI();
    }
}
