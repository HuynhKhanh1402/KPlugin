package dev.khanh.plugin.kplugin;

import dev.khanh.plugin.kplugin.util.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An abstract base class for Spigot plugins. It handles the lifecycle of a plugin by ensuring that
 * only one instance of a plugin is active at a time. It also provides an abstract interface for enabling and disabling the plugin.
 */
public abstract class KPlugin extends JavaPlugin {
    private static KPlugin kInstance;

    /**
     * Called when the plugin is enabled. This method checks if an instance of the plugin already exists and throws an exception if so.
     * Otherwise, it sets the current instance and calls the {@link #enable()} method.
     *
     * @throws IllegalStateException if an instance of the plugin is already defined
     */
    @Override
    public void onEnable() {
        if (kInstance != null && !kInstance.getClass().equals(getClass())) {
            throw new IllegalStateException("An instance of " + kInstance.getClass().getName() + " is already active. Only one instance of a plugin can be active at a time.");
        }

        printAuthorInfo();

        kInstance = this;
        enable();
    }

    /**
     * Called when the plugin is disabled. This method calls the {@link #disable()} method and clears the current instance.
     */
    @Override
    public void onDisable() {
        disable();
    }

    /**
     * Abstract method to be implemented by subclasses to define behavior when the plugin is enabled.
     */
    public abstract void enable();

    /**
     * Abstract method to be implemented by subclasses to define behavior when the plugin is disabled.
     */
    public abstract void disable();

    /**
     * Gets the current instance of the plugin.
     *
     * @return the current instance of the plugin, or null if no instance is active
     */
    public static KPlugin getKInstance() {
        return kInstance;
    }

    /**
     * Print out author information
     */
    private void printAuthorInfo() {
        Bukkit.getConsoleSender().sendMessage("");

        try {
            Bukkit.getConsoleSender().sendMessage(Component.text("[" + getDescription().getName() + "] ", NamedTextColor.AQUA)
                    .append(Component.text("Enabling plugin: " + getDescription().getName(), NamedTextColor.AQUA)));
            Bukkit.getConsoleSender().sendMessage(Component.text("[" + getDescription().getName() + "] ", NamedTextColor.AQUA)
                    .append(Component.text("This plugin is developed by "))
                    .append(MiniMessage.miniMessage().deserialize("<gradient:#EC9F05:#FF4E00>KhanhHuynh</gradient>")));
            Bukkit.getConsoleSender().sendMessage(Component.text("[" + getDescription().getName() + "] ", NamedTextColor.AQUA)
                    .append(Component.text("Discord: khanhhuynh", NamedTextColor.AQUA)));
        } catch (NoClassDefFoundError error) {
            LoggerUtil.info("This plugin is developed by KhanhHuynh");
            LoggerUtil.info("Discord: khanhhuynh");
        }

        Bukkit.getConsoleSender().sendMessage("");
    }
}
