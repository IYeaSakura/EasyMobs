package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

/**
 * Delay is handled by {@link net.sakurain.mc.aeternumgenesis.skill.SkillExecutor}.
 * This class exists so the effect type can be registered.
 */
public class DelayEffect extends AbstractSkillEffect {

    public DelayEffect() {
        super("delay");
    }

    @Override
    public void execute(SkillContext context) {
        // No-op; scheduling is handled by the executor.
    }
}
