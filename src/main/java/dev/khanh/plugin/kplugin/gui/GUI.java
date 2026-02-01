package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.gui.holder.GUIHolder;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import dev.khanh.plugin.kplugin.gui.slot.MultiSlotHandle;
import dev.khanh.plugin.kplugin.gui.slot.SlotHandle;
import dev.khanh.plugin.kplugin.gui.slot.SlotRangeHandle;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Core GUI class for creating interactive inventory menus.
 * <p>
 * Use {@link #builder(int)} to create new GUI instances. Access slots via
 * {@link #slot(int)}, {@link #slotRange(int, int)}, or {@link #slots(int...)}.
 * </p>
 *
 * @see GUIBuilder
 * @see SlotHandle
 */
public class GUI {
    
    // ==================== Core Fields ====================
    
    /** Unique identifier for this GUI instance. */
    protected final UUID guiId;
    
    /** Number of rows in this GUI (1-6). */
    protected final int rows;
    
    /** Total size of the inventory (rows * 9). */
    protected final int size;
    
    /** The title displayed at the top of the GUI. */
    protected String title;
    
    /** The GUIHolder instance containing manager UUID and GUI validation data. */
    protected GUIHolder holder;
    
    /** The Bukkit inventory instance. */
    protected Inventory inventory;
    
    /** Whether this GUI has been initialized. */
    protected boolean initialized;
    
    /** Whether this GUI is view-only (blocks all item interactions). */
    protected boolean viewOnly;
    
    // ==================== Slot Data ====================
    
    /** Click handlers per slot */
    protected final Map<Integer, Consumer<ClickContext>> clickHandlers;
    
    /** Disabled slot messages (slot {@literal ->} message) */
    protected final Map<Integer, String> disabledSlots;
    
    /** Per-slot metadata storage */
    protected final Map<Integer, Map<String, Object>> slotMeta;
    
    // ==================== Global Handlers ====================
    
    /** Global click handler for all slots */
    protected Consumer<ClickContext> globalClickHandler;
    
    /** Close handler */
    protected Consumer<Player> closeHandler;
    
    /** Open handler */
    protected Consumer<Player> openHandler;
    
    // ==================== GUI Metadata ====================
    
    /** GUI-wide metadata storage */
    protected final Map<String, Object> guiMeta;
    
    // ==================== Constructors ====================
    
    /**
     * Creates a new GUI.
     *
     * @param rows the number of rows (1-6)
     * @param title the title (supports &amp; color codes)
     */
    protected GUI(int rows, @NotNull String title) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6, got: " + rows);
        }
        
        this.guiId = UUID.randomUUID();
        this.rows = rows;
        this.size = rows * 9;
        this.title = ColorUtil.colorize(title);
        this.viewOnly = true;
        this.initialized = false;
        
        this.clickHandlers = new HashMap<>();
        this.disabledSlots = new HashMap<>();
        this.slotMeta = new HashMap<>();
        this.guiMeta = new HashMap<>();
    }
    
    // ==================== Static Factory Methods ====================
    
    /**
     * Creates a new GUIBuilder.
     *
     * @param rows the number of rows (1-6)
     * @return a new GUIBuilder
     */
    @NotNull
    public static GUIBuilder builder(int rows) {
        return new GUIBuilder(rows);
    }
    
    /**
     * Creates a new GUIBuilder by size.
     *
     * @param size the inventory size (9, 18, 27, 36, 45, or 54)
     * @return a new GUIBuilder
     */
    @NotNull
    public static GUIBuilder builderBySize(int size) {
        if (size % 9 != 0 || size < 9 || size > 54) {
            throw new IllegalArgumentException("Size must be 9, 18, 27, 36, 45, or 54, got: " + size);
        }
        return new GUIBuilder(size / 9);
    }
    
    /**
     * Creates a GUI directly without builder.
     *
     * @param rows the number of rows (1-6)
     * @param title the title
     * @return a new GUI
     */
    @NotNull
    public static GUI create(int rows, @NotNull String title) {
        return new GUI(rows, title);
    }
    
    /**
     * Converts this GUI to a builder for modification.
     * <p>
     * This creates a new GUIBuilder pre-configured with all settings
     * from this GUI instance. Useful for creating modified copies.
     * </p>
     *
     * @return a new GUIBuilder with this GUI's configuration
     */
    @NotNull
    public GUIBuilder toBuilder() {
        return GUIBuilder.from(this);
    }
    
    // ==================== Slot Access Methods ====================
    
    /**
     * Gets a handle for a slot.
     *
     * @param slot the slot index
     * @return a SlotHandle
     */
    @NotNull
    public SlotHandle slot(int slot) {
        validateSlot(slot);
        return new SlotHandle(this, slot);
    }
    
    /**
     * Gets a handle for a range of slots (inclusive).
     *
     * @param start the start slot
     * @param end the end slot
     * @return a SlotRangeHandle
     */
    @NotNull
    public SlotRangeHandle slotRange(int start, int end) {
        validateSlot(start);
        validateSlot(end);
        return new SlotRangeHandle(this, start, end);
    }
    
    /**
     * Gets a handle for multiple slots.
     *
     * @param indices the slot indices
     * @return a MultiSlotHandle
     */
    @NotNull
    public MultiSlotHandle slots(int... indices) {
        for (int slot : indices) {
            validateSlot(slot);
        }
        return new MultiSlotHandle(this, indices);
    }
    
    /**
     * Gets a handle for a row.
     *
     * @param row the row number (0-based)
     * @return a SlotRangeHandle
     */
    @NotNull
    public SlotRangeHandle row(int row) {
        if (row < 0 || row >= rows) {
            throw new IllegalArgumentException("Row " + row + " is out of bounds (0-" + (rows - 1) + ")");
        }
        return slotRange(row * 9, row * 9 + 8);
    }
    
    /**
     * Gets a handle for a column.
     *
     * @param column the column number (0-8)
     * @return a MultiSlotHandle
     */
    @NotNull
    public MultiSlotHandle column(int column) {
        if (column < 0 || column > 8) {
            throw new IllegalArgumentException("Column " + column + " is out of bounds (0-8)");
        }
        int[] columnSlots = new int[rows];
        for (int row = 0; row < rows; row++) {
            columnSlots[row] = row * 9 + column;
        }
        return new MultiSlotHandle(this, columnSlots);
    }
    
    // ==================== Internal Slot Operations ====================
    
    /**
     * Sets an item in a slot. Internal use only.
     *
     * @param slot the slot index
     * @param item the item
     */
    public void setSlotItem(int slot, @Nullable ItemStack item) {
        ensureInitialized();
        if (item == null) {
            inventory.setItem(slot, null);
        } else {
            inventory.setItem(slot, item.clone());
        }
    }
    
    /**
     * Gets the item in a slot. Internal use only.
     *
     * @param slot the slot index
     * @return the item, or null
     */
    @Nullable
    public ItemStack getSlotItem(int slot) {
        ensureInitialized();
        return inventory.getItem(slot);
    }
    
    /**
     * Sets a click handler for a slot. Internal use only.
     *
     * @param slot the slot index
     * @param handler the click handler (null to remove)
     */
    public void setSlotClickHandler(int slot, @Nullable Consumer<ClickContext> handler) {
        if (handler == null) {
            clickHandlers.remove(slot);
        } else {
            clickHandlers.put(slot, handler);
        }
    }
    
    /**
     * Gets the click handler for a slot.
     *
     * @param slot the slot index
     * @return the click handler, or null if none
     */
    @Nullable
    Consumer<ClickContext> getSlotClickHandler(int slot) {
        return clickHandlers.get(slot);
    }
    
    /**
     * Disables a slot with a message. Internal use only.
     *
     * @param slot the slot index
     * @param message the message shown when clicked
     */
    public void setSlotDisabled(int slot, @NotNull String message) {
        disabledSlots.put(slot, ColorUtil.colorize(message));
    }
    
    /**
     * Enables a slot. Internal use only.
     *
     * @param slot the slot index
     */
    public void setSlotEnabled(int slot) {
        disabledSlots.remove(slot);
    }
    
    /**
     * Checks if a slot is disabled. Internal use only.
     *
     * @param slot the slot index
     * @return true if disabled
     */
    public boolean isSlotDisabled(int slot) {
        return disabledSlots.containsKey(slot);
    }
    
    /**
     * Gets the disabled message for a slot.
     *
     * @param slot the slot index
     * @return the message, or null if not disabled
     */
    @Nullable
    String getDisabledMessage(int slot) {
        return disabledSlots.get(slot);
    }
    
    /**
     * Sets slot metadata. Internal use only.
     *
     * @param slot the slot index
     * @param key the key
     * @param value the value
     */
    public void setSlotMeta(int slot, @NotNull String key, @Nullable Object value) {
        Map<String, Object> meta = slotMeta.computeIfAbsent(slot, k -> new HashMap<>());
        if (value == null) {
            meta.remove(key);
        } else {
            meta.put(key, value);
        }
    }
    
    /**
     * Gets slot metadata. Internal use only.
     *
     * @param slot the slot index
     * @param key the key
     * @param <T> the type
     * @return the value, or null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getSlotMeta(int slot, @NotNull String key) {
        Map<String, Object> meta = slotMeta.get(slot);
        if (meta == null) {
            return null;
        }
        return (T) meta.get(key);
    }
    
    // ==================== Global Handlers ====================
    
    /**
     * Sets a global click handler called for all slot clicks.
     *
     * @param handler the handler
     * @return this GUI
     */
    @NotNull
    public GUI onGlobalClick(@Nullable Consumer<ClickContext> handler) {
        this.globalClickHandler = handler;
        return this;
    }
    
    /**
     * Sets a handler called when the GUI is closed.
     *
     * @param handler the handler
     * @return this GUI
     */
    @NotNull
    public GUI onClose(@Nullable Consumer<Player> handler) {
        this.closeHandler = handler;
        return this;
    }
    
    /**
     * Sets a handler called when the GUI is opened.
     *
     * @param handler the handler
     * @return this GUI
     */
    @NotNull
    public GUI onOpen(@Nullable Consumer<Player> handler) {
        this.openHandler = handler;
        return this;
    }
    
    // ==================== GUI Properties ====================
    
    /**
     * Gets the GUI unique identifier.
     *
     * @return the GUI ID
     */
    @NotNull
    public UUID getGuiId() {
        return guiId;
    }
    
    /**
     * Gets the number of rows.
     *
     * @return the row count
     */
    public int getRows() {
        return rows;
    }
    
    /**
     * Gets the total number of slots.
     *
     * @return the slot count
     */
    public int getSize() {
        return size;
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
     * Gets whether this GUI is view-only.
     *
     * @return true if view-only
     */
    public boolean isViewOnly() {
        return viewOnly;
    }
    
    /**
     * Sets whether the GUI is view-only.
     *
     * @param viewOnly true for view-only
     * @return this GUI
     */
    @NotNull
    public GUI setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        return this;
    }
    
    /**
     * Updates the GUI title and refreshes for all viewers.
     * <p>
     * This method recreates the inventory with a new title and
     * reopens it for all current viewers. All items and handlers
     * are preserved.
     * </p>
     * <p>
     * <b>Note:</b> This operation will briefly close and reopen
     * the GUI for all viewers. Use sparingly for performance.
     * </p>
     *
     * @param newTitle the new title (supports &amp; color codes)
     * @return this GUI
     */
    @NotNull
    public GUI updateTitle(@NotNull String newTitle) {
        if (!initialized) {
            // If not initialized yet, just update the title field
            this.title = ColorUtil.colorize(newTitle);
            return this;
        }
        
        // Store current viewers
        List<Player> viewers = new ArrayList<>();
        for (org.bukkit.entity.HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
            if (viewer instanceof Player) {
                viewers.add((Player) viewer);
            }
        }
        
        // Update title
        this.title = ColorUtil.colorize(newTitle);
        
        // Store all current items
        ItemStack[] contents = inventory.getContents().clone();
        
        // Recreate inventory with new title (reuse existing holder)
        this.inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);
        
        // Restore all items
        inventory.setContents(contents);
        
        // Reopen for all viewers
        for (Player viewer : viewers) {
            viewer.openInventory(inventory);
        }
        
        return this;
    }
    
    // ==================== GUI Metadata ====================
    
    /**
     * Sets GUI metadata.
     *
     * @param key the key
     * @param value the value
     * @return this GUI
     */
    @NotNull
    public GUI setMeta(@NotNull String key, @Nullable Object value) {
        if (value == null) {
            guiMeta.remove(key);
        } else {
            guiMeta.put(key, value);
        }
        return this;
    }
    
    /**
     * Gets GUI metadata.
     *
     * @param key the key
     * @param <T> the type
     * @return the value, or null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getMeta(@NotNull String key) {
        return (T) guiMeta.get(key);
    }
    
    /**
     * Gets GUI metadata with default value.
     *
     * @param key the key
     * @param defaultValue the default
     * @param <T> the type
     * @return the value, or default if not found
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getMeta(@NotNull String key, @NotNull T defaultValue) {
        Object value = guiMeta.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Fills the border with an item.
     *
     * @param item the border item
     * @return this GUI
     */
    @NotNull
    public GUI fillBorder(@NotNull ItemStack item) {
        ensureInitialized();
        
        // Top row
        for (int i = 0; i < 9; i++) {
            setSlotItem(i, item);
        }
        
        // Bottom row
        if (rows > 1) {
            for (int i = 0; i < 9; i++) {
                setSlotItem(size - 9 + i, item);
            }
        }
        
        // Side columns
        if (rows > 2) {
            for (int row = 1; row < rows - 1; row++) {
                setSlotItem(row * 9, item);
                setSlotItem(row * 9 + 8, item);
            }
        }
        
        return this;
    }
    
    /**
     * Fills the border using an ItemBuilder.
     *
     * @param builder the item builder
     * @return this GUI for chaining
     */
    @NotNull
    public GUI fillBorder(@NotNull ItemBuilder builder) {
        return fillBorder(builder.build());
    }
    
    /**
     * Fills empty slots with an item.
     *
     * @param item the filler item
     * @return this GUI
     */
    @NotNull
    public GUI fillEmpty(@NotNull ItemStack item) {
        ensureInitialized();
        for (int i = 0; i < size; i++) {
            ItemStack current = inventory.getItem(i);
            if (current == null || current.getType().isAir()) {
                setSlotItem(i, item);
            }
        }
        return this;
    }
    
    /**
     * Fills all empty slots using an ItemBuilder.
     *
     * @param builder the item builder
     * @return this GUI for chaining
     */
    @NotNull
    public GUI fillEmpty(@NotNull ItemBuilder builder) {
        return fillEmpty(builder.build());
    }
    
    /**
     * Clears all items and handlers.
     *
     * @return this GUI
     */
    @NotNull
    public GUI clear() {
        ensureInitialized();
        inventory.clear();
        clickHandlers.clear();
        disabledSlots.clear();
        slotMeta.clear();
        return this;
    }
    
    /**
     * Refreshes the GUI for all viewers.
     *
     * @return this GUI
     */
    @NotNull
    public GUI refresh() {
        if (initialized && inventory != null) {
            for (org.bukkit.entity.HumanEntity viewer : inventory.getViewers()) {
                if (viewer instanceof Player) {
                    ((Player) viewer).updateInventory();
                }
            }
        }
        return this;
    }
    
    // ==================== Inventory Access ====================
    
    /**
     * Gets the Bukkit inventory.
     *
     * @return the inventory
     */
    @NotNull
    public Inventory getInventory() {
        ensureInitialized();
        return inventory;
    }
    
    /**
     * Gets the GUI holder.
     *
     * @return the holder
     */
    @NotNull
    public GUIHolder getHolder() {
        ensureInitialized();
        return holder;
    }
    
    /**
     * Checks if this GUI has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    // ==================== Open/Close ====================
    
    /**
     * Opens this GUI for a player.
     *
     * @param player the player
     */
    public void open(@NotNull Player player) {
        ensureInitialized();
        GUIManager.getInstance().openGUI(player, this);
    }
    
    /**
     * Closes this GUI for a player.
     *
     * @param player the player
     */
    public void close(@NotNull Player player) {
        player.closeInventory();
    }
    
    // ==================== Event Callbacks ====================
    
    /**
     * Handles a click event. Internal use only.
     *
     * @param context the click context
     */
    public void handleClick(@NotNull ClickContext context) {
        int slot = context.slot();
        
        // Check if slot is disabled
        String disabledMsg = disabledSlots.get(slot);
        if (disabledMsg != null) {
            context.cancel();
            context.player().sendMessage(disabledMsg);
            return;
        }
        
        // Execute slot-specific handler
        Consumer<ClickContext> handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(context);
        }
        
        // Execute global handler
        if (globalClickHandler != null) {
            globalClickHandler.accept(context);
        }
    }
    
    /**
     * Called when this GUI is opened.
     *
     * @param player the player who opened the GUI
     */
    public void handleOpen(@NotNull Player player) {
        if (openHandler != null) {
            openHandler.accept(player);
        }
    }
    
    /**
     * Called when this GUI is closed.
     *
     * @param player the player who closed the GUI
     */
    public void handleClose(@NotNull Player player) {
        if (closeHandler != null) {
            closeHandler.accept(player);
        }
    }
    
    // ==================== Internal Methods ====================
    
    /**
     * Ensures the inventory is initialized.
     */
    protected void ensureInitialized() {
        if (!initialized) {
            initializeInventory();
        }
    }
    
    /**
     * Initializes the inventory.
     */
    protected void initializeInventory() {
        if (initialized) {
            return;
        }
        
        // Create holder with GUIManager UUID
        UUID managerUUID = GUIManager.getInstance().getManagerUUID();
        this.holder = new GUIHolder(managerUUID, guiId, this);
        
        // Create inventory
        this.inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);
        
        initialized = true;
    }
    
    /**
     * Validates that a slot index is within bounds.
     *
     * @param slot the slot index
     */
    protected void validateSlot(int slot) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException("Slot " + slot + " is out of bounds (0-" + (size - 1) + ")");
        }
    }
}
