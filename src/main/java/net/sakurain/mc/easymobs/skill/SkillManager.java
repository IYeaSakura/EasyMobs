package net.sakurain.mc.easymobs.skill;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import net.sakurain.mc.easymobs.skill.condition.*;
import net.sakurain.mc.easymobs.skill.effect.*;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class SkillManager {

    private final EasyMobsPlugin plugin;
    private final SkillEffectRegistry effectRegistry = new SkillEffectRegistry();
    private final SkillConditionRegistry conditionRegistry = new SkillConditionRegistry();
    private final SkillExecutor executor;
    private final Map<String, SkillTemplate> templates = new HashMap<>();
    private final Map<String, List<SkillBinding>> bindingsByTrigger = new HashMap<>();

    public SkillManager(Map<String, YamlConfiguration> skillConfigs) {
        this.plugin = EasyMobsPlugin.getInstance();
        registerDefaultEffects();
        registerDefaultConditions();
        this.executor = new SkillExecutor(plugin, effectRegistry, conditionRegistry);
        ConditionParser.setDefaultRegistry(conditionRegistry);
        loadTemplates(skillConfigs);
    }

    private void registerDefaultEffects() {
        registerEffect("damage", DamageEffect::new);
        registerEffect("damage_percent", DamagePercentEffect::new);
        registerEffect("heal", HealEffect::new);
        registerEffect("heal_percent", HealPercentEffect::new);
        registerEffect("potion", PotionEffectEffect::new);
        registerEffect("potion_clear", PotionClearEffect::new);
        registerEffect("teleport", TeleportEffect::new);
        registerEffect("summon", SummonEffect::new);
        registerEffect("particle", ParticleEffect::new);
        registerEffect("sound", SoundEffect::new);
        registerEffect("lightning", LightningEffect::new);
        registerEffect("explosion", ExplosionEffect::new);
        registerEffect("ignite", IgniteEffect::new);
        registerEffect("extinguish", ExtinguishEffect::new);
        registerEffect("knockback", KnockbackEffect::new);
        registerEffect("message", MessageEffect::new);
        registerEffect("title", TitleEffect::new);
        registerEffect("actionbar", ActionBarEffect::new);
        registerEffect("drop_item", DropItemEffect::new);
        registerEffect("execute_command", ExecuteCommandEffect::new);
        registerEffect("delay", DelayEffect::new);
    }

    private void registerDefaultConditions() {
        registerCondition("health_percent", HealthPercentCondition::new);
        registerCondition("health", HealthCondition::new);
        registerCondition("target_health_percent", TargetHealthPercentCondition::new);
        registerCondition("target_health", TargetHealthCondition::new);
        registerCondition("chance", ChanceCondition::new);
        registerCondition("target_type", TargetTypeCondition::new);
        registerCondition("target_distance", TargetDistanceCondition::new);
        registerCondition("time_of_day", TimeOfDayCondition::new);
        registerCondition("weather", WeatherCondition::new);
        registerCondition("world", WorldCondition::new);
        registerCondition("biome", BiomeCondition::new);
        registerCondition("light_level", LightLevelCondition::new);
        registerCondition("y_above", YAboveCondition::new);
        registerCondition("y_below", YBelowCondition::new);
        registerCondition("has_potion", HasPotionCondition::new);
        registerCondition("is_on_ground", IsOnGroundCondition::new);
        registerCondition("mobs_in_radius", MobsInRadiusCondition::new);
    }

    private void loadTemplates(Map<String, YamlConfiguration> skillConfigs) {
        templates.clear();
        for (Map.Entry<String, YamlConfiguration> entry : skillConfigs.entrySet()) {
            String fileName = entry.getKey();
            String id = stripExtension(fileName);
            YamlConfiguration config = entry.getValue();
            if (config.contains("id")) {
                id = config.getString("id", id);
            }
            try {
                SkillTemplate template = SkillTemplate.fromConfig(id, config);
                templates.put(id.toLowerCase(), template);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load skill template " + fileName + ": " + e.getMessage());
            }
        }
    }

    private String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index > 0 ? name.substring(0, index) : name;
    }

    public void handleTrigger(String trigger, LivingEntity caster, LivingEntity target, double damage) {
        if (caster == null || trigger == null) {
            return;
        }
        List<SkillBinding> bindings = bindingsByTrigger.get(trigger.toLowerCase());
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        for (SkillBinding binding : bindings) {
            if (ThreadLocalRandom.current().nextDouble() * 100.0 >= binding.chance()) {
                continue;
            }
            triggerBinding(caster, target, damage, binding);
        }
    }

    private void triggerBinding(LivingEntity caster, LivingEntity target, double damage, SkillBinding binding) {
        SkillTemplate template = templates.get(binding.skillId().toLowerCase());
        if (template == null) {
            return;
        }
        double cooldown = binding.cooldownOverride() >= 0 ? binding.cooldownOverride() : template.cooldown();
        if (SkillCooldownManager.isOnCooldown(caster, template.id())) {
            executeOnCooldownSkill(caster, target, damage, template);
            return;
        }
        executeSkill(caster, target, damage, template);
        if (cooldown > 0) {
            SkillCooldownManager.setCooldown(caster, template.id(), cooldown);
        }
    }

    public void triggerSkill(LivingEntity caster, LivingEntity target, String skillId) {
        triggerSkill(caster, target, skillId, 0.0);
    }

    public void triggerSkill(LivingEntity caster, LivingEntity target, String skillId, double damage) {
        if (caster == null || skillId == null) {
            return;
        }
        SkillTemplate template = getTemplate(skillId);
        if (template == null) {
            return;
        }
        if (SkillCooldownManager.isOnCooldown(caster, template.id())) {
            executeOnCooldownSkill(caster, target, damage, template);
            return;
        }
        executeSkill(caster, target, damage, template);
        if (template.cooldown() > 0) {
            SkillCooldownManager.setCooldown(caster, template.id(), template.cooldown());
        }
    }

    private void executeSkill(LivingEntity caster, LivingEntity target, double damage, SkillTemplate template) {
        Location origin = target != null ? target.getLocation() : caster.getLocation();
        SkillContext context = new SkillContext(plugin, caster, target, origin, damage);
        executor.execute(template, context);
    }

    private void executeOnCooldownSkill(LivingEntity caster, LivingEntity target, double damage, SkillTemplate template) {
        if (template.onCooldownSkill() == null || template.onCooldownSkill().isEmpty()) {
            return;
        }
        SkillTemplate fallback = getTemplate(template.onCooldownSkill());
        if (fallback == null) {
            return;
        }
        Location origin = target != null ? target.getLocation() : caster.getLocation();
        SkillContext context = new SkillContext(plugin, caster, target, origin, damage);
        executor.execute(fallback, context);
    }

    public void addBinding(SkillBinding binding) {
        if (binding == null || binding.trigger().isEmpty() || binding.skillId().isEmpty()) {
            return;
        }
        bindingsByTrigger.computeIfAbsent(binding.trigger().toLowerCase(), k -> new ArrayList<>()).add(binding);
    }

    public void addBindings(List<SkillBinding> bindings) {
        if (bindings == null) {
            return;
        }
        for (SkillBinding binding : bindings) {
            addBinding(binding);
        }
    }

    public SkillTemplate getTemplate(String id) {
        return id != null ? templates.get(id.toLowerCase()) : null;
    }

    public boolean hasSkill(String id) {
        return id != null && templates.containsKey(id.toLowerCase());
    }

    public Set<String> getAllSkillIds() {
        return Set.copyOf(templates.keySet());
    }

    public int getTemplateCount() {
        return templates.size();
    }

    public boolean isOnCooldown(LivingEntity entity, String skillId) {
        return SkillCooldownManager.isOnCooldown(entity, skillId);
    }

    public double getRemainingCooldown(LivingEntity entity, String skillId) {
        return SkillCooldownManager.getRemainingCooldown(entity, skillId);
    }

    public double getSkillCooldown(String skillId) {
        SkillTemplate template = getTemplate(skillId);
        return template != null ? template.cooldown() : 0.0;
    }

    public void setCooldown(LivingEntity entity, String skillId, double seconds) {
        SkillCooldownManager.setCooldown(entity, skillId, seconds);
    }

    public void clearAllCooldowns(LivingEntity entity) {
        SkillCooldownManager.clearAllCooldowns(entity);
    }

    public void registerEffect(String type, Supplier<net.sakurain.mc.easymobs.skill.effect.SkillEffect> supplier) {
        effectRegistry.register(type, supplier);
    }

    public void registerCondition(String type, Supplier<SkillCondition> supplier) {
        conditionRegistry.register(type, supplier);
    }

    public Set<String> getEffectTypes() {
        return effectRegistry.getTypes();
    }

    public Set<String> getConditionTypes() {
        return conditionRegistry.getTypes();
    }

    public void playParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, Object data) {
        if (location == null || location.getWorld() == null || particle == null) {
            return;
        }
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }

    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
        if (location == null || location.getWorld() == null || sound == null) {
            return;
        }
        location.getWorld().playSound(location, sound, category != null ? category : SoundCategory.MASTER, volume, pitch);
    }

    public void sendActionBar(Player player, String text) {
        if (player == null) {
            return;
        }
        net.sakurain.mc.easymobs.util.MessageUtil.sendActionBar(player, text);
    }

    public SkillEffectRegistry getEffectRegistry() {
        return effectRegistry;
    }

    public SkillConditionRegistry getConditionRegistry() {
        return conditionRegistry;
    }
}
