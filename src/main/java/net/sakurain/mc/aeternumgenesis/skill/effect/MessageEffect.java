package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.entity.Player;

public class MessageEffect extends AbstractSkillEffect {

    public MessageEffect() {
        super("message");
    }

    @Override
    public void execute(SkillContext context) {
        String text = string("text", "");
        if (text.isEmpty()) {
            return;
        }
        Player player = context.getTargetPlayer();
        if (player == null) {
            player = context.getCasterPlayer();
        }
        if (player == null) {
            return;
        }
        player.sendMessage(MessageUtil.color(replace(text, context)));
    }

    private String replace(String text, SkillContext context) {
        if (context.getCaster() != null) {
            text = text.replace("<caster>", context.getCaster().getName());
        }
        if (context.getTarget() != null) {
            text = text.replace("<target>", context.getTarget().getName());
        }
        return text;
    }
}
