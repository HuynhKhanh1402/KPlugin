package dev.khanh.plugin.kplugin.gui.animation;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.util.TaskUtil;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Diff-based frame animation for GUIs with optimized updates.
 * <p>
 * Only updates slots that change between frames, reducing packet overhead.
 * </p>
 */
public final class FrameAnimation {
    
    private final GUI gui;
    private final List<Map<Integer, ItemStack>> frames;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private final AtomicInteger currentFrameIndex;
    
    // Configuration
    private long intervalTicks;
    private boolean loop;
    private Runnable completionCallback;
    
    // State tracking for diff updates
    private final Map<Integer, ItemStack> currentVisibleState;
    private WrappedTask animationTask;
    
    /**
     * Creates a new frame animation for the specified GUI.
     *
     * @param gui the GUI to animate
     */
    private FrameAnimation(@NotNull GUI gui) {
        this.gui = gui;
        this.frames = new ArrayList<>();
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.currentFrameIndex = new AtomicInteger(0);
        this.intervalTicks = 10; // 0.5 seconds default
        this.loop = false;
        this.currentVisibleState = new HashMap<>();
    }
    
    // ==================== Static Factory ====================
    
    /**
     * Creates a FrameAnimation.
     *
     * @param gui the GUI
     * @return a new FrameAnimation
     */
    @NotNull
    public static FrameAnimation create(@NotNull GUI gui) {
        return new FrameAnimation(gui);
    }
    
    // ==================== Configuration ====================
    
    /**
     * Sets the interval between frames in ticks.
     *
     * @param ticks the interval (20 ticks = 1 second)
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation interval(long ticks) {
        this.intervalTicks = Math.max(1, ticks);
        return this;
    }
    
    /**
     * Sets whether the animation should loop.
     *
     * @param loop true to loop indefinitely
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation loop(boolean loop) {
        this.loop = loop;
        return this;
    }
    
    /**
     * Sets a callback to run when the animation completes.
     * <p>
     * Only called when loop is false and all frames have played.
     * </p>
     *
     * @param callback the completion callback
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation onComplete(@Nullable Runnable callback) {
        this.completionCallback = callback;
        return this;
    }
    
    // ==================== Frame Management ====================
    
    /**
     * Adds a frame to the animation.
     *
     * @param frame the frame data (slot {@literal ->} ItemStack mapping)
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation addFrame(@NotNull Map<Integer, ItemStack> frame) {
        Map<Integer, ItemStack> frameCopy = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> entry : frame.entrySet()) {
            ItemStack item = entry.getValue();
            frameCopy.put(entry.getKey(), item != null ? item.clone() : null);
        }
        frames.add(frameCopy);
        return this;
    }
    
    /**
     * Adds a frame using a consumer for configuration.
     *
     * @param frameBuilder consumer that populates the frame map
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation addFrame(@NotNull Consumer<Map<Integer, ItemStack>> frameBuilder) {
        Map<Integer, ItemStack> frame = new HashMap<>();
        frameBuilder.accept(frame);
        return addFrame(frame);
    }
    
    /**
     * Adds multiple frames at once.
     *
     * @param frames the frames to add
     * @return this animation for chaining
     */
    @NotNull
    @SafeVarargs
    public final FrameAnimation addFrames(@NotNull Map<Integer, ItemStack>... frames) {
        for (Map<Integer, ItemStack> frame : frames) {
            addFrame(frame);
        }
        return this;
    }
    
    /**
     * Gets the number of frames in this animation.
     *
     * @return the frame count
     */
    public int getFrameCount() {
        return frames.size();
    }
    
    /**
     * Clears all frames.
     *
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation clearFrames() {
        frames.clear();
        return this;
    }
    
    // ==================== Playback Control ====================
    
    /**
     * Starts or resumes the animation.
     */
    public void start() {
        if (frames.isEmpty()) {
            return;
        }
        
        if (paused.get()) {
            paused.set(false);
            scheduleNextFrame();
            return;
        }
        
        if (running.get()) {
            return;
        }
        
        running.set(true);
        paused.set(false);
        currentFrameIndex.set(0);
        
        // Capture current visible state for diffing
        captureCurrentState();
        
        // Apply first frame immediately
        applyFrameWithDiff(0);
        
        // Schedule remaining frames
        if (frames.size() > 1 || loop) {
            scheduleNextFrame();
        } else if (completionCallback != null) {
            completionCallback.run();
        }
    }
    
    /**
     * Stops the animation.
     */
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        paused.set(false);
        
        if (animationTask != null) {
            TaskUtil.cancel(animationTask);
            animationTask = null;
        }
    }
    
    /**
     * Pauses the animation.
     * <p>
     * The current frame remains displayed. Call {@link #start()} to resume.
     * </p>
     */
    public void pause() {
        if (!running.get() || paused.get()) {
            return;
        }
        
        paused.set(true);
        
        if (animationTask != null) {
            TaskUtil.cancel(animationTask);
            animationTask = null;
        }
    }
    
    /**
     * Checks if the animation is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Checks if the animation is paused.
     *
     * @return true if paused
     */
    public boolean isPaused() {
        return paused.get();
    }
    
    /**
     * Gets the current frame index.
     *
     * @return the current frame index
     */
    public int getCurrentFrameIndex() {
        return currentFrameIndex.get();
    }
    
    /**
     * Jumps to a specific frame.
     *
     * @param frameIndex the frame index to jump to
     */
    public void jumpToFrame(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= frames.size()) {
            return;
        }
        
        currentFrameIndex.set(frameIndex);
        applyFrameWithDiff(frameIndex);
    }
    
    // ==================== Internal Methods ====================
    
    /**
     * Captures the current visible state of animated slots.
     */
    private void captureCurrentState() {
        currentVisibleState.clear();
        
        if (!gui.isInitialized()) {
            return;
        }
        
        // Determine all slots that will be animated
        Set<Integer> animatedSlots = new HashSet<>();
        for (Map<Integer, ItemStack> frame : frames) {
            animatedSlots.addAll(frame.keySet());
        }
        
        // Capture current state
        Inventory inventory = gui.getInventory();
        for (int slot : animatedSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                ItemStack item = inventory.getItem(slot);
                currentVisibleState.put(slot, item != null ? item.clone() : null);
            }
        }
    }
    
    /**
     * Applies a frame using differential updates.
     * <p>
     * Only slots that differ from the current visible state are updated.
     * </p>
     */
    private void applyFrameWithDiff(int frameIndex) {
        if (!gui.isInitialized()) {
            return;
        }
        
        Map<Integer, ItemStack> frame = frames.get(frameIndex);
        Inventory inventory = gui.getInventory();
        
        for (Map.Entry<Integer, ItemStack> entry : frame.entrySet()) {
            int slot = entry.getKey();
            ItemStack newItem = entry.getValue();
            ItemStack currentItem = currentVisibleState.get(slot);
            
            // Check if the item has changed
            if (!itemsEqual(currentItem, newItem)) {
                // Apply update
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, newItem != null ? newItem.clone() : null);
                    // Update tracked state
                    currentVisibleState.put(slot, newItem != null ? newItem.clone() : null);
                }
            }
        }
    }
    
    /**
     * Compares two ItemStacks for equality.
     */
    private boolean itemsEqual(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.isSimilar(b);
    }
    
    /**
     * Schedules the next frame update.
     */
    private void scheduleNextFrame() {
        if (!running.get() || paused.get()) {
            return;
        }
        
        animationTask = TaskUtil.runSync(() -> {
            if (!running.get() || paused.get()) {
                return;
            }
            
            int nextIndex = currentFrameIndex.incrementAndGet();
            
            if (nextIndex >= frames.size()) {
                if (loop) {
                    nextIndex = 0;
                    currentFrameIndex.set(0);
                } else {
                    // Animation complete
                    running.set(false);
                    if (completionCallback != null) {
                        completionCallback.run();
                    }
                    return;
                }
            }
            
            applyFrameWithDiff(nextIndex);
            scheduleNextFrame();
            
        }, intervalTicks);
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Creates a simple alternating animation between two items.
     *
     * @param gui the GUI
     * @param slots the slots to animate
     * @param item1 the first item
     * @param item2 the second item
     * @param intervalTicks the interval between frames
     * @return a configured FrameAnimation
     */
    @NotNull
    public static FrameAnimation alternating(@NotNull GUI gui, int @NotNull [] slots,
                                             @NotNull ItemStack item1, @NotNull ItemStack item2,
                                             long intervalTicks) {
        FrameAnimation animation = create(gui).interval(intervalTicks).loop(true);
        
        Map<Integer, ItemStack> frame1 = new HashMap<>();
        Map<Integer, ItemStack> frame2 = new HashMap<>();
        
        for (int slot : slots) {
            frame1.put(slot, item1);
            frame2.put(slot, item2);
        }
        
        animation.addFrame(frame1).addFrame(frame2);
        return animation;
    }
    
    /**
     * Creates a wave animation that propagates through slots.
     *
     * @param gui the GUI
     * @param slots the slots in order
     * @param activeItem the active (wave) item
     * @param inactiveItem the inactive item
     * @param intervalTicks the interval between frames
     * @return a configured FrameAnimation
     */
    @NotNull
    public static FrameAnimation wave(@NotNull GUI gui, int @NotNull [] slots,
                                      @NotNull ItemStack activeItem, @NotNull ItemStack inactiveItem,
                                      long intervalTicks) {
        FrameAnimation animation = create(gui).interval(intervalTicks).loop(true);
        
        for (int activeIdx = 0; activeIdx < slots.length; activeIdx++) {
            final int currentActive = activeIdx;
            animation.addFrame(frame -> {
                for (int i = 0; i < slots.length; i++) {
                    frame.put(slots[i], i == currentActive ? activeItem : inactiveItem);
                }
            });
        }
        
        return animation;
    }
    
    /**
     * Creates a pulse animation for a single slot.
     *
     * @param gui the GUI
     * @param slot the slot to animate
     * @param items the items to cycle through
     * @param intervalTicks the interval between frames
     * @return a configured FrameAnimation
     */
    @NotNull
    public static FrameAnimation pulse(@NotNull GUI gui, int slot,
                                       @NotNull ItemStack[] items, long intervalTicks) {
        FrameAnimation animation = create(gui).interval(intervalTicks).loop(true);
        
        for (ItemStack item : items) {
            animation.addFrame(frame -> frame.put(slot, item));
        }
        
        return animation;
    }
}
