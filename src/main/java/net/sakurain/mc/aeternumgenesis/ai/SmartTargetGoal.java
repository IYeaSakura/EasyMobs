package net.sakurain.mc.aeternumgenesis.ai;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.mob.CustomMobTemplate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class SmartTargetGoal extends CustomAIController.BaseGoal {

    public static final GoalKey<Mob> KEY = GoalKey.of(Mob.class,
            new NamespacedKey("genesis", "smart_target"));

    public SmartTargetGoal(@NotNull Mob mob, @NotNull CustomMobTemplate template) {
        super(mob, template);
    }

    @Override
    public boolean shouldActivate() {
        CustomMobTemplate.AIConfig ai = template.getAi();
        return ai != null && ai.useCustomAi();
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        if (Bukkit.getCurrentTick() % 20 != 0) return;
        CustomAIController.getInstance().tick(mob, template);
    }

    @Override
    @NotNull
    public GoalKey<Mob> getKey() {
        return KEY;
    }

    @Override
    @NotNull
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }
}
