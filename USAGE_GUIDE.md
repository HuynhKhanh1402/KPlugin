# KPlugin Usage Guide

A comprehensive guide for building Spigot/Folia plugins using KPlugin framework.

> **Note:** GUI features are covered in [GUI_GUIDE.md](GUI_GUIDE.md).

---

## Table of Contents

- [Getting Started](#getting-started)
- [Plugin Lifecycle](#plugin-lifecycle)
- [Command System](#command-system)
- [File Management](#file-management)
- [Item Building](#item-building)
- [Placeholder System](#placeholder-system)
- [Task Scheduling](#task-scheduling)
- [Instance Manager](#instance-manager)
- [Utilities](#utilities)

---

## Getting Started

### Extending KPlugin

Your main plugin class **must** extend `KPlugin` instead of `JavaPlugin`:

```java
public class MyPlugin extends KPlugin {

    @Override
    public void enable() {
        // Plugin startup logic
    }

    @Override
    public void disable() {
        // Plugin shutdown logic
    }
}
```

### Important Rules

- **Only one `KPlugin` subclass** can be active at a time. Attempting to run two will throw `IllegalStateException`.
- Use `enable()` / `disable()` instead of `onEnable()` / `onDisable()`.
- Access the plugin instance via `KPlugin.getKInstance()`.
- Access FoliaLib via `KPlugin.getFoliaLib()`.

---

## Command System

KPlugin provides `KCommand` — a powerful abstract command class with built-in subcommand routing, permission checking, tab-completion, and error handling.

### Creating a Basic Command

```java
public class GreetCommand extends KCommand {

    public GreetCommand() {
        super(
            "greet",                           // command name
            Arrays.asList("hello", "hi"),      // aliases
            "myplugin.greet",                  // permission
            "Greet a player",                  // description
            "/greet <player>"                  // usage
        );
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("Hello, " + sender.getName() + "!");
        } else {
            sender.sendMessage("Hello, " + args.get(0) + "!");
        }
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull List<String> args) {
        // Called when sender is a Player
        onCommand((CommandSender) player, args);
    }

    @Override
    public @NotNull String getNoPermissionMessage() {
        return "&cYou don't have permission to use this command.";
    }
}
```

### Registering the Command

```java
@Override
public void enable() {
    new GreetCommand().registerCommand(this);
}
```

> **Note:** `registerCommand()` dynamically registers the command at runtime — **no need to add it to `plugin.yml`**.

### Adding Subcommands

```java
public class AdminCommand extends KCommand {

    public AdminCommand() {
        super("myadmin", "myplugin.admin", "Admin commands", "/myadmin <sub>");

        // Add subcommands
        addSubCommand(new ReloadSubCommand(this));
        addSubCommand(new InfoSubCommand(this));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull List<String> args) {
        sender.sendMessage("Usage: /myadmin <reload|info>");
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull List<String> args) {
        onCommand((CommandSender) player, args);
    }

    @Override
    public @NotNull String getNoPermissionMessage() {
        return "&cNo permission.";
    }
}
```

```java
public class ReloadSubCommand extends KCommand {

    public ReloadSubCommand(KCommand parent) {
        super("reload", parent, "myplugin.admin.reload");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull List<String> args) {
        // Reload logic here
        sender.sendMessage("&aPlugin reloaded!");
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull List<String> args) {
        onCommand((CommandSender) player, args);
    }

    @Override
    public @NotNull String getNoPermissionMessage() {
        return "&cNo permission to reload.";
    }
}
```

**Result:** `/myadmin reload` routes to `ReloadSubCommand`, and `/myadmin info` routes to `InfoSubCommand`.

### Custom Tab Completion

```java
@Override
public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull List<String> args) {
    if (args.size() == 1) {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.startsWith(args.get(0)))
            .collect(Collectors.toList());
    }
    return super.onTabComplete(sender, args); // Default: shows subcommand names
}
```

### Command System Notes

| Feature | Detail |
|---|---|
| Auto routing | Subcommands are automatically routed based on the first argument |
| Permission check | Permissions are checked before execution; `getNoPermissionMessage()` is sent on denial |
| Error handling | Exceptions during execution are caught and a red error message is sent to the sender |
| Alias support | Both command aliases and subcommand aliases are checked |
| Duplicate check | `addSubCommand()` throws `IllegalArgumentException` if a duplicate name/alias is found |

---

## File Management

### GenericYamlFile

A base class for any YAML file with version-based auto-upgrade:

```java
public class DataFile extends GenericYamlFile {

    public DataFile(KPlugin plugin) {
        super(
            plugin,
            new File(plugin.getDataFolder(), "data.yml"), // disk file
            "data.yml"                                      // resource path in JAR
        );
    }

    @Override
    protected void update(int oldVersion, int newVersion) {
        // Migrate data between versions
        if (oldVersion < 2) {
            yaml.set("new-key", yaml.get("old-key"));
            yaml.set("old-key", null);
        }
    }
}
```

> **Important:** The YAML file **must** contain a `version` key (or custom key via the 4-argument constructor). If missing, an exception is thrown.

### AbstractConfigFile

A convenience wrapper for `config.yml`:

```java
public class ConfigFile extends AbstractConfigFile {

    public ConfigFile(KPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void update(int oldVersion, int newVersion) {
        // Auto-loads config.yml with version key "config-version"
    }
}
```

### MessageFile

Manages `messages.yml` with auto-update and color support:

```java
@Override
public void enable() {
    MessageFile messageFile = new MessageFile(this);

    // Usage
    String raw = messageFile.getMessage("welcome");
    String colored = messageFile.getColorizedMessage("welcome");
    Component modern = messageFile.getModernColorizedMessage("welcome");

    // With placeholder replacement
    String msg = messageFile.getMessage("player-join", s -> s.replace("{player}", "Steve"));

    // Send with prefix
    messageFile.sendMessage(player, "welcome");
    messageFile.sendMessage(player, "player-join", s -> s.replace("{player}", player.getName()));
}
```

### File Management Notes

- `save()` persists changes to disk; `reload()` reloads from disk.
- All `GenericYamlFile` subclasses expose delegated methods: `getString()`, `getInt()`, `getBoolean()`, `getList()`, `getStringList()`, `getConfigurationSection()`, etc.
- `MessageFile` automatically initializes `MessageUtil` for static access.
- Empty messages from `sendMessage()` are **not sent** by default (configurable with `allowEmpty` parameter).

---

## Item Building

### ItemBuilder (Recommended)

Fluent builder for creating `ItemStack` objects:

```java
ItemStack item = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("&6Legendary Sword")
    .lore("&7A powerful weapon", "&7Damage: &c50")
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .flags(ItemFlag.HIDE_ENCHANTS)
    .unbreakable()
    .glow()
    .customModelData(1001)
    .build();
```

#### Skull Support

The `skull()` method auto-detects the input format:

```java
// By player name
ItemBuilder.of(Material.PLAYER_HEAD).skull("Notch").build();

// By UUID
ItemBuilder.of(Material.PLAYER_HEAD).skull("069a79f4-44e9-4726-a5be-fca90e38aaf5").build();

// By texture URL
ItemBuilder.of(Material.PLAYER_HEAD).skull("http://textures.minecraft.net/texture/abc123...").build();

// By base64
ItemBuilder.of(Material.PLAYER_HEAD).skull("eyJ0ZXh0dXJlcyI6ey...").build();
```

#### Loading from Config

```yaml
# config.yml
my-item:
  material: DIAMOND_SWORD
  name: "&6Legendary Sword"
  lore:
    - "&7A powerful weapon"
    - "&7Damage: &c50"
  enchantments:
    sharpness: 5
  flags:
    - HIDE_ENCHANTS
  glow: true
  unbreakable: true
  custom-model-data: 1001
  skull: "Notch"          # or skull-owner / skull-texture
```

```java
ItemBuilder builder = ItemBuilder.fromConfig(config.getConfigurationSection("my-item"));
if (builder != null) {
    ItemStack item = builder.build();
}
```

#### Placeholder Replacement

```java
ItemStack item = ItemBuilder.of(Material.PAPER)
    .name("&eProfile: {player}")
    .lore("&7Level: {level}", "&7Balance: {balance}")
    .replacePlaceholders(s -> s
        .replace("{player}", player.getName())
        .replace("{level}", String.valueOf(player.getLevel()))
        .replace("{balance}", "1000"))
    .build();
```

### ItemTemplate

Config-based item template with built-in slot and placeholder support:

```java
// Load from config
ItemTemplate template = ItemTemplate.fromConfig(config.getConfigurationSection("my-item"));

// Build with placeholders
ItemStack item = template.build(Map.of("{player}", "Steve", "{level}", "10"));

// Build with Placeholders system
Placeholders ph = Placeholders.create().set("{player}", player.getName());
ItemStack item2 = template.build(ph);
ItemStack item3 = template.build(ph, player); // player-aware resolvers

// Slot configuration (supports ranges, lists, comma-separated)
List<Integer> slots = template.getSlots();   // e.g., [0, 1, 2, 10, 12, 14]
int firstSlot = template.getSlot();          // first slot or -1

// Convert to ItemBuilder for further customization
ItemStack modified = template.toBuilder().addLore("&7Extra line").build();
```

**Slot config formats:**

```yaml
slot: 13            # single
slot: "0-8"         # range
slot: "1,3,5,7"     # comma-separated
slots:              # list (mixed)
  - 10
  - "12-14"
  - "20,22"
```

### ItemStackWrapper (Deprecated)

> Use `ItemBuilder` instead. See the migration guide in the API reference.

---

## Placeholder System

The `Placeholders` class provides a flexible, format-free placeholder replacement system:

```java
// Basic static values
Placeholders ph = Placeholders.create()
    .set("{player}", player.getName())
    .set("%balance%", "1000")
    .set("$level", "10");

String result = ph.apply("Hello {player}! Balance: %balance%");
// → "Hello Steve! Balance: 1000"
```

### Dynamic Resolvers

```java
Placeholders ph = Placeholders.create()
    .resolver("{time}", () -> LocalTime.now().toString())
    .resolver("{random}", () -> String.valueOf(new Random().nextInt(100)));
```

### Player-Aware Resolvers

```java
Placeholders ph = Placeholders.create()
    .playerResolver("%health%", p -> String.valueOf((int) p.getHealth()))
    .playerResolver("%level%", p -> String.valueOf(p.getLevel()));

String result = ph.apply("HP: %health%, Level: %level%", player);
```

### Built-in Player Placeholders

```java
Placeholders ph = Placeholders.create()
    .withPlayerPlaceholders();          // {player}, {player_name}, {player_uuid}, etc.
    // or custom format:
    // .withPlayerPlaceholders("%", "%"); // %player%, %player_name%, etc.
```

### Integration with Other APIs

```java
// Combine with PlaceholderAPI
Function<String, String> resolver = ph.toFunction()
    .andThen(str -> PlaceholderAPI.setPlaceholders(player, str));

String result = resolver.apply("{custom} and %player_name%");
```

### Placeholder Notes

- **No forced format** — you define the exact replacement string (e.g., `{key}`, `%key%`, `$key`).
- Thread-safe (uses `ConcurrentHashMap`).
- Use `copy()` to clone, `merge()` to combine two instances.
- Use `toFunction()` to convert for use with `ItemBuilder.replacePlaceholders()` or `ItemTemplate.build()`.

---

## Task Scheduling

> ⭐ **Core Utility** — `TaskUtil` is one of the most frequently used classes in KPlugin. Nearly every plugin needs to schedule delayed, repeating, or asynchronous operations. Always use `TaskUtil` instead of the raw Bukkit scheduler or FoliaLib directly.

`TaskUtil` provides a **unified scheduling API** that works seamlessly on **Spigot**, **Paper**, and **Folia** servers. It wraps FoliaLib internally and exposes a clean, consistent interface — so you write your task code once and it runs correctly on any server platform.

> All delay/period values are in **ticks** (20 ticks = 1 second) unless using `TimeUnit` overloads.

### Synchronous Tasks

Run code on the **main server thread** (global region on Folia):

```java
// Run on the next server tick
TaskUtil.runSync(() -> player.sendMessage("Hello!"));

// Run after 60 ticks (3 seconds)
ScheduledTask task = TaskUtil.runSync(() -> doSomething(), 60);

// Repeating: starts after 20 ticks, then runs every 100 ticks
ScheduledTask repeating = TaskUtil.runSyncRepeating(() -> checkPlayers(), 20, 100);
```

### Asynchronous Tasks

Run code on a **separate thread** — ideal for database I/O, HTTP requests, or heavy computation. **Never access Bukkit API from async tasks** (no world/entity manipulation).

```java
// Run immediately on an async thread
TaskUtil.runAsync(() -> loadDataFromDatabase());

// Run after a delay (100 ticks = 5 seconds)
TaskUtil.runAsync(() -> saveData(), 100);

// Repeating async: runs every 1200 ticks (1 minute)
ScheduledTask task = TaskUtil.runAsyncRepeating(() -> syncExternalData(), 0, 1200);

// Common pattern: load async, then apply sync
TaskUtil.runAsync(() -> {
    Map<String, Object> data = fetchFromDatabase();
    TaskUtil.runSync(() -> applyData(player, data));  // Back to main thread
});
```

### Entity & Location Region Tasks (Folia Support)

On **Folia**, the server is multithreaded by region. You **must** use entity/location tasks to access entities or blocks safely. On Spigot/Paper, these fall back to the main thread.

```java
// Run on entity's owning region (critical for Folia thread safety)
TaskUtil.runAtEntity(player, () -> player.teleport(location));

// With delay
TaskUtil.runAtEntity(entity, () -> entity.remove(), 20);

// With fallback if entity becomes invalid (e.g., player disconnects)
TaskUtil.runAtEntity(entity, () -> entity.setHealth(20), () -> {
    LoggerUtil.warning("Entity no longer valid, skipping heal.");
}, 20);

// Repeating at entity's region
TaskUtil.runAtEntityRepeating(player, () -> applyEffect(player), 0, 20);
TaskUtil.runAtEntityRepeating(player, () -> tickEffect(), () -> cleanupEffect(), 0, 20);

// Location-based: run at the region owning a specific location
TaskUtil.runAtLocation(location, () -> location.getBlock().setType(Material.AIR));
TaskUtil.runAtLocation(location, () -> spawnParticles(location), 10);
TaskUtil.runAtLocationRepeating(location, () -> animateBlock(location), 0, 5);
```

### Self-Referencing Tasks (schedule* methods)

The `schedule*` variants pass the `ScheduledTask` handle into the consumer, allowing the task to **cancel itself**:

```java
// A repeating task that cancels itself when a condition is met
TaskUtil.scheduleSyncRepeating(task -> {
    if (conditionMet()) {
        task.cancel();
        return;
    }
    doWork();
}, 0, 20);

// Also works for async
TaskUtil.scheduleAsyncRepeating(task -> {
    if (pollExternalService()) task.cancel();
}, 0, 100);
```

### Cancellation & Utilities

```java
ScheduledTask task = TaskUtil.runSyncRepeating(() -> {}, 0, 20);
TaskUtil.cancel(task);       // Cancel single task
TaskUtil.cancelAllTasks();   // Cancel all plugin tasks

// Region ownership checks (useful on Folia)
boolean owns = TaskUtil.isOwnedByCurrentRegion(location);
boolean isFolia = TaskUtil.isFolia();
```

### Task Scheduling Best Practices

- **Prefer tick-based methods** over `TimeUnit` for delays under 1 minute — ticks are native to Minecraft.
- **Use `runAtEntity()`** for player/entity actions — ensures Folia thread safety, falls back to main thread on Spigot.
- **Use `runAsync()` for I/O** — never block the main thread with database/file/network calls.
- **Return to sync after async** — call `TaskUtil.runSync()` inside an async task to access Bukkit API.
- **Provide fallbacks on entity tasks** — players can disconnect, entities can despawn.
- **Call `cancelAllTasks()` in `disable()`** — prevents leaked tasks after plugin reload.

---

## Instance Manager

A centralized singleton registry for managing class instances:

```java
// Register
InstanceManager.registerInstance(MyService.class, new MyService());

// Retrieve (nullable)
MyService service = InstanceManager.getInstance(MyService.class);

// Retrieve (throws NoSuchElementException if missing)
MyService service = InstanceManager.getInstanceOrElseThrow(MyService.class);

// Remove
InstanceManager.removeInstance(MyService.class);
```

> **Note:** `InstanceManager.clearInstances()` is called automatically on plugin disable. Do not rely on registered instances after `disable()`.

---

## Utilities

### ColorUtil

> ⭐ **Frequently Used** — `ColorUtil` is the backbone of all text colorization in KPlugin. It is used internally by `MessageFile`, `MessageUtil`, `LoggerUtil`, and `ItemBuilder`.

`ColorUtil` converts legacy `&` color codes (e.g., `&a`, `&c`, `&l`) into colored text. It supports all standard Minecraft color codes (`&0`–`&f`) and formatting codes (`&l` bold, `&m` strikethrough, `&n` underline, `&o` italic, `&r` reset).

#### `colorize(String)` → String

Returns a legacy section-based string (`§a`). Use when APIs expect a plain colored `String`.

```java
String colored = ColorUtil.colorize("&aGreen &cRed &b&lBold Aqua");
player.sendMessage(ColorUtil.colorize("&aYou received &e" + amount + " &agold!"));
```

#### `modernColorize(String)` → Component

Returns an Adventure `Component`. Use with Paper's native Component API.

```java
Component message = ColorUtil.modernColorize("&6Gold text with &c&lbold red");
player.sendMessage(ColorUtil.modernColorize("&aSuccess!"));
```

| Method | Return Type | Use When |
|---|---|---|
| `colorize()` | `String` | Bukkit legacy APIs, scoreboard, boss bar, config values |
| `modernColorize()` | `Component` | Paper/Adventure APIs, rich text composition |

> **Tip:** `ColorUtil` is called internally throughout the framework (`MessageFile`, `ItemBuilder`, etc.). You only need to call it directly for custom text processing outside of the built-in utilities.

### LoggerUtil

Plugin-prefixed logging with debug mode:

```java
LoggerUtil.info("Server started");
LoggerUtil.warning("Config value missing: %s", key);
LoggerUtil.severe("Fatal error occurred");
LoggerUtil.message("&aColorized console message");

// Debug mode
LoggerUtil.setDebug(true);
LoggerUtil.debug("Loaded %d items", count); // Only printed when debug is on
```

### MessageUtil

> ⭐⭐ **Most Important Utility** — `MessageUtil` is the **recommended way** to send messages to players in KPlugin. **Always prefer `MessageUtil` over raw `sender.sendMessage()`** for a consistent, maintainable, and configurable messaging system.

`MessageUtil` is a static proxy for `MessageFile`. Once you create a `MessageFile` in `enable()`, `MessageUtil` is **automatically initialized** and ready to use from anywhere — commands, listeners, tasks — with no instance passing required.

#### Setup

```java
public class MyPlugin extends KPlugin {
    @Override
    public void enable() {
        new MessageFile(this); // ← MessageUtil is now ready to use everywhere
    }
}
```

Example `messages.yml`:

```yaml
prefix: "&8[&bMyPlugin&8] &7"
welcome: "&aWelcome to the server, {player}!"
no-permission: "&cYou do not have permission to do that."
player-join: "&e{player} &7has joined the server."
item-received: "&7You received &e{amount}x &b{item}&7."
```

#### Retrieving Messages

```java
// Raw message
String raw = MessageUtil.getMessage("welcome");

// With placeholder replacement
String msg = MessageUtil.getMessage("welcome", s -> s.replace("{player}", playerName));

// With legacy color (§ codes)
String colored = MessageUtil.getColorizedMessage("welcome");
String coloredWithPh = MessageUtil.getColorizedMessage("welcome", s -> s.replace("{player}", "Steve"));

// As modern Adventure Component
Component modern = MessageUtil.getModernColorizedMessage("welcome");
Component modernWithPh = MessageUtil.getModernColorizedMessage("welcome", s -> s.replace("{player}", "Steve"));
```

#### Sending Messages (Recommended)

`sendMessage()` **automatically prepends the prefix** from `messages.yml` and colorizes the output. Empty messages are silently skipped.

```java
// Simple message with prefix
MessageUtil.sendMessage(player, "welcome");

// With placeholder replacement
MessageUtil.sendMessage(player, "player-join", s -> s.replace("{player}", player.getName()));

// With multiple placeholders
MessageUtil.sendMessage(player, "item-received", s -> s
    .replace("{amount}", String.valueOf(amount))
    .replace("{item}", itemName));

// Allow sending even if message is empty
MessageUtil.sendMessage(player, "some-key", true);

// Full overload: sender + key + function + allowEmpty
MessageUtil.sendMessage(sender, "teleport", s -> s.replace("{loc}", loc), false);

// Custom one-off message with prefix (not from messages.yml)
MessageUtil.sendMessageWithPrefix(sender, "&aReloaded in " + ms + "ms.");
```

#### Integration with Placeholders

```java
Placeholders ph = new Placeholders()
    .set("{player}", player.getName())
    .set("{balance}", formatBalance(balance));

MessageUtil.sendMessage(player, "welcome", ph.toFunction());
```

#### Why MessageUtil Over Raw sendMessage?

```java
// ❌ Bad: Hard-coded, no prefix, not configurable
player.sendMessage(ChatColor.GREEN + "Welcome, " + player.getName() + "!");

// ✅ Good: Centralized, prefixed, configurable by server admins
MessageUtil.sendMessage(player, "welcome", s -> s.replace("{player}", player.getName()));
```

> **Best Practice:** Define **every player-facing message** in `messages.yml` and access it through `MessageUtil`. This lets server admins edit messages without recompiling, keeps all messages consistently prefixed, and automatically handles color codes and empty message safety.

### TeleportUtil

Async teleportation compatible with Folia/Paper/Spigot:

```java
TeleportUtil.teleportAsync(player, targetLocation).thenAccept(success -> {
    if (success) {
        player.sendMessage("Teleported!");
    }
});

// With teleport cause
TeleportUtil.teleportAsync(player, location, TeleportCause.PLUGIN);
```

> **Important:** On Spigot, teleport is performed on the next tick (not instant). Always use the returned `CompletableFuture` for post-teleport logic.

### SoundUtil

Multi-version sound playback with legacy-to-modern mapping:

```java
// Modern format
SoundUtil.play(player, "entity.player.levelup", 1.0f, 1.0f);

// Legacy format (auto-converted)
SoundUtil.play(player, "LEVEL_UP", 1.0f, 1.0f);

// Predefined GUI sounds
SoundUtil.playClickSound(player);
SoundUtil.playOpenSound(player);
SoundUtil.playCloseSound(player);
SoundUtil.playNavigateSound(player);
SoundUtil.playErrorSound(player);
SoundUtil.playSuccessSound(player);

// Validate
boolean valid = SoundUtil.isValidSound("entity.player.levelup"); // true
```

---

## Quick Reference

| Feature | Class | Key Method |
|---|---|---|
| Plugin base | `KPlugin` | `enable()`, `disable()` |
| Commands | `KCommand` | `registerCommand()`, `addSubCommand()` |
| Config files | `AbstractConfigFile`, `GenericYamlFile` | `save()`, `reload()`, `update()` |
| Messages | `MessageFile`, `MessageUtil` | `getMessage()`, `sendMessage()` |
| Items | `ItemBuilder`, `ItemTemplate` | `build()`, `fromConfig()` |
| Placeholders | `Placeholders` | `set()`, `apply()`, `toFunction()` |
| Scheduling | `TaskUtil` | `runSync()`, `runAsync()`, `runAtEntity()` |
| Instances | `InstanceManager` | `registerInstance()`, `getInstance()` |
| Colors | `ColorUtil` | `colorize()`, `modernColorize()` |
| Logging | `LoggerUtil` | `info()`, `warning()`, `debug()` |
| Teleport | `TeleportUtil` | `teleportAsync()` |
| Sounds | `SoundUtil` | `play()`, `playClickSound()` |
