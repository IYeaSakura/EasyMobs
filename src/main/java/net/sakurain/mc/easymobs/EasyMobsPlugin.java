package net.sakurain.mc.easymobs;

import net.sakurain.mc.easymobs.api.EasyMobsAPI;
import net.sakurain.mc.easymobs.api.impl.EasyMobsAPIImpl;
import net.sakurain.mc.easymobs.command.EasyMobsCommand;
import net.sakurain.mc.easymobs.config.ConfigManager;
import net.sakurain.mc.easymobs.item.CustomItemManager;
import net.sakurain.mc.easymobs.item.effect.ItemEffectHandler;
import net.sakurain.mc.easymobs.item.set.ItemSetManager;
import net.sakurain.mc.easymobs.listener.CreatureSpawnListener;
import net.sakurain.mc.easymobs.listener.EntityEventListener;
import net.sakurain.mc.easymobs.listener.SkillTriggerListener;
import net.sakurain.mc.easymobs.mob.CustomMobManager;
import net.sakurain.mc.easymobs.mob.ImmunityHandler;
import net.sakurain.mc.easymobs.mob.MobEquipmentAttackHandler;
import net.sakurain.mc.easymobs.mob.MobTracker;
import net.sakurain.mc.easymobs.skill.SkillManager;
import net.sakurain.mc.easymobs.spawn.SpawnManager;
import net.sakurain.mc.easymobs.ai.AICombatListener;
import net.sakurain.mc.easymobs.ai.CustomAIController;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyMobsPlugin extends JavaPlugin {

    private static EasyMobsPlugin instance;

    private ConfigManager configManager;
    private CustomItemManager itemManager;
    private ItemSetManager itemSetManager;
    private CustomMobManager mobManager;
    private SkillManager skillManager;
    private SpawnManager spawnManager;
    private ItemEffectHandler itemEffectHandler;
    private EasyMobsAPIImpl api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        configManager.loadAll();

        this.itemSetManager = new ItemSetManager(this);
        itemSetManager.load(configManager.getSetConfigs());

        this.itemManager = new CustomItemManager(configManager.getItemConfigs());
        this.mobManager = new CustomMobManager(configManager.getMobConfigs());
        this.skillManager = new SkillManager(configManager.getSkillConfigs());
        this.spawnManager = new SpawnManager(configManager.getSpawnConfigs());

        registerListeners();
        registerCommands();
        registerAPI();

        getLogger().info("EasyMobs enabled! Items: " + itemManager.getTemplateCount()
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
    }

    private void registerCommands() {
        EasyMobsCommand command = new EasyMobsCommand();
        getCommand("ezmobs").setExecutor(command);
        getCommand("ezmobs").setTabCompleter(command);
    }

    private void registerAPI() {
        this.api = new EasyMobsAPIImpl(this);
        Bukkit.getServer().getServicesManager().register(EasyMobsAPI.class, api, this, ServicePriority.Normal);
    }

    public static EasyMobsPlugin getInstance() {
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

    public EasyMobsAPI getAPI() {
        return api;
    }
}
