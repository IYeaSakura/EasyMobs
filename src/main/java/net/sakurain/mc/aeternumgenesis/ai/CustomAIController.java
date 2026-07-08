package net.sakurain.mc.aeternumgenesis.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import net.sakurain.mc.aeternumgenesis.mob.MobTracker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CustomAIController {

    private static CustomAIController instance;

    private final AeternumGenesisPlugin plugin;
    private final Map<UUID, AggroTable> aggroTables = new ConcurrentHashMap<>();
    private final Map<UUID, Long> switchCooldowns = new ConcurrentHashMap<>();

    private CustomAIController(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    public static synchronized CustomAIController getInstance() {
        if (instance == null) {
            instance = new CustomAIController(AeternumGenesisPlugin.getInstance());
        }
        return instance;
    }

    public void setupAI(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        CustomMobTemplate.AIConfig ai = template.getAi();
        if (ai == null) return;

        if (ai.removeDefaultGoals()) {
            Bukkit.getMobGoals().removeAllGoals(mob);
        }

        Bukkit.getMobGoals().addGoal(mob, 1, new SmartTargetGoal(mob, template));
        Bukkit.getMobGoals().addGoal(mob, 2, new SmartAttackGoal(mob, template));

        if (ai.behavior() != null && ai.behavior().circleTarget()) {
            Bukkit.getMobGoals().addGoal(mob, 3, new CircleTargetGoal(mob, template));
        }

        CustomMobTemplate.BreakDoorConfig bd = template.getBreakDoor();
        if (bd != null && bd.enabled()) {
            Bukkit.getMobGoals().addGoal(mob, 4, new BreakDoorGoal(mob, template));
        }

        CustomMobTemplate.TargetingStrategy strategy = ai.targetingStrategy();
        int memory = strategy != null ? strategy.maxTargetsMemory() : 5;
        aggroTables.put(mob.getUniqueId(), new AggroTable(memory));
        switchCooldowns.put(mob.getUniqueId(), 0L);
    }

    public void tick(@NotNull LivingEntity entity, @NotNull CustomMobTemplate template) {
        if (!(entity instanceof Mob mob)) return;
        CustomMobTemplate.AIConfig ai = template.getAi();
        if (ai == null || !ai.useCustomAi()) return;

        AggroTable aggro = aggroTables.get(mob.getUniqueId());
        if (aggro != null) aggro.decay(0.95);

        evaluateTargetSwitch(mob, template);
        checkLeashRange(mob, template);
    }

    public void recordDamage(@NotNull Mob mob, @NotNull LivingEntity attacker, double damage) {
        AggroTable aggro = aggroTables.get(mob.getUniqueId());
        if (aggro != null) aggro.addThreat(attacker.getUniqueId(), damage);
    }

    private void evaluateTargetSwitch(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        CustomMobTemplate.AIConfig ai = template.getAi();
        CustomMobTemplate.TargetingStrategy strategy = ai != null ? ai.targetingStrategy() : null;
        if (strategy == null) return;

        long currentTick = Bukkit.getCurrentTick();
        long lastSwitch = switchCooldowns.getOrDefault(mob.getUniqueId(), 0L);
        if (currentTick - lastSwitch < strategy.switchInterval()) return;

        LivingEntity bestTarget = findBestTarget(mob, template);
        LivingEntity currentTarget = mob.getTarget();

        if (bestTarget == null) {
            if (currentTarget != null) mob.setTarget(null);
            return;
        }

        if (currentTarget == null || !currentTarget.equals(bestTarget)) {
            double currentScore = scoreTarget(mob, currentTarget, strategy, template);
            double bestScore = scoreTarget(mob, bestTarget, strategy, template);
            if (bestScore >= currentScore * (1 + strategy.switchThreshold())) {
                mob.setTarget(bestTarget);
                switchCooldowns.put(mob.getUniqueId(), currentTick);
            }
        }
    }

    private LivingEntity findBestTarget(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        CustomMobTemplate.AIConfig ai = template.getAi();
        CustomMobTemplate.TargetingStrategy strategy = ai != null ? ai.targetingStrategy() : null;
        if (strategy == null) return null;

        CustomMobTemplate.SensesConfig senses = template.getSenses();
        double range = ai.targetRange();
        Location loc = mob.getLocation();
        List<String> targets = ai.targets();
        boolean hasCustomTargets = targets != null && !targets.isEmpty();
        List<LivingEntity> candidates = new ArrayList<>();

        for (Entity e : mob.getWorld().getNearbyEntities(loc, range, range, range)) {
            if (!(e instanceof LivingEntity target)) continue;
            if (target.equals(mob)) continue;
            if (target.isDead()) continue;
            if (isSameFaction(template, target)) continue;
            if (hasCustomTargets) {
                if (!matchesTarget(target, targets)) continue;
            } else {
                if (strategy.preferPlayers() && !(target instanceof Player)) continue;
            }
            if (senses != null && !SenseSystem.canSense(mob, target, senses)) continue;
            candidates.add(target);
        }
        if (candidates.isEmpty()) return null;

        String type = strategy.type().toLowerCase();
        return switch (type) {
            case "lowest_hp" -> candidates.stream()
                    .min(Comparator.comparingDouble(LivingEntity::getHealth)).orElse(null);
            case "highest_threat" -> {
                AggroTable aggro = aggroTables.get(mob.getUniqueId());
                yield candidates.stream()
                        .max(Comparator.comparingDouble(e -> aggro != null ? aggro.getThreat(e.getUniqueId()) : 0))
                        .orElse(null);
            }
            case "random" -> candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            case "first_sight" -> candidates.get(0);
            default -> candidates.stream()
                    .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(loc))).orElse(null);
        };
    }

    private boolean isSameFaction(@NotNull CustomMobTemplate template, @NotNull LivingEntity candidate) {
        String faction = template.getFaction();
        if (faction == null || faction.isEmpty()) return false;
        CustomMobTemplate other = MobTracker.getInstance().getTemplate(candidate);
        if (other == null) return false;
        return faction.equalsIgnoreCase(other.getFaction());
    }

    private boolean matchesTarget(@NotNull LivingEntity candidate, @NotNull List<String> targets) {
        for (String target : targets) {
            String lower = target.toLowerCase();
            switch (lower) {
                case "players" -> { if (candidate instanceof Player) return true; }
                case "mobs" -> { if (candidate instanceof Mob) return true; }
                default -> {
                    if (lower.startsWith("faction:")) {
                        String faction = lower.substring(8);
                        CustomMobTemplate t = MobTracker.getInstance().getTemplate(candidate);
                        if (t != null && faction.equalsIgnoreCase(t.getFaction())) return true;
                    } else {
                        try {
                            org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf(target.toUpperCase());
                            if (candidate.getType() == type) return true;
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        }
        return false;
    }

    private double scoreTarget(@NotNull Mob mob, @Nullable LivingEntity target,
                               @NotNull CustomMobTemplate.TargetingStrategy strategy,
                               @NotNull CustomMobTemplate template) {
        if (target == null || target.isDead()) return 0;
        if (!mob.getWorld().equals(target.getWorld())) return 0;
        double score;
        double dist = mob.getLocation().distance(target.getLocation());
        String type = strategy.type().toLowerCase();
        switch (type) {
            case "lowest_hp" -> score = 1.0 / (target.getHealth() + 1);
            case "highest_threat" -> {
                AggroTable aggro = aggroTables.get(mob.getUniqueId());
                score = aggro != null ? aggro.getThreat(target.getUniqueId()) : 0;
            }
            case "random" -> score = Math.random();
            case "first_sight" -> score = 1.0;
            default -> score = 1.0 / (dist + 1);
        }
        double range = template.getAi().targetRange();
        if (dist > range * 0.7) score *= 0.5;
        return score;
    }

    private void checkLeashRange(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        CustomMobTemplate.BehaviorConfig behavior = template.getAi() != null ? template.getAi().behavior() : null;
        if (behavior == null) return;
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        if (!mob.getWorld().equals(target.getWorld())) {
            mob.setTarget(null);
            return;
        }
        double dist = mob.getLocation().distance(target.getLocation());
        if (dist > behavior.leashRange()) {
            mob.setTarget(null);
        }
    }

    public void cleanup(@NotNull UUID uuid) {
        aggroTables.remove(uuid);
        switchCooldowns.remove(uuid);
    }

    public static abstract class BaseGoal implements Goal<Mob> {

        protected final Mob mob;
        protected final CustomMobTemplate template;

        protected BaseGoal(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
            this.mob = mob;
            this.template = template;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }
}
