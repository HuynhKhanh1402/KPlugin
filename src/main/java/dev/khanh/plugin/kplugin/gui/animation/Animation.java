package dev.khanh.plugin.kplugin.gui.animation;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an animation that can be played in a GUI.
 * <p>
 * Animations provide visual effects by updating GUI items over time.
 * Common animation types include frame-based animations, transitions,
 * and pulsing effects.
 * </p>
 * 
 * <p>
 * All animations run on the main thread using the scheduler and
 * are thread-safe for start/stop operations.
 * </p>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public interface Animation {
    
    /**
     * Starts the animation.
     * <p>
     * If the animation is already running, this method has no effect.
     * </p>
     */
    void start();
    
    /**
     * Stops the animation.
     * <p>
     * If the animation is not running, this method has no effect.
     * </p>
     */
    void stop();
    
    /**
     * Pauses the animation.
     * <p>
     * The animation can be resumed by calling {@link #start()}.
     * </p>
     */
    void pause();
    
    /**
     * Checks if the animation is currently running.
     *
     * @return true if running, false otherwise
     */
    boolean isRunning();
    
    /**
     * Checks if the animation is paused.
     *
     * @return true if paused, false otherwise
     */
    boolean isPaused();
    
    /**
     * Sets the callback to execute when the animation completes.
     * <p>
     * This is called when the animation finishes naturally (not when stopped manually).
     * </p>
     *
     * @param callback the completion callback
     */
    void onComplete(@NotNull Runnable callback);
    
    /**
     * Gets the delay between animation updates in ticks.
     *
     * @return the update interval in ticks
     */
    long getUpdateInterval();
    
    /**
     * Sets the delay between animation updates in ticks.
     *
     * @param ticks the update interval in ticks (20 ticks = 1 second)
     */
    void setUpdateInterval(long ticks);
}
