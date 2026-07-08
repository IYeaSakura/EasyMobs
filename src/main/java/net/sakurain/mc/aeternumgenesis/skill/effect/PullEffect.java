package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class PullEffect extends AbstractSkillEffect {

    private static final double EPSILON = 1e-9;

    public PullEffect() {
        super("pull");
    }

    @Override
    public void execute(SkillContext context) {
        LivingEntity target = singleTarget(context);
        if (target == null) {
            return;
        }
        double horizontal = number("horizontal", 0.8);
        double vertical = number("vertical", 0.0);

        Vector direction;
        if (context.getCaster() != null) {
            direction = context.getCaster().getLocation().toVector().subtract(target.getLocation().toVector());
        } else if (context.getOrigin() != null) {
            direction = context.getOrigin().toVector().subtract(target.getLocation().toVector());
        } else {
            direction = target.getLocation().getDirection().multiply(-1);
        }

        if (direction.lengthSquared() < EPSILON) {
            direction = new Vector(1, 0, 0);
        }
        direction.setY(0).normalize();
        direction.multiply(horizontal).setY(vertical);
        target.setVelocity(direction);
    }
}
