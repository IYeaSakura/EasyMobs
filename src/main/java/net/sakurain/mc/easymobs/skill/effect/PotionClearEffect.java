package net.sakurain.mc.easymobs.skill.effect;

import net.sakurain.mc.easymobs.skill.SkillContext;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class PotionClearEffect extends AbstractSkillEffect {

    public PotionClearEffect() {
        super("potion_clear");
    }

    @Override
    public void execute(SkillContext context) {
        String typeName = string("type", "").toLowerCase();
        LivingEntity target = singleTarget(context);
        if (target == null) {
            return;
        }
        if (typeName.isEmpty()) {
            for (PotionEffectType effect : target.getActivePotionEffects().stream().map(org.bukkit.potion.PotionEffect::getType).toList()) {
                target.removePotionEffect(effect);
            }
            return;
        }
        PotionEffectType effectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(typeName));
        if (effectType != null) {
            target.removePotionEffect(effectType);
        }
    }
}
