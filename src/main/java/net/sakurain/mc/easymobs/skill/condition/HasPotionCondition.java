package net.sakurain.mc.easymobs.skill.condition;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class HasPotionCondition extends AbstractSkillCondition {

    public HasPotionCondition() {
        super("has_potion");
    }

    @Override
    public boolean test(SkillContext context) {
        String typeName = string("type", "").toUpperCase();
        if (typeName.isEmpty()) return false;
        PotionEffectType effectType = PotionEffectType.getByName(typeName);
        if (effectType == null) return false;
        LivingEntity entity = bool("target", false) ? context.getTarget() : context.getCaster();
        if (entity == null) return false;
        return entity.hasPotionEffect(effectType);
    }
}
