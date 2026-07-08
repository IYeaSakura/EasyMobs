package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.util.MessageUtil;
import org.bukkit.entity.Player;

public class ActionBarEffect extends AbstractSkillEffect {

    public ActionBarEffect() {
        super("actionbar");
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
        MessageUtil.sendActionBar(player, text);
    }
}
