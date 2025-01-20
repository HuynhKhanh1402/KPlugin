package dev.khanh.plugin.kplugin.command;

import dev.khanh.plugin.kplugin.util.ColorUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * An abstract class representing a Bukkit command.
 * Extend this class to create custom Bukkit commands.
 */
public abstract class BukkitCommand implements CommandExecutor, TabCompleter {
    @Getter
    @Nullable
    protected final BukkitCommand parent;
    protected final List<BukkitCommand> subCommands;
    @Getter
    @NotNull
    protected final String name;
    @Getter
    @NotNull
    protected final List<String> alias;
    @Getter
    @NotNull
    protected final String permission;
    @Getter
    @NotNull
    protected final String description;
    @Getter
    @NotNull
    protected final String usage;
    @Getter
    protected static CommandMap commandMap;

    static {
        // Initialize the command map
        if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
            final SimplePluginManager manager = (SimplePluginManager) Bukkit.getPluginManager();

            try {
                final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);

                commandMap = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name        the name of the command
     * @param parent      the parent command (nullable)
     * @param alias       the aliases of the command (nullable)
     * @param permission  the permission required to execute the command (nullable)
     * @param description the description of the command (nullable)
     * @param usage       the usage syntax of the command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable BukkitCommand parent, @Nullable List<String> alias,
                         @Nullable String permission, @Nullable String description, @Nullable String usage) {
        this.parent = parent;
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.alias = alias != null ? alias : new ArrayList<>();
        this.permission = permission != null ? permission : "";
        this.description = description != null ? description : "";
        this.usage = usage != null ? usage : "";
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name        the name of the command
     * @param alias       the aliases of the command (nullable)
     * @param permission  the permission required to execute the command (nullable)
     * @param description the description of the command (nullable)
     * @param usage       the usage syntax of the command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable List<String> alias, @Nullable String permission,
                         @Nullable String description, @Nullable String usage) {
        this(name, null, alias, permission, description, usage);
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name        the name of the command
     * @param permission  the permission required to execute the command (nullable)
     * @param description the description of the command (nullable)
     * @param usage       the usage syntax of the command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable String permission, @Nullable String description,
                         @Nullable String usage) {
        this(name, null, null, permission, description, usage);
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name        the name of the command
     * @param parent      the parent command (nullable)
     * @param permission  the permission required to execute the command (nullable)
     * @param description the description of the command (nullable)
     * @param usage       the usage syntax of the command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable BukkitCommand parent, @Nullable String permission,
                         @Nullable String description, @Nullable String usage) {
        this(name, parent, null, permission, description, usage);
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name       the name of the command
     * @param parent     the parent command (nullable)
     * @param permission the permission required to execute the command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable BukkitCommand parent, @Nullable String permission) {
        this(name, parent, null, permission, null, null);
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name        the name of the command
     * @param parent      the parent command (nullable)
     * @param description the description of the command (nullable)
     * @param usage       the usage syntax of the command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable BukkitCommand parent, @Nullable String description,
                         @Nullable String usage) {
        this(name, parent, null, null, description, usage);
    }

    /**
     * Constructs a BukkitCommand with the specified parameters.
     *
     * @param name   the name of the command
     * @param parent the parent command (nullable)
     */
    public BukkitCommand(@NotNull String name, @Nullable BukkitCommand parent) {
        this(name, parent, null);
    }

    /**
     * Adds a subcommand to this BukkitCommand.
     *
     * @param subCommand the subcommand to add
     * @return the BukkitCommand instance
     * @throws IllegalArgumentException if a duplicate subcommand or alias is found
     */
    public final BukkitCommand addSubCommand(BukkitCommand subCommand) {
        for (BukkitCommand command : subCommands) {
            if (command.getName().equalsIgnoreCase(subCommand.getName())) {
                throw new IllegalArgumentException("Duplicate subcommand: " + command.getName());
            }

            for (String alias : subCommand.getAlias()) {
                if (command.getAlias().contains(alias)) {
                    throw new IllegalArgumentException("Duplicate subcommand alias: " + alias);
                }
            }
        }

        subCommands.add(subCommand);
        return this;
    }

    /**
     * Registers the command with the specified JavaPlugin.
     *
     * @param plugin the JavaPlugin to register the command with
     */
    public final void registerCommand(JavaPlugin plugin) {
        try {
            final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            final PluginCommand pluginCommand = constructor.newInstance(name, plugin);
            pluginCommand.setTabCompleter(this);
            pluginCommand.setExecutor(this);
            pluginCommand.setUsage(usage);
            pluginCommand.setPermission(permission.isEmpty() ? null : permission);
            pluginCommand.setDescription(description);
            pluginCommand.setAliases(alias);

            commandMap.register(name, pluginCommand);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Executes the command when it is called by a CommandSender.
     *
     * @param sender the CommandSender executing the command
     * @param args   the arguments passed with the command
     */
    public abstract void onCommand(@NotNull CommandSender sender, @NotNull List<String> args);

    /**
     * Executes the command when it is called by a Player.
     *
     * @param player the Player executing the command
     * @param args   the arguments passed with the command
     */
    public abstract void onCommand(@NotNull Player player, @NotNull List<String> args);

    /**
     * Provides tab-completion for the command when it is called by a CommandSender.
     *
     * @param sender the CommandSender tab-completing the command
     * @param args   the arguments passed with the command
     * @return a list of tab-completion options
     */
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        return subCommands.stream()
                .filter(bukkitCommand -> bukkitCommand.hasPermission(sender))
                .map(BukkitCommand::getName)
                .filter(s -> s.startsWith(args.get(0)))
                .collect(Collectors.toList());
    }

    /**
     * Provides tab-completion for the command when it is called by a Player.
     *
     * @param player the Player tab-completing the command
     * @param args   the arguments passed with the command
     * @return a list of tab-completion options
     */
    public List<String> onTabComplete(Player player, List<String> args) {
        return onTabComplete((CommandSender) player, args);
    }

    /**
     * Returns the message to be displayed when the CommandSender does not have permission to execute the command.
     *
     * @return the "no permission" message
     */
    @NotNull
    public abstract String getNoPermissionMessage();

    /**
     * Replaces placeholders in a string with the corresponding values from a replacement map.
     *
     * @param message    the string to replace placeholders in
     * @param replaceMap the map containing placeholder-value pairs
     * @return the string with replaced placeholders
     */
    protected static String replacePlaceholders(@NotNull String message, @NotNull Map<String, String> replaceMap) {
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    /**
     * Checks if a CommandSender has permission to execute the command.
     *
     * @param sender the CommandSender to check
     * @return true if the CommandSender has permission, false otherwise
     */
    protected boolean hasPermission(@NotNull CommandSender sender) {
        return permission.isEmpty() || sender.hasPermission(permission);
    }

    /**
     * Checks if a Player has permission to execute the command.
     *
     * @param player the Player to check
     * @return true if the Player has permission, false otherwise
     */
    protected boolean hasPermission(@NotNull Player player) {
        return hasPermission((CommandSender) player);
    }

    /**
     * Executes a command as a Runnable and handles any errors that occur during execution.
     *
     * @param sender the CommandSender executing the command
     * @param runnable the Runnable representing the command to be executed
     */
    private void executeCommand(CommandSender sender, Runnable runnable){
        try {
            runnable.run();
        } catch (Throwable throwable){

            sender.sendMessage(ChatColor.RED + "An error occurred while execute command.");
            sender.sendMessage(ChatColor.RED + String.format("%s: %s", throwable.getClass().getName(), throwable.getMessage()));

            throwable.printStackTrace();
        }
    }

    /**
     * Executes a tab complete as a Runnable and handles any errors that occur during execution.
     *
     * @param sender the CommandSender executing the command
     * @param callable the Callable representing the tab complete to be executed
     * @return tab complete
     */
    private List<String> executeTabComplete(CommandSender sender, Callable<List<String>> callable){
        try {
            return callable.call();
        } catch (Throwable throwable){

            sender.sendMessage(ChatColor.RED + "An error occurred while handle tab complete.");
            sender.sendMessage(ChatColor.RED + String.format("%s: %s", throwable.getClass().getName(), throwable.getMessage()));

            throwable.printStackTrace();

            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a subcommand based on its name or alias.
     *
     * @param name the name or alias of the subcommand to retrieve
     * @param checkAlias {@code true} to also check subcommand aliases, {@code false} otherwise
     * @return the {@link BukkitCommand} object representing the subcommand, or {@code null} if not found
     */
    public BukkitCommand getSubCommand(String name, boolean checkAlias){
        for (BukkitCommand subCommand: subCommands){

            if (subCommand.getName().equalsIgnoreCase(name)){
                return subCommand;
            }

            if (checkAlias){

                for (String s: subCommand.getAlias()){
                    if (name.equalsIgnoreCase(s)){
                        return subCommand;
                    }
                }

            }
        }
        return null;
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!hasPermission(sender)){
            sender.sendMessage(ColorUtil.colorize(getNoPermissionMessage()));
            return true;
        }

        if (args.length == 0){

            List<String> arguments = new ArrayList<>();

            if (sender instanceof Player){
                executeCommand(sender, () -> onCommand((Player) sender, arguments));
            } else {
                executeCommand(sender, () -> onCommand(sender, arguments));
            }

        } else {

            String nexArgs = args[0];

            BukkitCommand subCommand = getSubCommand(nexArgs, true);

            if (subCommand == null){

                List<String> arguments = Arrays.asList(args);

                if (sender instanceof Player){
                    executeCommand(sender, () -> onCommand((Player) sender, arguments));
                } else {
                    executeCommand(sender, () -> onCommand(sender, arguments));
                }

            } else {

                subCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));

            }
        }

        return true;
    }

    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (!hasPermission(sender)){
            return new ArrayList<>();
        }

        if (args.length == 1){

            List<String> list = Arrays.asList(args);

            if (sender instanceof Player){
                return executeTabComplete(sender, () -> onTabComplete((Player) sender, list));
            } else {
                return executeTabComplete(sender, () -> onTabComplete(sender, list));
            }

        } else {

            String nexArgs = args[0];

            BukkitCommand subCommand = getSubCommand(nexArgs, true);

            if (subCommand != null){

                return subCommand.onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));

            } else {

                List<String> arguments = Arrays.asList(args);

                if (sender instanceof Player){
                    return executeTabComplete(sender, () -> onTabComplete((Player) sender, arguments));
                } else {
                    return executeTabComplete(sender, () -> onTabComplete(sender, arguments));
                }
            }
        }
    }
}