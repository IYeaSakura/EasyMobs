package net.sakurain.mc.aeternumgenesis.examples.rpg;

import net.sakurain.mc.aeternumgenesis.api.SkillEffect;
import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Example custom skill effect registered by the RPG plugin at runtime.
 * Launches a firework at the skill's origin location.
 */
public class LaunchFireworkEffect implements SkillEffect {

    @Override
    @NotNull
    public String getType() {
        return "launch_firework";
    }

    private Color color = Color.RED;
    private Color fade = Color.ORANGE;
    private int power = 1;

    @Override
    public void loadParameters(@NotNull Map<String, Object> params) {
        String colorName = String.valueOf(params.getOrDefault("color", "RED")).toUpperCase();
        String fadeName = String.valueOf(params.getOrDefault("fade", "ORANGE")).toUpperCase();
        try {
            this.color = (Color) Color.class.getField(colorName).get(null);
        } catch (ReflectiveOperationException e) {
            this.color = Color.RED;
        }
        try {
            this.fade = (Color) Color.class.getField(fadeName).get(null);
        } catch (ReflectiveOperationException e) {
            this.fade = Color.ORANGE;
        }
        Object powerObj = params.get("power");
        if (powerObj instanceof Number n) {
            this.power = n.intValue();
        }
    }

    @Override
    public void execute(@NotNull SkillContext context) {
        Location loc = context.getOrigin();
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(color)
                .withFade(fade)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build());
        meta.setPower(Math.max(1, power));
        firework.setFireworkMeta(meta);
    }
}
