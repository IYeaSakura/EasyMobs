package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Bukkit;

import java.util.List;

public class ExecuteCommandEffect extends AbstractSkillEffect {

    public ExecuteCommandEffect() {
        super("execute_command");
    }

    @Override
    public void execute(SkillContext context) {
        String command = string("command", "");
        if (command.isEmpty()) {
            return;
        }

        AeternumGenesisPlugin plugin = AeternumGenesisPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("skills.allow-execute-command", false)) {
            return;
        }

        List<String> allowed = plugin.getConfig().getStringList("skills.allowed-execute-commands");
        String base = command.trim();
        if (base.startsWith("/")) {
            base = base.substring(1);
        }
        int space = base.indexOf(' ');
        if (space > 0) {
            base = base.substring(0, space);
        }
        if (!allowed.isEmpty() && !allowed.contains(base)) {
            plugin.getLogger().warning("Blocked execute_command skill: '" + base + "' is not in skills.allowed-execute-commands");
            return;
        }

        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        command = replacePlaceholders(command, context);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring(1));
    }

    private String replacePlaceholders(String command, SkillContext context) {
        if (context.getCaster() != null) {
            command = command.replace("<caster>", context.getCaster().getName());
            command = command.replace("<caster_uuid>", context.getCaster().getUniqueId().toString());
        }
        if (context.getTarget() != null) {
            command = command.replace("<target>", context.getTarget().getName());
            command = command.replace("<target_uuid>", context.getTarget().getUniqueId().toString());
        }
        command = command.replace("<x>", String.valueOf(context.getOrigin() != null ? context.getOrigin().getX() : 0));
        command = command.replace("<y>", String.valueOf(context.getOrigin() != null ? context.getOrigin().getY() : 0));
        command = command.replace("<z>", String.valueOf(context.getOrigin() != null ? context.getOrigin().getZ() : 0));
        return command;
    }
}
