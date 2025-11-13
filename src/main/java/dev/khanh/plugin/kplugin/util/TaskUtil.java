package dev.khanh.plugin.kplugin.util;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.khanh.plugin.kplugin.KPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Utility class for managing tasks using FoliaLib.
 * Provides unified task scheduling that works on both Spigot/Paper and Folia servers.
 * All delay and period parameters are in ticks (20 ticks = 1 second).
 */
public class TaskUtil {

    /**
     * Runs a task synchronously on the global region (main thread for Spigot/Paper).
     * Executes on the next tick.
     *
     * @param task the task to run
     */
    public static void runSync(Runnable task) {
        KPlugin.getFoliaLib().getScheduler().runNextTick(wrappedTask -> task.run());
    }

    /**
     * Runs a task synchronously on the global region after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks before the task is executed
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runSync(Runnable task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runLater(task, delay);
    }

    /**
     * Runs a repeating task synchronously on the global region.
     *
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runSyncRepeating(Runnable task, long delay, long period) {
        return KPlugin.getFoliaLib().getScheduler().runTimer(task, delay, period);
    }

    /**
     * Runs a task asynchronously in a separate thread.
     * Executes immediately on async thread.
     *
     * @param task the task to run
     */
    public static void runAsync(Runnable task) {
        KPlugin.getFoliaLib().getScheduler().runAsync(wrappedTask -> task.run());
    }

    /**
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks before the task is executed
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAsync(Runnable task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runLaterAsync(task, delay);
    }

    /**
     * Runs a repeating task asynchronously in a separate thread.
     *
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAsyncRepeating(Runnable task, long delay, long period) {
        return KPlugin.getFoliaLib().getScheduler().runTimerAsync(task, delay, period);
    }

    /**
     * Runs a task on the region that owns the specified entity.
     * For Folia: Runs on the entity's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     */
    public static void runAtEntity(Entity entity, Runnable task) {
        KPlugin.getFoliaLib().getScheduler().runAtEntity(entity, wrappedTask -> task.run());
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @param delay  the delay in ticks before the task is executed
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtEntity(Entity entity, Runnable task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, delay);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtEntityRepeating(Entity entity, Runnable task, long delay, long period) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, delay, period);
    }

    /**
     * Runs a task on the region that owns the specified location.
     * For Folia: Runs on the location's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     */
    public static void runAtLocation(Location location, Runnable task) {
        KPlugin.getFoliaLib().getScheduler().runAtLocation(location, wrappedTask -> task.run());
    }

    /**
     * Runs a task on the region that owns the specified location after a delay.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay in ticks before the task is executed
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtLocation(Location location, Runnable task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, task, delay);
    }

    /**
     * Runs a repeating task on the region that owns the specified location.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtLocationRepeating(Location location, Runnable task, long delay, long period) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, task, delay, period);
    }

    /**
     * Cancels a task.
     *
     * @param task the task to cancel
     */
    public static void cancel(WrappedTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Checks if the current thread is the main/global thread (Spigot/Paper)
     * or if Folia is being used.
     *
     * @return true if running on Folia or the main thread
     */
    public static boolean isFolia() {
        return KPlugin.getFoliaLib().isFolia();
    }

    /**
     * Checks if the current thread is a tick thread (global region thread).
     *
     * @return true if on a tick thread
     */
    public static boolean isGlobalTickThread() {
        return Bukkit.isPrimaryThread();
    }
}

