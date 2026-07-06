package net.sakurain.mc.easymobs.skill;

public record SkillBinding(
        String skillId,
        String trigger,
        double chance,
        double cooldownOverride,
        int level
) {

    public SkillBinding {
        if (skillId == null) skillId = "";
        if (trigger == null) trigger = "";
    }

    public static SkillBinding fromMap(java.util.Map<String, Object> map) {
        String skillId = String.valueOf(map.getOrDefault("skill", map.get("skill_id")));
        String trigger = String.valueOf(map.getOrDefault("trigger", ""));
        double chance = toDouble(map.get("chance"), 100.0);
        double cooldownOverride = toDouble(map.get("cooldown_override"), -1.0);
        int level = toInt(map.get("level"), 1);
        return new SkillBinding(skillId, trigger, chance, cooldownOverride, level);
    }

    private static double toDouble(Object obj, double def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int toInt(Object obj, int def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
