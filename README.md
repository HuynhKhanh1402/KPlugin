# KPlugin

A comprehensive and reusable Bukkit/Spigot plugin framework designed to simplify Minecraft plugin development. This library provides a solid foundation for building Minecraft plugins with a focus on maintainability, code reuse, and developer convenience.

## Features

- **Plugin Lifecycle Management**: Easily manage your plugin's lifecycle with a simple abstraction.
- **Command Framework**: Create complex command structures with subcommands and built-in tab completion.
- **Configuration System**: Handle YAML configuration files with automatic version management and updates.
- **Message System**: Centralized message management with support for color codes and MiniMessage format.
- **Item Builder API**: A fluent API for building and modifying ItemStacks.
- **Instance Management**: Singleton pattern implementation for easy access to your plugin's components.
- **Utility Classes**: Various utility classes for common tasks like scheduling, logging, and color formatting.

## Installation

### Maven

Add the following to your pom.xml:

```xml
<repositories>
    <!-- Add repository if not already included -->
    <repository>
        <id>your-repo</id>
        <url>https://your-repo-url</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.khanh.plugin</groupId>
        <artifactId>kplugin</artifactId>
        <version>3.0.4</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

## Usage

### Creating a Plugin

Extend the `KPlugin` class and implement the required methods:

```java
public class MyPlugin extends KPlugin {
    @Override
    public void enable() {
        // Plugin startup logic
        LoggerUtil.info("Plugin enabled!");
        
        // Register commands, event listeners, etc.
    }
    
    @Override
    public void disable() {
        // Plugin shutdown logic
        LoggerUtil.info("Plugin disabled!");
    }
}
```

### Creating Commands

Extend the `KCommand` class to create custom commands:

```java
public class MyCommand extends KCommand {
    public MyCommand() {
        super(null, "mycommand", Arrays.asList("mc", "mycmd"), 
              "myplugin.command", "My custom command", "/mycommand <arg>");
        
        // Register subcommands
        addSubCommand(new MySubCommand());
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // Command execution logic
        sender.sendMessage("Command executed!");
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        // Tab completion logic
        return Arrays.asList("option1", "option2");
    }
}
```

### Configuration Management

Create and manage configuration files:

```java
public class ConfigFile extends AbstractConfigFile {
    public ConfigFile(KPlugin plugin) {
        super(plugin);
    }
    
    @Override
    protected void update(int oldVersion, int newVersion) {
        // Handle config version updates
    }
    
    public String getSetting() {
        return getConfig().getString("setting", "default");
    }
}
```

### Message Management

Create and use a message file:

```java
public class Messages extends MessageFile {
    public Messages(KPlugin plugin) {
        super(plugin);
    }
    
    public void sendWelcome(Player player) {
        send(player, "welcome");
    }
}
```

### Item Creation

Use the ItemStackWrapper for easy ItemStack creation:

```java
ItemStack item = new ItemStackWrapper(Material.DIAMOND_SWORD)
    .setDisplayName("&bMagic Sword")
    .setLore(Arrays.asList("&7A powerful sword", "&7with magic properties"))
    .addEnchantment(Enchantment.DAMAGE_ALL, 5)
    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
    .build();
```

## Requirements

- Java 8 or higher
- Bukkit/Spigot/Paper 1.16.5 or higher

## Documentation

For detailed documentation, refer to the Javadoc included in the project.

## License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## Author

- **KhanhHuynh** - Discord: khanhhuynh

## Contributing

Contributions are welcome! Feel free to submit pull requests or open issues.
