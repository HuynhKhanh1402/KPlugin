# KPlugin Framework - Complete API Reference

> **Version**: 3.2.0  
> **Compatibility**: Java 8+, Minecraft 1.16.5+  
> **Last Updated**: January 30, 2026

---

## Table of Contents

1. [Core Package](#core-package)
2. [Command System](#command-system)
3. [Configuration Files](#configuration-files)
4. [GUI System](#gui-system)
5. [Instance Management](#instance-management)
6. [Item System](#item-system)
7. [Placeholder System](#placeholder-system)
8. [Utility Classes](#utility-classes)
9. [Important Warnings & Best Practices](#important-warnings--best-practices)

---

## Core Package

### `KPlugin` (Abstract Class)

**Package**: `dev.khanh.plugin.kplugin`

**Purpose**: Base class for all plugins using the KPlugin framework. Manages lifecycle, singleton enforcement, and FoliaLib initialization.

**Inheritance**: `extends JavaPlugin`

#### Usage Pattern

```java
public class MyPlugin extends KPlugin {
    @Override
    protected void enable() {
        // Your plugin enable logic
    }
    
    @Override
    protected void disable() {
        // Your plugin disable logic
    }
}
```

#### Public Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `enable()` | `void` | Override this instead of `onEnable()` |
| `disable()` | `void` | Override this instead of `onDisable()` |
| `getInstance()` | `KPlugin` | Get the current plugin instance |
| `getFoliaLib()` | `FoliaLib` | Get the FoliaLib scheduler instance |

#### Important Notes

- ⚠️ **Singleton Enforcement**: Only one instance per plugin class is allowed via `InstanceManager`
- The framework calls `onEnable()` internally, which prints author branding and registers the instance
- Always call `super.enable()` and `super.disable()` if you override them
- FoliaLib is initialized automatically during `onEnable()`
- All instances are cleared from `InstanceManager` on disable

#### Example

```java
public class MyPlugin extends KPlugin {
    private GUIManager guiManager;
    
    @Override
    protected void enable() {
        LoggerUtil.info("MyPlugin enabled!");
        guiManager = new GUIManager(this);
        // Register commands, listeners, etc.
    }
    
    @Override
    protected void disable() {
        if (guiManager != null) {
            guiManager.shutdown();
        }
        LoggerUtil.info("MyPlugin disabled!");
    }
}
```

---

## Command System

### `KCommand` (Abstract Class)

**Package**: `dev.khanh.plugin.kplugin.command`

**Purpose**: Reflection-based command system with dynamic registration and subcommand tree support.

**Inheritance**: `implements CommandExecutor, TabCompleter`

#### Constructor

```java
protected KCommand(String name)
protected KCommand(String name, String... aliases)
```

#### Public Fields

| Field | Type | Description |
|-------|------|-------------|
| `parent` | `KCommand` | Parent command reference |
| `name` | `String` | Command name |
| `aliases` | `List<String>` | Command aliases |
| `permission` | `String` | Required permission (nullable) |
| `description` | `String` | Command description |
| `usage` | `String` | Usage syntax |

#### Abstract Methods (Must Implement)

| Method | Parameters | Description |
|--------|------------|-------------|
| `onCommand()` | `CommandSender sender, List<String> args` | Handler for all senders |
| `onCommand()` | `Player player, List<String> args` | Handler for player senders |
| `onTabComplete()` | `CommandSender sender, List<String> args` | Tab completion for all |
| `onTabComplete()` | `Player player, List<String> args` | Tab completion for players |

#### Public Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `addSubCommand(KCommand)` | `KCommand` | Adds a subcommand to the tree |
| `registerCommand(KPlugin)` | `void` | Registers command via reflection |
| `getNoPermissionMessage()` | `String` | Returns no-permission message |
| `getSubCommand(String)` | `KCommand` | Gets subcommand by name/alias |

#### Important Notes

- ⚠️ **Dual Overload Pattern**: You MUST implement BOTH `onCommand()` methods - one for `CommandSender`, one for `Player`
- ⚠️ **Manual Registration**: Call `registerCommand(plugin)` explicitly - no auto-registration
- The framework routes to the correct handler based on sender type automatically
- Uses reflection to access Bukkit's internal `CommandMap` and create `PluginCommand` instances
- Supports nested subcommand tree structure with parent-child relationships

#### Example

```java
public class TeleportCommand extends KCommand {
    
    public TeleportCommand() {
        super("teleport", "tp");
        this.permission = "myplugin.teleport";
        this.description = "Teleport to a location";
        this.usage = "/teleport <x> <y> <z>";
    }
    
    @Override
    public void onCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return;
        }
        // Automatically routed to player handler below
    }
    
    @Override
    public void onCommand(Player player, List<String> args) {
        if (args.size() != 3) {
            player.sendMessage(usage);
            return;
        }
        
        double x = Double.parseDouble(args.get(0));
        double y = Double.parseDouble(args.get(1));
        double z = Double.parseDouble(args.get(2));
        
        Location loc = new Location(player.getWorld(), x, y, z);
        TeleportUtil.teleport(player, loc).thenAccept(result -> {
            if (result) {
                MessageUtil.sendMessageWithPrefix(player, "&aTeleported!");
            }
        });
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> onTabComplete(Player player, List<String> args) {
        if (args.size() == 1) {
            return Arrays.asList("100", "64", "0");
        }
        return Collections.emptyList();
    }
}

// Registration:
TeleportCommand tpCmd = new TeleportCommand();
tpCmd.registerCommand(this);
```

#### Subcommand Example

```java
public class AdminCommand extends KCommand {
    public AdminCommand() {
        super("admin");
        
        // Add subcommands
        addSubCommand(new ReloadSubCommand());
        addSubCommand(new DebugSubCommand());
    }
    
    @Override
    public void onCommand(CommandSender sender, List<String> args) {
        sender.sendMessage("Use /admin <reload|debug>");
    }
    
    // Implement other required methods...
}
```

---

## Configuration Files

### `GenericYamlFile` (Abstract Class)

**Package**: `dev.khanh.plugin.kplugin.file`

**Purpose**: Base class for YAML configuration files with version tracking and automatic migration support.

#### Constructor

```java
protected GenericYamlFile(KPlugin plugin, String fileName, String resourcePath)
protected GenericYamlFile(KPlugin plugin, String fileName, String resourcePath, String versionKey)
```

#### Public Methods

| Method                           | Return Type | Description |
|----------------------------------|-------------|-------------|
| `save()`                         | `void` | Saves YAML to disk |
| `reload()`                       | `void` | Reloads YAML from disk |
| `update(int oldVer, int newVer)` | `void` | Override for version migrations |
| `getFile()`                      | `File` | Returns file path |
| `getConfig()`                    | `FileConfiguration` | Returns active YAML config |
| `getDefaultConfig()`             | `FileConfiguration` | Returns default resource YAML |

Plus all standard YAML getters: `getString()`, `getInt()`, `getBoolean()`, `getStringList()`, `getConfigurationSection()`, etc.

#### Important Notes

- Default version key is `"version"` (configurable via constructor)
- Auto-copies default resource from JAR if file doesn't exist
- Calls `update()` method when `defaultVersion > fileVersion`
- Thread-safe for reading

#### Example

```java
public class DatabaseConfig extends GenericYamlFile {
    
    public DatabaseConfig(KPlugin plugin) {
        super(plugin, "database.yml", "database.yml");
    }
    
    @Override
    protected void update(int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migrate from v1 to v2
            getConfig().set("new-option", "default-value");
            LoggerUtil.info("Migrated database.yml from v1 to v2");
        }
        
        if (oldVersion < 3) {
            // Migrate from v2 to v3
            getConfig().set("another-option", true);
            LoggerUtil.info("Migrated database.yml from v2 to v3");
        }
        
        // Framework auto-saves after this method
    }
    
    // Convenience getters
    public String getHost() {
        return getString("mysql.host", "localhost");
    }
    
    public int getPort() {
        return getInt("mysql.port", 3306);
    }
}
```

---

### `AbstractConfigFile` (Abstract Class)

**Package**: `dev.khanh.plugin.kplugin.file`

**Purpose**: Specialized config file for `config.yml` with hardcoded version key.

**Inheritance**: `extends GenericYamlFile`

#### Constructor

```java
protected AbstractConfigFile(KPlugin plugin)
```

#### Important Notes

- ⚠️ Hardcoded file name: `config.yml`
- ⚠️ Hardcoded version key: `"config-version"` (differs from GenericYamlFile's `"version"`)
- Always uses `config.yml` from plugin data folder and JAR resource

#### Example

```java
public class MainConfig extends AbstractConfigFile {
    
    public MainConfig(KPlugin plugin) {
        super(plugin);
    }
    
    @Override
    protected void update(int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            getConfig().set("enable-feature-x", true);
        }
    }
    
    public boolean isFeatureXEnabled() {
        return getBoolean("enable-feature-x", false);
    }
}
```

---

### `MessageFile` (Class)

**Package**: `dev.khanh.plugin.kplugin.file`

**Purpose**: Manages `messages.yml` for multilingual support with auto-updating missing keys.

#### Constructor

```java
public MessageFile(KPlugin plugin)
public MessageFile(KPlugin plugin, String fileName, String resourcePath)
```

#### Public Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getMessage(String)` | `String` | Gets raw message |
| `getMessage(String, Function)` | `String` | Gets with function applied |
| `getColoredMessage(String)` | `String` | Gets with `&` codes converted |
| `getColoredMessage(String, Function)` | `String` | Colorized with function |
| `getComponent(String)` | `Component` | Gets Adventure Component |
| `getComponent(String, Function)` | `Component` | Component with function |
| `sendMessage(Player, String)` | `void` | Sends with prefix |
| `sendMessage(Player, String, Function)` | `void` | Sends with function |
| `sendMessageIfNotEmpty(Player, String)` | `void` | With empty check |
| `sendMessage(Player, String, boolean, Function)` | `void` | Full-featured send |

#### Important Notes

- ⚠️ **No version tracking** - only adds missing keys from default resource
- Initializes `MessageUtil` static helper automatically
- Prints warning + stack trace if key not found
- Always uses `messages.yml` filename by default

#### Example

```java
// messages.yml:
/*
prefix: "&8[&6MyPlugin&8]"
welcome: "{prefix} &aWelcome, {player}!"
goodbye: "{prefix} &cGoodbye!"
*/

MessageFile messages = new MessageFile(plugin);

// Get raw
String raw = messages.getMessage("welcome"); // "{prefix} &aWelcome, {player}!"

// Get colored
String colored = messages.getColoredMessage("welcome"); // With & codes

// Get with placeholder
Placeholders ph = Placeholders.create().set("{player}", player.getName());
messages.sendMessage(player, "welcome", ph.toFunction());
```

---

## GUI System

### `GUI` (Class)

**Package**: `dev.khanh.plugin.kplugin.gui`

**Purpose**: Core GUI class for creating interactive inventory menus with holder-based validation.

#### Static Factory Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `builder()` | `int rows` | `GUIBuilder` | Creates builder (1-6 rows) |
| `builderBySize()` | `int size` | `GUIBuilder` | By size (9/18/27/36/45/54) |
| `create()` | `int rows, String title` | `GUI` | Creates GUI directly |

#### Slot Access Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `slot()` | `int slot` | `SlotHandle` | Single slot handle |
| `slotRange()` | `int start, int end` | `SlotRangeHandle` | Range (inclusive) |
| `slots()` | `int... indices` | `MultiSlotHandle` | Multiple slots |
| `row()` | `int row` | `SlotRangeHandle` | Entire row (0-based) |
| `column()` | `int column` | `MultiSlotHandle` | Column (0-8) |

#### Event Handlers

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `onGlobalClick()` | `Consumer<ClickContext>` | `GUI` | Global click handler |
| `onClose()` | `Consumer<Player>` | `GUI` | Close handler |
| `onOpen()` | `Consumer<Player>` | `GUI` | Open handler |

#### Properties

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getGuiId()` | `UUID` | Unique GUI identifier |
| `getRows()` | `int` | Number of rows |
| `getSize()` | `int` | Total slot count |
| `getTitle()` | `String` | GUI title |
| `isViewOnly()` | `boolean` | Check if view-only |
| `setViewOnly(boolean)` | `GUI` | Set view-only mode |

#### Helper Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `fillBorder()` | `ItemStack` | `GUI` | Fills border |
| `fillEmpty()` | `ItemStack` | `GUI` | Fills empty slots |
| `clear()` | - | `GUI` | Clears all |
| `refresh()` | - | `GUI` | Refreshes for viewers |
| `updateTitle()` | `String newTitle` | `GUI` | Updates title dynamically |
| `open()` | `Player` | `void` | Opens GUI |
| `close()` | `Player` | `void` | Closes GUI |

#### Metadata Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `setMeta()` | `String key, Object value` | `GUI` | Sets GUI metadata |
| `getMeta()` | `String key` | `T` | Gets metadata |
| `getMeta()` | `String key, T default` | `T` | With default |

#### Important Notes

- Uses `GUIHolder` with manager UUID for validation (NOT PDC tags)
- Automatically invalidates stale GUIs from plugin reloads
- Supports per-slot click handlers and disabled states
- View-only mode prevents all item interactions
- `updateTitle()` recreates inventory with new title while reusing the same GUIHolder (efficient)

#### Example

```java
GUI gui = GUI.builder(3)
    .title("&6My Shop")
    .viewOnly(true)
    .onOpen(p -> MessageUtil.sendMessageWithPrefix(p, "&aShop opened!"))
    .onClose(p -> MessageUtil.sendMessageWithPrefix(p, "&cShop closed!"))
    .build();

// Fill border
gui.fillBorder(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

// Add items
gui.slot(13).set(ItemBuilder.of(Material.DIAMOND)
        .name("&bDiamond")
        .lore("&7Price: &e$100")
        .build())
    .onClick(ctx -> {
        MessageUtil.sendMessageWithPrefix(ctx.player(), "&aPurchased diamond!");
        ctx.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    });

// Open
gui.open(player);

// Update title dynamically (e.g., for progress tracking)
TaskUtil.runSync(() -> {
    gui.updateTitle("&aShop - Sale 50% OFF!");
}, 40L); // 2 seconds later
```

**Dynamic Title Update Example**:

```java
// Real-time countdown
GUI countdown = GUI.builder(3)
    .title("&cTime: 60s")
    .build();

countdown.open(player);

final int[] seconds = {60};
TaskUtil.runSyncRepeating(() -> {
    if (seconds[0] <= 0) {
        countdown.updateTitle("&a&lComplete!");
        return;
    }
    seconds[0]--;
    countdown.updateTitle("&cTime: " + seconds[0] + "s");
}, 0L, 20L);
```

**Important Notes on `updateTitle()`**:
- Before GUI initialization: Only updates the internal title field (very efficient)
- After initialization: Recreates inventory with new title and reopens for viewers
- **Reuses existing GUIHolder** - no new object allocation (memory efficient)
- All items, handlers, and metadata are preserved
- Use for progress bars, countdowns, dynamic stats
- Avoid excessive updates (e.g., every tick) - use item lore for very frequent updates

---

### `GUIBuilder` (Final Class)

**Package**: `dev.khanh.plugin.kplugin.gui`

**Purpose**: Fluent builder for creating GUI instances with pre-configured slots.

#### Public Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `title()` | `String` | `GUIBuilder` | Sets title |
| `viewOnly()` | `boolean` | `GUIBuilder` | Sets view-only |
| `onGlobalClick()` | `Consumer` | `GUIBuilder` | Global handler |
| `onClose()` | `Consumer` | `GUIBuilder` | Close handler |
| `onOpen()` | `Consumer` | `GUIBuilder` | Open handler |
| `slot()` | `int, ItemStack` | `GUIBuilder` | Configure slot |
| `slot()` | `int, ItemBuilder` | `GUIBuilder` | With builder |
| `slot()` | `int, ItemStack, Consumer` | `GUIBuilder` | With click |
| `disabledSlot()` | `int, ItemStack, String` | `GUIBuilder` | Disabled |
| `fillBorder()` | `ItemStack` | `GUIBuilder` | Fill border |
| `fillEmpty()` | `ItemStack` | `GUIBuilder` | Fill empty |
| `build()` | - | `GUI` | Builds GUI |

#### Important Notes

- Apply order: border → slot configs → fill empty
- All configurations stored until `build()` is called

#### Example

```java
GUI gui = GUI.builder(6)
    .title("&cAdmin Panel")
    .viewOnly(true)
    .fillBorder(ItemBuilder.of(Material.RED_STAINED_GLASS_PANE).name(" ").build())
    .slot(13, ItemBuilder.of(Material.COMMAND_BLOCK).name("&eReload").build(), 
        ctx -> {
            plugin.reload();
            ctx.player().sendMessage("&aReloaded!");
        })
    .slot(14, ItemBuilder.of(Material.BARRIER).name("&cStop Server").build(),
        ctx -> Bukkit.shutdown())
    .build();

gui.open(player);
```

---

### `GUIManager` (Singleton Class)

**Package**: `dev.khanh.plugin.kplugin.gui`

**Purpose**: Central manager for GUI lifecycle, player tracking, and event handling.

#### Public Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getInstance()` | `GUIManager` | Gets singleton |
| `getManagerUUID()` | `UUID` | Manager UUID for validation |
| `openGUI(Player, GUI)` | `void` | Opens GUI |
| `closeGUI(Player)` | `void` | Closes player's GUI |
| `getOpenGUI(Player)` | `GUI` | Gets player's open GUI |
| `getGUI(Inventory)` | `GUI` | Gets GUI from inventory |
| `hasGUIOpen(Player)` | `boolean` | Check if has GUI open |
| `clearAll()` | `void` | Clears tracking |
| `closeAll()` | `void` | Closes all GUIs |
| `shutdown()` | `void` | Shuts down manager |

#### Important Notes

- ⚠️ **Singleton** - automatically registered in `InstanceManager`
- Generates unique UUID on init - changes on plugin reload
- Validates `GUIHolder` manager UUID to detect stale GUIs
- Thread-safe tracking with `ConcurrentHashMap`

#### Example

```java
// In your plugin
GUIManager manager = new GUIManager(this);

// Open GUI
GUI gui = GUI.builder(3).title("Test").build();
manager.openGUI(player, gui);

// Check if has GUI
if (manager.hasGUIOpen(player)) {
    manager.closeGUI(player);
}

// Shutdown on disable
manager.shutdown();
```

---

### `ClickContext` (Final Class)

**Package**: `dev.khanh.plugin.kplugin.gui.context`

**Purpose**: Context object for GUI click events with comprehensive click information and utilities.

#### Public Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `player()` | `Player` | Gets player |
| `gui()` | `GUI` | Gets GUI instance |
| `slot()` | `int` | Gets slot index |
| `item()` | `ItemStack` | Gets clicked item |
| `itemOptional()` | `Optional<ItemStack>` | As Optional |
| `clickType()` | `ClickType` | Gets click type |
| `event()` | `InventoryClickEvent` | Bukkit event |
| `isLeftClick()` | `boolean` | Left click check |
| `isRightClick()` | `boolean` | Right click check |
| `isShiftClick()` | `boolean` | Shift click check |
| `isMiddleClick()` | `boolean` | Middle click check |
| `isDropClick()` | `boolean` | Drop click check |
| `isNumberKeyClick()` | `boolean` | Number key check |
| `getNumberKey()` | `int` | Gets key (0-8 or -1) |
| `cancel()` | `void` | Cancels event |
| `isCancelled()` | `boolean` | Check cancelled |
| `playSound(Sound)` | `void` | Plays sound |
| `playSound(Sound, float, float)` | `void` | With volume/pitch |
| `closeGUI()` | `void` | Closes GUI |
| `getMeta(String)` | `T` | Gets metadata |
| `getMeta(String, T)` | `T` | With default |
| `setMeta(String, Object)` | `void` | Sets metadata |
| `hasMeta(String)` | `boolean` | Check exists |
| `getAllMeta()` | `Map<String, Object>` | Gets all |

#### Example

```java
gui.slot(10).set(someItem)
    .onClick(ctx -> {
        if (ctx.isLeftClick()) {
            ctx.player().sendMessage("Left clicked!");
        } else if (ctx.isRightClick()) {
            ctx.player().sendMessage("Right clicked!");
        }
        
        if (ctx.isShiftClick()) {
            ctx.player().sendMessage("With shift!");
        }
        
        ctx.playSound(Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        ctx.cancel();
    });
```

---

### Slot Handles

#### `SlotHandle` (Final Class)

**Purpose**: Fluent API for configuring a single GUI slot.

**Key Methods**: `set()`, `clear()`, `onClick()`, `disable()`, `enable()`, `setMeta()`, `getMeta()`, `update()`, `transform()`

#### `SlotRangeHandle` (Final Class)

**Purpose**: Bulk operations for contiguous slot ranges.

**Key Methods**: `fill()`, `fillEmpty()`, `fillAlternating()`, `clear()`, `forEach()`, `onClick()`, `toArray()`, `filter()`, `edges()`

#### `MultiSlotHandle` (Final Class)

**Purpose**: Operations for non-contiguous slot sets.

**Key Methods**: `fill()`, `fillEmpty()`, `fillAlternating()`, `clear()`, `forEach()`, `onClick()`, `filter()`, `combine()`

#### Example

```java
// Single slot
gui.slot(13).set(item).onClick(ctx -> {...});

// Range
gui.slotRange(10, 16).fill(glassPane);

// Multiple
gui.slots(10, 12, 14, 16).fill(diamond);

// Row
gui.row(0).fill(borderItem);

// Column
gui.column(0).fill(borderItem);

// Alternating
gui.slotRange(0, 8).fillAlternating(blackGlass, whiteGlass);
```

---

### `Pagination<T>` (Final Generic Class)

**Package**: `dev.khanh.plugin.kplugin.gui.pagination`

**Purpose**: Pagination system for displaying large datasets with sync/async loading support.

#### Static Factory Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create()` | `GUI, int[]` | `Builder<T>` | Creates builder |
| `createForItems()` | `GUI, int[]` | `Builder<ItemStack>` | For ItemStacks |

#### Pagination Control

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getCurrentPage()` | `int` | Current page (0-indexed) |
| `getTotalPages()` | `int` | Total pages |
| `hasPreviousPage()` | `boolean` | Check previous |
| `hasNextPage()` | `boolean` | Check next |
| `previousPage(Player)` | `void` | Go to previous |
| `nextPage(Player)` | `void` | Go to next |
| `goToPage(int, Player)` | `void` | Jump to page |
| `render(Player)` | `void` | Renders page |

#### Builder Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `items()` | `List<T>` | Sets items (sync) |
| `asyncLoader()` | `Function` | Async loader |
| `totalPages()` | `Supplier<Integer>` or `int` | Total pages |
| `itemRenderer()` | `Function<T, ItemStack>` | Item renderer |
| `onItemClick()` | `BiConsumer<T, ClickContext>` | Click handler |
| `previousButton()` | `int, ItemStack` | Previous button |
| `previousButton()` | `int, ItemStack, ItemStack` | With disabled |
| `nextButton()` | `int, ItemStack` | Next button |
| `nextButton()` | `int, ItemStack, ItemStack` | With disabled |
| `pageInfoButton()` | `int, Function` | Page info |
| `onPageChange()` | `BiConsumer<Integer, Player>` | Page change |
| `build()` | - | Builds pagination |

#### Nested Classes

**`PageRequest`**: Contains `page`, `itemsPerPage`, `offset` for async loading  
**`PageInfo`**: Contains `currentPage`, `totalPages`, `itemsPerPage` for display

#### Important Notes

- ⚠️ **Lazy Loading Requirement**: When using `asyncLoader()`, you **MUST** call `totalPages()` to set the total page count
- Calling `getTotalPages()` without setting it will throw `IllegalStateException`
- For eager loading with `items()`, total pages is calculated automatically
- For async loading, you need to provide total pages from your data source (e.g., database count)

#### Example (Eager)

```java
List<Material> items = Arrays.asList(Material.DIAMOND, Material.EMERALD, /*...*/);

int[] contentSlots = IntStream.rangeClosed(10, 34)
    .filter(i -> i % 9 != 0 && i % 9 != 8)
    .toArray();

Pagination<Material> pagination = Pagination.<Material>create(gui, contentSlots)
    .items(items)
    .itemRenderer(mat -> ItemBuilder.of(mat).name("&e" + mat.name()).build())
    .onItemClick((mat, ctx) -> ctx.player().sendMessage("Clicked: " + mat.name()))
    .previousButton(48, prevItem, disabledPrevItem)
    .nextButton(50, nextItem, disabledNextItem)
    .pageInfoButton(49, info -> ItemBuilder.of(Material.PAPER)
        .name("&6Page " + info.currentPage + "/" + info.totalPages)
        .build())
    .onPageChange((page, p) -> p.sendMessage("Page: " + (page + 1)))
    .build();

pagination.render(player);
gui.open(player);
```

#### Example (Lazy/Async)

```java
Pagination<Material> pagination = Pagination.<Material>create(gui, contentSlots)
    .asyncLoader(request -> CompletableFuture.supplyAsync(() -> {
        // Simulate database query
        Thread.sleep(500);
        List<Material> pageData = fetchFromDatabase(request.offset, request.itemsPerPage);
        return pageData;
    }))
    .totalPages(10) // ⚠️ REQUIRED for async loader! Calculate from total item count
    .itemRenderer(mat -> ItemBuilder.of(mat).name("&b" + mat.name()).build())
    .previousButton(48, prevItem)
    .nextButton(50, nextItem)
    .pageInfoButton(49, info -> ItemBuilder.of(Material.PAPER)
        .name("&6Page " + info.currentPage + "/" + info.totalPages)
        .build())
    .build();

pagination.render(player);

// Example with dynamic total pages from database
int totalItems = database.count();
int itemsPerPage = contentSlots.length;
int calculatedTotalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

Pagination<Material> paginationFromDB = Pagination.<Material>create(gui, contentSlots)
    .asyncLoader(request -> loadFromDatabase(request))
    .totalPages(calculatedTotalPages)  // Calculated from database count
    .build();
```

---

### `FrameAnimation` (Final Class)

**Package**: `dev.khanh.plugin.kplugin.gui.animation`

**Purpose**: Diff-based frame animation for GUIs with optimized updates.

#### Static Factory Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `create()` | `GUI` | Creates animation |
| `alternating()` | `GUI, int[], ItemStack, ItemStack` | Two-item alternating |
| `wave()` | `GUI, int[], ItemStack` | Wave animation |
| `pulse()` | `GUI, int, ItemStack, ItemStack` | Pulse for slot |

#### Configuration

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `interval()` | `long` | `FrameAnimation` | Frame interval (ticks) |
| `loop()` | `boolean` | `FrameAnimation` | Sets looping |
| `onComplete()` | `Runnable` | `FrameAnimation` | Completion callback |

#### Frame Management

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `addFrame()` | `Map<Integer, ItemStack>` | `FrameAnimation` | Adds frame |
| `addFrame()` | `Consumer<Map>` | `FrameAnimation` | Via consumer |
| `addFrames()` | `List<Map>` | `FrameAnimation` | Multiple frames |
| `getFrameCount()` | - | `int` | Total frames |
| `clearFrames()` | - | `FrameAnimation` | Clears all |

#### Playback Control

| Method | Return Type | Description |
|--------|-------------|-------------|
| `play()` | `void` | Starts/resumes |
| `stop()` | `void` | Stops animation |
| `pause()` | `void` | Pauses animation |
| `isPlaying()` | `boolean` | Check playing |
| `isPaused()` | `boolean` | Check paused |
| `getCurrentFrame()` | `int` | Current frame index |
| `jumpToFrame(int)` | `void` | Jump to frame |

#### Important Notes

- ⚠️ **Diff-based updates** - Only updates slots that changed between frames (performance)
- Uses `TaskUtil` for scheduling
- Maintains current visible state for diffing

#### Example

```java
FrameAnimation wave = FrameAnimation.create(gui)
    .interval(5) // 5 ticks = 0.25 seconds
    .loop(true);

// Add frames
for (int pos = 0; pos < 9; pos++) {
    final int position = pos;
    wave.addFrame(frame -> {
        frame.put(10 + position, ItemBuilder.of(Material.CYAN_STAINED_GLASS_PANE)
            .name("&b●")
            .build());
    });
}

wave.play();

// Later...
wave.pause();
wave.stop();
```

---

## Instance Management

### `InstanceManager` (Class)

**Package**: `dev.khanh.plugin.kplugin.instance`

**Purpose**: Thread-safe singleton registry for managing plugin component instances.

#### Public Static Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `clearAll()` | - | `void` | Clears all instances |
| `registerInstance()` | `Class<T>, T` | `void` | Registers instance |
| `getInstance()` | `Class<T>` | `T` | Gets instance (nullable) |
| `getInstanceOrElseThrow()` | `Class<T>` | `T` | Gets or throws |
| `removeInstance()` | `Class<T>` | `void` | Removes instance |

#### Important Notes

- Uses `ConcurrentHashMap` for thread safety
- ⚠️ Does NOT enforce singleton construction - only storage pattern
- Cleared by `KPlugin` automatically on disable
- Used by framework for `KPlugin`, `GUIManager`, etc.

#### Example

```java
// Register
MyService service = new MyService();
InstanceManager.registerInstance(MyService.class, service);

// Retrieve
MyService retrieved = InstanceManager.getInstance(MyService.class);
if (retrieved != null) {
    retrieved.doSomething();
}

// Or throw if not found
MyService service = InstanceManager.getInstanceOrElseThrow(MyService.class);

// Remove
InstanceManager.removeInstance(MyService.class);
```

---

## Item System

### `ItemBuilder` (Final Class)

**Package**: `dev.khanh.plugin.kplugin.item`

**Purpose**: Modern fluent builder for creating ItemStacks with comprehensive features.

#### Static Factory Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `of()` | `Material` | `ItemBuilder` | Creates builder |
| `of()` | `ItemStack` | `ItemBuilder` | Clones item |
| `fromConfig()` | `ConfigurationSection` | `ItemBuilder` | From config |

#### Builder Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `material()` | `Material` | `ItemBuilder` | Sets material |
| `amount()` | `int` | `ItemBuilder` | Sets amount (1-64) |
| `name()` | `String` | `ItemBuilder` | Display name (colorizes `&`) |
| `lore()` | `String...` | `ItemBuilder` | Sets lore (replaces) |
| `lore()` | `List<String>` | `ItemBuilder` | Sets from list |
| `addLore()` | `String...` | `ItemBuilder` | Adds lines |
| `insertLore()` | `int, String` | `ItemBuilder` | Inserts at index |
| `clearLore()` | - | `ItemBuilder` | Clears lore |
| `enchant()` | `Enchantment, int` | `ItemBuilder` | Adds enchantment |
| `removeEnchant()` | `Enchantment` | `ItemBuilder` | Removes |
| `clearEnchants()` | - | `ItemBuilder` | Clears all |
| `flags()` | `ItemFlag...` | `ItemBuilder` | Adds flags |
| `allFlags()` | - | `ItemBuilder` | Hides all |
| `removeFlags()` | `ItemFlag...` | `ItemBuilder` | Removes flags |
| `customModelData()` | `int` | `ItemBuilder` | Sets CMD |
| `unbreakable()` | `boolean` | `ItemBuilder` | Sets unbreakable |
| `unbreakable()` | - | `ItemBuilder` | Makes unbreakable |
| `glow()` | - | `ItemBuilder` | Glow effect |
| `skull()` | `String` | `ItemBuilder` | Sets skull |
| `placeholder()` | `Function` | `ItemBuilder` | Placeholder function |
| `apply()` | `Consumer` | `ItemBuilder` | Applies consumer |
| `build()` | - | `ItemStack` | Builds final item |

#### Skull Support

The `skull()` method accepts:
- **Player names**: `"Notch"` (2-16 alphanumeric)
- **Player UUIDs**: `"069a79f4-44e9-4726-a5be-fca90e38aaf5"`
- **Texture URLs**: `"http://textures.minecraft.net/texture/..."`
- **Base64 texture data**: Any other string

#### Important Notes

- ⚠️ **Replaces ItemStackWrapper** (deprecated)
- Auto-colorizes `&` codes in name/lore
- Supports both Paper's PlayerProfile API and legacy methods for skulls
- Config loading supports: `material`, `skull`, `name`, `display-name`, `lore`, `amount`, `enchantments`, `flags`, `custom-model-data`, `glow`, `unbreakable`

#### Example

```java
ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("&b&lLegendary Sword")
    .lore(
        "&7Damage: &c+15",
        "&7Speed: &a+3",
        "",
        "&6Legendary Tier"
    )
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .unbreakable()
    .flags(ItemFlag.HIDE_UNBREAKABLE)
    .glow()
    .build();

// From config
ItemStack item = ItemBuilder.fromConfig(config.getConfigurationSection("items.diamond"))
    .placeholder(str -> str.replace("{player}", player.getName()))
    .build();
```

#### Config Format

```yaml
items:
  diamond:
    material: DIAMOND
    name: "&b{player}'s Diamond"
    lore:
      - "&7A precious gem"
      - "&7Owner: &f{player}"
    amount: 1
    enchantments:
      DURABILITY: 3
    flags:
      - HIDE_ENCHANTS
    glow: true
    unbreakable: true
```

---

### `ItemTemplate` (Final Class)

**Package**: `dev.khanh.plugin.kplugin.item`

**Purpose**: Template for creating items from configuration with placeholder support at build time.

#### Static Factory Methods

| Method | Parameters | Return Type |
|--------|------------|-------------|
| `fromConfig()` | `ConfigurationSection` | `ItemTemplate` |
| `builder()` | - | `Builder` |

#### Build Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `build()` | - | `ItemStack` | No placeholders |
| `build()` | `Map<String, String>` | `ItemStack` | Map replacement |
| `build()` | `Placeholders` | `ItemStack` | Placeholders system |
| `build()` | `Player, Placeholders` | `ItemStack` | With player |
| `build()` | `Function<String, String>` | `ItemStack` | Custom function |
| `toBuilder()` | - | `ItemBuilder` | Convert to builder |

#### Slot Configuration Parsing

Supports multiple formats in config:
- Single integer: `slot: 13`
- Range notation: `slots: "10-16"`
- Comma-separated: `slots: "10, 12, 14"`
- List format: `slots: [10, 12, 14]`

#### Important Notes

- Stores raw strings, applies placeholders at build time
- Includes slot configuration for GUI placement
- Config supports same keys as `ItemBuilder` plus `slot`/`slots` keys

#### Example

```yaml
items:
  welcome-item:
    material: DIAMOND
    name: "&aWelcome, {player}!"
    lore:
      - "&7Your rank: {rank}"
      - "&7Balance: ${balance}"
    slot: 13
```

```java
ItemTemplate template = ItemTemplate.fromConfig(config.getConfigurationSection("items.welcome-item"));

// Build with placeholders
Placeholders ph = Placeholders.create()
    .set("{player}", player.getName())
    .set("{rank}", "VIP")
    .set("{balance}", "1000");

ItemStack item = template.build(ph);

// Get slot
int[] slots = template.getSlots(); // [13]
```

---

## Placeholder System

### `Placeholders` (Final Class)

**Package**: `dev.khanh.plugin.kplugin.placeholder`

**Purpose**: Flexible placeholder system using direct string replacement (no forced format).

#### Static Factory Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `create()` | `Placeholders` | Empty instance |
| `of()` | `Placeholders` | From map |

#### Configuration Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `set()` | `String, Object` | `Placeholders` | Static placeholder |
| `set()` | `String, String` | `Placeholders` | String value |
| `setAll()` | `Map` | `Placeholders` | Multiple |
| `resolver()` | `String, Supplier` | `Placeholders` | Dynamic |
| `resolver()` | `String, Function<Player, String>` | `Placeholders` | Player-aware |
| `addPlayerPlaceholders()` | `Player` | `Placeholders` | Common player |
| `addPlayerPlaceholders()` | `Player, String` | `Placeholders` | Custom format |
| `remove()` | `String` | `Placeholders` | Removes |
| `clear()` | - | `Placeholders` | Clears all |

#### Application Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `apply()` | `String` | `String` | Applies |
| `apply()` | `String, Player` | `String` | With player |
| `toFunction()` | - | `Function` | As function |
| `toFunction()` | `Player` | `Function` | With player |

#### Utility Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `copy()` | `Placeholders` | Creates copy |
| `merge()` | `Placeholders` | Merges another |
| `getPlaceholders()` | `Map` | Gets map |
| `getResolvers()` | `Map` | Gets resolvers |
| `getPlayerResolvers()` | `Map` | Gets player resolvers |
| `isEmpty()` | `boolean` | Check empty |

#### Important Notes

- ⚠️ **Free-style format** - YOU define the exact string (e.g., `{player}`, `%player%`, `$var`, `PLAYER`)
- Thread-safe with `ConcurrentHashMap`
- Works with PlaceholderAPI - chain functions
- Common player placeholders: `{player}`, `{player_name}`, `{player_uuid}`, `{player_displayname}`, `{player_health}`, `{player_level}`, `{player_world}`

#### Example

```java
// Static placeholders
Placeholders ph = Placeholders.create()
    .set("{player}", player.getName())
    .set("{rank}", "VIP")
    .set("{balance}", 1000);

String message = ph.apply("Welcome, {player}! Rank: {rank}, Balance: ${balance}");

// Dynamic resolvers
Placeholders dynamic = Placeholders.create()
    .resolver("{time}", () -> String.valueOf(System.currentTimeMillis()))
    .resolver("{online}", p -> String.valueOf(Bukkit.getOnlinePlayers().size()));

// Player placeholders
Placeholders playerPh = Placeholders.create()
    .addPlayerPlaceholders(player); // Adds {player}, {player_name}, etc.

String text = playerPh.apply("Hello, {player_name}! Health: {player_health}");

// Custom format
Placeholders custom = Placeholders.create()
    .addPlayerPlaceholders(player, "%player%"); // Uses %player%, %player_name%, etc.

// Merge
Placeholders merged = Placeholders.create()
    .set("{server}", "Lobby")
    .merge(playerPh);

// As function
Function<String, String> func = ph.toFunction();
String result = func.apply("Text with {player}");
```

---

## Utility Classes

### `ColorUtil` (Class)

**Package**: `dev.khanh.plugin.kplugin.util`

**Purpose**: Color code conversion utilities.

#### Public Static Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `colorize()` | `String` | `String` | `&` → legacy `§` |
| `modernColorize()` | `String` | `Component` | `&` → Component |

#### Example

```java
String colored = ColorUtil.colorize("&aGreen &bBlue"); // §aGreen §bBlue
Component component = ColorUtil.modernColorize("&aGreen &bBlue");
player.sendMessage(component);
```

---

### `LoggerUtil` (Class)

**Package**: `dev.khanh.plugin.kplugin.util`

**Purpose**: Static logging with debug mode support.

#### Public Static Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `setDebug()` | `boolean` | Enable/disable debug |
| `isDebug()` | - | Check debug mode |
| `info()` | `String` | Info log |
| `info()` | `String, Object...` | Formatted info |
| `warning()` | `String` | Warning log |
| `warning()` | `String, Object...` | Formatted warning |
| `severe()` | `String` | Severe log |
| `severe()` | `String, Object...` | Formatted severe |
| `console()` | `String` | Colorized console |
| `console()` | `String, Object...` | Formatted console |
| `debug()` | `String` | Debug log |
| `debug()` | `String, Object...` | Formatted debug |

#### Important Notes

- Uses `KPlugin.getInstance().getLogger()`
- Debug mode can be enabled via `-Dkplugin.debug=true` system property
- `console()` methods send to console with color support

#### Example

```java
LoggerUtil.info("Plugin enabled!");
LoggerUtil.warning("Config missing key: %s", key);
LoggerUtil.severe("Critical error!");
LoggerUtil.console("&aSuccess!");

// Debug mode
LoggerUtil.setDebug(true);
LoggerUtil.debug("Debug info: %s", data);
```

---

### `MessageUtil` (Class)

**Package**: `dev.khanh.plugin.kplugin.util`

**Purpose**: Static wrapper for `MessageFile` operations.

#### Public Static Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `initialize()` | `MessageFile` | Initialize with file |
| `getMessage()` | `String` | Gets raw message |
| `getMessage()` | `String, Function` | With function |
| `getColoredMessage()` | `String` | Colorized |
| `getColoredMessage()` | `String, Function` | Colorized + function |
| `getComponent()` | `String` | Adventure Component |
| `getComponent()` | `String, Function` | Component + function |
| `sendMessage()` | `Player, String` | Sends with prefix |
| `sendMessage()` | `Player, String, Function` | With function |
| `sendMessageIfNotEmpty()` | `Player, String` | Empty check |
| `sendMessage()` | `Player, String, boolean, Function` | Full-featured |
| `sendMessageWithPrefix()` | `Player, String` | Direct message |

#### Important Notes

- Automatically initialized by `MessageFile` constructor
- Throws `IllegalStateException` if used before initialization

#### Example

```java
// Initialize (done by MessageFile)
MessageFile messages = new MessageFile(plugin);

// Use static methods
MessageUtil.sendMessage(player, "welcome");
MessageUtil.sendMessageWithPrefix(player, "&aWelcome!");

String msg = MessageUtil.getColoredMessage("prefix");
```

---

### `SoundUtil` (Class)

**Package**: `dev.khanh.plugin.kplugin.util`

**Purpose**: Multi-version sound playback with legacy-to-modern mapping.

#### Public Static Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `playSound()` | `Player, String` | Play sound |
| `playSound()` | `Player, String, float, float` | With volume/pitch |
| `playSound()` | `Location, String, float, float` | At location |
| `playSoundUnsafe()` | `Player, String, float, float` | Without exception handling |
| `isValidSound()` | `String` | Validates name |

#### Predefined GUI Sounds

| Method | Description |
|--------|-------------|
| `playClickSound()` | `ui.button.click` |
| `playNavigateSound()` | Navigate sound |
| `playSuccessSound()` | Success sound |
| `playErrorSound()` | Error sound |
| `playCloseSound()` | Close sound |
| `playOpenSound()` | Open sound |

#### Important Notes

- Auto-converts legacy enum names (e.g., `LEVEL_UP`) to modern format (`entity.player.levelup`)
- Accepts both Sound enum names and namespaced keys
- Handles version differences between pre-1.9 and 1.9+

#### Example

```java
SoundUtil.playSound(player, "entity.player.levelup", 1.0f, 1.0f);
SoundUtil.playClickSound(player);
SoundUtil.playSuccessSound(player);

// At location
SoundUtil.playSound(location, "block.note_block.pling", 1.0f, 2.0f);
```

---

### `TaskUtil` (Class)

**Package**: `dev.khanh.plugin.kplugin.util`

**Purpose**: Unified task scheduling using FoliaLib for Spigot/Paper/Folia compatibility.

#### Global Region (Sync)

| Method | Parameters | Description |
|--------|------------|-------------|
| `runSync()` | `Runnable` | Next tick |
| `runSync()` | `Plugin, Runnable` | Next tick |
| `runSync()` | `Runnable, long` | Delayed (ticks) |
| `runSync()` | `Plugin, Runnable, long, TimeUnit` | Custom unit |
| `runSyncRepeating()` | `Runnable, long, long` | Repeating (ticks) |
| `runSyncRepeating()` | `Plugin, Runnable, long, long, TimeUnit` | Custom unit |

#### Async Thread

| Method | Parameters | Description |
|--------|------------|-------------|
| `runAsync()` | `Runnable` | Immediate |
| `runAsync()` | `Plugin, Runnable` | Immediate |
| `runAsync()` | `Runnable, long` | Delayed (ticks) |
| `runAsyncRepeating()` | `Runnable, long, long` | Repeating (ticks) |

#### Entity Region (Folia)

| Method | Parameters | Description |
|--------|------------|-------------|
| `runAtEntity()` | `Entity, Runnable` | Immediate |
| `runAtEntity()` | `Plugin, Entity, Runnable` | Immediate |
| `runAtEntity()` | `Entity, Runnable, long` | Delayed |
| `runAtEntityRepeating()` | `Entity, Runnable, long, long` | Repeating |
| Variants with `Runnable retired` | | For invalid entity |

#### Location Region (Folia)

| Method | Parameters | Description |
|--------|------------|-------------|
| `runAtLocation()` | `Location, Runnable` | Immediate |
| `runAtLocation()` | `Plugin, Location, Runnable` | Immediate |
| `runAtLocation()` | `Location, Runnable, long` | Delayed |
| `runAtLocationRepeating()` | `Location, Runnable, long, long` | Repeating |

#### Task Management

| Method | Return Type | Description |
|--------|-------------|-------------|
| `cancel()` | `void` | Cancel task |
| `cancelAll()` | `void` | Cancel all |
| `getTask()` | `WrappedTask` | Get task |
| `getAllTasks()` | `Collection` | All tasks |

#### Utility

| Method | Return Type | Description |
|--------|-------------|-------------|
| `isFolia()` | `boolean` | Check Folia |
| `isOwnedByCurrentRegion()` | `boolean` | Check region |
| Various region checks | `boolean` | Location/entity region |

#### Important Notes

- ⚠️ **Prefer tick-based methods** over TimeUnit for performance
- Returns `WrappedTask` for cancellation
- FoliaLib initialized by `KPlugin`
- All delays/periods in ticks by default (20 ticks = 1 second)

#### Example

```java
// Sync task
TaskUtil.runSync(() -> {
    player.sendMessage("Next tick!");
});

// Delayed
TaskUtil.runSync(() -> {
    player.sendMessage("5 seconds later!");
}, 100L); // 100 ticks = 5 seconds

// Repeating
WrappedTask task = TaskUtil.runSyncRepeating(() -> {
    player.sendMessage("Every second!");
}, 0L, 20L);

// Cancel later
TaskUtil.cancel(task);

// Async
TaskUtil.runAsync(() -> {
    // Database query
    List<Data> data = database.query();
    
    // Back to sync
    TaskUtil.runSync(() -> {
        processData(data);
    });
});

// Entity region (Folia)
TaskUtil.runAtEntity(player, () -> {
    player.sendMessage("Entity region!");
});
```

---

### `TeleportUtil` (Class)

**Package**: `dev.khanh.plugin.kplugin.util`

**Purpose**: Async entity teleportation using FoliaLib.

#### Public Static Methods

| Method | Parameters | Return Type |
|--------|------------|-------------|
| `teleport()` | `Entity, Location` | `CompletableFuture<Boolean>` |
| `teleport()` | `Entity, Location, TeleportCause` | `CompletableFuture<Boolean>` |

#### Platform Behavior

- **Folia**: Teleports in entity's region asynchronously
- **Paper**: Teleports asynchronously if supported
- **Spigot**: Falls back to next-tick teleport (NOT instant)

#### Important Notes

- ⚠️ Returns `CompletableFuture` - ALWAYS handle result
- On Spigot, NOT instant to avoid thread safety issues

#### Example

```java
TeleportUtil.teleport(player, targetLocation).thenAccept(result -> {
    if (result) {
        MessageUtil.sendMessageWithPrefix(player, "&aTeleported!");
    } else {
        MessageUtil.sendMessageWithPrefix(player, "&cTeleport failed!");
    }
});

// With cause
TeleportUtil.teleport(player, location, TeleportCause.PLUGIN).thenAccept(result -> {
    LoggerUtil.info("Teleport result: " + result);
});
```

---

## Important Warnings & Best Practices

### Critical Patterns

#### 1. KCommand Dual Overload Pattern

⚠️ **MUST implement BOTH overloads**:

```java
@Override
public void onCommand(CommandSender sender, List<String> args) {
    // Handle all senders
}

@Override
public void onCommand(Player player, List<String> args) {
    // Handle player-specific logic
}
```

⚠️ **Must call `registerCommand()` manually**:

```java
MyCommand cmd = new MyCommand();
cmd.registerCommand(plugin); // NOT automatic!
```

---

#### 2. Config File Version Keys

⚠️ **Different default keys**:

- `AbstractConfigFile` (config.yml): Uses `"config-version"`
- `GenericYamlFile`: Uses `"version"` by default
- `MessageFile`: No version tracking

```yaml
# config.yml
config-version: 2

# database.yml (GenericYamlFile)
version: 1
```

---

#### 3. GUI Security Model

⚠️ **Uses GUIHolder with manager UUID** (NOT PDC tags):

```java
// CORRECT - Framework handles this
GUI gui = GUI.builder(3).title("Test").build();

// DO NOT manually create GUIHolder
// DO NOT add PDC tags to GUI items
```

The framework automatically:
- Creates `GUIHolder` with manager UUID
- Validates on every click
- Invalidates stale GUIs on reload

---

#### 4. ItemBuilder vs ItemStackWrapper

⚠️ **ItemStackWrapper is DEPRECATED**:

```java
// OLD (deprecated)
new ItemStackWrapper(Material.DIAMOND).setDisplayName("...").build()

// NEW (preferred)
ItemBuilder.of(Material.DIAMOND).name("...").build()
```

---

#### 5. TaskUtil - Prefer Ticks

⚠️ **Use tick-based methods for better performance**:

```java
// PREFERRED
TaskUtil.runSync(() -> {...}, 100L); // 100 ticks

// AVOID (slower)
TaskUtil.runSync(plugin, () -> {...}, 5L, TimeUnit.SECONDS);
```

---

#### 6. Placeholders - Free Format

⚠️ **YOU define the replacement format**:

```java
// Any format you want
Placeholders.create()
    .set("{player}", name)      // Curly braces
    .set("%player%", name)      // Percent signs
    .set("$player", name)       // Dollar sign
    .set("PLAYER_NAME", name);  // Uppercase

// NO forced format like %s or {0}
```

---

#### 7. Java 8 Compatibility

⚠️ **Don't use Java 9+ features**:

```java
// AVOID
var player = event.getPlayer();       // Java 10+
record Data(String name) {}           // Java 14+
player.sendMessage("Text " + value);  // Use String.format instead

// PREFER
Player player = event.getPlayer();
String msg = String.format("Text %s", value);
```

---

### Common Gotchas

#### 1. InstanceManager Usage

```java
// InstanceManager does NOT enforce singleton construction
// You must handle singleton pattern yourself

public class MyService {
    private static MyService instance;
    
    private MyService() {} // Private constructor
    
    public static MyService getInstance() {
        if (instance == null) {
            instance = new MyService();
            InstanceManager.registerInstance(MyService.class, instance);
        }
        return instance;
    }
}
```

---

#### 2. FoliaLib Initialization

⚠️ **FoliaLib is initialized in `KPlugin.onEnable()`**:

```java
// WRONG - FoliaLib not ready yet
public class MyPlugin extends KPlugin {
    public MyPlugin() {
        TaskUtil.runSync(() -> {}); // WILL FAIL!
    }
    
    // CORRECT
    @Override
    protected void enable() {
        TaskUtil.runSync(() -> {}); // Works!
    }
}
```

---

#### 3. Color Code Format

⚠️ **Always use `&` codes** (converted automatically):

```java
// CORRECT
gui.slot(0).set(ItemBuilder.of(Material.DIAMOND)
    .name("&bBlue Diamond")
    .lore("&7Description")
    .build());

// WRONG - use & not §
.name("§bBlue Diamond") // Will work but not idiomatic
```

---

#### 4. Pagination Page Numbers

⚠️ **PageInfo.currentPage is 1-based for display**:

```java
.pageInfoButton(49, info -> ItemBuilder.of(Material.PAPER)
    // CORRECT - currentPage is already 1-based
    .name("&6Page " + info.currentPage + "/" + info.totalPages)
    
    // WRONG - double increment
    .name("&6Page " + (info.currentPage + 1) + "/" + info.totalPages)
    .build())
```

---

#### 5. MessageFile Initialization

⚠️ **MessageFile auto-initializes MessageUtil**:

```java
// CORRECT order
MessageFile messages = new MessageFile(plugin);
MessageUtil.sendMessage(player, "welcome");

// WRONG - MessageUtil not initialized
MessageUtil.sendMessage(player, "welcome"); // IllegalStateException!
MessageFile messages = new MessageFile(plugin);
```

---

### Performance Tips

1. **Use tick-based scheduling** instead of TimeUnit conversions
2. **Batch GUI updates** with `refresh()` instead of per-slot updates
3. **Use FrameAnimation** for complex animations (diff-based)
4. **Prefer `SlotRangeHandle`** for bulk operations
5. **Use async loading** for large datasets in Pagination
6. **Cache ItemBuilder results** if creating same item multiple times

---

### Thread Safety

- **InstanceManager**: Thread-safe (ConcurrentHashMap)
- **Placeholders**: Thread-safe (ConcurrentHashMap)
- **TaskUtil**: Always routes to correct thread/region
- **GUI operations**: Must be on main thread (use TaskUtil.runSync)
- **Config files**: Thread-safe for reading, synchronized for writing

---

## Quick Reference Tables

### Common Workflows

| Task | Code |
|------|------|
| Create GUI | `GUI.builder(rows).title("...").build()` |
| Create Item | `ItemBuilder.of(Material.X).name("...").build()` |
| Register Command | `new MyCmd().registerCommand(plugin)` |
| Schedule Task | `TaskUtil.runSync(() -> {}, delay)` |
| Pagination | `Pagination.create(gui, slots).items(list).build()` |
| Placeholders | `Placeholders.create().set("{key}", val)` |
| Play Sound | `SoundUtil.playClickSound(player)` |
| Send Message | `MessageUtil.sendMessage(player, "key")` |

---

### Color Codes

| Code | Color | Code | Format |
|------|-------|------|--------|
| `&0` | Black | `&l` | Bold |
| `&1` | Dark Blue | `&m` | Strikethrough |
| `&2` | Dark Green | `&n` | Underline |
| `&3` | Dark Aqua | `&o` | Italic |
| `&4` | Dark Red | `&r` | Reset |
| `&5` | Dark Purple | | |
| `&6` | Gold | | |
| `&7` | Gray | | |
| `&8` | Dark Gray | | |
| `&9` | Blue | | |
| `&a` | Green | | |
| `&b` | Aqua | | |
| `&c` | Red | | |
| `&d` | Light Purple | | |
| `&e` | Yellow | | |
| `&f` | White | | |

---

## Version History

- **v4.0**: Current version - Added lazy pagination, fixed GUI events, modern API
- **v3.x**: GUI system with FrameAnimation
- **v2.x**: Command system and config files
- **v1.x**: Initial release

---

## Support & Resources

- **Javadoc**: `docs/index.html` (generated by `mvn javadoc:javadoc`)
- **Test Suite**: `dev.khanh.plugin.kplugin.gui.test.TestGUI`
- **Example Usage**: See test methods in TestGUI class
- **Build**: `mvn clean install`

---

**End of API Reference**
