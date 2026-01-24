package dev.khanh.plugin.kplugin.gui.animation;

import dev.khanh.plugin.kplugin.gui.GUI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Frame-based animation for GUIs.
 * <p>
 * This animation type displays a sequence of frames, where each frame
 * is a mapping of slot positions to ItemStacks. Frames are cycled through
 * at the specified update interval.
 * </p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * FrameAnimation animation = new FrameAnimation(gui, 5); // 5 ticks per frame
 * 
 * // Frame 0
 * animation.addFrame(frame -> {
 *     frame.put(0, new ItemStack(Material.RED_STAINED_GLASS_PANE));
 *     frame.put(1, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
 * });
 * 
 * // Frame 1
 * animation.addFrame(frame -> {
 *     frame.put(0, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
 *     frame.put(1, new ItemStack(Material.RED_STAINED_GLASS_PANE));
 * });
 * 
 * animation.setLoop(true);
 * animation.start();
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class FrameAnimation extends AbstractAnimation {
    
    private final GUI gui;
    private final List<Map<Integer, ItemStack>> frames;
    private boolean loop;
    
    /**
     * Creates a new frame animation for the specified GUI.
     *
     * @param gui the GUI to animate
     * @param updateInterval the ticks between frame updates
     */
    public FrameAnimation(@NotNull GUI gui, long updateInterval) {
        super(updateInterval);
        this.gui = gui;
        this.frames = new ArrayList<>();
        this.loop = false;
    }
    
    /**
     * Adds a frame to the animation.
     *
     * @param frame the frame (slot → ItemStack mapping)
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation addFrame(@NotNull Map<Integer, ItemStack> frame) {
        frames.add(new HashMap<>(frame));
        return this;
    }
    
    /**
     * Adds a frame using a builder pattern.
     *
     * @param frameBuilder consumer that builds the frame
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation addFrame(@NotNull java.util.function.Consumer<Map<Integer, ItemStack>> frameBuilder) {
        Map<Integer, ItemStack> frame = new HashMap<>();
        frameBuilder.accept(frame);
        frames.add(frame);
        return this;
    }
    
    /**
     * Sets whether the animation should loop.
     *
     * @param loop true to loop, false to play once
     * @return this animation for chaining
     */
    @NotNull
    public FrameAnimation setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }
    
    /**
     * Gets whether the animation loops.
     *
     * @return true if looping
     */
    public boolean isLoop() {
        return loop;
    }
    
    /**
     * Gets the number of frames in this animation.
     *
     * @return the frame count
     */
    public int getFrameCount() {
        return frames.size();
    }
    
    @Override
    protected boolean update(int frameIndex) {
        if (frames.isEmpty()) {
            return false;
        }
        
        int actualFrame = frameIndex % frames.size();
        Map<Integer, ItemStack> frame = frames.get(actualFrame);
        
        // Update GUI with current frame
        for (Map.Entry<Integer, ItemStack> entry : frame.entrySet()) {
            gui.setItem(entry.getKey(), entry.getValue());
        }
        
        // Check if we should continue
        if (loop) {
            return true;
        } else {
            return frameIndex < frames.size() - 1;
        }
    }
}
