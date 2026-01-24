package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstract base class for all GUI implementations.
 * <p>
 * This class provides the foundation for creating interactive inventory-based GUIs
 * in Minecraft. It handles inventory creation, item placement, click handling,
 * and security features to prevent item duplication exploits.
 * </p>
 * 
 * <p>
 * GUI validation is performed using {@link GUIHolder} which tracks the GUIManager UUID
 * and GUI instance UUID. This approach prevents exploits from plugin reloads and
 * is more efficient than item-based PDC validation.
 * </p>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public abstract class GUI {
    
    protected final UUID guiId;
    protected final GUIType type;
    protected final String title;
    protected final GUIHolder holder;
    protected final Inventory inventory;
    protected final Map<Integer, GUIButton> buttons;
    protected final Map<Integer, Consumer<Player>> clickHandlers;
    protected boolean viewOnly;
    
    /**
     * Creates a new GUI with the specified type and title.
     *
     * @param type the inventory type
     * @param title the display title (supports color codes with &amp;)
     */
    protected GUI(@NotNull GUIType type, @NotNull String title) {
        this.guiId = UUID.randomUUID();
        this.type = type;
        this.title = title;
        
        // Create holder with GUIManager UUID
        UUID managerUUID = GUIManager.getInstance().getManagerUUID();
        this.holder = new GUIHolder(managerUUID, guiId, this);
        
        // Create inventory with holder
        this.inventory = Bukkit.createInventory(holder, type.getSize(), title);
        holder.setInventory(inventory);
        
        this.buttons = new HashMap<>();
        this.clickHandlers = new HashMap<>();
        this.viewOnly = true;
    }
    
    /**
     * Gets the unique identifier for this GUI instance.
     *
     * @return the GUI ID
     */
    @NotNull
    public UUID getGuiId() {
        return guiId;
    }
    
    /**
     * Gets the GUI type.
     *
     * @return the GUI type
     */
    @NotNull
    public GUIType getType() {
        return type;
    }
    
    /**
     * Gets the GUI title.
     *
     * @return the title
     */
    @NotNull
    public String getTitle() {
        return title;
    }
    
    /**
     * Gets whether this GUI is view-only (prevents item removal/placement).
     *
     * @return true if view-only, false if interactive
     */
    public boolean isViewOnly() {
        return viewOnly;
    }
    
    /**
     * Sets whether this GUI is view-only.
     *
     * @param viewOnly true to make view-only, false to allow interaction
     */
    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }
    
    /**
     * Sets an item in the specified slot.
     * <p>
     * The item is cloned to prevent shared references.
     * Security validation is handled via {@link GUIHolder}.
     * </p>
     *
     * @param slot the slot index (0-based)
     * @param item the item to set (null to clear)
     */
    public void setItem(int slot, @Nullable ItemStack item) {
        if (slot < 0 || slot >= type.getSize()) {
            throw new IllegalArgumentException("Slot " + slot + " is out of bounds for " + type);
        }
        
        if (item == null) {
            inventory.setItem(slot, null);
            return;
        }
        
        // Clone to prevent shared references
        ItemStack cloned = item.clone();
        
        inventory.setItem(slot, cloned);
    }
    
    /**
     * Sets a button in the specified slot.
     *
     * @param slot the slot index
     * @param button the button to set
     */
    public void setButton(int slot, @NotNull GUIButton button) {
        buttons.put(slot, button);
        setItem(slot, button.getItem());
    }
    
    /**
     * Sets a click handler for the specified slot.
     *
     * @param slot the slot index
     * @param handler the click handler
     */
    public void setClickHandler(int slot, @NotNull Consumer<Player> handler) {
        clickHandlers.put(slot, handler);
    }
    
    /**
     * Gets the button at the specified slot.
     *
     * @param slot the slot index
     * @return the button, or null if none exists
     */
    @Nullable
    public GUIButton getButton(int slot) {
        return buttons.get(slot);
    }
    
    /**
     * Gets the click handler for the specified slot.
     *
     * @param slot the slot index
     * @return the click handler, or null if none exists
     */
    @Nullable
    public Consumer<Player> getClickHandler(int slot) {
        return clickHandlers.get(slot);
    }
    
    /**
     * Clears all items and buttons from the GUI.
     */
    public void clear() {
        inventory.clear();
        buttons.clear();
        clickHandlers.clear();
    }
    
    /**
     * Refreshes the GUI display by re-rendering all buttons.
     */
    public void refresh() {
        for (Map.Entry<Integer, GUIButton> entry : buttons.entrySet()) {
            setItem(entry.getKey(), entry.getValue().getItem());
        }
    }
    
    /**
     * Opens this GUI for the specified player.
     * <p>
     * This method is Folia-safe and will execute in the correct region.
     * </p>
     *
     * @param player the player to open the GUI for
     */
    public abstract void open(@NotNull Player player);
    
    /**
     * Closes this GUI for the specified player.
     *
     * @param player the player to close the GUI for
     */
    public void close(@NotNull Player player) {
        player.closeInventory();
    }
    
    /**
     * Called when a player clicks in this GUI.
     * <p>
     * Override this method to handle custom click logic.
     * </p>
     *
     * @param player the player who clicked
     * @param slot the slot that was clicked
     * @param item the item that was clicked (may be null)
     */
    public void onClick(@NotNull Player player, int slot, @Nullable ItemStack item) {
        // Execute button click handler if exists
        GUIButton button = buttons.get(slot);
        if (button != null) {
            button.onClick(player);
        }
        
        // Execute custom click handler if exists
        Consumer<Player> handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(player);
        }
    }
    
    /**
     * Called when this GUI is opened for a player.
     * <p>
     * Override this method to perform custom logic on open.
     * </p>
     *
     * @param player the player who opened the GUI
     */
    public void onOpen(@NotNull Player player) {
        // Override in subclasses
    }
    
    /**
     * Called when this GUI is closed for a player.
     * <p>
     * Override this method to perform custom logic on close.
     * </p>
     *
     * @param player the player who closed the GUI
     */
    public void onClose(@NotNull Player player) {
        // Override in subclasses
    }
    
    /**
     * Gets the GUI holder.
     *
     * @return the GUI holder
     */
    @NotNull
    public GUIHolder getHolder() {
        return holder;
    }
    
    /**
     * Gets the inventory.
     *
     * @return the inventory
     */
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}
