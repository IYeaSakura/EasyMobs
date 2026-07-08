package net.sakurain.mc.aeternumgenesis.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Random;

public final class LocationUtil {

    private static final Random RANDOM = new Random();

    private LocationUtil() {
    }

    public static Location randomNearby(Location center, double minRadius, double maxRadius) {
        if (minRadius < 0) minRadius = 0;
        if (maxRadius < minRadius) maxRadius = minRadius;

        double angle = RANDOM.nextDouble() * 2 * Math.PI;
        double distance = minRadius + RANDOM.nextDouble() * (maxRadius - minRadius);

        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        double y = center.getY();

        return new Location(center.getWorld(), x, y, z);
    }

    public static Location randomNearbyFlat(Location center, double minRadius, double maxRadius) {
        Location loc = randomNearby(center, minRadius, maxRadius);
        World world = loc.getWorld();
        if (world != null) {
            int y = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
            loc.setY(y + 1);
        }
        return loc;
    }

    public static Location behindTarget(Location target, Location from, double distance) {
        Vector direction = target.toVector().subtract(from.toVector()).normalize();
        return target.clone().subtract(direction.multiply(distance));
    }

    public static Location awayFrom(Location source, Location from, double distance) {
        Vector direction = source.toVector().subtract(from.toVector()).normalize();
        if (direction.lengthSquared() == 0) {
            direction = new Vector(1, 0, 0);
        }
        return source.clone().add(direction.multiply(distance));
    }

    public static boolean isOutside(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        for (int i = y + 1; i <= world.getMaxHeight(); i++) {
            if (!world.getBlockAt(x, i, z).isPassable()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isUnderground(Location location) {
        return !isOutside(location);
    }

    public static boolean isLand(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        return world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).getType().isSolid()
                && world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()).isPassable()
                && !world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()).isLiquid();
    }

    public static boolean isSea(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        return world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()).isLiquid();
    }

    public static boolean isAir(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        return world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).isPassable()
                && world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()).isPassable();
    }
}
