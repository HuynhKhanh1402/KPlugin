package dev.khanh.plugin.kplugin.gui.slot;

import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Handle for a single GUI slot, providing fluent API for slot configuration.
 *
 * @see GUI#slot(int)
 */
public final class SlotHandle {
    
    private final GUI gui;
    private final int slot;
    
    /**
     * Creates a slot handle. Internal use only.
     *
     * @param gui the GUI
     * @param slot the slot index
     */
    public SlotHandle(@NotNull GUI gui, int slot) {
        this.gui = gui;
        this.slot = slot;
    }
    
    /**
     * Gets the slot index this handle represents.
     *
     * @return the slot index
     */
    public int index() {
        return slot;
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
     * Sets the item in this slot.
     *
     * @param item the item (null to clear)
     * @return this handle
     */
    @NotNull
    public SlotHandle set(@Nullable ItemStack item) {
        gui.setSlotItem(slot, item);
        return this;
    }
    
    /**
     * Sets an item built from an ItemBuilder.
     *
     * @param builder the item builder
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle set(@NotNull ItemBuilder builder) {
        return set(builder.build());
    }
    
    /**
     * Clears the item from this slot.
     *
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle clear() {
        return set((ItemStack) null);
    }
    
    /**
     * Gets the current item in this slot.
     *
     * @return the item, or null if empty
     */
    @Nullable
    public ItemStack item() {
        return gui.getSlotItem(slot);
    }
    
    /**
     * Sets a click handler for this slot.
     *
     * @param handler the handler
     * @return this handle
     */
    @NotNull
    public SlotHandle onClick(@NotNull Consumer<ClickContext> handler) {
        gui.setSlotClickHandler(slot, handler);
        return this;
    }
    
    /**
     * Clears the click handler for this slot.
     *
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle clearClickHandler() {
        gui.setSlotClickHandler(slot, null);
        return this;
    }
    
    /**
     * Disables this slot with a message.
     *
     * @param message the message (supports &amp; codes)
     * @return this handle
     */
    @NotNull
    public SlotHandle disable(@NotNull String message) {
        gui.setSlotDisabled(slot, message);
        return this;
    }
    
    /**
     * Enables this slot (removes disabled state).
     *
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle enable() {
        gui.setSlotEnabled(slot);
        return this;
    }
    
    /**
     * Checks if this slot is disabled.
     *
     * @return true if disabled
     */
    public boolean isDisabled() {
        return gui.isSlotDisabled(slot);
    }
    
    /**
     * Updates the item in this slot using a consumer.
     * <p>
     * This is useful for modifying the current item without replacing it entirely.
     * If the slot is empty, the consumer is not called.
     * </p>
     *
     * @param updater the item updater consumer
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle update(@NotNull Consumer<ItemStack> updater) {
        ItemStack current = item();
        if (current != null) {
            updater.accept(current);
            set(current);
        }
        return this;
    }
    
    /**
     * Replaces the item using a transformation function.
     *
     * @param transformer the transformation function
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle transform(@NotNull java.util.function.Function<ItemStack, ItemStack> transformer) {
        ItemStack current = item();
        if (current != null) {
            set(transformer.apply(current));
        }
        return this;
    }
    
    /**
     * Checks if this slot is empty.
     *
     * @return true if no item is in this slot
     */
    public boolean isEmpty() {
        ItemStack item = item();
        return item == null || item.getType().isAir();
    }
    
    /**
     * Sets metadata for this slot.
     * <p>
     * Slot metadata can be used to store custom data associated with the slot,
     * which can be accessed during click handling via {@link ClickContext}.
     * </p>
     *
     * @param key the metadata key
     * @param value the metadata value
     * @return this handle for chaining
     */
    @NotNull
    public SlotHandle setMeta(@NotNull String key, @Nullable Object value) {
        gui.setSlotMeta(slot, key, value);
        return this;
    }
    
    /**
     * Gets metadata for this slot.
     *
     * @param key the metadata key
     * @param <T> the expected type
     * @return the metadata value, or null if not found
     */
    @Nullable
    public <T> T getMeta(@NotNull String key) {
        return gui.getSlotMeta(slot, key);
    }

    @NotNull
    public <T> T getMeta(@NotNull String key, @NotNull T defaultValue) {
        T value = getMeta(key);
        return value != null ? value : defaultValue;
    }
}
