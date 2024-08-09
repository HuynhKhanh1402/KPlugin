package dev.khanh.plugin.kplugin.file;

import dev.khanh.plugin.kplugin.KPlugin;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * A class for managing message configuration files in a Spigot plugin. It provides methods for
 * retrieving and sending messages with color formatting.
 */
public class MessageFile {
    private final KPlugin plugin;
    private final File file;
    private final YamlConfiguration defaultYaml;
    private final YamlConfiguration yaml;

    /**
     * Constructs a MessageFile instance. Loads the messages from the configuration file and updates them if necessary.
     *
     * @param plugin the plugin instance
     * @throws RuntimeException if the default message file cannot be loaded
     */
    public MessageFile(KPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        InputStream inputStream = plugin.getResource("messages.yml");
        assert inputStream != null;

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)){
            defaultYaml = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        yaml = YamlConfiguration.loadConfiguration(file);

        updateMessages();
    }

    /**
     * Updates the messages in the configuration file with the default messages if they are missing.
     * If any changes are made, the file is saved.
     */
    private void updateMessages() {
        boolean isChanged = false;

        for (String path: defaultYaml.getKeys(true)) {
            if (!yaml.contains(path)) {
                yaml.set(path, defaultYaml.get(path));
                isChanged = true;
            }
        }

        if (isChanged) {
            try {
                yaml.save(file);
                plugin.getLogger().info("Old message file detected. Trying to update.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Retrieves a message from the configuration file by its key.
     *
     * @param key the key of the message
     * @return the message as a string, or an empty string if the key does not exist
     */
    public String getMessage(String key) {
        return yaml.getString(key, "");
    }

    /**
     * Retrieves a message from the configuration file by its key and applies a function to modify the message.
     *
     * @param key      the key of the message
     * @param function the function to apply to the message
     * @return the modified message as a string
     */
    public String getMessage(String key, Function<String, String> function) {
        return function.apply(getMessage(key));
    }

    /**
     * Retrieves a colorized message from the configuration file by its key.
     *
     * @param key the key of the message
     * @return the colorized message as a string
     */
    public String getColorizedMessage(String key) {
        return ColorUtil.colorize(getMessage(key));
    }

    /**
     * Retrieves a colorized message from the configuration file by its key and applies a function to modify the message.
     *
     * @param key      the key of the message
     * @param function the function to apply to the message
     * @return the modified and colorized message as a string
     */
    public String getColorizedMessage(String key, Function<String, String> function) {
        return ColorUtil.colorize(getMessage(key, function));
    }

    /**
     * Retrieves a modern colorized message from the configuration file by its key.
     *
     * @param key the key of the message
     * @return the modern colorized message as a {@link Component}
     */
    public Component getModernColorizedMessage(String key) {
        return ColorUtil.modernColorize(getMessage(key));
    }

    /**
     * Retrieves a modern colorized message from the configuration file by its key and applies a function to modify the message.
     *
     * @param key      the key of the message
     * @param function the function to apply to the message
     * @return the modified and modern colorized message as a {@link Component}
     */
    public Component getModernColorizedMessage(String key, Function<String, String> function) {
        return ColorUtil.modernColorize(getMessage(key, function));
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix and applies a function to modify the message.
     *
     * @param sender   the command sender to receive the message
     * @param key      the key of the message
     * @param function the function to apply to the message
     */
    public void sendMessage(CommandSender sender, String key, Function<String, String> function) {
        sender.sendMessage(getModernColorizedMessage("prefix").append(getModernColorizedMessage(key, function)));
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix.
     *
     * @param sender the command sender to receive the message
     * @param key    the key of the message
     */
    public void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, Function.identity());
    }
}
