# KPlugin Library - Usage Guide

Quick reference guide for using the KPlugin framework in your Bukkit/Spigot plugin.

## Plugin Setup

Extend `KPlugin` and override `enable()` / `disable()` methods:

```java
public class MyPlugin extends KPlugin {
    @Override
    public void enable() {
        new MyConfigFile(this);
        new MyMessageFile(this);
        new MyCommand().registerCommand(this); // Must call registerCommand()
    }
    
    @Override
    public void disable() {
        LoggerUtil.info("Plugin disabled!");
    }
}
```

---

## Command System

**CRITICAL**: Implement BOTH `onCommand()` overloads and call `registerCommand(plugin)`

KCommand: dev.khanh.plugin.kplugin.command.KCommand

```java
public class MyCommand extends KCommand {
    public MyCommand() {
        super("mycommand", Arrays.asList("mc"), "myplugin.cmd", "My command", "/mycommand");
        addSubCommand(new ReloadSubCommand(this));
    }
    
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull List<String> args) {
        sender.sendMessage(ColorUtil.colorize("&aCommand executed!"));
    }
    
    @Override
    public void onCommand(@NotNull Player player, @NotNull List<String> args) {
        MessageUtil.sendMessage(player, "command.success");
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull List<String> args) {
        return Arrays.asList("reload", "help");
    }
    
    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull List<String> args) {
        return onTabComplete((CommandSender) player, args);
    }
    
    @Override
    public String getNoPermissionMessage() {
        return "&cNo permission!";
    }
}

// Subcommand
public class ReloadSubCommand extends KCommand {
    public ReloadSubCommand(KCommand parent) {
        super("reload", parent, "myplugin.reload");
    }
    
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull List<String> args) {
        sender.sendMessage(ColorUtil.colorize("&aReloaded!"));
    }
    
    @Override
    public void onCommand(@NotNull Player player, @NotNull List<String> args) {
        onCommand((CommandSender) player, args);
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull List<String> args) {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull List<String> args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getNoPermissionMessage() {
        return "&cNo permission!";
    }
}
```

**Key Methods:**
- `addSubCommand(KCommand)` - Add subcommand
- `registerCommand(JavaPlugin)` - Register with server
- `hasPermission(CommandSender)` - Check permission
- `replacePlaceholders(String, Map<String, String>)` - Replace placeholders

---

## Configuration Files

### AbstractConfigFile (for config.yml)

AbstractConfigFile: dev.khanh.plugin.kplugin.file.AbstractConfigFile

Uses `"config-version"` key:

```java
public class MyConfigFile extends AbstractConfigFile {
    public MyConfigFile(KPlugin plugin) {
        super(plugin); // Auto creates config.yml
    }
    
    @Override
    protected void update(int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            getYaml().set("new-setting", getYaml().getString("old-setting"));
            getYaml().set("old-setting", null);
        }
    }
    
    public String getServerName() {
        return getYaml().getString("server.name", "Default");
    }
}
```

```yaml
config-version: 2  # Required key name
server:
  name: "My Server"
```

### GenericYamlFile (for custom YAML files)

GenericYamlFile: dev.khanh.plugin.kplugin.file.GenericYamlFile

Uses `"version"` key by default:

```java
public class DataFile extends GenericYamlFile {
    public DataFile(KPlugin plugin) {
        super(plugin, new File(plugin.getDataFolder(), "data.yml"), "data.yml", "version");
    }
    
    @Override
    protected void update(int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            getYaml().set("new-data", new ArrayList<>());
        }
    }
}
```

```yaml
version: 1  # Default key for GenericYamlFile
players:
  uuid-here:
    score: 100
```

**Key Methods:**
- `getYaml()` / `getDefaultYaml()` - Get YAML config
- `save()` / `reload()` - Save/reload file
- `getString(path, def)` / `getInt(path, def)` / `getBoolean(path, def)` - Get values
- `set(path, value)` / `contains(path)` - Set/check values

---

## Message System

MessageFile: dev.khanh.plugin.kplugin.file.MessageFile
MessageUtil: dev.khanh.plugin.kplugin.util.MessageUtil

```java
public class MyMessageFile extends MessageFile {
    public MyMessageFile(KPlugin plugin) {
        super(plugin); // Auto creates messages.yml and initializes MessageUtil
    }
}
```

**messages.yml:**
```yaml
prefix: "&8[&bMyPlugin&8] &r"
no-permission: "&cNo permission!"
player:
  welcome: "&aWelcome, &e%player%&a!"
  balance: "&aBalance: &e$%balance%"
```

**Usage:**
```java
// Get messages
String msg = MessageUtil.getMessage("player.welcome");
String colored = MessageUtil.getColorizedMessage("no-permission");
Component component = MessageUtil.getModernColorizedMessage("player.welcome");

// With placeholders
String welcome = MessageUtil.getColorizedMessage("player.welcome", 
    m -> m.replace("%player%", player.getName()));

// Send to player (with prefix)
MessageUtil.sendMessage(player, "player.welcome", 
    m -> m.replace("%player%", player.getName()));

// Send custom message (with prefix)
MessageUtil.sendMessageWithPrefix(player, "&aCustom message!");
```

**Key Methods:**
- `getMessage(key)` / `getColorizedMessage(key)` / `getModernColorizedMessage(key)`
- `getMessage(key, Function)` - With transformation
- `sendMessage(sender, key)` / `sendMessage(sender, key, Function)`
- `sendMessageWithPrefix(sender, message)` - Custom message with prefix

---

## Item Builder

ItemStackWrapper: dev.khanh.plugin.kplugin.item.ItemStackWrapper

**Fluent API:**
```java
ItemStack sword = new ItemStackWrapper(Material.DIAMOND_SWORD)
    .setDisplayName("&bLegendary Sword")
    .addLore("&7Powerful weapon", "&7Deals massive damage")
    // .setLore(List.of("Line 1", "Line 2"))
    .addEnchant(Enchantment.DAMAGE_ALL, 5)
    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
    .setUnbreakable(true)
    .build();

ItemStack head = new ItemStackWrapper(Material.PLAYER_HEAD)
    .setDisplayName("&eCustom Head")
    .setSkull("Notch")  // Player name, UUID, URL, or base64
    .build();
```

**From Configuration:**

**items.yml:**
```yaml
legendary-sword:
  material: DIAMOND_SWORD
  display-name: "&bLegendary Sword"
  lore:
    - "&7Powerful weapon"
    - "&eRarity: &6Legendary"
  enchantments:
    DAMAGE_ALL: 5
    FIRE_ASPECT: 2
  flags:
    - HIDE_ENCHANTS
  custom-model-data: 1001

player-head:
  skull: "Notch"
  display-name: "&eCustom Head"
```

**Load from config:**
```java
ConfigurationSection section = yaml.getConfigurationSection("legendary-sword");

// Without translation
ItemStack item = ItemStackWrapper.fromConfigurationSection(section);

// With placeholders
ItemStack item = ItemStackWrapper.fromConfigurationSection(section, text -> 
    text.replace("%player%", player.getName())
);
```

**Key Methods:**
- `setDisplayName(String)` / `setLore(String...)` / `addLore(String...)`
- `addEnchant(Enchantment, int)` / `addItemFlags(ItemFlag...)`
- `setUnbreakable(boolean)` / `setCustomModelData(int)` / `setSkull(String)`
- `build()` - Get final ItemStack
- `fromConfigurationSection(ConfigurationSection)` - Create from YAML

---

## Utilities

### ColorUtil

ColorUtil: dev.khanh.plugin.kplugin.util.ColorUtil

```java
String colored = ColorUtil.colorize("&aGreen &bBlue &cRed");  // & to §
Component component = ColorUtil.modernColorize("&aHello");     // & to Component
```

### LoggerUtil
LoggerUtil: dev.khanh.plugin.kplugin.util.LoggerUtil
```java
LoggerUtil.info("Plugin started");
LoggerUtil.info("Loaded %d players", count);
LoggerUtil.warning("Config missing!");
LoggerUtil.severe("Database error: %s", error);
LoggerUtil.message("&aPlugin reloaded!");  // Colorized console message
LoggerUtil.setDebug(true);
LoggerUtil.debug("Debug info: %s", data);
```