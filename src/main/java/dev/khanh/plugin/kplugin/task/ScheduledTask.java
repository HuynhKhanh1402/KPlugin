package dev.khanh.plugin.kplugin.task;

/**
 * Represents a scheduled task that can be cancelled.
 * Provides an abstraction layer over the underlying task scheduler implementation,
 * decoupling consumer code from any specific scheduling library.
 */
public interface ScheduledTask {

    /**
     * Cancels this task. If the task has already been cancelled or completed,
     * this method has no effect.
     */
    void cancel();

    /**
     * Checks whether this task has been cancelled.
     *
     * @return true if the task has been cancelled
     */
    boolean isCancelled();
}
