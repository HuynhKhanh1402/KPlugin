package dev.khanh.plugin.kplugin.util;

import dev.khanh.plugin.kplugin.file.MessageFile;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Utility class for sending messages from the messages.yml file to command senders.
 * <p>
 * Provides static methods for retrieving, colorizing, and sending messages with or without prefix.
 * Must be initialized with a {@link MessageFile} before use.
 */
public class MessageUtil {
    private static MessageFile messageFile;

    /**
     * Initializes the MessageUtil with the given MessageFile.
     *
     * @param messageFile the MessageFile to use for message retrieval
     */
    public static void initialize(MessageFile messageFile) {
        MessageUtil.messageFile = messageFile;
    }

    private static MessageFile getMessageFile() {
        if (messageFile == null) {
            throw new IllegalStateException("MessageFile must be initialized before using this method.");
        }
        return messageFile;
    }

    /**
     * Retrieves a message from the configuration file by its key.
     *
     * @param key the key of the message
     * @return the message as a string, or an empty string if the key does not exist
     */
    public static @NotNull String getMessage(String key) {
        return getMessageFile().getMessage(key);
    }

    /**
     * Retrieves a message from the configuration file by its key and applies a function to modify the message.
     *
     * @param key      the key of the message
     * @param function the function to apply to the message
     * @return the modified message as a string
     */
    public static @NotNull String getMessage(String key, Function<String, String> function) {
        return getMessageFile().getMessage(key, function);
    }

    /**
     * Retrieves a colorized message from the configuration file by its key.
     *
     * @param key the key of the message
     * @return the colorized message as a string
     */
    public static @NotNull String getColorizedMessage(String key) {
        return getMessageFile().getColorizedMessage(key);
    }

    /**
     * Retrieves a colorized message from the configuration file by its key and applies a function to modify the message.
     *
     * @param key      the key of the message
     * @param function the function to apply to the message
     * @return the modified and colorized message as a string
     */
    public static @NotNull String getColorizedMessage(String key, Function<String, String> function) {
        return getMessageFile().getColorizedMessage(key, function);
    }

    /**
     * Retrieves a modern colorized message from the configuration file by its key.
     *
     * @param key the key of the message
     * @return the modern colorized message as a {@link Component}
     */
    public static @NotNull Component getModernColorizedMessage(String key) {
        return getMessageFile().getModernColorizedMessage(key);
    }

    /**
     * Retrieves a modern colorized message from the configuration file by its key and applies a function to modify the message.
     *
     * @param key      the key of the message
     * @param function the function to apply to the message
     * @return the modified and modern colorized message as a {@link Component}
     */
    public static @NotNull Component getModernColorizedMessage(String key, Function<String, String> function) {
        return getMessageFile().getModernColorizedMessage(key, function);
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix and applies a function to modify the message.
     *
     * @param sender     the command sender to receive the message
     * @param key        the key of the message
     * @param function   the function to apply to the message
     * @param allowEmpty whether to send the message if it's empty (defaults to false)
     */
    public static void sendMessage(CommandSender sender, String key, Function<String, String> function, boolean allowEmpty) {
        getMessageFile().sendMessage(sender, key, function, allowEmpty);
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix and applies a function to modify the message.
     * Empty messages will not be sent by default.
     *
     * @param sender   the command sender to receive the message
     * @param key      the key of the message
     * @param function the function to apply to the message
     */
    public static void sendMessage(CommandSender sender, String key, Function<String, String> function) {
        getMessageFile().sendMessage(sender, key, function, false);
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix.
     * Empty messages will not be sent by default.
     *
     * @param sender the command sender to receive the message
     * @param key    the key of the message
     */
    public static void sendMessage(CommandSender sender, String key) {
        getMessageFile().sendMessage(sender, key, false);
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix.
     *
     * @param sender     the command sender to receive the message
     * @param key        the key of the message
     * @param allowEmpty whether to send the message if it's empty
     */
    public static void sendMessage(CommandSender sender, String key, boolean allowEmpty) {
        getMessageFile().sendMessage(sender, key, allowEmpty);
    }

    /**
     * Sends a message to the specified {@link CommandSender} with a prefix.
     *
     * @param sender  the command sender to receive the message
     * @param message the message
     */
    public static void sendMessageWithPrefix(CommandSender sender, String message) {
        getMessageFile().sendMessageWithPrefix(sender, message);
    }

}
