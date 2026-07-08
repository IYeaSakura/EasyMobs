package net.sakurain.mc.aeternumgenesis.examples.rpg;

import net.sakurain.mc.aeternumgenesis.api.AeternumGenesisAPI;
import net.sakurain.mc.aeternumgenesis.api.ItemAPI;
import net.sakurain.mc.aeternumgenesis.api.MobAPI;
import net.sakurain.mc.aeternumgenesis.api.SkillAPI;
import net.sakurain.mc.aeternumgenesis.api.SpawnAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Example external plugin that customizes AeternumGenesis entirely through the public API.
 *
 * <p>It registers items, mobs, skills, and a spawn rule at runtime, and also
 * registers a custom skill effect type. Players can test the content with
 * {@code /rpgitem}, {@code /rpgmob}, and {@code /rpgskill}.
 */
public class RpgIntegrationPlugin extends JavaPlugin {

    private AeternumGenesisAPI api;

    @Override
    public void onEnable() {
        if (!hookAeternumGenesis()) {
            getLogger().severe("AeternumGenesis is not available; disabling RPG integration example.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerCustomEffect();
        registerRpgContent();
        registerCommands();
        getLogger().info("AeternumGenesis RPG integration example enabled.");
    }

    private boolean hookAeternumGenesis() {
        Plugin aeternumGenesis = Bukkit.getPluginManager().getPlugin("AeternumGenesis");
        if (aeternumGenesis == null || !aeternumGenesis.isEnabled()) {
            return false;
        }
        this.api = AeternumGenesisAPI.getInstance();
        return this.api != null;
    }

    /**
     * Registers a custom skill effect that is not built into AeternumGenesis.
     */
    private void registerCustomEffect() {
        api.getSkillAPI().registerEffect("launch_firework", LaunchFireworkEffect::new);
        getLogger().info("Registered custom skill effect 'launch_firework'.");
    }

    /**
     * Registers all RPG content through the AeternumGenesis public API.
     *
     * <p>Each template is built from a {@link MemoryConfiguration} so that the
     * external plugin does not need to ship YAML files for content that should
     * live inside AeternumGenesis.
     */
    private void registerRpgContent() {
        registerItems();
        registerMobs();
        registerSkills();
        registerSpawnRule();
    }

    private void registerItems() {
        MemoryConfiguration fierySword = new MemoryConfiguration();
        fierySword.set("material", "DIAMOND_SWORD");
        fierySword.set("name", "&cFiery Longsword");
        fierySword.set("lore", List.of(
                "&7Forged in the Nether.",
                "&eRight-click to feel the heat."
        ));
        fierySword.set("glow", true);
        fierySword.set("unbreakable", true);
        fierySword.set("attributes", List.of(
                Map.of("type", "ATTACK_DAMAGE", "amount", 9.0, "operation", "ADD_NUMBER", "slot", "HAND"),
                Map.of("type", "ATTACK_SPEED", "amount", 1.6, "operation", "SET_VALUE", "slot", "HAND")
        ));
        fierySword.set("attack_effects", List.of(
                Map.of("type", "ignite", "params", Map.of("ticks", 60))
        ));

        if (api.getItemAPI().registerTemplate("rpg_fiery_longsword", fierySword)) {
            getLogger().info("Registered item template 'rpg_fiery_longsword'.");
        }

        MemoryConfiguration guardianShield = new MemoryConfiguration();
        guardianShield.set("material", "SHIELD");
        guardianShield.set("name", "&bGuardian Shield");
        guardianShield.set("lore", List.of("&7Grants knights unwavering defense."));
        guardianShield.set("attributes", List.of(
                Map.of("type", "MAX_HEALTH", "amount", 6.0, "operation", "ADD_NUMBER", "slot", "OFF_HAND"),
                Map.of("type", "ARMOR", "amount", 4.0, "operation", "ADD_NUMBER", "slot", "OFF_HAND")
        ));

        if (api.getItemAPI().registerTemplate("rpg_guardian_shield", guardianShield)) {
            getLogger().info("Registered item template 'rpg_guardian_shield'.");
        }
    }

    private void registerMobs() {
        MemoryConfiguration fallenKnight = new MemoryConfiguration();
        fallenKnight.set("type", "SKELETON");
        fallenKnight.set("display_name", "&cFallen Knight");
        fallenKnight.set("health", 60.0);
        fallenKnight.set("max_health", 60.0);
        fallenKnight.set("faction", "undead_legion");
        fallenKnight.set("attributes", List.of(
                Map.of("type", "ATTACK_DAMAGE", "amount", 6.0, "operation", "ADD_NUMBER"),
                Map.of("type", "MOVEMENT_SPEED", "amount", 0.12, "operation", "SET_VALUE"),
                Map.of("type", "ARMOR", "amount", 8.0, "operation", "ADD_NUMBER")
        ));
        fallenKnight.set("equipment", Map.of(
                "HAND", Map.of("item", "genesis:rpg_fiery_longsword", "drop_chance", 5.0),
                "OFF_HAND", Map.of("item", "genesis:rpg_guardian_shield", "drop_chance", 5.0),
                "HEAD", Map.of("item", "IRON_HELMET", "drop_chance", 0.0),
                "CHEST", Map.of("item", "IRON_CHESTPLATE", "drop_chance", 0.0),
                "LEGS", Map.of("item", "IRON_LEGGINGS", "drop_chance", 0.0),
                "FEET", Map.of("item", "IRON_BOOTS", "drop_chance", 0.0)
        ));
        fallenKnight.set("ai", Map.of(
                "use_custom_ai", true,
                "remove_default_goals", false,
                "target_range", 24.0,
                "always_aggressive", true,
                "targets", List.of("players", "faction:heroic_order")
        ));
        fallenKnight.set("glowing", true);
        fallenKnight.set("glowing_color", "RED");
        fallenKnight.set("bossbar", Map.of("enabled", true, "title", "&cFallen Knight", "color", "RED", "style", "SOLID"));

        if (api.getMobAPI().registerTemplate("rpg_fallen_knight", fallenKnight)) {
            getLogger().info("Registered mob template 'rpg_fallen_knight'.");
        }

        MemoryConfiguration necromancer = new MemoryConfiguration();
        necromancer.set("type", "EVOKER");
        necromancer.set("display_name", "&5Necromancer");
        necromancer.set("health", 40.0);
        necromancer.set("max_health", 40.0);
        necromancer.set("faction", "undead_legion");
        necromancer.set("attributes", List.of(
                Map.of("type", "MAX_HEALTH", "amount", 40.0, "operation", "SET_VALUE"),
                Map.of("type", "MOVEMENT_SPEED", "amount", 0.22, "operation", "SET_VALUE")
        ));
        necromancer.set("ai", Map.of(
                "use_custom_ai", true,
                "target_range", 32.0,
                "targets", List.of("players")
        ));
        necromancer.set("skills", List.of(
                Map.of("trigger", "on_attack", "skill", "rpg_necrotic_blast", "chance", 25.0)
        ));

        if (api.getMobAPI().registerTemplate("rpg_necromancer", necromancer)) {
            getLogger().info("Registered mob template 'rpg_necromancer'.");
        }
    }

    private void registerSkills() {
        MemoryConfiguration necroticBlast = new MemoryConfiguration();
        necroticBlast.set("cooldown", 8.0);
        necroticBlast.set("target_selector", "TARGET");
        necroticBlast.set("effects", List.of(
                Map.of("type", "damage", "params", Map.of("amount", 8.0)),
                Map.of("type", "potion", "params", Map.of("type", "WITHER", "duration", 100, "amplifier", 1)),
                Map.of("type", "particle", "params", Map.of("type", "SOUL", "count", 30))
        ));

        if (api.getSkillAPI().registerSkill("rpg_necrotic_blast", necroticBlast)) {
            getLogger().info("Registered skill template 'rpg_necrotic_blast'.");
        }

        MemoryConfiguration fireworks = new MemoryConfiguration();
        fireworks.set("cooldown", 2.0);
        fireworks.set("target_selector", "CASTER");
        fireworks.set("effects", List.of(
                Map.of("type", "launch_firework", "params", Map.of("color", "PURPLE", "fade", "GREEN", "power", 2))
        ));

        if (api.getSkillAPI().registerSkill("rpg_fireworks", fireworks)) {
            getLogger().info("Registered skill template 'rpg_fireworks'.");
        }
    }

    private void registerSpawnRule() {
        MemoryConfiguration rule = new MemoryConfiguration();
        rule.set("action", "REPLACE");
        rule.set("type", "rpg_fallen_knight");
        rule.set("replace_types", "SKELETON,WITHER_SKELETON");
        rule.set("chance", 15.0);
        rule.set("priority", 5);
        rule.set("level_range", "3-6");
        rule.set("worlds", "world,world_nether");
        rule.set("conditions", List.of(
                "light_level <= 7",
                "y_below 64"
        ));

        if (api.getSpawnAPI().registerRule("rpg_fallen_knight_replace", rule)) {
            getLogger().info("Registered spawn rule 'rpg_fallen_knight_replace'.");
        }
    }

    private void registerCommands() {
        getCommand("rpgitem").setExecutor(this::onRpgItem);
        getCommand("rpgmob").setExecutor(this::onRpgMob);
        getCommand("rpgskill").setExecutor(this::onRpgSkill);
    }

    private boolean onRpgItem(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /rpgitem <player> <template>", NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + args[0], NamedTextColor.RED));
            return true;
        }
        ItemAPI itemAPI = api.getItemAPI();
        String templateId = args[1].toLowerCase();
        if (!itemAPI.hasTemplate(templateId)) {
            sender.sendMessage(Component.text("Unknown item template: " + templateId, NamedTextColor.RED));
            return true;
        }
        Optional<ItemStack> built = itemAPI.buildItem(templateId);
        if (built.isPresent()) {
            target.getInventory().addItem(built.get());
            sender.sendMessage(Component.text("Gave " + templateId + " to " + target.getName(), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to build item.", NamedTextColor.RED));
        }
        return true;
    }

    private boolean onRpgMob(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by a player.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /rpgmob <template>", NamedTextColor.RED));
            return true;
        }
        String templateId = args[0].toLowerCase();
        Location loc = player.getLocation();
        api.getMobAPI().spawnMob(templateId, loc, 5).ifPresentOrElse(
                mob -> sender.sendMessage(Component.text("Spawned " + templateId + " at level 5.", NamedTextColor.GREEN)),
                () -> sender.sendMessage(Component.text("Failed to spawn " + templateId, NamedTextColor.RED))
        );
        return true;
    }

    private boolean onRpgSkill(@NotNull CommandSender sender, @NotNull Command command,
                               @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by a player.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /rpgskill <skill>", NamedTextColor.RED));
            return true;
        }
        SkillAPI skillAPI = api.getSkillAPI();
        String skillId = args[0].toLowerCase();
        if (!skillAPI.hasSkill(skillId)) {
            sender.sendMessage(Component.text("Unknown skill: " + skillId, NamedTextColor.RED));
            return true;
        }
        if (skillAPI.triggerSkill(player, skillId)) {
            sender.sendMessage(Component.text("Triggered skill " + skillId + ".", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Skill trigger failed or is on cooldown.", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public void onDisable() {
        if (api != null) {
            // Clean up runtime-registered content so it does not persist across reloads.
            api.getItemAPI().unregisterTemplate("rpg_fiery_longsword");
            api.getItemAPI().unregisterTemplate("rpg_guardian_shield");
            api.getMobAPI().unregisterTemplate("rpg_fallen_knight");
            api.getMobAPI().unregisterTemplate("rpg_necromancer");
            api.getSkillAPI().unregisterSkill("rpg_necrotic_blast");
            api.getSkillAPI().unregisterSkill("rpg_fireworks");
            api.getSpawnAPI().unregisterRule("rpg_fallen_knight_replace");
        }
        getLogger().info("AeternumGenesis RPG integration example disabled.");
    }
}
