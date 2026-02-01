package dev.khanh.plugin.kplugin.util;

import com.tcoded.folialib.enums.EntityTaskResult;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.khanh.plugin.kplugin.KPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for managing tasks using FoliaLib.
 * Provides unified task scheduling that works on both Spigot/Paper and Folia servers.
 * All delay and period parameters are in ticks by default (20 ticks = 1 second).
 * 
 * <p><b>Important:</b> Prefer using tick-based methods (long delay/period) over TimeUnit methods.
 * TimeUnit methods should only be used for very long durations (minutes, hours) where ticks 
 * would be impractical. For most use cases (seconds, sub-minute delays), use tick-based methods 
 * for better performance and consistency with Minecraft's tick system.
 * 
 * <p>Methods with TimeUnit parameter allow specifying custom time units, but should be avoided
 * unless absolutely necessary for long-duration tasks.
 */
public class TaskUtil {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TaskUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== Sync Methods (Global Region) ====================

    /**
     * Runs a task synchronously on the global region (main thread for Spigot/Paper).
     * Executes on the next tick.
     *
     * @param task the task to run
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runSync(Consumer<WrappedTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runNextTick(task);
    }

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
     * Runs a task synchronously on the global region after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runSync(Consumer<WrappedTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runLater(task, delay);
    }

    /**
     * Runs a task synchronously on the global region after a specified delay.
     *
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runSync(Runnable task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runLater(task, delay, timeUnit);
    }

    /**
     * Runs a task synchronously on the global region after a specified delay.
     *
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runSync(Consumer<WrappedTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runLater(task, delay, timeUnit);
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
     * Runs a repeating task synchronously on the global region.
     *
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     */
    public static void runSyncRepeating(Consumer<WrappedTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runTimer(task, delay, period);
    }

    /**
     * Runs a repeating task synchronously on the global region.
     *
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runSyncRepeating(Runnable task, long delay, long period, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runTimer(task, delay, period, timeUnit);
    }

    /**
     * Runs a repeating task synchronously on the global region.
     *
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void runSyncRepeating(Consumer<WrappedTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runTimer(task, delay, period, timeUnit);
    }

    // ==================== Async Methods ====================

    /**
     * Runs a task asynchronously in a separate thread.
     * Executes immediately on async thread.
     *
     * @param task the task to run
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAsync(Consumer<WrappedTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runAsync(task);
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
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAsync(Consumer<WrappedTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runLaterAsync(task, delay);
    }

    /**
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAsync(Runnable task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runLaterAsync(task, delay, timeUnit);
    }

    /**
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAsync(Consumer<WrappedTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runLaterAsync(task, delay, timeUnit);
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
     * Runs a repeating task asynchronously in a separate thread.
     *
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     */
    public static void runAsyncRepeating(Consumer<WrappedTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runTimerAsync(task, delay, period);
    }

    /**
     * Runs a repeating task asynchronously in a separate thread.
     *
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAsyncRepeating(Runnable task, long delay, long period, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runTimerAsync(task, delay, period, timeUnit);
    }

    /**
     * Runs a repeating task asynchronously in a separate thread.
     *
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void runAsyncRepeating(Consumer<WrappedTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runTimerAsync(task, delay, period, timeUnit);
    }

    // ==================== Entity Region Methods ====================

    /**
     * Runs a task on the region that owns the specified entity.
     * For Folia: Runs on the entity's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @return CompletableFuture with EntityTaskResult indicating the result
     */
    public static CompletableFuture<EntityTaskResult> runAtEntity(Entity entity, Consumer<WrappedTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntity(entity, task);
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
     * Runs a task on the region that owns the specified entity with a fallback.
     * For Folia: Runs on the entity's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @return CompletableFuture with EntityTaskResult indicating the result
     */
    public static CompletableFuture<EntityTaskResult> runAtEntityWithFallback(Entity entity, Consumer<WrappedTask> task, Runnable fallback) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityWithFallback(entity, task, fallback);
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
     * Runs a task on the region that owns the specified entity after a delay.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @param delay  the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAtEntity(Entity entity, Consumer<WrappedTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, delay);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay with a fallback.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay in ticks before the task is executed
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtEntity(Entity entity, Runnable task, Runnable fallback, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, fallback, delay);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay with a fallback.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAtEntity(Entity entity, Consumer<WrappedTask> task, Runnable fallback, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, fallback, delay);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtEntity(Entity entity, Runnable task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, delay, timeUnit);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAtEntity(Entity entity, Consumer<WrappedTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, delay, timeUnit);
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
     * Runs a repeating task on the region that owns the specified entity with a fallback.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtEntityRepeating(Entity entity, Runnable task, Runnable fallback, long delay, long period) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, fallback, delay, period);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     */
    public static void runAtEntityRepeating(Entity entity, Consumer<WrappedTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, delay, period);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity with a fallback.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     */
    public static void runAtEntityRepeating(Entity entity, Consumer<WrappedTask> task, Runnable fallback, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, fallback, delay, period);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtEntityRepeating(Entity entity, Runnable task, long delay, long period, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, delay, period, timeUnit);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void runAtEntityRepeating(Entity entity, Consumer<WrappedTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, delay, period, timeUnit);
    }

    // ==================== Location Region Methods ====================

    /**
     * Runs a task on the region that owns the specified location.
     * For Folia: Runs on the location's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAtLocation(Location location, Consumer<WrappedTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocation(location, task);
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
     * Runs a task on the region that owns the specified location after a delay.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAtLocation(Location location, Consumer<WrappedTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, task, delay);
    }

    /**
     * Runs a task on the region that owns the specified location after a delay.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtLocation(Location location, Runnable task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, task, delay, timeUnit);
    }

    /**
     * Runs a task on the region that owns the specified location after a delay.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> runAtLocation(Location location, Consumer<WrappedTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, task, delay, timeUnit);
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
     * Runs a repeating task on the region that owns the specified location.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     */
    public static void runAtLocationRepeating(Location location, Consumer<WrappedTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, task, delay, period);
    }

    /**
     * Runs a repeating task on the region that owns the specified location.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the wrapped task that was scheduled
     */
    public static WrappedTask runAtLocationRepeating(Location location, Runnable task, long delay, long period, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, task, delay, period, timeUnit);
    }

    /**
     * Runs a repeating task on the region that owns the specified location.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void runAtLocationRepeating(Location location, Consumer<WrappedTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, task, delay, period, timeUnit);
    }

    // ==================== Utility Methods ====================

    /**
     * Checks if a location is owned by the current region.
     *
     * @param location the location to check
     * @return true if the location is owned by the current region
     */
    public static boolean isOwnedByCurrentRegion(Location location) {
        return KPlugin.getFoliaLib().getScheduler().isOwnedByCurrentRegion(location);
    }

    /**
     * Checks if a location is owned by the current region within a specified radius.
     *
     * @param location the location to check
     * @param radius   the radius to check
     * @return true if the location is owned by the current region
     */
    public static boolean isOwnedByCurrentRegion(Location location, int radius) {
        return KPlugin.getFoliaLib().getScheduler().isOwnedByCurrentRegion(location, radius);
    }

    /**
     * Checks if a block is owned by the current region.
     *
     * @param block the block to check
     * @return true if the block is owned by the current region
     */
    public static boolean isOwnedByCurrentRegion(Block block) {
        return KPlugin.getFoliaLib().getScheduler().isOwnedByCurrentRegion(block);
    }

    /**
     * Checks if coordinates in a world are owned by the current region.
     *
     * @param world the world
     * @param x     the x coordinate
     * @param z     the z coordinate
     * @return true if the coordinates are owned by the current region
     */
    public static boolean isOwnedByCurrentRegion(World world, int x, int z) {
        return KPlugin.getFoliaLib().getScheduler().isOwnedByCurrentRegion(world, x, z);
    }

    /**
     * Checks if coordinates in a world are owned by the current region.
     *
     * @param world the world
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return true if the coordinates are owned by the current region
     */
    public static boolean isOwnedByCurrentRegion(World world, int x, int y, int z) {
        return KPlugin.getFoliaLib().getScheduler().isOwnedByCurrentRegion(world, x, y, z);
    }

    /**
     * Checks if an entity is owned by the current region.
     *
     * @param entity the entity to check
     * @return true if the entity is owned by the current region
     */
    public static boolean isOwnedByCurrentRegion(Entity entity) {
        return KPlugin.getFoliaLib().getScheduler().isOwnedByCurrentRegion(entity);
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
     * Cancels a task using the scheduler's cancel method.
     *
     * @param task the task to cancel
     */
    public static void cancelTask(WrappedTask task) {
        KPlugin.getFoliaLib().getScheduler().cancelTask(task);
    }

    /**
     * Cancels all tasks registered by this plugin.
     */
    public static void cancelAllTasks() {
        KPlugin.getFoliaLib().getScheduler().cancelAllTasks();
    }

    /**
     * Gets all tasks registered by this plugin.
     *
     * @return a list of all tasks
     */
    public static List<WrappedTask> getAllTasks() {
        return KPlugin.getFoliaLib().getScheduler().getAllTasks();
    }

    /**
     * Gets all server tasks (all tasks from all plugins).
     *
     * @return a list of all server tasks
     */
    public static List<WrappedTask> getAllServerTasks() {
        return KPlugin.getFoliaLib().getScheduler().getAllServerTasks();
    }

    /**
     * Wraps a Bukkit task into a WrappedTask.
     *
     * @param task the Bukkit task to wrap
     * @return the wrapped task
     */
    public static WrappedTask wrapTask(Object task) {
        return KPlugin.getFoliaLib().getScheduler().wrapTask(task);
    }

    /**
     * Checks if the current thread is a global tick thread.
     *
     * @return true if on a global tick thread
     */
    public static boolean isGlobalTickThread() {
        return KPlugin.getFoliaLib().getScheduler().isGlobalTickThread();
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
}
