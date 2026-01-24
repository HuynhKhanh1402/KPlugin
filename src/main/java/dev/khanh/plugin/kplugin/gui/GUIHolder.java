package dev.khanh.plugin.kplugin.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Custom InventoryHolder for GUI instances.
 * <p>
 * This holder stores metadata about the GUI for validation purposes:
 * <ul>
 *   <li>{@code managerUUID} - UUID scoped to the plugin instance (GUIManager)</li>
 *   <li>{@code guiUUID} - UUID of the specific GUI instance</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The {@code managerUUID} is used to detect plugin reloads. If a player tries to
 * interact with a GUI from a previous plugin instance (different managerUUID),
 * the event is cancelled and the GUI is closed to prevent exploits.
 * </p>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class GUIHolder implements InventoryHolder {
    
    private final UUID managerUUID;
    private final UUID guiUUID;
    private final GUI gui;
    private Inventory inventory;
    
    /**
     * Creates a new GUI holder.
     *
     * @param managerUUID the UUID of the GUIManager instance
     * @param guiUUID the UUID of the GUI instance
     * @param gui the GUI instance
     */
    public GUIHolder(@NotNull UUID managerUUID, @NotNull UUID guiUUID, @NotNull GUI gui) {
        this.managerUUID = managerUUID;
        this.guiUUID = guiUUID;
        this.gui = gui;
    }
    
    /**
     * Gets the GUIManager UUID.
     * <p>
     * This UUID is unique per plugin instance and changes when the plugin is reloaded.
     * </p>
     *
     * @return the manager UUID
     */
    @NotNull
    public UUID getManagerUUID() {
        return managerUUID;
    }
    
    /**
     * Gets the GUI instance UUID.
     *
     * @return the GUI UUID
     */
    @NotNull
    public UUID getGuiUUID() {
        return guiUUID;
    }
    
    /**
     * Gets the GUI instance.
     *
     * @return the GUI
     */
    @NotNull
    public GUI getGui() {
        return gui;
    }
    
    /**
     * Sets the inventory reference.
     * <p>
     * This is called after inventory creation to establish the circular reference.
     * </p>
     *
     * @param inventory the inventory
     */
    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }
    
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Validates if this holder belongs to the specified GUIManager.
     *
     * @param currentManagerUUID the current GUIManager UUID
     * @return true if the holder is valid for the current manager
     */
    public boolean isValid(@NotNull UUID currentManagerUUID) {
        return managerUUID.equals(currentManagerUUID);
    }
}
