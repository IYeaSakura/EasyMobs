package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class TargetTypeCondition extends AbstractSkillCondition {

    public TargetTypeCondition() {
        super("target_type");
    }

    @Override
    public boolean test(SkillContext context) {
        LivingEntity target = context.getTarget();
        if (target == null) return false;
        List<String> types = stringList("types");
        if (types.isEmpty()) return true;
        return types.contains(target.getType().name().toLowerCase());
    }
}
