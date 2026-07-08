package net.sakurain.mc.aeternumgenesis.ai;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class BreakDoorGoal extends CustomAIController.BaseGoal {

    public static final GoalKey<Mob> KEY = GoalKey.of(Mob.class,
            new NamespacedKey("genesis", "break_door"));

    private static final int BREAK_TIME = 120;

    private final CustomMobTemplate.BreakDoorConfig config;
    private Block targetDoor;
    private int breakProgress;

    public BreakDoorGoal(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        super(mob, template);
        this.config = template.getBreakDoor();
    }

    @Override
    public boolean shouldActivate() {
        if (config == null || !config.enabled()) return false;
        if (Math.random() > config.chance()) return false;
        LivingEntity target = mob.getTarget();
        if (target == null) return false;
        Location eye = mob.getEyeLocation();
        Location targetLoc = target.getLocation();
        BlockFace direction = getFacingDirection(eye, targetLoc);
        Block front = eye.clone().add(direction.getModX(), 0, direction.getModZ()).getBlock();
        if (isDoor(front)) {
            targetDoor = front;
            return true;
        }
        Block below = front.getRelative(BlockFace.DOWN);
        if (isDoor(below)) {
            targetDoor = below;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return targetDoor != null && isDoor(targetDoor) && mob.getTarget() != null;
    }

    @Override
    public void start() {
        breakProgress = 0;
    }

    @Override
    public void tick() {
        if (targetDoor == null) return;
        breakProgress++;
        mob.lookAt(targetDoor.getLocation().add(0.5, 0.5, 0.5));
        if (breakProgress >= BREAK_TIME) {
            targetDoor.breakNaturally();
            targetDoor = null;
            breakProgress = 0;
        } else if (breakProgress % 20 == 0) {
            mob.getWorld().playSound(targetDoor.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 1.0f);
        }
    }

    @Override
    public void stop() {
        targetDoor = null;
        breakProgress = 0;
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

    private boolean isDoor(@NotNull Block block) {
        Material type = block.getType();
        return type.name().contains("DOOR") && !type.name().contains("TRAP");
    }

    @NotNull
    private BlockFace getFacingDirection(@NotNull Location from, @NotNull Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }
}
