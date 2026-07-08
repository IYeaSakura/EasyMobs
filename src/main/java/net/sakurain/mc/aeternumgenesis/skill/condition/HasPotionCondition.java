package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class HasPotionCondition extends AbstractSkillCondition {

    public HasPotionCondition() {
        super("has_potion");
    }

    @Override
    public boolean test(SkillContext context) {
        String typeName = string("type", "").toLowerCase();
        if (typeName.isEmpty()) return false;
        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(typeName));
        if (effectType == null) return false;
        LivingEntity entity = bool("target", false) ? context.getTarget() : context.getCaster();
        if (entity == null) return false;
        return entity.hasPotionEffect(effectType);
    }
}
