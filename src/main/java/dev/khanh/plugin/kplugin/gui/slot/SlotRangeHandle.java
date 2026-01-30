package dev.khanh.plugin.kplugin.gui.slot;

import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handle for a contiguous range of slots.
 * <p>
 * Provides bulk operations like filling, iteration, and applying click handlers.
 * </p>
 *
 * @see GUI#slotRange(int, int)
 */
public final class SlotRangeHandle {
    
    private final GUI gui;
    private final int startSlot;
    private final int endSlot;
    
    /**
     * Creates a slot range handle. Internal use only.
     *
     * @param gui the GUI
     * @param startSlot the start slot (inclusive)
     * @param endSlot the end slot (inclusive)
     */
    public SlotRangeHandle(@NotNull GUI gui, int startSlot, int endSlot) {
        this.gui = gui;
        this.startSlot = Math.min(startSlot, endSlot);
        this.endSlot = Math.max(startSlot, endSlot);
    }
    
    /**
     * Gets the start slot of this range (inclusive).
     *
     * @return the start slot
     */
    public int start() {
        return startSlot;
    }
    
    /**
     * Gets the end slot of this range (inclusive).
     *
     * @return the end slot
     */
    public int end() {
        return endSlot;
    }
    
    /**
     * Gets the number of slots in this range.
     *
     * @return the size of the range
     */
    public int size() {
        return endSlot - startSlot + 1;
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
     * Fills all slots with an item.
     *
     * @param item the item
     * @return this handle
     */
    @NotNull
    public SlotRangeHandle fill(@Nullable ItemStack item) {
        for (int slot = startSlot; slot <= endSlot; slot++) {
            gui.setSlotItem(slot, item);
        }
        return this;
    }
    
    /**
     * Fills all slots in this range with an item built from a builder.
     *
     * @param builder the item builder
     * @return this handle for chaining
     */
    @NotNull
    public SlotRangeHandle fill(@NotNull ItemBuilder builder) {
        ItemStack item = builder.build();
        return fill(item);
    }
    
    /**
     * Fills empty slots with an item.
     *
     * @param item the item
     * @return this handle
     */
    @NotNull
    public SlotRangeHandle fillEmpty(@Nullable ItemStack item) {
        for (int slot = startSlot; slot <= endSlot; slot++) {
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
    public SlotRangeHandle fillAlternating(@Nullable ItemStack item1, @Nullable ItemStack item2) {
        boolean useFirst = true;
        for (int slot = startSlot; slot <= endSlot; slot++) {
            gui.setSlotItem(slot, useFirst ? item1 : item2);
            useFirst = !useFirst;
        }
        return this;
    }
    
    /**
     * Clears all slots in this range.
     *
     * @return this handle for chaining
     */
    @NotNull
    public SlotRangeHandle clear() {
        return fill((ItemStack) null);
    }
    
    /**
     * Iterates over all slots.
     *
     * @param action the action receiving slot index and SlotHandle
     * @return this handle
     */
    @NotNull
    public SlotRangeHandle forEach(@NotNull BiConsumer<Integer, SlotHandle> action) {
        for (int slot = startSlot; slot <= endSlot; slot++) {
            action.accept(slot, gui.slot(slot));
        }
        return this;
    }
    
    /**
     * Iterates over all slots in this range with just the slot index.
     *
     * @param action the action to perform, receiving slot index
     * @return this handle for chaining
     */
    @NotNull
    public SlotRangeHandle forEachIndex(@NotNull Consumer<Integer> action) {
        for (int slot = startSlot; slot <= endSlot; slot++) {
            action.accept(slot);
        }
        return this;
    }
    
    /**
     * Sets a click handler for all slots in this range.
     *
     * @param handler the click handler
     * @return this handle for chaining
     */
    @NotNull
    public SlotRangeHandle onClick(@NotNull Consumer<ClickContext> handler) {
        for (int slot = startSlot; slot <= endSlot; slot++) {
            gui.setSlotClickHandler(slot, handler);
        }
        return this;
    }
    
    /**
     * Disables all slots in this range with a message.
     *
     * @param message the message to show when clicked
     * @return this handle for chaining
     */
    @NotNull
    public SlotRangeHandle disable(@NotNull String message) {
        for (int slot = startSlot; slot <= endSlot; slot++) {
            gui.setSlotDisabled(slot, message);
        }
        return this;
    }
    
    /**
     * Enables all slots in this range.
     *
     * @return this handle for chaining
     */
    @NotNull
    public SlotRangeHandle enable() {
        for (int slot = startSlot; slot <= endSlot; slot++) {
            gui.setSlotEnabled(slot);
        }
        return this;
    }
    
    /**
     * Gets the slot indices in this range as an array.
     *
     * @return array of slot indices
     */
    public int @NotNull [] toArray() {
        int[] slots = new int[size()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = startSlot + i;
        }
        return slots;
    }
    
    /**
     * Filters slots to only those where the predicate returns true.
     *
     * @param predicate the predicate to test each slot
     * @return a MultiSlotHandle containing only matching slots
     */
    @NotNull
    public MultiSlotHandle filter(@NotNull java.util.function.IntPredicate predicate) {
        java.util.List<Integer> matching = new java.util.ArrayList<>();
        for (int slot = startSlot; slot <= endSlot; slot++) {
            if (predicate.test(slot)) {
                matching.add(slot);
            }
        }
        return new MultiSlotHandle(gui, matching.stream().mapToInt(i -> i).toArray());
    }
    
    /**
     * Gets slots at the edges of this range (useful for borders).
     * <p>
     * This returns slots that are on the first row, last row, first column, or last column
     * within the range, assuming a 9-wide inventory.
     * </p>
     *
     * @return a MultiSlotHandle containing edge slots
     */
    @NotNull
    public MultiSlotHandle edges() {
        return filter(slot -> {
            int row = slot / 9;
            int col = slot % 9;
            int startRow = startSlot / 9;
            int endRow = endSlot / 9;
            int startCol = startSlot % 9;
            int endCol = endSlot % 9;
            
            return row == startRow || row == endRow || col == startCol || col == endCol;
        });
    }
}
