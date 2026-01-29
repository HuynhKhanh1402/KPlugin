package dev.khanh.plugin.kplugin.gui.holder;

import dev.khanh.plugin.kplugin.gui.GUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Custom InventoryHolder for GUI instances.
 * <p>
 * Stores manager UUID and GUI UUID for validation.
 * The manager UUID changes on plugin reload to detect stale GUIs.
 * </p>
 */
public class GUIHolder implements InventoryHolder {
    
    private final UUID managerUUID;
    private final UUID guiUUID;
    private final GUI gui;
    private Inventory inventory;
    
    /**
     * Creates a GUI holder.
     *
     * @param managerUUID the manager UUID
     * @param guiUUID the GUI UUID
     * @param gui the GUI
     */
    public GUIHolder(@NotNull UUID managerUUID, @NotNull UUID guiUUID, @NotNull GUI gui) {
        this.managerUUID = managerUUID;
        this.guiUUID = guiUUID;
        this.gui = gui;
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
     * Gets the GUI UUID.
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
     * Sets the inventory reference. Internal use only.
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
