package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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
 * <h3>Usage Patterns</h3>
 * 
 * <p><strong>Extending GUI:</strong></p>
 * <pre>{@code
 * public class MyCustomGUI extends GUI {
 *     public MyCustomGUI() {
 *         super(GUIType.CHEST_3_ROWS, "&6&lMy Custom Menu");
 *     }
 *     
 *     {@literal @}Override
 *     protected void setup(@NotNull Player player) {
 *         setItem(13, new ItemStack(Material.DIAMOND));
 *         setButton(22, new GUIButton(
 *             new ItemStack(Material.EMERALD),
 *             p -> p.sendMessage("Clicked!")
 *         ));
 *     }
 * }
 * 
 * // Usage:
 * new MyCustomGUI().open(player);
 * }</pre>
 * 
 * <p><strong>Using SimpleGUI directly:</strong></p>
 * <pre>{@code
 * SimpleGUI gui = new SimpleGUI(GUIType.CHEST_3_ROWS, "&6&lMy Menu");
 * gui.setItem(0, new ItemStack(Material.DIAMOND));
 * gui.open(player);
 * }</pre>
 * 
 * <p><strong>Using GUIBuilder:</strong></p>
 * <pre>{@code
 * GUI gui = new GUIBuilder(GUIType.CHEST_3_ROWS, "&6&lMy Menu")
 *     .setItem(0, new ItemStack(Material.DIAMOND))
 *     .setButton(4, myButton)
 *     .setViewOnly(true)
 *     .build();
 * gui.open(player);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public abstract class GUI {
    
    protected final UUID guiId;
    protected final GUIType type;
    protected final String title;
    protected final Map<Integer, GUIButton> buttons;
    protected final Map<Integer, Consumer<Player>> clickHandlers;
    
    protected GUIHolder holder;
    protected Inventory inventory;
    protected boolean viewOnly;
    protected boolean initialized;
    
    // Global click handler
    private BiConsumer<Player, Integer> globalClickHandler;
    // Close handler
    private Consumer<Player> closeHandler;
    
    /**
     * Creates a new GUI with the specified type and title.
     * <p>
     * The inventory is created lazily when {@link #open(Player)} is called.
     * This allows for proper setup before the inventory is shown.
     * </p>
     *
     * @param type the inventory type
     * @param title the display title (supports color codes with &amp;)
     */
    protected GUI(@NotNull GUIType type, @NotNull String title) {
        this.guiId = UUID.randomUUID();
        this.type = type;
        this.title = ColorUtil.colorize(title);
        this.buttons = new HashMap<>();
        this.clickHandlers = new HashMap<>();
        this.viewOnly = true;
        this.initialized = false;
    }
    
    /**
     * Initializes the inventory. Called once before first open.
     */
    protected void initializeInventory() {
        if (initialized) {
            return;
        }
        
        // Create holder with GUIManager UUID
        UUID managerUUID = GUIManager.getInstance().getManagerUUID();
        this.holder = new GUIHolder(managerUUID, guiId, this);
        
        // Create inventory with holder
        this.inventory = Bukkit.createInventory(holder, type.getSize(), title);
        holder.setInventory(inventory);
        
        initialized = true;
    }
    
    /**
     * Called to set up the GUI contents before opening.
     * <p>
     * Override this method to populate the GUI with items and buttons.
     * This is called once per {@link #open(Player)} call, allowing for
     * player-specific content.
     * </p>
     *
     * @param player the player the GUI is being opened for
     */
    protected void setup(@NotNull Player player) {
        // Override in subclasses to add items/buttons
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
     * Gets the size of this GUI in slots.
     *
     * @return the number of slots
     */
    public int getSize() {
        return type.getSize();
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
     * Sets a global click handler that is called for any slot click.
     *
     * @param handler the handler that receives player and slot
     */
    public void setGlobalClickHandler(@Nullable BiConsumer<Player, Integer> handler) {
        this.globalClickHandler = handler;
    }
    
    /**
     * Sets a close handler that is called when the GUI is closed.
     *
     * @param handler the close handler
     */
    public void setCloseHandler(@Nullable Consumer<Player> handler) {
        this.closeHandler = handler;
    }
    
    /**
     * Gets the global click handler.
     *
     * @return the global click handler, or null if none set
     */
    @Nullable
    public BiConsumer<Player, Integer> getGlobalClickHandler() {
        return globalClickHandler;
    }
    
    /**
     * Gets the close handler.
     *
     * @return the close handler, or null if none set
     */
    @Nullable
    public Consumer<Player> getCloseHandler() {
        return closeHandler;
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
        ensureInitialized();
        
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
     * Gets the item at the specified slot.
     *
     * @param slot the slot index
     * @return the item, or null if the slot is empty
     */
    @Nullable
    public ItemStack getItem(int slot) {
        ensureInitialized();
        return inventory.getItem(slot);
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
     * Removes a button from the specified slot.
     *
     * @param slot the slot index
     */
    public void removeButton(int slot) {
        buttons.remove(slot);
        clickHandlers.remove(slot);
        setItem(slot, null);
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
        ensureInitialized();
        inventory.clear();
        buttons.clear();
        clickHandlers.clear();
    }
    
    /**
     * Clears only the content slots (not buttons or static items).
     *
     * @param slots the slots to clear
     */
    public void clearSlots(@NotNull int... slots) {
        ensureInitialized();
        for (int slot : slots) {
            if (slot >= 0 && slot < type.getSize()) {
                inventory.setItem(slot, null);
                buttons.remove(slot);
                clickHandlers.remove(slot);
            }
        }
    }
    
    /**
     * Refreshes the GUI display by re-rendering all buttons.
     */
    public void refresh() {
        ensureInitialized();
        for (Map.Entry<Integer, GUIButton> entry : buttons.entrySet()) {
            setItem(entry.getKey(), entry.getValue().getItem());
        }
    }
    
    /**
     * Fills empty slots with the specified item.
     *
     * @param filler the item to fill with
     */
    public void fillEmpty(@NotNull ItemStack filler) {
        ensureInitialized();
        for (int i = 0; i < type.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                setItem(i, filler);
            }
        }
    }
    
    /**
     * Fills the border slots with the specified item.
     *
     * @param borderItem the border item
     */
    public void fillBorder(@NotNull ItemStack borderItem) {
        ensureInitialized();
        int rows = type.getRows();
        int size = type.getSize();
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            setItem(i, borderItem);
            if (rows > 1) {
                setItem(size - 9 + i, borderItem);
            }
        }
        
        // Side columns
        if (rows > 2) {
            for (int row = 1; row < rows - 1; row++) {
                setItem(row * 9, borderItem);
                setItem(row * 9 + 8, borderItem);
            }
        }
    }
    
    /**
     * Fills the specified slots with an item.
     *
     * @param slots the slots to fill
     * @param item the item to place
     */
    public void fillSlots(@NotNull int[] slots, @NotNull ItemStack item) {
        for (int slot : slots) {
            setItem(slot, item);
        }
    }
    
    /**
     * Ensures the inventory is initialized.
     */
    protected void ensureInitialized() {
        if (!initialized) {
            initializeInventory();
        }
    }
    
    /**
     * Opens this GUI for the specified player.
     * <p>
     * This method is Folia-safe and will execute in the correct region.
     * The {@link #setup(Player)} method is called before opening to populate the GUI.
     * </p>
     *
     * @param player the player to open the GUI for
     */
    public void open(@NotNull Player player) {
        ensureInitialized();
        setup(player);
        GUIManager.getInstance().openGUI(player, this);
    }
    
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
     * The default implementation executes button handlers, slot handlers,
     * and the global click handler in that order.
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
        
        // Execute global click handler if exists
        if (globalClickHandler != null) {
            globalClickHandler.accept(player, slot);
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
     * The default implementation calls the close handler if set.
     * </p>
     *
     * @param player the player who closed the GUI
     */
    public void onClose(@NotNull Player player) {
        if (closeHandler != null) {
            closeHandler.accept(player);
        }
    }
    
    /**
     * Gets the GUI holder.
     *
     * @return the GUI holder
     */
    @NotNull
    public GUIHolder getHolder() {
        ensureInitialized();
        return holder;
    }
    
    /**
     * Gets the inventory.
     *
     * @return the inventory
     */
    @NotNull
    public Inventory getInventory() {
        ensureInitialized();
        return inventory;
    }
    
    /**
     * Checks if this GUI has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
