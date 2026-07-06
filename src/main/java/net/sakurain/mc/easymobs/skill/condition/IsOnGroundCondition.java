package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.entity.LivingEntity;

public class IsOnGroundCondition extends AbstractSkillCondition {

    public IsOnGroundCondition() {
        super("is_on_ground");
    }

    @Override
    public boolean test(SkillContext context) {
        boolean expected = bool("value", true);
        LivingEntity entity = bool("target", false) ? context.getTarget() : context.getCaster();
        if (entity == null) return false;
        return entity.isOnGround() == expected;
    }
}
