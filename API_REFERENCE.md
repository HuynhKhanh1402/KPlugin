# KPlugin API Reference

Complete API reference for all packages, classes, methods, and fields in the KPlugin framework.

**Root package:** `dev.khanh.plugin.kplugin`

---

## Table of Contents

- [dev.khanh.plugin.kplugin](#devkhanhpluginkplugin)
- [dev.khanh.plugin.kplugin.command](#command)
- [dev.khanh.plugin.kplugin.file](#file)
- [dev.khanh.plugin.kplugin.gui](#gui)
- [dev.khanh.plugin.kplugin.gui.context](#guicontext)
- [dev.khanh.plugin.kplugin.gui.holder](#guiholder)
- [dev.khanh.plugin.kplugin.gui.slot](#guislot)
- [dev.khanh.plugin.kplugin.gui.animation](#guianimation)
- [dev.khanh.plugin.kplugin.gui.pagination](#guipagination)
- [dev.khanh.plugin.kplugin.instance](#instance)
- [dev.khanh.plugin.kplugin.item](#item)
- [dev.khanh.plugin.kplugin.placeholder](#placeholder)
- [dev.khanh.plugin.kplugin.task](#task)
- [dev.khanh.plugin.kplugin.util](#util)

---

## dev.khanh.plugin.kplugin

### `abstract class KPlugin extends JavaPlugin`

Abstract base class for Spigot plugins. Handles lifecycle and singleton enforcement.

#### Fields

| Modifier | Type | Name | Description |
|---|---|---|---|
| `private static` | `KPlugin` | `kInstance` | Current active plugin instance |
| `private static` | `FoliaLib` | `foliaLib` | FoliaLib instance for scheduler operations |

#### Constructors

| Modifier | Signature |
|---|---|
| `protected` | `KPlugin()` |

#### Methods

| Modifier | Return | Method | Description |
|---|---|---|---|
| `public` | `void` | `onEnable()` | Lifecycle callback. Sets singleton, creates FoliaLib, registers in InstanceManager, calls `enable()` |
| `public` | `void` | `onDisable()` | Lifecycle callback. Clears InstanceManager, calls `disable()` |
| `public abstract` | `void` | `enable()` | Override to define plugin enable behavior |
| `public abstract` | `void` | `disable()` | Override to define plugin disable behavior |
| `public static` | `KPlugin` | `getKInstance()` | Returns the active plugin instance (nullable) |
| `public static` | `FoliaLib` | `getFoliaLib()` | Returns the FoliaLib instance |
| `private` | `void` | `printAuthorInfo()` | Prints author branding to console |

---

## command

### `abstract class KCommand implements CommandExecutor, TabCompleter`

Abstract command class with subcommand routing, permission checking, and error handling.

#### Fields

| Modifier | Type | Name | Description |
|---|---|---|---|
| `@Getter @Nullable private final` | `KCommand` | `parent` | Parent command (null for root) |
| `protected final` | `List<KCommand>` | `subCommands` | Registered subcommands |
| `@Getter @NotNull protected final` | `String` | `name` | Primary command name |
| `@Getter @NotNull protected final` | `List<String>` | `alias` | Command aliases |
| `@Getter @NotNull protected final` | `String` | `permission` | Required permission (empty = none) |
| `@Getter @NotNull protected final` | `String` | `description` | Command description |
| `@Getter @NotNull protected final` | `String` | `usage` | Usage syntax string |
| `@Getter protected static` | `CommandMap` | `commandMap` | Bukkit CommandMap (initialized via reflection) |

#### Constructors

| Signature | Description |
|---|---|
| `KCommand(String name, KCommand parent, List<String> alias, String permission, String description, String usage)` | Full constructor |
| `KCommand(String name, List<String> alias, String permission, String description, String usage)` | Without parent |
| `KCommand(String name, String permission, String description, String usage)` | Without parent/alias |
| `KCommand(String name, KCommand parent, String permission, String description, String usage)` | Without alias |
| `KCommand(String name, KCommand parent, String permission)` | Minimal with parent |
| `KCommand(String name, KCommand parent, String description, String usage)` | Parent + desc/usage |
| `KCommand(String name, KCommand parent)` | Name + parent only |

#### Methods

| Modifier | Return | Method | Description |
|---|---|---|---|
| `public final` | `KCommand` | `addSubCommand(KCommand subCommand)` | Registers a subcommand. Throws on duplicates |
| `public final` | `void` | `registerCommand(JavaPlugin plugin)` | Dynamically registers the command (no plugin.yml needed) |
| `public abstract` | `void` | `onCommand(CommandSender sender, List<String> args)` | Execute for any sender |
| `public abstract` | `void` | `onCommand(Player player, List<String> args)` | Execute for Player sender |
| `@Nullable public` | `List<String>` | `onTabComplete(CommandSender sender, List<String> args)` | Tab-complete for any sender. Default: subcommand names |
| `@Nullable public` | `List<String>` | `onTabComplete(Player player, List<String> args)` | Tab-complete for Player. Default: delegates to sender version |
| `public abstract @NotNull` | `String` | `getNoPermissionMessage()` | Message shown when permission denied |
| `protected static` | `String` | `replacePlaceholders(String message, Map<String, String> replaceMap)` | Simple placeholder replacement |
| `protected` | `boolean` | `hasPermission(CommandSender sender)` | Checks sender has required permission |
| `protected` | `boolean` | `hasPermission(Player player)` | Checks player has required permission |
| `public` | `KCommand` | `getSubCommand(String name, boolean checkAlias)` | Finds subcommand by name/alias |
| `public final` | `boolean` | `onCommand(CommandSender, Command, String, String[])` | Bukkit interface (auto-routes) |
| `public final` | `List<String>` | `onTabComplete(CommandSender, Command, String, String[])` | Bukkit interface (auto-routes) |

---

## file

### `abstract class GenericYamlFile`

Generic YAML file handler with version-based auto-migration.

#### Fields

| Modifier | Type | Name | Description |
|---|---|---|---|
| `private final` | `KPlugin` | `plugin` | Plugin instance |
| `protected final` | `File` | `file` | File on disk |
| `private final` | `String` | `configVersionKey` | YAML key for version tracking |
| `protected` | `YamlConfiguration` | `yaml` | Current loaded YAML |
| `private final @Nullable` | `YamlConfiguration` | `defaultYaml` | Default YAML from JAR resource |

#### Constructors

| Signature | Description |
|---|---|
| `GenericYamlFile(KPlugin plugin, File file, @Nullable String resourcePath)` | Default version key `"version"` |
| `GenericYamlFile(KPlugin plugin, File file, @Nullable String resourcePath, String configVersionKey)` | Custom version key |

#### Methods

| Modifier | Return | Method | Description |
|---|---|---|---|
| `public` | `void` | `save()` | Save to disk. Throws `IOException` |
| `public` | `void` | `reload()` | Reload from disk |
| `protected abstract` | `void` | `update(int oldVersion, int newVersion)` | Called on version upgrade |
| `public` | `File` | `getFile()` | Returns the file |
| `public` | `YamlConfiguration` | `getYaml()` | Returns YAML config |
| `public @Nullable` | `YamlConfiguration` | `getDefaultYaml()` | Returns default YAML |
| `public` | `Set<String>` | `getKeys(boolean deep)` | Delegated to YAML |
| `public` | `Map<String, Object>` | `getValues(boolean deep)` | Delegated to YAML |
| `public` | `boolean` | `contains(String path)` | Delegated to YAML |
| `public` | `boolean` | `contains(String path, boolean ignoreDefault)` | Delegated to YAML |
| `public` | `boolean` | `isSet(String path)` | Delegated to YAML |
| `public @Nullable` | `Object` | `get(String path)` | Delegated to YAML |
| `public @Nullable` | `Object` | `get(String path, Object def)` | Delegated to YAML |
| `public` | `void` | `set(String path, Object value)` | Delegated to YAML |
| `public` | `ConfigurationSection` | `createSection(String path)` | Delegated to YAML |
| `public` | `ConfigurationSection` | `createSection(String path, Map<?, ?> map)` | Delegated to YAML |
| `public @Nullable` | `String` | `getString(String path)` | Delegated to YAML |
| `public @Nullable` | `String` | `getString(String path, String def)` | Delegated to YAML |
| `public` | `boolean` | `isString(String path)` | Delegated to YAML |
| `public` | `int` | `getInt(String path)` | Delegated to YAML |
| `public` | `int` | `getInt(String path, int def)` | Delegated to YAML |
| `public` | `boolean` | `isInt(String path)` | Delegated to YAML |
| `public` | `boolean` | `getBoolean(String path)` | Delegated to YAML |
| `public` | `boolean` | `getBoolean(String path, boolean def)` | Delegated to YAML |
| `public` | `boolean` | `isBoolean(String path)` | Delegated to YAML |
| `public` | `double` | `getDouble(String path)` | Delegated to YAML |
| `public` | `double` | `getDouble(String path, double def)` | Delegated to YAML |
| `public` | `boolean` | `isDouble(String path)` | Delegated to YAML |
| `public` | `long` | `getLong(String path)` | Delegated to YAML |
| `public` | `long` | `getLong(String path, long def)` | Delegated to YAML |
| `public` | `boolean` | `isLong(String path)` | Delegated to YAML |
| `public @Nullable` | `List<?>` | `getList(String path)` | Delegated to YAML |
| `public @Nullable` | `List<?>` | `getList(String path, List<?> def)` | Delegated to YAML |
| `public` | `boolean` | `isList(String path)` | Delegated to YAML |
| `public @NotNull` | `List<String>` | `getStringList(String path)` | Delegated to YAML |
| `public @Nullable` | `<T> T` | `getObject(String path, Class<T> clazz)` | Delegated to YAML |
| `public @Nullable` | `<T> T` | `getObject(String path, Class<T> clazz, T def)` | Delegated to YAML |
| `public @Nullable` | `ItemStack` | `getItemStack(String path)` | Delegated to YAML |
| `public @Nullable` | `ItemStack` | `getItemStack(String path, ItemStack def)` | Delegated to YAML |
| `public` | `boolean` | `isItemStack(String path)` | Delegated to YAML |
| `public @Nullable` | `Location` | `getLocation(String path)` | Delegated to YAML |
| `public @Nullable` | `Location` | `getLocation(String path, Location def)` | Delegated to YAML |
| `public` | `boolean` | `isLocation(String path)` | Delegated to YAML |
| `public @Nullable` | `ConfigurationSection` | `getConfigurationSection(String path)` | Delegated to YAML |
| `public` | `boolean` | `isConfigurationSection(String path)` | Delegated to YAML |

---

### `abstract class AbstractConfigFile extends GenericYamlFile`

Convenience wrapper for `config.yml` with version key `"config-version"`.

#### Constructors

| Signature | Description |
|---|---|
| `AbstractConfigFile(KPlugin plugin)` | Loads `config.yml` from data folder / JAR |

#### Methods

| Modifier | Return | Method | Description |
|---|---|---|---|
| `protected abstract` | `void` | `update(int oldVersion, int newVersion)` | Config migration logic |

---

### `class MessageFile`

Message configuration file manager with color and prefix support.

#### Fields

| Modifier | Type | Name | Description |
|---|---|---|---|
| `@Getter private final` | `KPlugin` | `plugin` | Plugin instance |
| `@Getter private final` | `File` | `file` | messages.yml file |
| `@Getter private final` | `YamlConfiguration` | `defaultYaml` | Default messages from JAR |
| `@Getter private final` | `YamlConfiguration` | `yaml` | Loaded messages |

#### Constructors

| Signature | Description |
|---|---|
| `MessageFile(KPlugin plugin)` | Loads messages.yml, auto-updates missing keys, initializes `MessageUtil` |

#### Methods

| Modifier | Return | Method | Description |
|---|---|---|---|
| `public @NotNull` | `String` | `getMessage(String key)` | Raw message by key |
| `public @NotNull` | `String` | `getMessage(String key, Function<String, String> function)` | Message with transformation |
| `public @NotNull` | `String` | `getColorizedMessage(String key)` | Legacy-colorized message |
| `public @NotNull` | `String` | `getColorizedMessage(String key, Function<String, String> function)` | Colorized with transformation |
| `public @NotNull` | `Component` | `getModernColorizedMessage(String key)` | Adventure Component message |
| `public @NotNull` | `Component` | `getModernColorizedMessage(String key, Function<String, String> function)` | Component with transformation |
| `public` | `void` | `sendMessage(CommandSender sender, String key, Function<String, String> function, boolean allowEmpty)` | Send prefixed message |
| `public` | `void` | `sendMessage(CommandSender sender, String key, Function<String, String> function)` | Send prefixed (skip empty) |
| `public` | `void` | `sendMessage(CommandSender sender, String key)` | Send prefixed (skip empty) |
| `public` | `void` | `sendMessage(CommandSender sender, String key, boolean allowEmpty)` | Send prefixed |
| `public` | `void` | `sendMessageWithPrefix(CommandSender sender, String message)` | Send raw string with prefix |

---

## gui

### `class GUI`

Core GUI class for interactive inventory menus.

#### Fields

| Modifier | Type | Name | Description |
|---|---|---|---|
| `protected final` | `UUID` | `guiId` | Unique GUI identifier |
| `protected final` | `int` | `rows` | Number of rows (1-6) |
| `protected final` | `int` | `size` | Total slots (rows × 9) |
| `protected` | `String` | `title` | GUI title |
| `protected` | `GUIHolder` | `holder` | Inventory holder |
| `protected` | `Inventory` | `inventory` | Bukkit inventory |
| `protected` | `boolean` | `initialized` | Whether inventory is created |
| `protected` | `boolean` | `viewOnly` | Blocks all item interactions (default: `true`) |
| `protected final` | `Map<Integer, Consumer<ClickContext>>` | `clickHandlers` | Per-slot click handlers |
| `protected final` | `Map<Integer, String>` | `disabledSlots` | Disabled slot messages |
| `protected final` | `Map<Integer, Map<String, Object>>` | `slotMeta` | Per-slot metadata |
| `protected` | `Consumer<ClickContext>` | `globalClickHandler` | Global click handler |
| `protected` | `Consumer<Player>` | `closeHandler` | Close event handler |
| `protected` | `Consumer<Player>` | `openHandler` | Open event handler |
| `protected final` | `Map<String, Object>` | `guiMeta` | GUI-wide metadata |

#### Constructors

| Modifier | Signature | Description |
|---|---|---|
| `protected` | `GUI(int rows, String title)` | Creates GUI (1-6 rows, supports & color codes) |

#### Static Factory Methods

| Return | Method | Description |
|---|---|---|
| `GUIBuilder` | `builder(int rows)` | Creates a GUIBuilder |
| `GUIBuilder` | `builderBySize(int size)` | Creates GUIBuilder by inventory size (9/18/27/36/45/54) |
| `GUI` | `create(int rows, String title)` | Creates GUI directly |

#### Slot Access

| Return | Method | Description |
|---|---|---|
| `SlotHandle` | `slot(int slot)` | Handle for a single slot |
| `SlotRangeHandle` | `slotRange(int start, int end)` | Handle for contiguous range (inclusive) |
| `MultiSlotHandle` | `slots(int... indices)` | Handle for multiple slots |
| `SlotRangeHandle` | `row(int row)` | Handle for an entire row (0-based) |
| `MultiSlotHandle` | `column(int column)` | Handle for an entire column (0-8) |

#### Slot Operations (Internal)

| Return | Method | Description |
|---|---|---|
| `void` | `setSlotItem(int slot, ItemStack item)` | Set item in slot |
| `ItemStack` | `getSlotItem(int slot)` | Get item from slot |
| `void` | `setSlotClickHandler(int slot, Consumer<ClickContext> handler)` | Set/remove click handler |
| `void` | `setSlotDisabled(int slot, String message)` | Disable slot with message |
| `void` | `setSlotEnabled(int slot)` | Enable slot |
| `boolean` | `isSlotDisabled(int slot)` | Check if slot is disabled |
| `void` | `setSlotMeta(int slot, String key, Object value)` | Set slot metadata |
| `<T> T` | `getSlotMeta(int slot, String key)` | Get slot metadata |

#### Global Handlers

| Return | Method | Description |
|---|---|---|
| `GUI` | `onGlobalClick(Consumer<ClickContext> handler)` | Set global click handler |
| `GUI` | `onClose(Consumer<Player> handler)` | Set close handler |
| `GUI` | `onOpen(Consumer<Player> handler)` | Set open handler |

#### Properties

| Return | Method | Description |
|---|---|---|
| `UUID` | `getGuiId()` | GUI unique ID |
| `int` | `getRows()` | Row count |
| `int` | `getSize()` | Slot count |
| `String` | `getTitle()` | Title |
| `boolean` | `isViewOnly()` | View-only mode |
| `GUI` | `setViewOnly(boolean viewOnly)` | Set view-only |
| `GUI` | `updateTitle(String newTitle)` | Update title and refresh for viewers |

#### Metadata

| Return | Method | Description |
|---|---|---|
| `GUI` | `setMeta(String key, Object value)` | Set GUI metadata |
| `<T> T` | `getMeta(String key)` | Get metadata (nullable) |
| `<T> T` | `getMeta(String key, T defaultValue)` | Get metadata with default |

#### Helper Methods

| Return | Method | Description |
|---|---|---|
| `GUI` | `fillBorder(ItemStack item)` | Fill border slots |
| `GUI` | `fillBorder(ItemBuilder builder)` | Fill border with builder |
| `GUI` | `fillEmpty(ItemStack item)` | Fill empty slots |
| `GUI` | `fillEmpty(ItemBuilder builder)` | Fill empty with builder |
| `GUI` | `clear()` | Clear all items and handlers |
| `GUI` | `refresh()` | Refresh for all viewers |
| `GUIBuilder` | `toBuilder()` | Convert to builder for modification |

#### Open/Close

| Return | Method | Description |
|---|---|---|
| `void` | `open(Player player)` | Open GUI for player |
| `void` | `close(Player player)` | Close GUI for player |

#### Event Callbacks (Internal)

| Return | Method | Description |
|---|---|---|
| `void` | `handleClick(ClickContext context)` | Process click event |
| `void` | `handleOpen(Player player)` | Process open event |
| `void` | `handleClose(Player player)` | Process close event |

---

### `final class GUIBuilder`

Fluent builder for creating GUI instances.

#### Constructors

| Modifier | Signature |
|---|---|
| package | `GUIBuilder(int rows)` |

#### Static Methods

| Return | Method | Description |
|---|---|---|
| `GUIBuilder` | `from(GUI gui)` | Create builder from existing GUI |

#### Methods

| Return | Method | Description |
|---|---|---|
| `GUIBuilder` | `title(String title)` | Set title |
| `GUIBuilder` | `viewOnly(boolean viewOnly)` | Set view-only |
| `GUIBuilder` | `onGlobalClick(Consumer<ClickContext>)` | Set global click handler |
| `GUIBuilder` | `onClose(Consumer<Player>)` | Set close handler |
| `GUIBuilder` | `onOpen(Consumer<Player>)` | Set open handler |
| `GUIBuilder` | `slot(int slot, ItemStack item)` | Configure slot with item |
| `GUIBuilder` | `slot(int slot, ItemBuilder builder)` | Configure slot with builder |
| `GUIBuilder` | `slot(int slot, ItemStack item, Consumer<ClickContext> handler)` | Slot with click handler |
| `GUIBuilder` | `slot(int slot, ItemBuilder builder, Consumer<ClickContext> handler)` | Slot builder + handler |
| `GUIBuilder` | `disabledSlot(int slot, ItemStack item, String disabledMessage)` | Disabled slot |
| `GUIBuilder` | `disabledSlot(int slot, ItemBuilder builder, String disabledMessage)` | Disabled slot with builder |
| `GUIBuilder` | `fillBorder(ItemStack item)` | Fill border |
| `GUIBuilder` | `fillBorder(ItemBuilder builder)` | Fill border with builder |
| `GUIBuilder` | `fillEmpty(ItemStack item)` | Fill empty slots |
| `GUIBuilder` | `fillEmpty(ItemBuilder builder)` | Fill empty with builder |
| `GUIBuilder` | `removeSlot(int slot)` | Remove slot config |
| `GUIBuilder` | `removeSlotRange(int start, int end)` | Remove range |
| `GUIBuilder` | `clearSlots()` | Remove all slot configs |
| `GUIBuilder` | `copy()` | Deep copy builder |
| `GUI` | `build()` | Build the GUI |

---

### `class GUIManager implements Listener`

Central manager for GUI lifecycle, event handling, and player tracking. Singleton via `InstanceManager`.

#### Constructors

| Signature | Description |
|---|---|
| `GUIManager(KPlugin plugin)` | Creates manager, registers events and in InstanceManager |

#### Methods

| Return | Method | Description |
|---|---|---|
| `static GUIManager` | `getInstance()` | Get singleton |
| `UUID` | `getManagerUUID()` | Manager UUID (changes on reload) |
| `void` | `openGUI(Player player, GUI gui)` | Open a GUI for player |
| `void` | `closeGUI(Player player)` | Close player's GUI |
| `@Nullable GUI` | `getOpenGUI(Player player)` | Get player's open GUI |
| `@Nullable GUI` | `getGUI(Inventory inventory)` | Get GUI for inventory |
| `boolean` | `hasGUIOpen(Player player)` | Check if player has GUI open |
| `void` | `clearAll()` | Clear tracking without closing |
| `void` | `closeAll()` | Close all open GUIs |
| `void` | `shutdown()` | Shutdown and cleanup |

#### Event Handlers

| Event | Priority | Description |
|---|---|---|
| `InventoryClickEvent` | HIGHEST | Routes clicks to GUI handlers; cancels if view-only |
| `InventoryDragEvent` | HIGHEST | Cancels drag if any slot is in GUI |
| `InventoryCloseEvent` | MONITOR | Removes tracking, calls close handler |
| `InventoryOpenEvent` | MONITOR | Adds tracking, calls open handler |
| `PlayerQuitEvent` | MONITOR | Removes player tracking |

---

## gui.context

### `final class ClickContext`

Context object for GUI click events.

#### Constructors

| Signature |
|---|
| `ClickContext(Player player, GUI gui, int slot, ItemStack item, ClickType clickType, InventoryClickEvent event)` |

#### Accessor Methods

| Return | Method |
|---|---|
| `Player` | `player()` |
| `GUI` | `gui()` |
| `int` | `slot()` |
| `@Nullable ItemStack` | `item()` |
| `Optional<ItemStack>` | `itemOptional()` |
| `ClickType` | `clickType()` |
| `InventoryClickEvent` | `event()` |

#### Click Type Helpers

| Return | Method |
|---|---|
| `boolean` | `isLeftClick()` |
| `boolean` | `isRightClick()` |
| `boolean` | `isShiftClick()` |
| `boolean` | `isMiddleClick()` |
| `boolean` | `isDoubleClick()` |
| `boolean` | `isNumberKey()` |
| `int` | `getHotbarButton()` |

#### Utility Methods

| Return | Method | Description |
|---|---|---|
| `void` | `cancel()` | Cancel the event |
| `boolean` | `isCancelled()` | Check cancellation |
| `void` | `playSound(Sound sound)` | Play sound at player |
| `void` | `playSound(Sound sound, float volume, float pitch)` | Play sound with params |
| `void` | `close()` | Close GUI for player |

#### Metadata

| Return | Method | Description |
|---|---|---|
| `<T> T` | `getMeta(String key)` | Get metadata |
| `<T> T` | `getMeta(String key, T defaultValue)` | Get with default |
| `ClickContext` | `setMeta(String key, Object value)` | Set metadata |
| `boolean` | `hasMeta(String key)` | Check key exists |
| `Map<String, Object>` | `meta()` | Get full metadata map |

---

## gui.holder

### `class GUIHolder implements InventoryHolder`

Custom inventory holder for GUI validation. Stores manager UUID and GUI UUID.

#### Constructors

| Signature |
|---|
| `GUIHolder(UUID managerUUID, UUID guiUUID, GUI gui)` |

#### Methods

| Return | Method | Description |
|---|---|---|
| `UUID` | `getManagerUUID()` | Manager UUID |
| `UUID` | `getGuiUUID()` | GUI UUID |
| `GUI` | `getGui()` | GUI instance |
| `void` | `setInventory(Inventory inventory)` | Set inventory ref (internal) |
| `Inventory` | `getInventory()` | InventoryHolder interface |
| `boolean` | `isValid(UUID currentManagerUUID)` | Validate against current manager |

---

## gui.slot

### `final class SlotHandle`

Handle for a single GUI slot with fluent API.

#### Constructors

| Signature |
|---|
| `SlotHandle(GUI gui, int slot)` |

#### Methods

| Return | Method | Description |
|---|---|---|
| `int` | `index()` | Slot index |
| `GUI` | `gui()` | Parent GUI |
| `SlotHandle` | `set(ItemStack item)` | Set item |
| `SlotHandle` | `set(ItemBuilder builder)` | Set from builder |
| `SlotHandle` | `clear()` | Clear item |
| `@Nullable ItemStack` | `item()` | Get current item |
| `SlotHandle` | `onClick(Consumer<ClickContext> handler)` | Set click handler |
| `SlotHandle` | `clearClickHandler()` | Remove click handler |
| `SlotHandle` | `disable(String message)` | Disable with message |
| `SlotHandle` | `enable()` | Enable slot |
| `boolean` | `isDisabled()` | Check disabled state |
| `SlotHandle` | `update(Consumer<ItemStack> updater)` | Modify current item in-place |
| `SlotHandle` | `transform(Function<ItemStack, ItemStack> transformer)` | Replace item via function |
| `boolean` | `isEmpty()` | Check if slot is empty |
| `SlotHandle` | `setMeta(String key, Object value)` | Set slot metadata |
| `<T> T` | `getMeta(String key)` | Get slot metadata |
| `<T> T` | `getMeta(String key, T defaultValue)` | Get with default |

---

### `final class SlotRangeHandle`

Handle for a contiguous range of slots (inclusive).

#### Constructors

| Signature |
|---|
| `SlotRangeHandle(GUI gui, int startSlot, int endSlot)` |

#### Methods

| Return | Method | Description |
|---|---|---|
| `int` | `start()` | Start slot |
| `int` | `end()` | End slot |
| `int` | `size()` | Number of slots |
| `GUI` | `gui()` | Parent GUI |
| `SlotRangeHandle` | `fill(ItemStack item)` | Fill all slots |
| `SlotRangeHandle` | `fill(ItemBuilder builder)` | Fill with builder |
| `SlotRangeHandle` | `fillEmpty(ItemStack item)` | Fill only empty slots |
| `SlotRangeHandle` | `fillAlternating(ItemStack item1, ItemStack item2)` | Alternate items |
| `SlotRangeHandle` | `clear()` | Clear all |
| `SlotRangeHandle` | `forEach(BiConsumer<Integer, SlotHandle> action)` | Iterate with handle |
| `SlotRangeHandle` | `forEachIndex(Consumer<Integer> action)` | Iterate indices |
| `SlotRangeHandle` | `onClick(Consumer<ClickContext> handler)` | Set handler for all |
| `SlotRangeHandle` | `disable(String message)` | Disable all |
| `SlotRangeHandle` | `enable()` | Enable all |
| `int[]` | `toArray()` | Convert to index array |
| `MultiSlotHandle` | `filter(IntPredicate predicate)` | Filter slots |
| `MultiSlotHandle` | `edges()` | Get edge slots only |

---

### `final class MultiSlotHandle`

Handle for multiple non-contiguous slots.

#### Constructors

| Signature |
|---|
| `MultiSlotHandle(GUI gui, int... slots)` |

#### Methods

| Return | Method | Description |
|---|---|---|
| `int[]` | `indices()` | Slot indices (cloned) |
| `int` | `size()` | Number of slots |
| `GUI` | `gui()` | Parent GUI |
| `MultiSlotHandle` | `fill(ItemStack item)` | Fill all |
| `MultiSlotHandle` | `fill(ItemBuilder builder)` | Fill with builder |
| `MultiSlotHandle` | `fillEmpty(ItemStack item)` | Fill only empty |
| `MultiSlotHandle` | `fillAlternating(ItemStack item1, ItemStack item2)` | Alternate items |
| `MultiSlotHandle` | `clear()` | Clear all |
| `MultiSlotHandle` | `forEach(BiConsumer<Integer, SlotHandle> action)` | Iterate with handle |
| `MultiSlotHandle` | `forEachIndex(Consumer<Integer> action)` | Iterate indices |
| `MultiSlotHandle` | `onClick(Consumer<ClickContext> handler)` | Set handler for all |
| `MultiSlotHandle` | `disable(String message)` | Disable all |
| `MultiSlotHandle` | `enable()` | Enable all |
| `MultiSlotHandle` | `filter(IntPredicate predicate)` | Filter slots |
| `MultiSlotHandle` | `combine(MultiSlotHandle other)` | Merge two handles |

---

## gui.animation

### `final class FrameAnimation`

Diff-based frame animation for GUIs. Only updates changed slots between frames.

#### Static Factory Methods

| Return | Method | Description |
|---|---|---|
| `FrameAnimation` | `create(GUI gui)` | Create new animation |
| `FrameAnimation` | `alternating(GUI gui, int[] slots, ItemStack item1, ItemStack item2, long intervalTicks)` | Pre-built alternating animation |
| `FrameAnimation` | `wave(GUI gui, int[] slots, ItemStack activeItem, ItemStack inactiveItem, long intervalTicks)` | Pre-built wave animation |
| `FrameAnimation` | `pulse(GUI gui, int slot, ItemStack[] items, long intervalTicks)` | Pre-built pulse animation |

#### Configuration Methods

| Return | Method | Description |
|---|---|---|
| `FrameAnimation` | `interval(long ticks)` | Set frame interval (default: 10) |
| `FrameAnimation` | `loop(boolean loop)` | Enable/disable looping |
| `FrameAnimation` | `onComplete(Runnable callback)` | Set completion callback |
| `FrameAnimation` | `addFrame(Map<Integer, ItemStack> frame)` | Add a frame |
| `FrameAnimation` | `addFrame(Consumer<Map<Integer, ItemStack>> frameBuilder)` | Add frame via builder |
| `FrameAnimation` | `addFrames(Map<Integer, ItemStack>... frames)` | Add multiple frames |
| `FrameAnimation` | `clearFrames()` | Remove all frames |

#### Control Methods

| Return | Method | Description |
|---|---|---|
| `void` | `start()` | Start/resume animation |
| `void` | `stop()` | Stop animation |
| `void` | `pause()` | Pause animation |
| `void` | `jumpToFrame(int frameIndex)` | Jump to specific frame |
| `boolean` | `isRunning()` | Check running state |
| `boolean` | `isPaused()` | Check paused state |
| `int` | `getCurrentFrameIndex()` | Current frame index |
| `int` | `getFrameCount()` | Total frame count |

---

## gui.pagination

### `final class Pagination<T>`

Pagination helper for displaying paginated content in GUIs.

#### Static Factory Methods

| Return | Method | Description |
|---|---|---|
| `Builder<T>` | `create(GUI gui, int[] contentSlots)` | Create builder for type T |
| `Builder<ItemStack>` | `createForItems(GUI gui, int[] contentSlots)` | Create builder for ItemStack |

#### Methods

| Return | Method | Description |
|---|---|---|
| `int` | `getCurrentPage()` | Current page (0-based) |
| `int` | `getTotalPages()` | Total pages |
| `boolean` | `hasPreviousPage()` | Check if previous exists |
| `boolean` | `hasNextPage()` | Check if next exists |
| `void` | `previousPage(Player player)` | Go to previous page |
| `void` | `nextPage(Player player)` | Go to next page |
| `void` | `goToPage(int page, Player player)` | Go to specific page |
| `void` | `render(Player player)` | Render current page |

### `Pagination.Builder<T>`

| Return | Method | Description |
|---|---|---|
| `Builder<T>` | `items(List<T> items)` | Set data list |
| `Builder<T>` | `asyncLoader(Function<PageRequest, CompletableFuture<List<T>>> loader)` | Set async loader |
| `Builder<T>` | `totalPages(Supplier<Integer> supplier)` | Set total pages (required for async) |
| `Builder<T>` | `totalPages(int pages)` | Set fixed total pages |
| `Builder<T>` | `itemRenderer(Function<T, ItemStack> renderer)` | Set item renderer |
| `Builder<T>` | `onItemClick(BiConsumer<T, ClickContext> handler)` | Set item click handler |
| `Builder<T>` | `previousButton(int slot, ItemStack item)` | Configure previous button |
| `Builder<T>` | `previousButton(int slot, ItemStack item, ItemStack disabledItem)` | Previous with disabled state |
| `Builder<T>` | `nextButton(int slot, ItemStack item)` | Configure next button |
| `Builder<T>` | `nextButton(int slot, ItemStack item, ItemStack disabledItem)` | Next with disabled state |
| `Builder<T>` | `pageInfoButton(int slot, Function<PageInfo, ItemStack> renderer)` | Page info display |
| `Builder<T>` | `pageInfoButton(int slot, ItemStack baseItem)` | Page info with base item |
| `Builder<T>` | `onPageChange(BiConsumer<Integer, Player> callback)` | Page change callback |
| `Pagination<T>` | `build()` | Build pagination |

### `Pagination.PageRequest`

| Type | Field | Description |
|---|---|---|
| `int` | `page` | Page number (0-based) |
| `int` | `itemsPerPage` | Items per page |
| `int` | `offset` | Offset (page × itemsPerPage) |

### `Pagination.PageInfo`

| Type | Field | Description |
|---|---|---|
| `int` | `currentPage` | Current page (1-based) |
| `int` | `totalPages` | Total pages |
| `int` | `itemsPerPage` | Items per page |

---

## instance

### `class InstanceManager`

Centralized singleton registry using `ConcurrentHashMap`.

#### Methods

| Modifier | Return | Method | Description |
|---|---|---|---|
| `public static` | `void` | `clearInstances()` | Clear all instances |
| `public static` | `<T> T` | `registerInstance(Class<? extends T> clazz, T instance)` | Register instance |
| `public static @Nullable` | `<T> T` | `getInstance(Class<T> clazz)` | Get instance (nullable) |
| `public static @NotNull` | `<T> T` | `getInstanceOrElseThrow(Class<T> clazz)` | Get instance or throw `NoSuchElementException` |
| `public static` | `<T> void` | `removeInstance(Class<T> clazz)` | Remove instance |

---

## item

### `final class ItemBuilder`

Fluent builder for creating ItemStacks with display name, lore, enchantments, flags, skull textures, and config loading.

#### Static Factory Methods

| Return | Method | Description |
|---|---|---|
| `ItemBuilder` | `of(Material material)` | Create from material |
| `ItemBuilder` | `of(ItemStack item)` | Create by copying ItemStack |
| `@Nullable ItemBuilder` | `fromConfig(ConfigurationSection section)` | Create from config section |

#### Builder Methods

| Return | Method | Description |
|---|---|---|
| `ItemBuilder` | `material(Material material)` | Set material |
| `ItemBuilder` | `amount(int amount)` | Set amount (1-64) |
| `ItemBuilder` | `name(String name)` | Set display name (& color codes) |
| `ItemBuilder` | `lore(String... lines)` | Set lore (replaces existing) |
| `ItemBuilder` | `lore(List<String> lines)` | Set lore from list |
| `ItemBuilder` | `addLore(String... lines)` | Append lore lines |
| `ItemBuilder` | `addLore(List<String> lines)` | Append lore from list |
| `ItemBuilder` | `insertLore(int index, String line)` | Insert lore at index |
| `ItemBuilder` | `clearLore()` | Clear all lore |
| `ItemBuilder` | `enchant(Enchantment enchantment, int level)` | Add enchantment |
| `ItemBuilder` | `enchant(Map<Enchantment, Integer> enchantments)` | Add multiple enchantments |
| `ItemBuilder` | `removeEnchant(Enchantment enchantment)` | Remove enchantment |
| `ItemBuilder` | `clearEnchants()` | Clear all enchantments |
| `ItemBuilder` | `flags(ItemFlag... flags)` | Add item flags |
| `ItemBuilder` | `hideAll()` | Add all item flags |
| `ItemBuilder` | `removeFlags(ItemFlag... flags)` | Remove flags |
| `ItemBuilder` | `customModelData(Integer customModelData)` | Set custom model data |
| `ItemBuilder` | `unbreakable(boolean unbreakable)` | Set unbreakable |
| `ItemBuilder` | `unbreakable()` | Make unbreakable |
| `ItemBuilder` | `glow()` | Add glow effect (Durability 1 + HIDE_ENCHANTS) |
| `ItemBuilder` | `skull(String value)` | Set skull (player name/UUID/URL/base64) |
| `ItemBuilder` | `replacePlaceholders(Function<String, String> replacer)` | Set placeholder replacer |
| `ItemBuilder` | `modify(Consumer<ItemBuilder> consumer)` | Apply consumer for conditional modification |
| `ItemStack` | `build()` | Build the ItemStack |

#### Config Section Keys

| Key | Type | Description |
|---|---|---|
| `material` | String | Material name |
| `name` / `display-name` | String | Display name |
| `lore` | List | Lore lines |
| `amount` | Integer | Stack amount (default: 1) |
| `enchantments` | Section | Enchantment name → level |
| `flags` | List | ItemFlag names |
| `custom-model-data` | Integer | Custom model data |
| `glow` | Boolean | Glow effect |
| `unbreakable` | Boolean | Unbreakable flag |
| `skull` / `skull-owner` / `skull-texture` | String | Skull value |

---

### `final class ItemTemplate`

Config-based item template with placeholder and slot support. Stores raw strings, applies placeholders at build time.

#### Static Factory Methods

| Return | Method | Description |
|---|---|---|
| `@Nullable ItemTemplate` | `fromConfig(ConfigurationSection section)` | Load from config |
| `Builder` | `builder(Material material)` | Create builder |

#### Getters

| Return | Method | Description |
|---|---|---|
| `Material` | `getMaterial()` | Material |
| `@Nullable String` | `getName()` | Raw display name |
| `List<String>` | `getLore()` | Raw lore (copy) |
| `int` | `getAmount()` | Stack amount |
| `List<Integer>` | `getSlots()` | Configured slots (copy) |
| `int` | `getSlot()` | First slot or -1 |
| `boolean` | `hasSlots()` | Has any slot configured |
| `boolean` | `isGlow()` | Glow enabled |
| `boolean` | `isUnbreakable()` | Unbreakable flag |

#### Build Methods

| Return | Method | Description |
|---|---|---|
| `ItemStack` | `build()` | Build without placeholders |
| `ItemStack` | `build(Map<String, String> placeholders)` | Build with map placeholders |
| `ItemStack` | `build(Placeholders placeholders)` | Build with Placeholders system |
| `ItemStack` | `build(Placeholders placeholders, Player player)` | Build with player-aware placeholders |
| `ItemStack` | `build(Function<String, String> placeholderReplacer)` | Build with custom replacer |
| `ItemBuilder` | `toBuilder()` | Convert to ItemBuilder |

### `ItemTemplate.Builder`

| Return | Method | Description |
|---|---|---|
| `Builder` | `name(String name)` | Set display name |
| `Builder` | `lore(String... lines)` | Set lore |
| `Builder` | `lore(List<String> lines)` | Set lore from list |
| `Builder` | `amount(int amount)` | Set amount |
| `Builder` | `slot(int slot)` | Add slot |
| `Builder` | `slots(int... slots)` | Add multiple slots |
| `Builder` | `enchant(Enchantment enchant, int level)` | Add enchantment |
| `Builder` | `flags(ItemFlag... flags)` | Add flags |
| `Builder` | `customModelData(int data)` | Set custom model data |
| `Builder` | `glow()` / `glow(boolean)` | Glow effect |
| `Builder` | `unbreakable()` / `unbreakable(boolean)` | Unbreakable |
| `Builder` | `skull(String value)` | Skull texture |
| `ItemTemplate` | `build()` | Build template |

---

### `@Deprecated class ItemStackWrapper`

Legacy item wrapper. **Use `ItemBuilder` instead.**

#### Migration Guide

| Legacy | Modern |
|---|---|
| `new ItemStackWrapper(material)` | `ItemBuilder.of(material)` |
| `new ItemStackWrapper(itemStack)` | `ItemBuilder.of(itemStack)` |
| `setDisplayName(name)` | `name(name)` |
| `setLore(lines)` | `lore(lines)` |
| `addLore(lines)` | `addLore(lines)` |
| `setSkull(value)` | `skull(value)` |
| `fromConfigurationSection(section)` | `ItemBuilder.fromConfig(section)` |

---

## placeholder

### `final class Placeholders`

Flexible, format-free placeholder replacement system. Thread-safe.

#### Static Factory Methods

| Return | Method | Description |
|---|---|---|
| `Placeholders` | `create()` | Create empty instance |
| `Placeholders` | `of(Map<String, String> values)` | Create from map |

#### Configuration Methods

| Return | Method | Description |
|---|---|---|
| `Placeholders` | `set(String key, String value)` | Set static value |
| `Placeholders` | `set(String key, Object value)` | Set static value (toString) |
| `Placeholders` | `setAll(Map<String, ?> values)` | Set multiple values |
| `Placeholders` | `resolver(String key, Supplier<String> resolver)` | Add dynamic resolver |
| `Placeholders` | `playerResolver(String key, Function<Player, String> resolver)` | Add player-aware resolver |
| `Placeholders` | `withPlayerPlaceholders()` | Add built-in player placeholders (`{...}` format) |
| `Placeholders` | `withPlayerPlaceholders(String prefix, String suffix)` | Add player placeholders with custom format |
| `Placeholders` | `remove(String key)` | Remove placeholder |
| `Placeholders` | `clear()` | Clear all |

#### Application Methods

| Return | Method | Description |
|---|---|---|
| `String` | `apply(String text)` | Apply placeholders |
| `String` | `apply(String text, Player player)` | Apply with player context |
| `Placeholders` | `copy()` | Clone instance |
| `Placeholders` | `merge(Placeholders other)` | Merge other into this |
| `Function<String, String>` | `toFunction()` | Convert to Function |
| `Function<String, String>` | `toFunction(Player player)` | Convert to Function with player |

#### Accessor Methods

| Return | Method | Description |
|---|---|---|
| `boolean` | `has(String key)` | Check if key exists |
| `@Nullable String` | `get(String key)` | Get static value |
| `int` | `size()` | Total placeholder count |
| `boolean` | `isEmpty()` | Check if empty |

---

## task

### `interface ScheduledTask`

| Return | Method | Description |
|---|---|---|
| `void` | `cancel()` | Cancel the task |
| `boolean` | `isCancelled()` | Check cancellation state |

---

### `class ScheduledTaskImpl implements ScheduledTask`

Wrapper around FoliaLib's `WrappedTask`.

#### Methods

| Return | Method | Description |
|---|---|---|
| `void` | `cancel()` | Cancel |
| `boolean` | `isCancelled()` | Check cancelled |
| `WrappedTask` | `unwrap()` | Get underlying FoliaLib task |
| `static Consumer<WrappedTask>` | `adaptConsumer(Consumer<ScheduledTask> consumer)` | Adapt consumer |
| `static List<ScheduledTask>` | `wrapList(List<WrappedTask> tasks)` | Wrap list |

---

### `enum TaskResult`

Result of entity-region task execution.

| Constant | Description |
|---|---|
| `SUCCESS` | Task executed successfully |
| `ENTITY_RETIRED` | Entity no longer valid |
| `SCHEDULER_RETIRED` | Scheduler no longer valid |

| Return | Method |
|---|---|
| `static TaskResult` | `from(EntityTaskResult result)` |

---

## util

### `class TaskUtil`

Unified task scheduling utility wrapping FoliaLib. Works on Spigot, Paper, and Folia.

#### Sync Methods (Global Region)

| Return | Method | Description |
|---|---|---|
| `CompletableFuture<Void>` | `scheduleSync(Consumer<ScheduledTask> task)` | Schedule with task handle |
| `void` | `runSync(Runnable task)` | Run next tick |
| `ScheduledTask` | `runSync(Runnable task, long delay)` | Run after delay (ticks) |
| `CompletableFuture<Void>` | `scheduleSync(Consumer<ScheduledTask> task, long delay)` | Schedule with delay |
| `ScheduledTask` | `runSync(Runnable task, long delay, TimeUnit timeUnit)` | Run with TimeUnit delay |
| `CompletableFuture<Void>` | `scheduleSync(Consumer<ScheduledTask> task, long delay, TimeUnit timeUnit)` | Schedule with TimeUnit |
| `ScheduledTask` | `runSyncRepeating(Runnable task, long delay, long period)` | Repeating (ticks) |
| `void` | `scheduleSyncRepeating(Consumer<ScheduledTask> task, long delay, long period)` | Repeating with handle |
| `ScheduledTask` | `runSyncRepeating(Runnable task, long delay, long period, TimeUnit timeUnit)` | Repeating with TimeUnit |
| `void` | `scheduleSyncRepeating(Consumer<ScheduledTask> task, long delay, long period, TimeUnit timeUnit)` | Repeating handle + TimeUnit |

#### Async Methods

| Return | Method | Description |
|---|---|---|
| `CompletableFuture<Void>` | `scheduleAsync(Consumer<ScheduledTask> task)` | Schedule async with handle |
| `void` | `runAsync(Runnable task)` | Run immediately async |
| `ScheduledTask` | `runAsync(Runnable task, long delay)` | Async after delay (ticks) |
| `CompletableFuture<Void>` | `scheduleAsync(Consumer<ScheduledTask> task, long delay)` | Async with delay + handle |
| `ScheduledTask` | `runAsync(Runnable task, long delay, TimeUnit timeUnit)` | Async with TimeUnit |
| `CompletableFuture<Void>` | `scheduleAsync(Consumer<ScheduledTask> task, long delay, TimeUnit timeUnit)` | Async handle + TimeUnit |
| `ScheduledTask` | `runAsyncRepeating(Runnable task, long delay, long period)` | Repeating async (ticks) |
| `void` | `scheduleAsyncRepeating(Consumer<ScheduledTask> task, long delay, long period)` | Repeating async + handle |
| `ScheduledTask` | `runAsyncRepeating(Runnable task, long delay, long period, TimeUnit timeUnit)` | Repeating async + TimeUnit |
| `void` | `scheduleAsyncRepeating(Consumer<ScheduledTask> task, long delay, long period, TimeUnit timeUnit)` | Repeating async handle + TimeUnit |

#### Entity Region Methods

| Return | Method | Description |
|---|---|---|
| `CompletableFuture<TaskResult>` | `scheduleAtEntity(Entity entity, Consumer<ScheduledTask> task)` | Schedule on entity's region |
| `void` | `runAtEntity(Entity entity, Runnable task)` | Run on entity's region |
| `CompletableFuture<TaskResult>` | `scheduleAtEntityWithFallback(Entity, Consumer<ScheduledTask>, Runnable fallback)` | With fallback |
| `ScheduledTask` | `runAtEntity(Entity entity, Runnable task, long delay)` | Entity region + delay |
| `CompletableFuture<Void>` | `scheduleAtEntity(Entity entity, Consumer<ScheduledTask> task, long delay)` | Schedule + delay |
| `ScheduledTask` | `runAtEntity(Entity entity, Runnable task, Runnable fallback, long delay)` | With fallback + delay |
| `CompletableFuture<Void>` | `scheduleAtEntity(Entity, Consumer<ScheduledTask>, Runnable fallback, long delay)` | Schedule fallback + delay |
| `ScheduledTask` | `runAtEntity(Entity entity, Runnable task, long delay, TimeUnit timeUnit)` | Entity + TimeUnit |
| `CompletableFuture<Void>` | `scheduleAtEntity(Entity, Consumer<ScheduledTask>, long delay, TimeUnit)` | Schedule + TimeUnit |
| `ScheduledTask` | `runAtEntityRepeating(Entity entity, Runnable task, long delay, long period)` | Repeating at entity |
| `ScheduledTask` | `runAtEntityRepeating(Entity, Runnable, Runnable fallback, long delay, long period)` | Repeating + fallback |
| `void` | `scheduleAtEntityRepeating(Entity, Consumer<ScheduledTask>, long delay, long period)` | Schedule repeating |
| `void` | `scheduleAtEntityRepeating(Entity, Consumer<ScheduledTask>, Runnable, long, long)` | Schedule + fallback |
| `ScheduledTask` | `runAtEntityRepeating(Entity, Runnable, long delay, long period, TimeUnit)` | Repeating + TimeUnit |
| `void` | `scheduleAtEntityRepeating(Entity, Consumer<ScheduledTask>, long, long, TimeUnit)` | Schedule + TimeUnit |

#### Location Region Methods

| Return | Method | Description |
|---|---|---|
| `CompletableFuture<Void>` | `scheduleAtLocation(Location, Consumer<ScheduledTask>)` | Schedule at location |
| `void` | `runAtLocation(Location, Runnable)` | Run at location |
| `ScheduledTask` | `runAtLocation(Location, Runnable, long delay)` | Location + delay |
| `CompletableFuture<Void>` | `scheduleAtLocation(Location, Consumer<ScheduledTask>, long delay)` | Schedule + delay |
| `ScheduledTask` | `runAtLocation(Location, Runnable, long delay, TimeUnit)` | Location + TimeUnit |
| `CompletableFuture<Void>` | `scheduleAtLocation(Location, Consumer<ScheduledTask>, long delay, TimeUnit)` | Schedule + TimeUnit |
| `ScheduledTask` | `runAtLocationRepeating(Location, Runnable, long delay, long period)` | Repeating at location |
| `void` | `scheduleAtLocationRepeating(Location, Consumer<ScheduledTask>, long, long)` | Schedule repeating |
| `ScheduledTask` | `runAtLocationRepeating(Location, Runnable, long, long, TimeUnit)` | Repeating + TimeUnit |
| `void` | `scheduleAtLocationRepeating(Location, Consumer<ScheduledTask>, long, long, TimeUnit)` | Schedule + TimeUnit |

#### Utility Methods

| Return | Method | Description |
|---|---|---|
| `boolean` | `isOwnedByCurrentRegion(Location location)` | Check location ownership |
| `boolean` | `isOwnedByCurrentRegion(Location location, int radius)` | Check with radius |
| `boolean` | `isOwnedByCurrentRegion(Block block)` | Check block ownership |
| `boolean` | `isOwnedByCurrentRegion(World world, int x, int z)` | Check coordinates |
| `boolean` | `isOwnedByCurrentRegion(World world, int x, int y, int z)` | Check 3D coordinates |
| `boolean` | `isOwnedByCurrentRegion(Entity entity)` | Check entity ownership |
| `void` | `cancel(ScheduledTask task)` | Cancel a task |
| `void` | `cancelTask(ScheduledTask task)` | Cancel via scheduler |
| `void` | `cancelAllTasks()` | Cancel all plugin tasks |
| `List<ScheduledTask>` | `getAllTasks()` | Get all plugin tasks |
| `List<ScheduledTask>` | `getAllServerTasks()` | Get all server tasks |
| `ScheduledTask` | `wrapTask(Object task)` | Wrap Bukkit task |
| `boolean` | `isGlobalTickThread()` | Check if on global tick thread |
| `boolean` | `isFolia()` | Check if running on Folia |

---

### `class ColorUtil`

Text colorization utility.

#### Methods

| Return | Method | Description |
|---|---|---|
| `static String` | `colorize(String input)` | Convert `&` codes to legacy `§` colored string |
| `static Component` | `modernColorize(String input)` | Convert `&` codes to Adventure Component |

---

### `class LoggerUtil`

Plugin-prefixed logging with debug mode.

#### Fields

| Modifier | Type | Name | Description |
|---|---|---|---|
| `private static` | `boolean` | `debugEnabled` | Debug mode flag |

#### Methods

| Return | Method | Description |
|---|---|---|
| `static void` | `setDebug(boolean enabled)` | Enable/disable debug |
| `static boolean` | `isDebug()` | Check debug state |
| `static void` | `info(String message)` | Log info |
| `static void` | `info(String message, Object... args)` | Log formatted info |
| `static void` | `warning(String message)` | Log warning |
| `static void` | `warning(String message, Object... args)` | Log formatted warning |
| `static void` | `severe(String message)` | Log severe |
| `static void` | `severe(String message, Object... args)` | Log formatted severe |
| `static void` | `message(String message)` | Send colorized console message |
| `static void` | `message(String message, Object... args)` | Send formatted colorized console message |
| `static void` | `debug(String message)` | Log debug (only if enabled) |
| `static void` | `debug(String message, Object... args)` | Log formatted debug |

---

### `class MessageUtil`

Static proxy for `MessageFile`. Auto-initialized when `MessageFile` is constructed.

#### Methods

| Return | Method | Description |
|---|---|---|
| `static void` | `initialize(MessageFile messageFile)` | Set the backing MessageFile |
| `static @NotNull String` | `getMessage(String key)` | Raw message by key |
| `static @NotNull String` | `getMessage(String key, Function<String, String> function)` | Message with transformation |
| `static @NotNull String` | `getColorizedMessage(String key)` | Legacy-colorized message |
| `static @NotNull String` | `getColorizedMessage(String key, Function<String, String> function)` | Colorized with transformation |
| `static @NotNull Component` | `getModernColorizedMessage(String key)` | Adventure Component message |
| `static @NotNull Component` | `getModernColorizedMessage(String key, Function<String, String> function)` | Component with transformation |
| `static void` | `sendMessage(CommandSender sender, String key, Function<String, String> function, boolean allowEmpty)` | Send prefixed message |
| `static void` | `sendMessage(CommandSender sender, String key, Function<String, String> function)` | Send prefixed (skip empty) |
| `static void` | `sendMessage(CommandSender sender, String key)` | Send prefixed (skip empty) |
| `static void` | `sendMessage(CommandSender sender, String key, boolean allowEmpty)` | Send prefixed |
| `static void` | `sendMessageWithPrefix(CommandSender sender, String message)` | Send raw with prefix |

---

### `class TeleportUtil`

Async teleportation utility wrapping FoliaLib.

#### Methods

| Return | Method | Description |
|---|---|---|
| `static CompletableFuture<Boolean>` | `teleportAsync(Entity entity, Location location)` | Teleport async |
| `static CompletableFuture<Boolean>` | `teleportAsync(Entity entity, Location location, TeleportCause cause)` | Teleport async with cause |

> **Platform behavior:** Folia → async region. Paper → async if supported. Spigot → next tick.

---

### `class SoundUtil`

Multi-version sound utility with legacy-to-modern mapping.

#### Sound Playback

| Return | Method | Description |
|---|---|---|
| `static void` | `play(Player player, String soundName, float volume, float pitch)` | Play at player location |
| `static void` | `play(Player player, Location location, String soundName, float volume, float pitch)` | Play at location |
| `static void` | `playUnsafe(Player player, String soundName, float volume, float pitch)` | Play without error catching |
| `static boolean` | `isValidSound(String soundName)` | Check if sound name is valid |

#### Predefined Sounds

| Return | Method | Sound |
|---|---|---|
| `static void` | `playClickSound(Player)` | `ui.button.click` |
| `static void` | `playOpenSound(Player)` | `block.chest.open` |
| `static void` | `playCloseSound(Player)` | `block.chest.close` |
| `static void` | `playNavigateSound(Player)` | `entity.experience_orb.pickup` (0.5 vol, 1.2 pitch) |
| `static void` | `playErrorSound(Player)` | `entity.villager.no` |
| `static void` | `playSuccessSound(Player)` | `entity.player.levelup` |

#### Legacy Mappings

| Legacy Name | Modern Name |
|---|---|
| `CLICK` / `UI_BUTTON_CLICK` | `ui.button.click` |
| `LEVEL_UP` / `ENTITY_PLAYER_LEVELUP` | `entity.player.levelup` |
| `CHEST_OPEN` / `BLOCK_CHEST_OPEN` | `block.chest.open` |
| `CHEST_CLOSE` / `BLOCK_CHEST_CLOSE` | `block.chest.close` |
| `NOTE_PLING` / `BLOCK_NOTE_BLOCK_PLING` | `block.note_block.pling` |
| `ORB_PICKUP` / `ENTITY_EXPERIENCE_ORB_PICKUP` | `entity.experience_orb.pickup` |
| `VILLAGER_NO` / `ENTITY_VILLAGER_NO` | `entity.villager.no` |
| `VILLAGER_YES` / `ENTITY_VILLAGER_YES` | `entity.villager.yes` |
