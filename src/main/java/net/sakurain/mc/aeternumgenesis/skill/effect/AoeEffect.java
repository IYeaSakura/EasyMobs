package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import net.sakurain.mc.aeternumgenesis.skill.TargetType;
import net.sakurain.mc.aeternumgenesis.util.ConfigParseUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Applies damage, healing, knockback and potion effects to all entities in an area.
 */
public class AoeEffect extends AbstractSkillEffect {

    public AoeEffect() {
        super("aoe");
    }

    @Override
    public void execute(SkillContext context) {
        double radius = number("radius", 5.0);
        double damage = number("damage", 0.0);
        double heal = number("heal", 0.0);
        double healPercent = number("heal_percent", 0.0);
        double knockback = number("knockback", 0.0);
        boolean damageToAll = bool("damage_to_all", false);

        Location center = resolveCenter(context);
        if (center == null || center.getWorld() == null) {
            return;
        }
        World world = center.getWorld();

        List<LivingEntity> targets = damageToAll
                ? context.resolveTargets(TargetType.ALL_NEARBY, radius)
                : context.resolveTargets(target("TARGET"), radius);

        for (LivingEntity entity : targets) {
            if (damage > 0) {
                entity.damage(damage, context.getCaster());
            }
            if (heal > 0) {
                double amount = heal;
                if (healPercent > 0) {
                    amount += entity.getMaxHealth() * (healPercent / 100.0);
                }
                entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + amount));
            }
            if (knockback > 0 && entity.getLocation().getWorld().equals(world)) {
                Vector kb = entity.getLocation().toVector().subtract(center.toVector()).normalize();
                kb.setY(0.4);
                entity.setVelocity(entity.getVelocity().add(kb.multiply(knockback)));
            }
            applyPotionEffects(entity);
        }

        spawnParticles(center);
        playSound(center);
    }

    private Location resolveCenter(SkillContext context) {
        String centerType = string("center", "caster");
        return switch (centerType.toLowerCase()) {
            case "target" -> context.getTarget() != null ? context.getTarget().getLocation() : context.getCaster().getLocation();
            case "origin" -> context.getOrigin() != null ? context.getOrigin() : context.getCaster().getLocation();
            default -> context.getCaster().getLocation();
        };
    }

    private void applyPotionEffects(LivingEntity entity) {
        Object potions = parameters().get("potion_effects");
        if (!(potions instanceof List<?> list)) {
            return;
        }
        for (Object obj : list) {
            if (!(obj instanceof java.util.Map<?, ?> raw)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) raw;
            String typeName = map.get("type") instanceof String s ? s : null;
            if (typeName == null) {
                continue;
            }
            PotionEffectType type = ConfigParseUtil.parsePotionEffectType(typeName);
            if (type == null) {
                continue;
            }
            int duration = ConfigParseUtil.toInt(map.get("duration"), 100);
            int amplifier = ConfigParseUtil.toInt(map.get("amplifier"), 0);
            boolean particles = ConfigParseUtil.toBoolean(map.get("show_particles"), true);
            boolean icon = ConfigParseUtil.toBoolean(map.get("show_icon"), true);
            entity.addPotionEffect(new PotionEffect(type, Math.max(1, duration), Math.max(0, amplifier), false, particles, icon));
        }
    }

    private void spawnParticles(Location center) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        String particleName = string("particle", "FLAME");
        Particle particle;
        particle = ConfigParseUtil.parseParticle(particleName);
        if (particle == null) {
            particle = Particle.FLAME;
        }
        int count = integer("particle_count", 50);
        double radius = number("radius", 5.0);
        world.spawnParticle(particle, center, count, radius / 2.0, radius / 4.0, radius / 2.0, 0);
    }

    private void playSound(Location center) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        String soundName = string("sound", "ENTITY_GENERIC_EXPLODE");
        Sound sound;
        sound = ConfigParseUtil.parseSound(soundName);
        if (sound == null) {
            sound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        world.playSound(center, sound, 1.0f, 1.0f);
    }
}
