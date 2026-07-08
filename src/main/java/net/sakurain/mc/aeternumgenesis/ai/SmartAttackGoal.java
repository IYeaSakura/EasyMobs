package net.sakurain.mc.aeternumgenesis.ai;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class SmartAttackGoal extends CustomAIController.BaseGoal {

    public static final GoalKey<Mob> KEY = GoalKey.of(Mob.class,
            new NamespacedKey("genesis", "smart_attack"));

    private int attackCooldown = 0;

    public SmartAttackGoal(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        super(mob, template);
    }

    @Override
    public boolean shouldActivate() {
        return mob.getTarget() != null && !mob.getTarget().isDead();
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        if (attackCooldown > 0) attackCooldown--;

        double dist = mob.getLocation().distance(target.getLocation());
        CustomMobTemplate.BehaviorConfig behavior = template.getAi() != null ? template.getAi().behavior() : null;
        double keepDistance = behavior != null && behavior.keepDistance() ? 8.0 : 0;

        if (keepDistance > 0) {
            if (dist < keepDistance - 1) retreat(target);
            else if (dist > keepDistance + 1) mob.getPathfinder().moveTo(target, 1.2);
            else if (behavior != null && behavior.strafe()) strafe(target);
        } else {
            if (dist > 2.0) mob.getPathfinder().moveTo(target, 1.0);
        }
        mob.lookAt(target);
    }

    private void retreat(@NotNull LivingEntity target) {
        Location mobLoc = mob.getLocation();
        Location targetLoc = target.getLocation();
        Vector away = mobLoc.toVector().subtract(targetLoc.toVector()).normalize().multiply(2);
        Location retreatLoc = mobLoc.clone().add(away);
        mob.getPathfinder().moveTo(retreatLoc, 1.0);
    }

    private void strafe(@NotNull LivingEntity target) {
        Location mobLoc = mob.getLocation();
        Location targetLoc = target.getLocation();
        Vector toTarget = targetLoc.toVector().subtract(mobLoc.toVector()).normalize();
        Vector perpendicular = new Vector(-toTarget.getZ(), 0, toTarget.getX()).normalize();
        if (Math.random() > 0.5) perpendicular.multiply(-1);
        Location strafeLoc = mobLoc.clone().add(perpendicular.multiply(2));
        mob.getPathfinder().moveTo(strafeLoc, 0.8);
    }

    @Override
    @NotNull
    public GoalKey<Mob> getKey() {
        return KEY;
    }

    @Override
    @NotNull
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
}
