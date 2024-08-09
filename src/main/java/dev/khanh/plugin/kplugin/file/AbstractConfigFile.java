package dev.khanh.plugin.kplugin.file;

import com.google.common.base.Preconditions;
import dev.khanh.plugin.kplugin.KPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * An abstract base class for managing configuration files in a Spigot plugin. This class provides
 * methods for loading, updating, and saving YAML configuration files.
 */
public abstract class AbstractConfigFile {
    private final KPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private final YamlConfiguration defaultYaml;

    /**
     * Constructs an AbstractConfigFile instance. Initializes the configuration file and loads the default configuration.
     * If the configuration file doesn't exist, it will be created from the resource.
     *
     * @param plugin the plugin instance
     * @throws RuntimeException if the default configuration file cannot be loaded
     */
    public AbstractConfigFile(KPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);

        // Load default config file
        try (InputStream inputStream = plugin.getResource("config.yml");
             InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8)) {
            defaultYaml = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        updateConfig();
    }

    /**
     * Checks if the configuration file needs to be updated based on the version number. If the current version is
     * older than the default version, the update method is called and the updated configuration is saved.
     *
     * @throws IllegalArgumentException if the config-version key is missing in the configuration file
     */
    public void updateConfig() {
        Preconditions.checkArgument(yaml.contains("config-version"), "Couldn't get config-version value!");

        int currentVersion = yaml.getInt("config-version");
        int defaultVersion = defaultYaml.getInt("config-version");

        if (currentVersion < defaultVersion) {
            plugin.getLogger().info("Old config file detected. Trying to update.");

            update(currentVersion, defaultVersion);

            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Saves the current configuration to the file.
     *
     * @throws IOException if an I/O error occurs
     */
    public void save() throws IOException {
        yaml.save(file);
    }

    /**
     * Abstract method to be implemented by subclasses to define behavior when the configuration file needs to be updated.
     */
    public abstract void update(int currentVersion, int defaultVersion);

    /**
     * Gets the configuration file.
     *
     * @return the configuration file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the current YAML configuration.
     *
     * @return the current YAML configuration
     */
    public YamlConfiguration getYaml() {
        return yaml;
    }

    /**
     * Gets the default YAML configuration.
     *
     * @return the default YAML configuration
     */
    public YamlConfiguration getDefaultYaml() {
        return defaultYaml;
    }
}
