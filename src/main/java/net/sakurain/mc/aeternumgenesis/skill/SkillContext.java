package net.sakurain.mc.aeternumgenesis.skill;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SkillContext {

    private final AeternumGenesisPlugin plugin;
    private final LivingEntity caster;
    private final LivingEntity target;
    private final Location origin;
    private final double damage;

    public SkillContext(AeternumGenesisPlugin plugin, LivingEntity caster, LivingEntity target, Location origin, double damage) {
        this.plugin = plugin;
        this.caster = caster;
        this.target = target;
        this.origin = origin != null ? origin.clone() : null;
        this.damage = damage;
    }

    public AeternumGenesisPlugin getPlugin() {
        return plugin;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public Location getOrigin() {
        return origin != null ? origin.clone() : null;
    }

    public double getDamage() {
        return damage;
    }

    public SkillContext withTarget(LivingEntity target) {
        return new SkillContext(plugin, caster, target, origin, damage);
    }

    public SkillContext withOrigin(Location origin) {
        return new SkillContext(plugin, caster, target, origin, damage);
    }

    public LivingEntity resolveSingleTarget(TargetType type) {
        return switch (type) {
            case CASTER -> caster;
            case TARGET -> target;
            case OWNER -> resolveOwner();
            case RANDOM_NEARBY -> {
                List<LivingEntity> nearby = resolveTargets(TargetType.ALL_NEARBY, 5.0);
                if (nearby.isEmpty()) {
                    yield null;
                }
                yield nearby.get(ThreadLocalRandom.current().nextInt(nearby.size()));
            }
            default -> target;
        };
    }

    private LivingEntity resolveOwner() {
        if (caster instanceof Tameable tameable) {
            if (tameable.getOwner() instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    public List<LivingEntity> resolveTargets(TargetType type, double radius) {
        Location center = getCenter();
        if (center == null || center.getWorld() == null) {
            return Collections.emptyList();
        }

        return switch (type) {
            case CASTER -> caster != null ? List.of(caster) : Collections.emptyList();
            case TARGET -> target != null ? List.of(target) : Collections.emptyList();
            case OWNER -> {
                LivingEntity owner = resolveOwner();
                yield owner != null ? List.of(owner) : Collections.emptyList();
            }
            case NEARBY -> getNearby(center, radius, true);
            case ALL_NEARBY -> getNearby(center, radius, false);
            case RANDOM_NEARBY -> {
                List<LivingEntity> nearby = getNearby(center, radius, true);
                if (nearby.isEmpty()) {
                    yield Collections.emptyList();
                }
                Random random = ThreadLocalRandom.current();
                yield List.of(nearby.get(random.nextInt(nearby.size())));
            }
            case ORIGIN -> Collections.emptyList();
        };
    }

    private List<LivingEntity> getNearby(Location center, double radius, boolean excludeCaster) {
        List<LivingEntity> entities = new ArrayList<>(center.getWorld().getNearbyLivingEntities(center, radius, radius, radius));
        if (excludeCaster && caster != null) {
            entities.remove(caster);
        }
        return entities;
    }

    public Location resolveLocation(TargetType type, double radius) {
        return switch (type) {
            case CASTER -> caster != null ? caster.getLocation() : origin;
            case TARGET -> target != null ? target.getLocation() : origin;
            case ORIGIN -> origin;
            default -> origin;
        };
    }

    private Location getCenter() {
        if (origin != null) {
            return origin;
        }
        if (target != null) {
            return target.getLocation();
        }
        if (caster != null) {
            return caster.getLocation();
        }
        return null;
    }

    public Player getTargetPlayer() {
        return target instanceof Player p ? p : null;
    }

    public Player getCasterPlayer() {
        return caster instanceof Player p ? p : null;
    }
}
