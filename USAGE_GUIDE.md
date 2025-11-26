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

### TaskUtil (with FoliaLib support)
TaskUtil: dev.khanh.plugin.kplugin.util.TaskUtil

> **⚠️ FOLIA SUPPORT**: KPlugin fully supports Folia with region-based threading. To ensure your plugin works correctly on Spigot/Paper and Folia, **always prioritize using the appropriate task method for the correct context**.

**Basic Task Scheduling:**
```java
// Sync (main/global thread) - for Bukkit API calls
TaskUtil.runSync(() -> player.teleport(location));
TaskUtil.runSync(() -> player.teleport(location), 60L);  // 3 seconds delay
TaskUtil.runSyncRepeating(() -> checkPlayers(), 0L, 20L);  // Every second

// Async (separate thread) - for heavy operations
TaskUtil.runAsync(() -> {
    Location location = fetchFromDatabase();
    TaskUtil.runSync(() -> player.teleport(location));  // Switch back to sync
});
TaskUtil.runAsync(() -> processData(), 100L);  // 5 seconds delay
TaskUtil.runAsyncRepeating(() -> autoSave(), 0L, 6000L);  // Every 5 minutes
```

**Folia Region-Specific Tasks:**
```java
// Run on entity's region (Folia) or main thread (Spigot/Paper)
TaskUtil.runAtEntity(player, () -> {
    player.setHealth(20.0);
});

TaskUtil.runAtEntity(player, () -> player.damage(5), 60L);  // Damage after 3s
TaskUtil.runAtEntityRepeating(npc, () -> npc.lookAt(target), 0L, 5L);  // Every 0.25s

// Run on location's region (Folia) or main thread (Spigot/Paper)
Location spawn = world.getSpawnLocation();
TaskUtil.runAtLocation(spawn, () -> {
    world.strikeLightning(spawn);
});

TaskUtil.runAtLocation(blockLocation, () -> block.setType(Material.AIR), 100L);
TaskUtil.runAtLocationRepeating(location, () -> spawnParticles(), 0L, 10L);
```

**Task Management:**
```java
// Store task reference
WrappedTask task = TaskUtil.runSyncRepeating(() -> updateBoard(), 0L, 20L);

// Cancel task later
TaskUtil.cancel(task);

// Check server type
if (TaskUtil.isFolia()) {
    LoggerUtil.info("Running on Folia!");
}
```

**Key Methods:**
- `runSync(Runnable)` / `runSync(Runnable, delay)` - Global region sync
- `runSyncRepeating(Runnable, delay, period)` - Repeating sync task
- `runAsync(Runnable)` / `runAsync(Runnable, delay)` - Async thread
- `runAsyncRepeating(Runnable, delay, period)` - Repeating async task
- `runAtEntity(Entity, Runnable)` / `runAtEntity(Entity, Runnable, delay)` - Entity region
- `runAtEntityRepeating(Entity, Runnable, delay, period)` - Repeating entity task
- `runAtLocation(Location, Runnable)` / `runAtLocation(Location, delay)` - Location region
- `runAtLocationRepeating(Location, Runnable, delay, period)` - Repeating location task
- `cancel(WrappedTask)` - Cancel task
- `isFolia()` - Check if running on Folia

---

### TeleportUtil (with FoliaLib support)
TeleportUtil: dev.khanh.plugin.kplugin.util.TeleportUtil

> **⚠️ FOLIA SUPPORT**: TeleportUtil provides safe, asynchronous entity teleportation compatible with Folia, Paper, and Spigot. Always use TeleportUtil for entity teleports to ensure region/thread safety on Folia servers.

**Platform Behavior:**
- **Folia:** Teleports the entity asynchronously in its region (thread-safe)
- **Paper (with async teleport support):** Teleports asynchronously
- **Spigot:** Teleports on the next tick (not instant)

**Why not instant teleport on Spigot?**
- Avoids thread safety issues
- Exposes bugs from expecting instant teleports on all platforms

**Usage:**
```java
// Teleport entity asynchronously
CompletableFuture<Boolean> future = TeleportUtil.teleportAsync(entity, location);
future.thenAccept(success -> {
    if (success) {
        // Teleport succeeded
    } else {
        // Teleport failed
    }
});

// With teleport cause
CompletableFuture<Boolean> future = TeleportUtil.teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
```

**Best Practices:**
- Always use the returned `CompletableFuture<Boolean>` to handle post-teleport logic
- For Folia compatibility, prefer region-based scheduling for entity/world operations (see TaskUtil section)
- Do not use Bukkit's synchronous teleport for entities on Folia

**Golden Rule:**
- For any entity teleport, use `TeleportUtil.teleportAsync()`
- For any entity/world operation, use the correct TaskUtil region method (`runAtEntity`, `runAtLocation`)

**See also:** [TaskUtil section above for region-based scheduling]

---

#### 📌 Best Practices for Folia Compatibility

**1. Entity Operations - Always use `runAtEntity()`:**
```java
// ✅ CORRECT: Interact with entity in its own region
TaskUtil.runAtEntity(player, () -> {
    player.setHealth(20.0);
    player.getInventory().addItem(item);
    player.teleport(location);
});

// ❌ WRONG: Using runSync() for entity operations (may crash on Folia)
TaskUtil.runSync(() -> player.setHealth(20.0)); // NOT SAFE!
```

**2. World/Block Operations - Always use `runAtLocation()`:**
```java
// ✅ CORRECT: Interact with block/world in its region
TaskUtil.runAtLocation(blockLocation, () -> {
    block.setType(Material.DIAMOND_BLOCK);
    world.spawnParticle(Particle.FLAME, location, 10);
    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
});

// ❌ WRONG: Using runSync() for world operations
TaskUtil.runSync(() -> block.setType(Material.STONE)); // NOT SAFE!
```

**3. Mixed Entity + Location Operations:**
```java
// ✅ CORRECT: Nested tasks when working with multiple regions
TaskUtil.runAtEntity(player, () -> {
    Location target = player.getTargetBlock(null, 5).getLocation();
    TaskUtil.runAtLocation(target, () -> {
        target.getWorld().createExplosion(target, 4.0f);
    });
});
```

**4. Global Operations - Use `runSync()`:**
```java
// ✅ CORRECT: Global operations not tied to specific entity/world
TaskUtil.runSync(() -> {
    Bukkit.broadcastMessage("Server restarting in 5 minutes!");
    plugin.saveAllData();
});
```

**5. Heavy Computations - Always use `runAsync()`:**
```java
// ✅ CORRECT: Heavy computation in async, then sync back when needed
TaskUtil.runAsync(() -> {
    List<UUID> topPlayers = calculateTopPlayers(); // Heavy operation
    
    TaskUtil.runSync(() -> {
        Bukkit.broadcastMessage("Top players calculated!");
    });
});

// ❌ WRONG: Heavy computation in sync task
TaskUtil.runSync(() -> calculateTopPlayers()); // Will lag the server!
```

**6. Database Operations - Async + Entity Region:**
```java
// ✅ CORRECT: Load from database async, then apply to entity in its region
TaskUtil.runAsync(() -> {
    PlayerData data = database.loadPlayerData(player.getUniqueId());
    
    TaskUtil.runAtEntity(player, () -> {
        player.setHealth(data.getHealth());
        player.getInventory().setContents(data.getInventory());
    });
});
```

**7. Repeating Tasks with Entity/Location:**
```java
// ✅ CORRECT: Repeating task tracking entity movement
TaskUtil.runAtEntityRepeating(boss, () -> {
    // Update boss AI, check nearby players, etc.
    for (Entity nearby : boss.getNearbyEntities(10, 10, 10)) {
        if (nearby instanceof Player) {
            ((Player) nearby).damage(1.0);
        }
    }
}, 0L, 20L); // Every second

// ✅ CORRECT: Repeating task for location-based effects
TaskUtil.runAtLocationRepeating(shrineLocation, () -> {
    shrineLocation.getWorld().spawnParticle(
        Particle.ENCHANTMENT_TABLE, 
        shrineLocation, 
        20
    );
}, 0L, 10L); // Every 0.5 seconds
```

**⚠️ Golden Rules:**
- **Entity operations** (player.xxx(), entity.xxx()) → `runAtEntity()`
- **World/Block operations** (world.xxx(), block.xxx(), location-based) → `runAtLocation()`
- **Global/Plugin operations** (Bukkit.xxx(), config, broadcasts) → `runSync()`
- **Heavy computations** (database, calculations, I/O) → `runAsync()`

**📚 Why This Matters:**
- **Folia** uses region-based threading: each world region runs on its own thread
- Accessing entity/world from wrong thread → **ConcurrentModificationException** or crash
- Using the correct methods ensures code works on **Spigot, Paper, and Folia**
- FoliaLib automatically handles compatibility: on Spigot/Paper, everything runs on main thread
