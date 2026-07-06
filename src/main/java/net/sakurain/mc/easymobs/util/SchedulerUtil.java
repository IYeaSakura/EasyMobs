package net.sakurain.mc.easymobs.util;

import net.sakurain.mc.easymobs.EasyMobsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public final class SchedulerUtil {

    private SchedulerUtil() {
    }

    public static BukkitTask run(Runnable runnable) {
        return Bukkit.getScheduler().runTask(EasyMobsPlugin.getInstance(), runnable);
    }

    public static BukkitTask runAsync(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(EasyMobsPlugin.getInstance(), runnable);
    }

    public static BukkitTask runLater(long delay, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLater(EasyMobsPlugin.getInstance(), runnable, delay);
    }

    public static BukkitTask runLaterAsync(long delay, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(EasyMobsPlugin.getInstance(), runnable, delay);
    }

    public static BukkitTask runTimer(long delay, long period, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimer(EasyMobsPlugin.getInstance(), runnable, delay, period);
    }

    public static BukkitTask runTimerAsync(long delay, long period, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(EasyMobsPlugin.getInstance(), runnable, delay, period);
    }

    public static void runDelayedSequence(long initialDelay, long step, int steps, Consumer<Integer> action) {
        new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (index >= steps) {
                    cancel();
                    return;
                }
                action.accept(index);
                index++;
            }
        }.runTaskTimer(EasyMobsPlugin.getInstance(), initialDelay, step);
    }
}
