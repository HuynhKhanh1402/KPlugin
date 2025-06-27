package dev.khanh.plugin.kplugin.file;

import dev.khanh.plugin.kplugin.KPlugin;

import java.io.File;

/**
 * Abstract base class for managing plugin configuration files in YAML format.
 * <p>
 * This class handles loading the configuration file, saving modifications,
 * and automatically upgrading the file structure when the default resource version increases.
 * </p>
 */
public abstract class AbstractConfigFile extends GenericYamlFile {

    /**
     * Creates a new instance of ConfigFile.
     * <p>
     * Always uses the file {@code config.yml} in the plugin's data folder
     * and the default resource {@code config.yml} within the plugin JAR.
     * </p>
     * @param plugin       Reference to the main plugin instance.
     */
    public AbstractConfigFile(KPlugin plugin) {
        super(plugin, new File(plugin.getDataFolder(), "config.yml"), "config.yml");
    }

    /**
     * Called when the configuration file needs to be upgraded
     * because the existing version is lower than the default resource version.
     * <p>
     * Implement this method to move, rename, or remove keys
     * when changing configuration schemas between
     * {@code oldVersion} and {@code newVersion}.
     * </p>
     *
     * @param oldVersion The version number currently present in the config file.
     * @param newVersion The latest version number from the default resource.
     */
    @Override
    abstract protected void update(int oldVersion, int newVersion);
}
