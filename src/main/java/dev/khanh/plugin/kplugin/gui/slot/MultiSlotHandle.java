package dev.khanh.plugin.kplugin.gui.slot;

import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.gui.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handle for multiple non-contiguous slots.
 *
 * @see GUI#slots(int...)
 */
public final class MultiSlotHandle {
    
    private final GUI gui;
    private final int[] slots;
    
    /**
     * Creates a multi-slot handle. Internal use only.
     *
     * @param gui the GUI
     * @param slots the slot indices
     */
    public MultiSlotHandle(@NotNull GUI gui, int... slots) {
        this.gui = gui;
        this.slots = slots.clone();
    }
    
    /**
     * Gets the slot indices this handle represents.
     *
     * @return copy of the slot indices array
     */
    public int @NotNull [] indices() {
        return slots.clone();
    }
    
    /**
     * Gets the number of slots in this handle.
     *
     * @return the number of slots
     */
    public int size() {
        return slots.length;
    }
    
    /**
     * Gets the parent GUI.
     *
     * @return the GUI instance
     */
    @NotNull
    public GUI gui() {
        return gui;
    }
    
    /**
     * Fills all slots with the specified item.
     *
     * @param item the item to fill with
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle fill(@Nullable ItemStack item) {
        for (int slot : slots) {
            gui.setSlotItem(slot, item);
        }
        return this;
    }
    
    /**
     * Fills all slots with an item built from a builder.
     *
     * @param builder the item builder
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle fill(@NotNull ItemBuilder builder) {
        ItemStack item = builder.build();
        return fill(item);
    }
    
    /**
     * Fills empty slots with the specified item.
     *
     * @param item the item to fill with
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle fillEmpty(@Nullable ItemStack item) {
        for (int slot : slots) {
            ItemStack current = gui.getSlotItem(slot);
            if (current == null || current.getType().isAir()) {
                gui.setSlotItem(slot, item);
            }
        }
        return this;
    }
    
    /**
     * Fills slots with alternating items.
     *
     * @param item1 the first item
     * @param item2 the second item
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle fillAlternating(@Nullable ItemStack item1, @Nullable ItemStack item2) {
        boolean useFirst = true;
        for (int slot : slots) {
            gui.setSlotItem(slot, useFirst ? item1 : item2);
            useFirst = !useFirst;
        }
        return this;
    }
    
    /**
     * Clears all slots.
     *
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle clear() {
        return fill((ItemStack) null);
    }
    
    /**
     * Iterates over all slots.
     *
     * @param action the action to perform, receiving slot index and SlotHandle
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle forEach(@NotNull BiConsumer<Integer, SlotHandle> action) {
        for (int slot : slots) {
            action.accept(slot, gui.slot(slot));
        }
        return this;
    }
    
    /**
     * Iterates over all slots with just the slot index.
     *
     * @param action the action to perform, receiving slot index
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle forEachIndex(@NotNull Consumer<Integer> action) {
        for (int slot : slots) {
            action.accept(slot);
        }
        return this;
    }
    
    /**
     * Sets a click handler for all slots.
     *
     * @param handler the click handler
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle onClick(@NotNull Consumer<ClickContext> handler) {
        for (int slot : slots) {
            gui.setSlotClickHandler(slot, handler);
        }
        return this;
    }
    
    /**
     * Disables all slots with a message.
     *
     * @param message the message to show when clicked
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle disable(@NotNull String message) {
        for (int slot : slots) {
            gui.setSlotDisabled(slot, message);
        }
        return this;
    }
    
    /**
     * Enables all slots.
     *
     * @return this handle for chaining
     */
    @NotNull
    public MultiSlotHandle enable() {
        for (int slot : slots) {
            gui.setSlotEnabled(slot);
        }
        return this;
    }
    
    /**
     * Filters to slots where the predicate returns true.
     *
     * @param predicate the predicate to test each slot
     * @return a new MultiSlotHandle with matching slots
     */
    @NotNull
    public MultiSlotHandle filter(@NotNull java.util.function.IntPredicate predicate) {
        java.util.List<Integer> matching = new java.util.ArrayList<>();
        for (int slot : slots) {
            if (predicate.test(slot)) {
                matching.add(slot);
            }
        }
        return new MultiSlotHandle(gui, matching.stream().mapToInt(i -> i).toArray());
    }
    
    /**
     * Combines this handle with another, returning a handle for all slots.
     *
     * @param other the other handle
     * @return a new MultiSlotHandle containing all slots from both
     */
    @NotNull
    public MultiSlotHandle combine(@NotNull MultiSlotHandle other) {
        java.util.Set<Integer> combined = new java.util.LinkedHashSet<>();
        for (int slot : this.slots) {
            combined.add(slot);
        }
        for (int slot : other.slots) {
            combined.add(slot);
        }
        return new MultiSlotHandle(gui, combined.stream().mapToInt(i -> i).toArray());
    }
}
