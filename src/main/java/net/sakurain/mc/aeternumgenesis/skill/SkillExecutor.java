package net.sakurain.mc.aeternumgenesis.skill;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.skill.condition.ConditionParser;
import net.sakurain.mc.aeternumgenesis.skill.condition.SkillCondition;
import net.sakurain.mc.aeternumgenesis.skill.effect.SkillEffect;
import net.sakurain.mc.aeternumgenesis.util.SchedulerUtil;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SkillExecutor {

    private final AeternumGenesisPlugin plugin;
    private final SkillEffectRegistry effectRegistry;
    private final SkillConditionRegistry conditionRegistry;

    public SkillExecutor(AeternumGenesisPlugin plugin, SkillEffectRegistry effectRegistry, SkillConditionRegistry conditionRegistry) {
        this.plugin = plugin;
        this.effectRegistry = effectRegistry;
        this.conditionRegistry = conditionRegistry;
    }

    public void execute(SkillTemplate template, SkillContext context) {
        if (context.getCaster() == null) {
            return;
        }

        List<Map<String, Object>> casterConditions = new ArrayList<>();
        casterConditions.addAll(template.conditions());
        casterConditions.addAll(template.triggerConditions());
        if (!testConditions(casterConditions, context)) {
            return;
        }

        List<LivingEntity> targets = context.resolveTargets(template.targetSelector(), template.radius());
        if (targets.isEmpty() && template.cancelIfNoTargets()) {
            return;
        }

        for (LivingEntity target : targets) {
            SkillContext targetContext = context.withTarget(target);
            if (!testConditions(template.targetConditions(), targetContext)) {
                continue;
            }
            applyEffects(template.effects(), 0, targetContext);
        }
    }

    private boolean testConditions(List<Map<String, Object>> conditions, SkillContext context) {
        for (Map<String, Object> map : conditions) {
            SkillCondition condition = ConditionParser.parse(map, conditionRegistry);
            if (condition == null) {
                continue;
            }
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    private void applyEffects(List<SkillEffectConfig> effects, int start, SkillContext context) {
        for (int i = start; i < effects.size(); i++) {
            SkillEffectConfig config = effects.get(i);
            if ("delay".equalsIgnoreCase(config.type())) {
                long ticks = Math.max(0, (long) toDouble(config.parameters().get("ticks"), 0.0));
                final int nextIndex = i + 1;
                SchedulerUtil.runLater(ticks, () -> applyEffects(effects, nextIndex, context));
                return;
            }
            config.createEffect(effectRegistry).ifPresent(effect -> runEffect(effect, context));
        }
    }

    private void runEffect(SkillEffect effect, SkillContext context) {
        try {
            effect.execute(context);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to execute skill effect " + effect.getType(), e);
        }
    }

    private static double toDouble(Object value, double def) {
        if (value == null) return def;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
