package net.sakurain.mc.aeternumgenesis.ai;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class CircleTargetGoal extends CustomAIController.BaseGoal {

    public static final GoalKey<Mob> KEY = GoalKey.of(Mob.class,
            new NamespacedKey("genesis", "circle_target"));

    private final double radius;
    private double angle = 0;

    public CircleTargetGoal(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        super(mob, template);
        CustomMobTemplate.BehaviorConfig behavior = template.getAi() != null ? template.getAi().behavior() : null;
        this.radius = behavior != null ? behavior.circleRadius() : 3.0;
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
        angle += 0.15;
        Location center = target.getLocation();
        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);
        Location circleLoc = new Location(center.getWorld(), x, center.getY(), z);
        mob.getPathfinder().moveTo(circleLoc, 1.0);
        mob.lookAt(target);
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
