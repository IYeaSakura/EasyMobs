package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class KnockbackEffect extends AbstractSkillEffect {

    public KnockbackEffect() {
        super("knockback");
    }

    @Override
    public void execute(SkillContext context) {
        LivingEntity target = singleTarget(context);
        if (target == null) {
            return;
        }
        double horizontal = number("horizontal", 1.0);
        double vertical = number("vertical", 0.5);

        Vector direction;
        if (context.getCaster() != null) {
            direction = target.getLocation().toVector().subtract(context.getCaster().getLocation().toVector());
        } else if (context.getOrigin() != null) {
            direction = target.getLocation().toVector().subtract(context.getOrigin().toVector());
        } else {
            direction = target.getLocation().getDirection().multiply(-1);
        }

        if (direction.lengthSquared() == 0) {
            direction = new Vector(1, 0, 0);
        }
        direction.setY(0).normalize();
        direction.multiply(horizontal).setY(vertical);
        target.setVelocity(direction);
    }
}
