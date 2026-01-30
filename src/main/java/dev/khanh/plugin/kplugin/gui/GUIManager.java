package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.KPlugin;
import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.gui.holder.GUIHolder;
import dev.khanh.plugin.kplugin.instance.InstanceManager;
import dev.khanh.plugin.kplugin.util.LoggerUtil;
import dev.khanh.plugin.kplugin.util.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for GUI operations and event handling.
 * <p>
 * Singleton that manages GUI lifecycle, player tracking, and security validation.
 * Automatically registered in {@link InstanceManager}.
 * </p>
 *
 * @see GUI
 * @see GUIHolder
 */
public class GUIManager implements Listener {
    
    private final KPlugin plugin;
    private final UUID managerUUID;
    private final Map<UUID, GUI> openGUIs;
    
    /**
     * Creates and initializes the GUI manager.
     *
     * @param plugin the plugin instance
     */
    public GUIManager(@NotNull KPlugin plugin) {
        this.plugin = plugin;
        this.managerUUID = UUID.randomUUID();
        this.openGUIs = new ConcurrentHashMap<>();
        
        // Register in InstanceManager
        InstanceManager.registerInstance(GUIManager.class, this);
        
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, plugin);

        LoggerUtil.info("GUIManager initialized");
    }
    
    /**
     * Gets the GUIManager instance.
     *
     * @return the GUIManager
     */
    @NotNull
    public static GUIManager getInstance() {
        return InstanceManager.getInstanceOrElseThrow(GUIManager.class);
    }

    /**
     * Gets the manager UUID.
     *
     * @return the manager UUID
     */
    @NotNull
    public UUID getManagerUUID() {
        return managerUUID;
    }
    
    /**
     * Opens a GUI for a player.
     *
     * @param player the player
     * @param gui the GUI
     */
    public void openGUI(@NotNull Player player, @NotNull GUI gui) {
        TaskUtil.runAtEntity(player, () -> {
            player.openInventory(gui.getInventory());
            // Note: GUI tracking is handled by onInventoryOpen event
        });
    }
    
    /**
     * Closes the open GUI for a player.
     *
     * @param player the player
     */
    public void closeGUI(@NotNull Player player) {
        TaskUtil.runAtEntity(player, () -> {
            GUI gui = openGUIs.remove(player.getUniqueId());
            if (gui != null) {
                player.closeInventory();
                gui.handleClose(player);
            }
        });
    }
    
    /**
     * Gets the GUI open for a player.
     *
     * @param player the player
     * @return the GUI, or null
     */
    @Nullable
    public GUI getOpenGUI(@NotNull Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    /**
     * Gets the GUI for an inventory.
     *
     * @param inventory the inventory
     * @return the GUI, or null if not a GUI or stale
     */
    @Nullable
    public GUI getGUI(@NotNull Inventory inventory) {
        if (inventory.getHolder() instanceof GUIHolder) {
            GUIHolder holder = (GUIHolder) inventory.getHolder();
            
            // Validate holder belongs to current manager instance
            if (!holder.isValid(managerUUID)) {
                LoggerUtil.warning("Detected stale GUI from previous plugin instance");
                return null;
            }
            
            return holder.getGui();
        }
        return null;
    }
    
    /**
     * Checks if a player has a GUI open.
     *
     * @param player the player
     * @return true if GUI is open
     */
    public boolean hasGUIOpen(@NotNull Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * Clears all tracked GUIs without closing them.
     */
    public void clearAll() {
        openGUIs.clear();
    }
    
    /**
     * Closes all open GUIs for all players.
     */
    public void closeAll() {
        // Create a copy to avoid ConcurrentModificationException
        Map<UUID, GUI> guisCopy = new ConcurrentHashMap<>(openGUIs);
        
        for (Map.Entry<UUID, GUI> entry : guisCopy.entrySet()) {
            UUID playerId = entry.getKey();
            GUI gui = entry.getValue();
            
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Runnable run = () -> {
                    openGUIs.remove(playerId);
                    player.closeInventory();
                    gui.handleClose(player);
                };
                if (TaskUtil.isFolia()) {
                    TaskUtil.runAtEntity(player, run);
                }
                run.run();
            } else {
                // Player is offline, just remove from tracking
                openGUIs.remove(playerId);
            }
        }
    }
    
    /**
     * Shuts down the manager and cleans up all resources.
     */
    public void shutdown() {
        LoggerUtil.info("Shutting down GUIManager...");
        
        // Close all open GUIs
        closeAll();
        
        // Clear all tracking data
        openGUIs.clear();
        
        LoggerUtil.info("GUIManager shutdown complete. Closed all open GUIs.");
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * Handles inventory click events for GUIs.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        GUI gui = getGUI(event.getInventory());
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Check if it's a stale GUI
        if (gui == null && holder != null && !(holder instanceof GUIHolder) && isGUIHolderClass(holder)) {
            event.setCancelled(true);
            player.closeInventory();
            LoggerUtil.info("Closed stale GUI for player " + player.getName());
            return;
        }
        
        if (gui == null) {
            return;
        }
        
        // Cancel if view-only
        if (gui.isViewOnly()) {
            event.setCancelled(true);
        }
        
        // Get clicked slot
        int slot = event.getRawSlot();
        
        // Only process clicks inside the GUI inventory
        if (slot < 0 || slot >= gui.getSize()) {
            // Allow player inventory clicks only if not view-only
            if (gui.isViewOnly()) {
                event.setCancelled(true);
            }
            return;
        }
        
        // Get clicked item
        ItemStack clicked = event.getCurrentItem();
        
        // Create click context
        ClickContext context = new ClickContext(
            player,
            gui,
            slot,
            clicked,
            event.getClick(),
            event
        );
        
        // Route to GUI handler
        gui.handleClick(context);
    }
    
    /**
     * Prevents item dragging in GUIs.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        GUI gui = getGUI(event.getInventory());
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Check for stale GUI
        if (gui == null && holder != null && !(holder instanceof GUIHolder) && isGUIHolderClass(holder)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            LoggerUtil.info("Closed stale GUI for player " + player.getName());
            return;
        }
        
        if (gui == null) {
            return;
        }
        
        // Cancel drag if any affected slot is in the GUI
        for (int slot : event.getRawSlots()) {
            if (slot < gui.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    /**
     * Handles inventory close events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        GUI gui = openGUIs.remove(player.getUniqueId());
        
        if (gui != null) {
            gui.handleClose(player);
        }
    }
    
    /**
     * Handles inventory open events for tracking.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        GUI gui = getGUI(event.getInventory());
        if (gui != null) {
            openGUIs.put(player.getUniqueId(), gui);
            gui.handleOpen(player);
        }
    }
    
    /**
     * Cleans up player data on disconnect.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }

    private boolean isGUIHolderClass(@Nullable InventoryHolder holder) {
        return holder != null && holder.getClass().getName().equals("dev.khanh.plugin.kplugin.gui.holder.GUIHolder");
    }
}
