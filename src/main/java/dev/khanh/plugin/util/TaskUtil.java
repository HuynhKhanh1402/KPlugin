package dev.khanh.plugin.util;

import dev.khanh.plugin.KPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Utility class for managing tasks in a Spigot plugin. This class provides methods for scheduling
 * synchronous and asynchronous tasks.
 */
public class TaskUtil {

    /**
     * Runs a task synchronously on the main server thread.
     *
     * @param task the task to run
     * @return the Bukkit task that was scheduled
     */
    public static BukkitTask runSync(Runnable task) {
        return Bukkit.getScheduler().runTask(KPlugin.getKInstance(), task);
    }

    /**
     * Runs a task synchronously on the main server thread after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks before the task is executed
     * @return the Bukkit task that was scheduled
     */
    public static BukkitTask runSync(Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLater(KPlugin.getKInstance(), task, delay);
    }

    /**
     * Runs a task asynchronously in a separate thread.
     *
     * @param task the task to run
     * @return the Bukkit task that was scheduled
     */
    public static BukkitTask runAsync(Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(KPlugin.getKInstance(), task);
    }

    /**
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks before the task is executed
     * @return the Bukkit task that was scheduled
     */
    public static BukkitTask runAsync(Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(KPlugin.getKInstance(), task, delay);
    }
}
