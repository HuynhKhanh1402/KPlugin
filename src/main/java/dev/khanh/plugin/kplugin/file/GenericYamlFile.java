package dev.khanh.plugin.kplugin.file;

import com.google.common.base.Preconditions;
import dev.khanh.plugin.kplugin.KPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class GenericYamlFile {
    private final KPlugin plugin;
    protected final File file;
    private final String configVersionKey;
    protected YamlConfiguration yaml;
    private final YamlConfiguration defaultYaml;
    @Nullable

    /**
     * Constructs a GenericYamlFile for the given file.
     * If the file does not exist, attempts to save it from plugin resources.
     * @param plugin the plugin instance
     * @param file the file to load/create
     * @param resourcePath the path inside the jar resources to copy default from (e.g. "config.yml"); null to skip
     */
    public GenericYamlFile(KPlugin plugin, File file, @Nullable String resourcePath) {
        this(plugin, file, resourcePath, "version");
    }

    /**
     * Constructs a GenericYamlFile for the given file with a custom config version key.
     * If the file does not exist, attempts to save it from plugin resources.
     *
     * @param plugin the plugin instance
     * @param file the file to load/create
     * @param resourcePath the path inside the jar resources to copy default from (e.g. "config.yml"); null to skip
     * @param configVersionKey the key used to track the config version in the YAML file
     */
    public GenericYamlFile(KPlugin plugin, File file, @Nullable String resourcePath, String configVersionKey) {
        this.plugin = plugin;
        this.file = file;
        this.configVersionKey = configVersionKey;

        if (resourcePath != null && !file.exists()) {
            plugin.saveResource(resourcePath, false);
        }

        Preconditions.checkArgument(file.exists(), "File %s does not exist", file);

        this.yaml = YamlConfiguration.loadConfiguration(file);

        if (resourcePath != null) {
            try (InputStream in = plugin.getResource(resourcePath);
                 InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(in), StandardCharsets.UTF_8)) {
                defaultYaml = YamlConfiguration.loadConfiguration(reader);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load default resource: " + resourcePath, e);
            }
            updateConfigVersions();
        } else {
            defaultYaml = null;
        }
    }

    /** Checks version and updates if default version is higher. */
    private void updateConfigVersions() {
        Preconditions.checkArgument(yaml.contains(configVersionKey),
                String.format("Missing config version key (%s)", configVersionKey));

        int current = yaml.getInt(configVersionKey);
        int def = defaultYaml.getInt(configVersionKey);
        if (current < def) {
            plugin.getLogger().info("Updating config from v" + current + " to v" + def);
            update(current, def);
            yaml.set(configVersionKey, def);
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Save the YAML file to disk. */
    public void save() throws IOException {
        yaml.save(file);
    }
    /** Reload the YAML file from disk. */
    public void reload() {
        this.yaml = YamlConfiguration.loadConfiguration(file);
        if (defaultYaml != null) {
            updateConfigVersions();
        }
    }


    /**
     * Called when config version key needs to be updated.
     * @param oldVersion current file version
     * @param newVersion default file version
     */
    protected abstract void update(int oldVersion, int newVersion);

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


    /**
     * Retrieves all keys in this configuration section.
     *
     * @param deep if true, includes keys from nested sections
     * @return a set of keys in this configuration section
     */
    public @NotNull Set<String> getKeys(boolean deep) {
        return yaml.getKeys(deep);
    }

    /**
     * Retrieves a map of all keys and values in this configuration section.
     *
     * @param deep if true, includes keys and values from nested sections
     * @return a map of keys and values in this configuration section
     */
    public @NotNull Map<String, Object> getValues(boolean deep) {
        return yaml.getValues(deep);
    }

    /**
     * Checks if a specific path exists in this configuration section.
     *
     * @param path the path to check
     * @return true if the path exists, otherwise false
     */
    public boolean contains(@NotNull String path) {
        return yaml.contains(path);
    }

    /**
     * Checks if a specific path exists in this configuration section, optionally ignoring the default value.
     *
     * @param path the path to check
     * @param ignoreDefault if true, ignores default values
     * @return true if the path exists or if the default value is present (depending on ignoreDefault)
     */
    public boolean contains(@NotNull String path, boolean ignoreDefault) {
        return yaml.contains(path, ignoreDefault);
    }

    /**
     * Checks if a specific path is set in this configuration section.
     *
     * @param path the path to check
     * @return true if the path is set, otherwise false
     */
    public boolean isSet(@NotNull String path) {
        return yaml.isSet(path);
    }

    /**
     * Retrieves an object from this configuration section by path.
     *
     * @param path the path to the object
     * @return the object at the specified path, or null if not found
     */
    public @Nullable Object get(@NotNull String path) {
        return yaml.get(path);
    }

    /**
     * Retrieves an object from this configuration section by path, returning a default if not found.
     *
     * @param path the path to the object
     * @param def the default object if the path is not found
     * @return the object at the specified path or the default if not found
     */
    @Contract("_, !null -> !null")
    public @Nullable Object get(@NotNull String path, @Nullable Object def) {
        return yaml.get(path, def);
    }

    /**
     * Sets the value at the specified path in this configuration section.
     *
     * @param path the path to set
     * @param value the value to set, or null to remove the path
     */
    public void set(@NotNull String path, @Nullable Object value) {
        yaml.set(path, value);
    }

    /**
     * Creates a new empty section at the specified path.
     *
     * @param path the path to create the section at
     * @return the newly created section
     */
    public @NotNull ConfigurationSection createSection(@NotNull String path) {
        return yaml.createSection(path);
    }

    /**
     * Creates a new section at the specified path, with the provided values.
     *
     * @param path the path to create the section at
     * @param map the initial values for the new section
     * @return the newly created section
     */
    public @NotNull ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        return yaml.createSection(path, map);
    }

    /**
     * Retrieves a string from this configuration section by path.
     *
     * @param path the path to the string
     * @return the string at the specified path, or null if not found
     */
    public @Nullable String getString(@NotNull String path) {
        return yaml.getString(path);
    }

    /**
     * Retrieves a string from this configuration section by path, with a default if not found.
     *
     * @param path the path to the string
     * @param def the default string if the path is not found
     * @return the string at the specified path or the default if not found
     */
    @Contract("_, !null -> !null")
    public @Nullable String getString(@NotNull String path, @Nullable String def) {
        return yaml.getString(path, def);
    }

    /**
     * Checks if the specified path is a string.
     *
     * @param path the path to check
     * @return true if the path is a string, otherwise false
     */
    public boolean isString(@NotNull String path) {
        return yaml.isString(path);
    }

    /**
     * Retrieves an integer from this configuration section by path.
     *
     * @param path the path to the integer
     * @return the integer at the specified path, or 0 if not found
     */
    public int getInt(@NotNull String path) {
        return yaml.getInt(path);
    }

    /**
     * Retrieves an integer from this configuration section by path, with a default if not found.
     *
     * @param path the path to the integer
     * @param def the default integer if the path is not found
     * @return the integer at the specified path or the default if not found
     */
    public int getInt(@NotNull String path, int def) {
        return yaml.getInt(path, def);
    }

    /**
     * Checks if the specified path is an integer.
     *
     * @param path the path to check
     * @return true if the path is an integer, otherwise false
     */
    public boolean isInt(@NotNull String path) {
        return yaml.isInt(path);
    }

    /**
     * Retrieves a boolean from this configuration section by path.
     *
     * @param path the path to the boolean
     * @return the boolean at the specified path, or false if not found
     */
    public boolean getBoolean(@NotNull String path) {
        return yaml.getBoolean(path);
    }

    /**
     * Retrieves a boolean from this configuration section by path, with a default if not found.
     *
     * @param path the path to the boolean
     * @param def the default boolean if the path is not found
     * @return the boolean at the specified path or the default if not found
     */
    public boolean getBoolean(@NotNull String path, boolean def) {
        return yaml.getBoolean(path, def);
    }

    /**
     * Checks if the specified path is a boolean.
     *
     * @param path the path to check
     * @return true if the path is a boolean, otherwise false
     */
    public boolean isBoolean(@NotNull String path) {
        return yaml.isBoolean(path);
    }

    /**
     * Retrieves a double from this configuration section by path.
     *
     * @param path the path to the double
     * @return the double at the specified path, or 0.0 if not found
     */
    public double getDouble(@NotNull String path) {
        return yaml.getDouble(path);
    }

    /**
     * Retrieves a double from this configuration section by path, with a default if not found.
     *
     * @param path the path to the double
     * @param def the default double if the path is not found
     * @return the double at the specified path or the default if not found
     */
    public double getDouble(@NotNull String path, double def) {
        return yaml.getDouble(path, def);
    }

    /**
     * Checks if the specified path is a double.
     *
     * @param path the path to check
     * @return true if the path is a double, otherwise false
     */
    public boolean isDouble(@NotNull String path) {
        return yaml.isDouble(path);
    }

    /**
     * Retrieves a long from this configuration section by path.
     *
     * @param path the path to the long
     * @return the long at the specified path, or 0L if not found
     */
    public long getLong(@NotNull String path) {
        return yaml.getLong(path);
    }

    /**
     * Retrieves a long from this configuration section by path, with a default if not found.
     *
     * @param path the path to the long
     * @param def the default long if the path is not found
     * @return the long at the specified path or the default if not found
     */
    public long getLong(@NotNull String path, long def) {
        return yaml.getLong(path, def);
    }

    /**
     * Checks if the specified path is a long.
     *
     * @param path the path to check
     * @return true if the path is a long, otherwise false
     */
    public boolean isLong(@NotNull String path) {
        return yaml.isLong(path);
    }

    /**
     * Retrieves a list from this configuration section by path.
     *
     * @param path the path to the list
     * @return the list at the specified path, or null if not found
     */
    public @Nullable List<?> getList(@NotNull String path) {
        return yaml.getList(path);
    }

    /**
     * Retrieves a list from this configuration section by path, with a default if not found.
     *
     * @param path the path to the list
     * @param def the default list if the path is not found
     * @return the list at the specified path or the default if not found
     */
    @Contract("_, !null -> !null")
    public @Nullable List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return yaml.getList(path, def);
    }

    /**
     * Checks if the specified path is a list.
     *
     * @param path the path to check
     * @return true if the path is a list, otherwise false
     */
    public boolean isList(@NotNull String path) {
        return yaml.isList(path);
    }

    /**
     * Retrieves a list of strings from this configuration section by path.
     * <p>
     * If the list does not exist, this will return an empty list.
     * Any non-string elements in the list will be excluded.
     *
     * @param path the path to the list of strings
     * @return a list of strings at the specified path, or an empty list if not found
     */
    @NotNull
    public List<String> getStringList(@NotNull String path) {
        return yaml.getStringList(path);
    }

    /**
     * Retrieves an object of a specified type from this configuration section by path.
     *
     * @param <T> the type of the object
     * @param path the path to the object
     * @param clazz the class type of the object
     * @return the object at the specified path, or null if not found
     */
    public <T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        return yaml.getObject(path, clazz);
    }

    /**
     * Retrieves an object of a specified type from this configuration section by path, with a default if not found.
     *
     * @param <T> the type of the object
     * @param path the path to the object
     * @param clazz the class type of the object
     * @param def the default object if the path is not found
     * @return the object at the specified path or the default if not found
     */
    public <T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return yaml.getObject(path, clazz, def);
    }

    /**
     * Retrieves an ItemStack from this configuration section by path.
     *
     * @param path the path to the ItemStack
     * @return the ItemStack at the specified path, or null if not found
     */
    public @Nullable ItemStack getItemStack(@NotNull String path) {
        return yaml.getItemStack(path);
    }

    /**
     * Retrieves an ItemStack from this configuration section by path, with a default if not found.
     *
     * @param path the path to the ItemStack
     * @param def the default ItemStack if the path is not found
     * @return the ItemStack at the specified path or the default if not found
     */
    @Contract("_, !null -> !null")
    public @Nullable ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        return yaml.getItemStack(path, def);
    }

    /**
     * Checks if the specified path is an ItemStack.
     *
     * @param path the path to check
     * @return true if the path is an ItemStack, otherwise false
     */
    public boolean isItemStack(@NotNull String path) {
        return yaml.isItemStack(path);
    }

    /**
     * Retrieves a Location from this configuration section by path.
     *
     * @param path the path to the Location
     * @return the Location at the specified path, or null if not found
     */
    public @Nullable Location getLocation(@NotNull String path) {
        return yaml.getLocation(path);
    }

    /**
     * Retrieves a Location from this configuration section by path, with a default if not found.
     *
     * @param path the path to the Location
     * @param def the default Location if the path is not found
     * @return the Location at the specified path or the default if not found
     */
    @Contract("_, !null -> !null")
    public @Nullable Location getLocation(@NotNull String path, @Nullable Location def) {
        return yaml.getLocation(path, def);
    }

    /**
     * Checks if the specified path is a Location.
     *
     * @param path the path to check
     * @return true if the path is a Location, otherwise false
     */
    public boolean isLocation(@NotNull String path) {
        return yaml.isLocation(path);
    }

    /**
     * Retrieves a ConfigurationSection from this configuration section by path.
     *
     * @param path the path to the ConfigurationSection
     * @return the ConfigurationSection at the specified path, or null if not found
     */
    public @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
        return yaml.getConfigurationSection(path);
    }

    /**
     * Checks if the specified path is a ConfigurationSection.
     *
     * @param path the path to check
     * @return true if the path is a ConfigurationSection, otherwise false
     */
    public boolean isConfigurationSection(@NotNull String path) {
        return yaml.isConfigurationSection(path);
    }
}
