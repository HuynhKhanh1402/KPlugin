package dev.khanh.plugin.kplugin.gui.animation;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.khanh.plugin.kplugin.util.TaskUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class for GUI animations.
 * <p>
 * This class provides common functionality for managing animation state,
 * scheduling updates, and handling completion callbacks.
 * </p>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public abstract class AbstractAnimation implements Animation {
    
    protected final AtomicBoolean running;
    protected final AtomicBoolean paused;
    protected final AtomicInteger currentFrame;
    protected long updateInterval;
    protected Runnable completionCallback;
    protected WrappedTask task;
    
    /**
     * Creates a new animation with the specified update interval.
     *
     * @param updateInterval the delay between updates in ticks
     */
    protected AbstractAnimation(long updateInterval) {
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.currentFrame = new AtomicInteger(0);
        this.updateInterval = updateInterval;
    }
    
    @Override
    public void start() {
        if (running.get() && !paused.get()) {
            return;
        }
        
        if (paused.get()) {
            paused.set(false);
            return;
        }
        
        running.set(true);
        paused.set(false);
        currentFrame.set(0);
        
        scheduleNextFrame();
    }
    
    @Override
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        paused.set(false);
        
        if (task != null) {
            TaskUtil.cancel(task);
            task = null;
        }
        
        onStop();
    }
    
    @Override
    public void pause() {
        if (!running.get() || paused.get()) {
            return;
        }
        
        paused.set(true);
        
        if (task != null) {
            TaskUtil.cancel(task);
            task = null;
        }
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    @Override
    public boolean isPaused() {
        return paused.get();
    }
    
    @Override
    public void onComplete(@NotNull Runnable callback) {
        this.completionCallback = callback;
    }
    
    @Override
    public long getUpdateInterval() {
        return updateInterval;
    }
    
    @Override
    public void setUpdateInterval(long ticks) {
        this.updateInterval = ticks;
    }
    
    /**
     * Schedules the next animation frame.
     */
    protected void scheduleNextFrame() {
        if (!running.get() || paused.get()) {
            return;
        }
        
        task = TaskUtil.runSync(() -> {
            if (!running.get() || paused.get()) {
                return;
            }
            
            boolean shouldContinue = update(currentFrame.get());
            
            if (shouldContinue) {
                currentFrame.incrementAndGet();
                scheduleNextFrame();
            } else {
                complete();
            }
        }, updateInterval);
    }
    
    /**
     * Updates the animation for the current frame.
     *
     * @param frame the current frame index
     * @return true to continue animation, false to complete
     */
    protected abstract boolean update(int frame);
    
    /**
     * Called when the animation is stopped manually.
     */
    protected void onStop() {
        // Override in subclasses if needed
    }
    
    /**
     * Completes the animation and calls the completion callback.
     */
    protected void complete() {
        running.set(false);
        paused.set(false);
        
        if (completionCallback != null) {
            completionCallback.run();
        }
    }
}
