package dev.khanh.plugin.kplugin.util;

import dev.khanh.plugin.kplugin.KPlugin;
import org.bukkit.Bukkit;

/**
 * Utility class for logging messages in a Spigot plugin. This class provides convenient methods for
 * logging messages with different severity levels.
 */
public class LoggerUtil {
    /**
     * Whether debug mode is enabled.
     */
    private static boolean debugEnabled = false;

    /**
     * Enables or disables debug logging.
     *
     * @param enabled true to enable debug mode, false to disable
     */
    public static void setDebug(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Returns whether debug mode is enabled.
     *
     * @return true if debug is enabled
     */
    public static boolean isDebug() {
        return debugEnabled;
    }

    /**
     * Logs an info level message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        KPlugin.getKInstance().getLogger().info(message);
    }

    /**
     * Logs a formatted info level message.
     *
     * @param message the message format string
     * @param args    the arguments referenced by the format specifiers in the message
     */
    public static void info(String message, Object... args) {
        KPlugin.getKInstance().getLogger().info(String.format(message, args));
    }

    /**
     * Logs a warning level message.
     *
     * @param message the message to log
     */
    public static void warning(String message) {
        KPlugin.getKInstance().getLogger().warning(message);
    }

    /**
     * Logs a formatted warning level message.
     *
     * @param message the message format string
     * @param args    the arguments referenced by the format specifiers in the message
     */
    public static void warning(String message, Object... args) {
        KPlugin.getKInstance().getLogger().warning(String.format(message, args));
    }

    /**
     * Logs a severe level message.
     *
     * @param message the message to log
     */
    public static void severe(String message) {
        KPlugin.getKInstance().getLogger().severe(message);
    }

    /**
     * Logs a formatted severe level message.
     *
     * @param message the message format string
     * @param args    the arguments referenced by the format specifiers in the message
     */
    public static void severe(String message, Object... args) {
        KPlugin.getKInstance().getLogger().severe(String.format(message, args));
    }

    /**
     * Sends a colorized message to the console
     *
     * @param message the message
     */
    public static void message(String message) {
        KPlugin plugin = KPlugin.getKInstance();
        Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] " + ColorUtil.colorize(message));
    }
    /**
     * Sends a formatted and colorized message to the console
     *
     * @param message the message template to send; supports formatting placeholders similar to {@link String#format(String, Object...)}.
     * @param args    the arguments to be used for formatting the message template.
     */
    public static void message(String message, Object... args) {
        message(String.format(message, args));
    }

    /**
     * Logs a debug level message if debug mode is enabled.
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        if (debugEnabled) {
            KPlugin.getKInstance().getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Logs a formatted debug level message if debug mode is enabled.
     *
     * @param message the message format string
     * @param args    the arguments referenced by the format specifiers in the message
     */
    public static void debug(String message, Object... args) {
        if (debugEnabled) {
            KPlugin.getKInstance().getLogger().info("[DEBUG] " + String.format(message, args));
        }
    }
}
