package dev.khanh.plugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

/**
 * Utility class for colorizing text in Minecraft using both legacy and modern methods.
 */
public class ColorUtil {
    /**
     * Converts a string with legacy color codes into a colorized string.
     *
     * @param input the string containing legacy color codes
     * @return the colorized string
     */
    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Converts a string with legacy color codes into a modern {@link Component}.
     *
     * @param input the string containing legacy color codes
     * @return the colorized {@link Component}
     */
    public static Component modernColorize(String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }
}
