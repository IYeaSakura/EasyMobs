package net.sakurain.mc.aeternumgenesis.skill.condition;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;

import java.util.List;

public class WorldCondition extends AbstractSkillCondition {

    public WorldCondition() {
        super("world");
    }

    @Override
    public boolean test(SkillContext context) {
        if (context.getCaster() == null || context.getCaster().getWorld() == null) return false;
        List<String> worlds = stringList("worlds");
        if (worlds.isEmpty()) return true;
        return worlds.contains(context.getCaster().getWorld().getName().toLowerCase());
    }
}
