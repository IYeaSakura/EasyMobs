package net.sakurain.mc.aeternumgenesis.atmosphere;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Runtime instance of an applied atmosphere.
 */
public final class ActiveAtmosphere {

    private final UUID instanceId;
    private final String templateId;
    private final AtmosphereTemplate template;
    private final Location center;
    private final double radius;
    private final long startTick;
    private final long durationTicks;

    public ActiveAtmosphere(String templateId, AtmosphereTemplate template, Location center,
                            double radius, long durationTicks) {
        this.instanceId = UUID.randomUUID();
        this.templateId = templateId;
        this.template = template;
        this.center = center.clone();
        this.radius = radius;
        this.startTick = System.currentTimeMillis();
        this.durationTicks = durationTicks;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public AtmosphereTemplate getTemplate() {
        return template;
    }

    public Location getCenter() {
        return center.clone();
    }

    public double getRadius() {
        return radius;
    }

    public long getStartTick() {
        return startTick;
    }

    public long getDurationTicks() {
        return durationTicks;
    }

    public boolean isExpired() {
        return durationTicks > 0 && getElapsedTicks() >= durationTicks;
    }

    public long getElapsedTicks() {
        return (System.currentTimeMillis() - startTick) / 50;
    }

    public long getRemainingTicks() {
        if (durationTicks <= 0) {
            return -1;
        }
        return Math.max(0, durationTicks - getElapsedTicks());
    }

    public double getProgressPercent() {
        if (durationTicks <= 0) {
            return 0.0;
        }
        return Math.min(100.0, (getElapsedTicks() * 100.0) / durationTicks);
    }

    public boolean isInside(Location location) {
        if (location == null || !location.getWorld().equals(center.getWorld())) {
            return false;
        }
        return location.distanceSquared(center) <= radius * radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveAtmosphere that)) return false;
        return instanceId.equals(that.instanceId);
    }

    @Override
    public int hashCode() {
        return instanceId.hashCode();
    }
}
