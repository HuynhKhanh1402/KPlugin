package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.KPlugin;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all GUI operations.
 * <p>
 * This singleton class handles GUI lifecycle management, player tracking,
 * event handling, and security enforcement. It is automatically registered
 * in the {@link InstanceManager} for global access.
 * </p>
 * 
 * <p>
 * The GUIManager provides:
 * <ul>
 *   <li>Folia-safe GUI opening and closing</li>
 *   <li>Player-to-GUI mapping for event routing</li>
 *   <li>Security validation to prevent item duplication</li>
 *   <li>Automatic cleanup on player disconnect</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Initialization:</strong></p>
 * <pre>{@code
 * // In your plugin's enable() method:
 * new GUIManager(this);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class GUIManager implements Listener {
    
    private final KPlugin plugin;
    private final UUID managerUUID;
    private final Map<UUID, GUI> openGUIs;
    
    /**
     * Creates and initializes the GUI manager.
     * <p>
     * This constructor registers the manager in the {@link InstanceManager}
     * and registers event listeners with Bukkit.
     * A unique UUID is generated for this manager instance to detect plugin reloads.
     * </p>
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
        
        LoggerUtil.info("GUIManager initialized with UUID: " + managerUUID);
    }
    
    /**
     * Gets the GUIManager instance from the InstanceManager.
     *
     * @return the GUIManager instance
     * @throws IllegalStateException if GUIManager has not been initialized
     */
    @NotNull
    public static GUIManager getInstance() {
        return InstanceManager.getInstanceOrElseThrow(GUIManager.class);
    }
    
    /**
     * Gets the manager UUID.
     * <p>
     * This UUID is unique per GUIManager instance and changes when the plugin is reloaded.
     * It is used to validate GUI holders and detect stale GUIs from previous plugin instances.
     * </p>
     *
     * @return the manager UUID
     */
    @NotNull
    public UUID getManagerUUID() {
        return managerUUID;
    }
    
    /**
     * Opens a GUI for the specified player.
     * <p>
     * This method is Folia-safe and will execute the open operation
     * in the correct region context for the player.
     * </p>
     *
     * @param player the player to open the GUI for
     * @param gui the GUI to open
     */
    public void openGUI(@NotNull Player player, @NotNull GUI gui) {
        // Use Folia-safe entity task or sync task
        TaskUtil.runAtEntity(player, () -> {
            openGUIs.put(player.getUniqueId(), gui);
            player.openInventory(gui.getInventory());
            gui.onOpen(player);
        });
    }
    
    /**
     * Closes the currently open GUI for the specified player.
     *
     * @param player the player to close the GUI for
     */
    public void closeGUI(@NotNull Player player) {
        TaskUtil.runAtEntity(player, () -> {
            GUI gui = openGUIs.remove(player.getUniqueId());
            if (gui != null) {
                player.closeInventory();
                gui.onClose(player);
            }
        });
    }
    
    /**
     * Gets the GUI currently open for the specified player.
     *
     * @param player the player
     * @return the open GUI, or null if none is open
     */
    @Nullable
    public GUI getOpenGUI(@NotNull Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    /**
     * Gets the GUI associated with the specified inventory.
     * <p>
     * Validates that the GUI belongs to the current GUIManager instance.
     * If the holder is from a previous plugin instance (different UUID),
     * returns null to trigger proper cleanup.
     * </p>
     *
     * @param inventory the inventory
     * @return the GUI, or null if the inventory is not a GUI or is stale
     */
    @Nullable
    public GUI getGUI(@NotNull Inventory inventory) {
        if (inventory.getHolder() instanceof GUIHolder) {
            GUIHolder holder = (GUIHolder) inventory.getHolder();
            
            // Validate holder belongs to current manager instance
            if (!holder.isValid(managerUUID)) {
                LoggerUtil.warning("Detected stale GUI from previous plugin instance (holder UUID: " 
                    + holder.getManagerUUID() + ", current: " + managerUUID + ")");
                return null;
            }
            
            return holder.getGui();
        }
        return null;
    }
    
    /**
     * Checks if the specified player has a GUI open.
     *
     * @param player the player
     * @return true if the player has a GUI open
     */
    public boolean hasGUIOpen(@NotNull Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * Clears all tracked GUIs.
     * <p>
     * This is typically called on plugin disable.
     * </p>
     */
    public void clearAll() {
        openGUIs.clear();
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
        
        // Check if GUI is null (either not a GUI or stale from plugin reload)
        if (gui == null) {
            // Check if it's a stale GUI
            if (event.getInventory().getHolder() instanceof GUIHolder) {
                event.setCancelled(true);
                player.closeInventory();
                player.sendMessage("§cThis GUI is no longer valid (plugin was reloaded).");
                LoggerUtil.info("Closed stale GUI for player " + player.getName());
            }
            return;
        }
        
        // Cancel if view-only
        if (gui.isViewOnly()) {
            event.setCancelled(true);
        }
        
        // Get clicked slot and item
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getRawSlot();
        
        // Only process clicks inside the GUI inventory
        if (slot < 0 || slot >= gui.getType().getSize()) {
            // Allow player inventory clicks only if not view-only
            if (gui.isViewOnly()) {
                event.setCancelled(true);
            }
            return;
        }
        
        // Execute click handler
        gui.onClick(player, slot, clicked);
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
        
        // Check if it's a stale GUI
        if (gui == null && event.getInventory().getHolder() instanceof GUIHolder) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            player.sendMessage("§cThis GUI is no longer valid (plugin was reloaded).");
            return;
        }
        
        if (gui == null) {
            return;
        }
        
        // Cancel all drag operations in GUIs
        event.setCancelled(true);
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
        GUI gui = getGUI(event.getInventory());
        
        if (gui == null) {
            return;
        }
        
        // Remove from tracking
        openGUIs.remove(player.getUniqueId());
        
        // Call close callback
        gui.onClose(player);
    }
    
    /**
     * Handles inventory open events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        GUI gui = getGUI(event.getInventory());
        
        if (gui == null) {
            return;
        }
        
        // Track the open GUI
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * Cleans up GUIs when a player disconnects.
     */
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }
}
