package dev.khanh.plugin.kplugin.gui.context;

import dev.khanh.plugin.kplugin.gui.GUI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context object for GUI click events.
 * <p>
 * Provides access to player, GUI, slot, item, click type, and the underlying event.
 * Also includes metadata storage and utility methods.
 * </p>
 */
public final class ClickContext {
    
    private final Player player;
    private final GUI gui;
    private final int slot;
    private final ItemStack item;
    private final ClickType clickType;
    private final InventoryClickEvent event;
    private final Map<String, Object> meta;
    private boolean cancelled;
    
    /**
     * Creates a click context. Internal use only.
     *
     * @param player the player
     * @param gui the GUI
     * @param slot the slot
     * @param item the item
     * @param clickType the click type
     * @param event the event
     */
    public ClickContext(@NotNull Player player, @NotNull GUI gui, int slot,
                        @Nullable ItemStack item, @NotNull ClickType clickType,
                        @NotNull InventoryClickEvent event) {
        this.player = player;
        this.gui = gui;
        this.slot = slot;
        this.item = item;
        this.clickType = clickType;
        this.event = event;
        this.meta = new HashMap<>();
        this.cancelled = false;
    }
    
    /**
     * Gets the player.
     *
     * @return the player
     */
    @NotNull
    public Player player() {
        return player;
    }
    
    /**
     * Gets the GUI that was clicked.
     *
     * @return the GUI instance
     */
    @NotNull
    public GUI gui() {
        return gui;
    }
    
    /**
     * Gets the slot index that was clicked.
     *
     * @return the slot index (0-based)
     */
    public int slot() {
        return slot;
    }
    
    /**
     * Gets the item in the clicked slot.
     *
     * @return the item, or null if the slot was empty
     */
    @Nullable
    public ItemStack item() {
        return item;
    }
    
    /**
     * Gets the item in the clicked slot as an Optional.
     *
     * @return an Optional containing the item, or empty if slot was empty
     */
    @NotNull
    public Optional<ItemStack> itemOptional() {
        return Optional.ofNullable(item);
    }
    
    /**
     * Gets the type of click performed.
     *
     * @return the click type
     */
    @NotNull
    public ClickType clickType() {
        return clickType;
    }
    
    /**
     * Gets the underlying Bukkit inventory click event.
     * <p>
     * Use this when you need access to additional event data not exposed
     * through ClickContext directly.
     * </p>
     *
     * @return the Bukkit event
     */
    @NotNull
    public InventoryClickEvent event() {
        return event;
    }
    
    /**
     * Checks if this was a left click.
     *
     * @return true if left click
     */
    public boolean isLeftClick() {
        return clickType.isLeftClick();
    }
    
    /**
     * Checks if this was a right click.
     *
     * @return true if right click
     */
    public boolean isRightClick() {
        return clickType.isRightClick();
    }
    
    /**
     * Checks if this was a shift click.
     *
     * @return true if shift was held
     */
    public boolean isShiftClick() {
        return clickType.isShiftClick();
    }
    
    /**
     * Checks if this was a middle (scroll wheel) click.
     *
     * @return true if middle click
     */
    public boolean isMiddleClick() {
        return clickType == ClickType.MIDDLE;
    }
    
    /**
     * Checks if this was a double click.
     *
     * @return true if double click
     */
    public boolean isDoubleClick() {
        return clickType == ClickType.DOUBLE_CLICK;
    }
    
    /**
     * Checks if a number key (1-9) was pressed.
     *
     * @return true if a number key was pressed
     */
    public boolean isNumberKey() {
        return clickType == ClickType.NUMBER_KEY;
    }
    
    /**
     * Gets the number key pressed (0-8), or -1 if no number key was pressed.
     *
     * @return the hotbar slot number (0-8), or -1
     */
    public int getHotbarButton() {
        return event.getHotbarButton();
    }
    
    /**
     * Cancels the underlying inventory click event.
     * <p>
     * This prevents any item movement or slot interaction.
     * Equivalent to calling {@code event.setCancelled(true)}.
     * </p>
     */
    public void cancel() {
        this.cancelled = true;
        event.setCancelled(true);
    }
    
    /**
     * Checks if this event has been cancelled.
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Plays a sound to the player at their location.
     *
     * @param sound the sound to play
     */
    public void playSound(@NotNull Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
    
    /**
     * Plays a sound to the player at their location with custom volume and pitch.
     *
     * @param sound the sound to play
     * @param volume the volume (1.0 is normal)
     * @param pitch the pitch (1.0 is normal)
     */
    public void playSound(@NotNull Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
    
    /**
     * Closes the GUI for the player.
     */
    public void close() {
        gui.close(player);
    }
    
    /**
     * Gets a metadata value.
     *
     * @param key the metadata key
     * @param <T> the expected type
     * @return the value, or null if not found
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getMeta(@NotNull String key) {
        return (T) meta.get(key);
    }
    
    /**
     * Gets a metadata value with a default.
     *
     * @param key the metadata key
     * @param defaultValue the default value if not found
     * @param <T> the expected type
     * @return the value, or the default if not found
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getMeta(@NotNull String key, @NotNull T defaultValue) {
        Object value = meta.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * Sets a metadata value.
     *
     * @param key the metadata key
     * @param value the value to store
     * @return this context for chaining
     */
    @NotNull
    public ClickContext setMeta(@NotNull String key, @Nullable Object value) {
        if (value == null) {
            meta.remove(key);
        } else {
            meta.put(key, value);
        }
        return this;
    }
    
    /**
     * Checks if a metadata key exists.
     *
     * @param key the metadata key
     * @return true if the key exists
     */
    public boolean hasMeta(@NotNull String key) {
        return meta.containsKey(key);
    }
    
    /**
     * Gets the full metadata map.
     *
     * @return the metadata map (modifiable)
     */
    @NotNull
    public Map<String, Object> meta() {
        return meta;
    }
}
