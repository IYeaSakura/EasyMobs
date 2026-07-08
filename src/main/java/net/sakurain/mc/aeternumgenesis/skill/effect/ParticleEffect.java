package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;

public class ParticleEffect extends AbstractSkillEffect {

    public ParticleEffect() {
        super("particle");
    }

    @Override
    public void execute(SkillContext context) {
        String particleName = string("particle", "").toLowerCase();
        if (particleName.isEmpty()) {
            return;
        }
        Particle particle = parseParticle(particleName);
        if (particle == null) {
            return;
        }

        Location location = location(context);
        if (location == null || location.getWorld() == null) {
            return;
        }

        int count = Math.max(0, Math.min(integer("count", 1), 1000));
        double offsetX = number("offset_x", 0.0);
        double offsetY = number("offset_y", 0.0);
        double offsetZ = number("offset_z", 0.0);
        double speed = number("speed", 0.0);

        Object data = null;
        String particleNameResolved = particle.name();
        if (particleNameResolved.equals("DUST") || particleNameResolved.equals("REDSTONE")) {
            Color color = parseColor(string("dust_color", "FFFFFF"));
            float size = (float) number("dust_size", 1.0);
            data = new Particle.DustOptions(color, size);
        }

        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }

    private Particle parseParticle(String name) {
        try {
            if (name.equals("redstone")) {
                Particle dust = Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft("dust"));
                if (dust != null) {
                    return dust;
                }
            }
            return Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(name));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Color parseColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            int rgb = Integer.parseInt(hex, 16);
            return Color.fromRGB(rgb);
        } catch (Exception e) {
            return Color.RED;
        }
    }
}
