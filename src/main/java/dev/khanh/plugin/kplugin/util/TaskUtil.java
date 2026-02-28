package dev.khanh.plugin.kplugin.util;

import dev.khanh.plugin.kplugin.KPlugin;
import dev.khanh.plugin.kplugin.task.ScheduledTaskImpl;
import dev.khanh.plugin.kplugin.task.ScheduledTask;
import dev.khanh.plugin.kplugin.task.TaskResult;
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
     * Schedules a task synchronously on the global region (main thread for Spigot/Paper).
     * Executes on the next tick. The task receives its own {@link ScheduledTask} handle
     * for self-cancellation or inspection.
     *
     * @param task the task to run, receiving its own task handle
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleSync(Consumer<ScheduledTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runNextTick(ScheduledTaskImpl.adaptConsumer(task));
    }

    /**
     * Runs a task synchronously on the global region (main thread for Spigot/Paper).
     * Executes on the next tick.
     *
     * @param task the task to run
     */
    public static void runSync(Runnable task) {
        KPlugin.getFoliaLib().getScheduler().runNextTick(t -> task.run());
    }

    /**
     * Runs a task synchronously on the global region after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks before the task is executed
     * @return the scheduled task
     */
    public static ScheduledTask runSync(Runnable task, long delay) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runLater(task, delay));
    }

    /**
     * Schedules a task synchronously on the global region after a specified delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task  the task to run, receiving its own task handle
     * @param delay the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleSync(Consumer<ScheduledTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runLater(ScheduledTaskImpl.adaptConsumer(task), delay);
    }

    /**
     * Runs a task synchronously on the global region after a specified delay.
     *
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the scheduled task
     */
    public static ScheduledTask runSync(Runnable task, long delay, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runLater(task, delay, timeUnit));
    }

    /**
     * Schedules a task synchronously on the global region after a specified delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleSync(Consumer<ScheduledTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runLater(ScheduledTaskImpl.adaptConsumer(task), delay, timeUnit);
    }

    /**
     * Runs a repeating task synchronously on the global region.
     *
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     * @return the scheduled task
     */
    public static ScheduledTask runSyncRepeating(Runnable task, long delay, long period) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runTimer(task, delay, period));
    }

    /**
     * Schedules a repeating task synchronously on the global region.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task   the task to run, receiving its own task handle
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     */
    public static void scheduleSyncRepeating(Consumer<ScheduledTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runTimer(ScheduledTaskImpl.adaptConsumer(task), delay, period);
    }

    /**
     * Runs a repeating task synchronously on the global region.
     *
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the scheduled task
     */
    public static ScheduledTask runSyncRepeating(Runnable task, long delay, long period, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runTimer(task, delay, period, timeUnit));
    }

    /**
     * Schedules a repeating task synchronously on the global region.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void scheduleSyncRepeating(Consumer<ScheduledTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runTimer(ScheduledTaskImpl.adaptConsumer(task), delay, period, timeUnit);
    }

    // ==================== Async Methods ====================

    /**
     * Schedules a task asynchronously in a separate thread.
     * Executes immediately on async thread. The task receives its own {@link ScheduledTask}
     * handle for self-cancellation or inspection.
     *
     * @param task the task to run, receiving its own task handle
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAsync(Consumer<ScheduledTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runAsync(ScheduledTaskImpl.adaptConsumer(task));
    }

    /**
     * Runs a task asynchronously in a separate thread.
     * Executes immediately on async thread.
     *
     * @param task the task to run
     */
    public static void runAsync(Runnable task) {
        KPlugin.getFoliaLib().getScheduler().runAsync(t -> task.run());
    }

    /**
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks before the task is executed
     * @return the scheduled task
     */
    public static ScheduledTask runAsync(Runnable task, long delay) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runLaterAsync(task, delay));
    }

    /**
     * Schedules a task asynchronously in a separate thread after a specified delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task  the task to run, receiving its own task handle
     * @param delay the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAsync(Consumer<ScheduledTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runLaterAsync(ScheduledTaskImpl.adaptConsumer(task), delay);
    }

    /**
     * Runs a task asynchronously in a separate thread after a specified delay.
     *
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the scheduled task
     */
    public static ScheduledTask runAsync(Runnable task, long delay, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runLaterAsync(task, delay, timeUnit));
    }

    /**
     * Schedules a task asynchronously in a separate thread after a specified delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAsync(Consumer<ScheduledTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runLaterAsync(ScheduledTaskImpl.adaptConsumer(task), delay, timeUnit);
    }

    /**
     * Runs a repeating task asynchronously in a separate thread.
     *
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     * @return the scheduled task
     */
    public static ScheduledTask runAsyncRepeating(Runnable task, long delay, long period) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runTimerAsync(task, delay, period));
    }

    /**
     * Schedules a repeating task asynchronously in a separate thread.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task   the task to run, receiving its own task handle
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     */
    public static void scheduleAsyncRepeating(Consumer<ScheduledTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runTimerAsync(ScheduledTaskImpl.adaptConsumer(task), delay, period);
    }

    /**
     * Runs a repeating task asynchronously in a separate thread.
     *
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the scheduled task
     */
    public static ScheduledTask runAsyncRepeating(Runnable task, long delay, long period, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runTimerAsync(task, delay, period, timeUnit));
    }

    /**
     * Schedules a repeating task asynchronously in a separate thread.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void scheduleAsyncRepeating(Consumer<ScheduledTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runTimerAsync(ScheduledTaskImpl.adaptConsumer(task), delay, period, timeUnit);
    }

    // ==================== Entity Region Methods ====================

    /**
     * Schedules a task on the region that owns the specified entity.
     * For Folia: Runs on the entity's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately. The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run, receiving its own task handle
     * @return CompletableFuture with TaskResult indicating the result
     */
    public static CompletableFuture<TaskResult> scheduleAtEntity(Entity entity, Consumer<ScheduledTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntity(entity, ScheduledTaskImpl.adaptConsumer(task))
                .thenApply(TaskResult::from);
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
        KPlugin.getFoliaLib().getScheduler().runAtEntity(entity, t -> task.run());
    }

    /**
     * Schedules a task on the region that owns the specified entity with a fallback.
     * For Folia: Runs on the entity's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately. The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param fallback the fallback task to run if entity is invalid
     * @return CompletableFuture with TaskResult indicating the result
     */
    public static CompletableFuture<TaskResult> scheduleAtEntityWithFallback(Entity entity, Consumer<ScheduledTask> task, Runnable fallback) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityWithFallback(entity, ScheduledTaskImpl.adaptConsumer(task), fallback)
                .thenApply(TaskResult::from);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @param delay  the delay in ticks before the task is executed
     * @return the scheduled task
     */
    public static ScheduledTask runAtEntity(Entity entity, Runnable task, long delay) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, delay));
    }

    /**
     * Schedules a task on the region that owns the specified entity after a delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run, receiving its own task handle
     * @param delay  the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAtEntity(Entity entity, Consumer<ScheduledTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, ScheduledTaskImpl.adaptConsumer(task), delay);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay with a fallback.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay in ticks before the task is executed
     * @return the scheduled task
     */
    public static ScheduledTask runAtEntity(Entity entity, Runnable task, Runnable fallback, long delay) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, fallback, delay));
    }

    /**
     * Schedules a task on the region that owns the specified entity after a delay with a fallback.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAtEntity(Entity entity, Consumer<ScheduledTask> task, Runnable fallback, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, ScheduledTaskImpl.adaptConsumer(task), fallback, delay);
    }

    /**
     * Runs a task on the region that owns the specified entity after a delay.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the scheduled task
     */
    public static ScheduledTask runAtEntity(Entity entity, Runnable task, long delay, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, task, delay, timeUnit));
    }

    /**
     * Schedules a task on the region that owns the specified entity after a delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAtEntity(Entity entity, Consumer<ScheduledTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtEntityLater(entity, ScheduledTaskImpl.adaptConsumer(task), delay, timeUnit);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     * @return the scheduled task
     */
    public static ScheduledTask runAtEntityRepeating(Entity entity, Runnable task, long delay, long period) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, delay, period));
    }

    /**
     * Runs a repeating task on the region that owns the specified entity with a fallback.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     * @return the scheduled task
     */
    public static ScheduledTask runAtEntityRepeating(Entity entity, Runnable task, Runnable fallback, long delay, long period) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, fallback, delay, period));
    }

    /**
     * Schedules a repeating task on the region that owns the specified entity.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity the entity whose region will execute the task
     * @param task   the task to run, receiving its own task handle
     * @param delay  the delay in ticks before the first execution
     * @param period the period in ticks between executions
     */
    public static void scheduleAtEntityRepeating(Entity entity, Consumer<ScheduledTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, ScheduledTaskImpl.adaptConsumer(task), delay, period);
    }

    /**
     * Schedules a repeating task on the region that owns the specified entity with a fallback.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param fallback the fallback task to run if entity is invalid
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     */
    public static void scheduleAtEntityRepeating(Entity entity, Consumer<ScheduledTask> task, Runnable fallback, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, ScheduledTaskImpl.adaptConsumer(task), fallback, delay, period);
    }

    /**
     * Runs a repeating task on the region that owns the specified entity.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the scheduled task
     */
    public static ScheduledTask runAtEntityRepeating(Entity entity, Runnable task, long delay, long period, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, task, delay, period, timeUnit));
    }

    /**
     * Schedules a repeating task on the region that owns the specified entity.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param entity   the entity whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void scheduleAtEntityRepeating(Entity entity, Consumer<ScheduledTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runAtEntityTimer(entity, ScheduledTaskImpl.adaptConsumer(task), delay, period, timeUnit);
    }

    // ==================== Location Region Methods ====================

    /**
     * Schedules a task on the region that owns the specified location.
     * For Folia: Runs on the location's region thread.
     * For Spigot/Paper: Runs on the main thread.
     * Executes immediately. The task receives its own {@link ScheduledTask} handle.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAtLocation(Location location, Consumer<ScheduledTask> task) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocation(location, ScheduledTaskImpl.adaptConsumer(task));
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
        KPlugin.getFoliaLib().getScheduler().runAtLocation(location, t -> task.run());
    }

    /**
     * Runs a task on the region that owns the specified location after a delay.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay in ticks before the task is executed
     * @return the scheduled task
     */
    public static ScheduledTask runAtLocation(Location location, Runnable task, long delay) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, task, delay));
    }

    /**
     * Schedules a task on the region that owns the specified location after a delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the task is executed
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAtLocation(Location location, Consumer<ScheduledTask> task, long delay) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, ScheduledTaskImpl.adaptConsumer(task), delay);
    }

    /**
     * Runs a task on the region that owns the specified location after a delay.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return the scheduled task
     */
    public static ScheduledTask runAtLocation(Location location, Runnable task, long delay, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, task, delay, timeUnit));
    }

    /**
     * Schedules a task on the region that owns the specified location after a delay.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the task is executed
     * @param timeUnit the time unit of the delay
     * @return CompletableFuture that completes when the task finishes
     */
    public static CompletableFuture<Void> scheduleAtLocation(Location location, Consumer<ScheduledTask> task, long delay, TimeUnit timeUnit) {
        return KPlugin.getFoliaLib().getScheduler().runAtLocationLater(location, ScheduledTaskImpl.adaptConsumer(task), delay, timeUnit);
    }

    /**
     * Runs a repeating task on the region that owns the specified location.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     * @return the scheduled task
     */
    public static ScheduledTask runAtLocationRepeating(Location location, Runnable task, long delay, long period) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, task, delay, period));
    }

    /**
     * Schedules a repeating task on the region that owns the specified location.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between executions
     */
    public static void scheduleAtLocationRepeating(Location location, Consumer<ScheduledTask> task, long delay, long period) {
        KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, ScheduledTaskImpl.adaptConsumer(task), delay, period);
    }

    /**
     * Runs a repeating task on the region that owns the specified location.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     * @return the scheduled task
     */
    public static ScheduledTask runAtLocationRepeating(Location location, Runnable task, long delay, long period, TimeUnit timeUnit) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, task, delay, period, timeUnit));
    }

    /**
     * Schedules a repeating task on the region that owns the specified location.
     * The task receives its own {@link ScheduledTask} handle.
     *
     * @param location the location whose region will execute the task
     * @param task     the task to run, receiving its own task handle
     * @param delay    the delay before the first execution
     * @param period   the period between executions
     * @param timeUnit the time unit of the delay and period
     */
    public static void scheduleAtLocationRepeating(Location location, Consumer<ScheduledTask> task, long delay, long period, TimeUnit timeUnit) {
        KPlugin.getFoliaLib().getScheduler().runAtLocationTimer(location, ScheduledTaskImpl.adaptConsumer(task), delay, period, timeUnit);
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
    public static void cancel(ScheduledTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Cancels a task using the scheduler's cancel method.
     *
     * @param task the task to cancel
     */
    public static void cancelTask(ScheduledTask task) {
        if (task instanceof ScheduledTaskImpl) {
            KPlugin.getFoliaLib().getScheduler().cancelTask(((ScheduledTaskImpl) task).unwrap());
        }
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
    public static List<ScheduledTask> getAllTasks() {
        return ScheduledTaskImpl.wrapList(KPlugin.getFoliaLib().getScheduler().getAllTasks());
    }

    /**
     * Gets all server tasks (all tasks from all plugins).
     *
     * @return a list of all server tasks
     */
    public static List<ScheduledTask> getAllServerTasks() {
        return ScheduledTaskImpl.wrapList(KPlugin.getFoliaLib().getScheduler().getAllServerTasks());
    }

    /**
     * Wraps a Bukkit task into a ScheduledTask.
     *
     * @param task the Bukkit task to wrap
     * @return the scheduled task
     */
    public static ScheduledTask wrapTask(Object task) {
        return new ScheduledTaskImpl(KPlugin.getFoliaLib().getScheduler().wrapTask(task));
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
