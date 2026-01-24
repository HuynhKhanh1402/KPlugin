package dev.khanh.plugin.kplugin.gui.file;

import dev.khanh.plugin.kplugin.KPlugin;
import dev.khanh.plugin.kplugin.file.GenericYamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Configuration file for GUI library settings.
 * <p>
 * This file extends {@link GenericYamlFile} and provides configuration
 * options for GUI sounds, animations, pagination, and other features.
 * </p>
 * 
 * <p>
 * The configuration file is automatically created at {@code gui.yml} in
 * the plugin's data folder with default values.
 * </p>
 * 
 * <p><strong>Default configuration structure:</strong></p>
 * <pre>{@code
 * gui-version: 1
 * 
 * sounds:
 *   enabled: true
 *   click: "ui.button.click"
 *   open: "block.chest.open"
 *   close: "block.chest.close"
 *   navigate: "entity.experience_orb.pickup"
 *   error: "entity.villager.no"
 *   success: "entity.player.levelup"
 *   volume: 1.0
 *   pitch: 1.0
 * 
 * pagination:
 *   items-per-page: 28
 *   lazy-load-timeout: 5000
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class GUIConfigFile extends GenericYamlFile {
    
    /**
     * Creates a new GUI configuration file.
     *
     * @param plugin the plugin instance
     */
    public GUIConfigFile(@NotNull KPlugin plugin) {
        super(plugin, 
              new File(plugin.getDataFolder(), "gui.yml"), 
              "gui.yml", 
              "gui-version");
    }
    
    @Override
    protected void update(int oldVersion, int newVersion) {
        // Version 1 is the initial version
        // Add migration logic here as new versions are released
        
        // Example for future versions:
        // if (oldVersion < 2) {
        //     yaml.set("new-feature.enabled", true);
        // }
    }
    
    // ==================== Sound Configuration ====================
    
    /**
     * Checks if GUI sounds are enabled.
     *
     * @return true if sounds are enabled
     */
    public boolean areSoundsEnabled() {
        return yaml.getBoolean("sounds.enabled", true);
    }
    
    /**
     * Gets the click sound name.
     *
     * @return the sound name
     */
    @NotNull
    public String getClickSound() {
        return yaml.getString("sounds.click", "ui.button.click");
    }
    
    /**
     * Gets the open sound name.
     *
     * @return the sound name
     */
    @NotNull
    public String getOpenSound() {
        return yaml.getString("sounds.open", "block.chest.open");
    }
    
    /**
     * Gets the close sound name.
     *
     * @return the sound name
     */
    @NotNull
    public String getCloseSound() {
        return yaml.getString("sounds.close", "block.chest.close");
    }
    
    /**
     * Gets the navigate sound name.
     *
     * @return the sound name
     */
    @NotNull
    public String getNavigateSound() {
        return yaml.getString("sounds.navigate", "entity.experience_orb.pickup");
    }
    
    /**
     * Gets the error sound name.
     *
     * @return the sound name
     */
    @NotNull
    public String getErrorSound() {
        return yaml.getString("sounds.error", "entity.villager.no");
    }
    
    /**
     * Gets the success sound name.
     *
     * @return the sound name
     */
    @NotNull
    public String getSuccessSound() {
        return yaml.getString("sounds.success", "entity.player.levelup");
    }
    
    /**
     * Gets the default sound volume.
     *
     * @return the volume (0.0 to 1.0+)
     */
    public float getSoundVolume() {
        return (float) yaml.getDouble("sounds.volume", 1.0);
    }
    
    /**
     * Gets the default sound pitch.
     *
     * @return the pitch (0.5 to 2.0)
     */
    public float getSoundPitch() {
        return (float) yaml.getDouble("sounds.pitch", 1.0);
    }
    
    // ==================== Pagination Configuration ====================
    
    /**
     * Gets the default items per page.
     *
     * @return the items per page
     */
    public int getDefaultItemsPerPage() {
        return yaml.getInt("pagination.items-per-page", 28);
    }
    
    /**
     * Gets the lazy load timeout in milliseconds.
     *
     * @return the timeout
     */
    public long getLazyLoadTimeout() {
        return yaml.getLong("pagination.lazy-load-timeout", 5000);
    }
}
