# KPlugin GUI System - Complete Usage Guide

**Version**: 4.0.0  
**Compatibility**: Java 8+, Minecraft 1.16.5+, Spigot/Paper/Folia  
**Author**: KhanhHuynh

---

## Table of Contents

1. [Introduction](#introduction)
2. [Installation & Setup](#installation--setup)
3. [Quick Start](#quick-start)
4. [Core Concepts](#core-concepts)
5. [Creating GUIs](#creating-guis)
6. [Slot Operations](#slot-operations)
7. [Click Handling](#click-handling)
8. [ItemBuilder](#itembuilder)
9. [Configuration Items](#configuration-items)
10. [Pagination](#pagination)
11. [Animations](#animations)
12. [Advanced Features](#advanced-features)
13. [Security & Thread Safety](#security--thread-safety)
14. [Best Practices](#best-practices)
15. [API Reference](#api-reference)
16. [Troubleshooting](#troubleshooting)

---

## Introduction

The KPlugin GUI System v4.0 is a modern, fluent API for creating interactive inventory-based GUIs in Minecraft. It features:

- **Slot-First Design**: All operations start from slots for maximum readability
- **Fluent Builders**: Method chaining for concise, readable code
- **Async-Safe**: Full Folia compatibility with automatic thread management
- **Diff-Based Updates**: Efficient animations that only update changed slots
- **Config-Driven**: YAML support for items and templates
- **Security Built-In**: GUIHolder validation, anti-duplication protection

### Key Improvements over v3.x

- ✅ Cleaner API with slot-first paradigm
- ✅ Rich `ClickContext` instead of raw Bukkit events
- ✅ Async pagination with database support
- ✅ Efficient diff-based animations
- ✅ Improved ItemBuilder with config support
- ✅ Better security with GUIHolder validation

---

## Installation & Setup

### 1. Add KPlugin Dependency

**Maven**:
```xml
<dependency>
    <groupId>dev.khanh.plugin</groupId>
    <artifactId>kplugin</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>
```

### 2. Initialize GUIManager

**CRITICAL**: Initialize `GUIManager` in your plugin's `enable()` method:

```java
package com.example.myplugin;

import dev.khanh.plugin.kplugin.KPlugin;
import dev.khanh.plugin.kplugin.gui.GUIManager;

public class MyPlugin extends KPlugin {
    
    @Override
    public void enable() {
        // Initialize GUIManager FIRST - this is required!
        GUIManager.init(this);
        LoggerUtil.info("GUI System initialized!");
        
        // Now you can create GUIs
    }
    
    @Override
    public void disable() {
        GUIManager.shutdown();
    }
}
```

### 3. Verify Installation

```java
GUIManager manager = GUIManager.getInstance();
if (manager != null) {
    LoggerUtil.info("✓ GUI System ready");
} else {
    LoggerUtil.severe("✗ GUIManager not initialized!");
}
```

---

## Quick Start

### Example 1: Simple Menu

```java
import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public void openMenu(Player player) {
    GUI gui = GUI.builder(3)
            .title("&6&lMain Menu")
            .viewOnly(true)
            .build();

    // Set items using slot-first pattern
    gui.slot(13)
            .set(ItemBuilder.of(Material.DIAMOND)
                    .name("&bShop")
                    .lore("&7Click to open shop")
                    .glow()
                    .build())
            .onClick(ctx -> {
                ctx.playSound(Sound.UI_BUTTON_CLICK);
                ctx.sendMessage("&aOpening shop...");
                // Open shop GUI
            });

    gui.open(player);
}
```

### Example 2: Using GUIBuilder

```java
public void openQuickGUI(Player player) {
    GUI.builder(3)
        .title("&aQuick Menu")
        .slot(11, createItem(Material.EMERALD, "&aOption 1"), 
            ctx -> ctx.sendMessage("Selected 1"))
        .slot(13, createItem(Material.GOLD_INGOT, "&eOption 2"),
            ctx -> ctx.sendMessage("Selected 2"))
        .slot(15, createItem(Material.DIAMOND, "&bOption 3"),
            ctx -> ctx.sendMessage("Selected 3"))
        .fillBorder(createGlass())
        .build()
        .open(player);
}

private ItemStack createItem(Material mat, String name) {
    return ItemBuilder.of(mat).name(name).build();
}

private ItemStack createGlass() {
    return ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
        .name(" ").build();
}
```

---

## Core Concepts

### 1. Lazy Initialization

GUIs use lazy initialization - the inventory is created when first opened, not in the constructor:

```java
GUI gui = GUI.builder(3).title("&aMenu").build();
// Inventory doesn't exist yet

gui.open(player);
// Now inventory is created and opened
```

**Benefits**:
- Memory efficient - inventory only created when needed
- Allows subclass customization before inventory creation
- Supports per-player GUI customization

### 2. GUIHolder Validation

Every GUI uses a `GUIHolder` (custom InventoryHolder) for security:

```java
// Automatic creation
UUID managerUUID = GUIManager.getInstance().getManagerUUID();
GUIHolder holder = new GUIHolder(managerUUID, guiId, this);
Inventory inventory = Bukkit.createInventory(holder, size, title);
```

**How it works**:
1. GUIManager generates unique UUID on initialization
2. Each GUI stores manager UUID in holder
3. On click, manager UUID is validated
4. If plugin reloaded (different UUID), GUI is closed
5. Prevents stale GUI exploits

**Advantages**:
- ✅ Zero item overhead (no PDC tags needed)
- ✅ Automatic plugin reload detection
- ✅ Simpler validation logic
- ✅ Works with any ItemStack

### 3. Slot-First Paradigm

All operations start from slots:

```java
// Old v3.x approach
gui.setItem(13, item);
gui.addClickHandler(13, handler);

// New v4.0 approach
gui.slot(13)
    .set(item)
    .onClick(handler);

// Chain multiple operations
gui.slot(13)
    .set(item)
    .onClick(ctx -> ctx.sendMessage("Clicked!"))
    .setMeta("data", myObject);
```

### 4. Three Slot Handle Types

```java
// Single slot
SlotHandle single = gui.slot(13);

// Contiguous range
SlotRangeHandle range = gui.slotRange(0, 8);    // Slots 0-8
SlotRangeHandle row = gui.row(0);                // First row
SlotRangeHandle col = gui.column(4);             // Middle column

// Non-contiguous multiple
MultiSlotHandle multi = gui.slots(0, 8, 45, 53); // Corner slots
```

---

## Creating GUIs

### Using GUIBuilder (Recommended)

```java
GUI gui = GUI.builder(3)
    // Configuration
    .title("&6My GUI")
    .viewOnly(true)
    
    // Pre-configure slots
    .slot(13, diamondItem, ctx -> handleClick(ctx))
    .disabledSlots(0, 1, 2, 6, 7, 8)
    
    // Fill operations
    .fill(backgroundItem)
    .fillBorder(borderItem)
    
    // Event handlers
    .onGlobalClick(ctx -> LoggerUtil.info("Slot " + ctx.slot() + " clicked"))
    .onOpen(p -> p.sendMessage("&aGUI opened"))
    .onClose(p -> p.sendMessage("&cGUI closed"))
    
    .build();
```

### Manual Creation

```java
GUI gui = GUI.builder(6).title("&bManual Setup").build();

// Configure after creation
gui.fill(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
gui.fillBorder(ItemBuilder.of(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());

// Set individual slots
for (int i = 10; i <= 16; i++) {
    final int slot = i;
    gui.slot(slot)
        .set(createShopItem(slot))
        .onClick(ctx -> purchaseItem(ctx.player(), slot));
}

gui.open(player);
```

### GUI Types

```java
// Chest GUIs (rows 1-6)
GUI.builder(1)  // 9 slots
GUI.builder(2)  // 18 slots
GUI.builder(3)  // 27 slots
GUI.builder(4)  // 36 slots
GUI.builder(5)  // 45 slots
GUI.builder(6)  // 54 slots

// Special inventory types
GUI.builder(InventoryType.DISPENSER)  // 3x3 grid
GUI.builder(InventoryType.HOPPER)     // 5 slots horizontal
```

---

## Slot Operations

### Single Slot Operations

```java
SlotHandle handle = gui.slot(13);

// Content
handle.set(itemStack);
handle.set(ItemBuilder.of(Material.DIAMOND).name("&bGem").build());
handle.clear();
ItemStack item = handle.get();
boolean empty = handle.isEmpty();

// Click handler
handle.onClick(ctx -> {
    ctx.player().sendMessage("Clicked!");
    ctx.close();
});
handle.clearClickHandler();

// State
handle.disable();  // Slot becomes non-interactive
handle.enable();   // Re-enable interaction
boolean disabled = handle.isDisabled();

// Metadata
handle.setMeta("price", 100);
handle.setMeta("item-id", "legendary_sword");
Integer price = handle.getMeta("price", Integer.class);

// Chain back to GUI
int slotNum = handle.getSlot();
GUI parentGui = handle.gui();
```

### Range Operations (Contiguous)

```java
SlotRangeHandle range = gui.row(0);  // or gui.slotRange(0, 8)

// Fill operations
range.fill(borderItem);
range.fillEmpty(fillerItem);  // Only fill empty slots
range.fillAlternating(item1, item2);  // Checkerboard pattern
range.clear();

// Iteration
range.forEach(slot -> slot.set(item));
range.forEachIndexed((index, slot) -> {
    slot.setMeta("index", index);
});

// Filtering
MultiSlotHandle emptyOnes = range.emptySlots();
MultiSlotHandle filtered = range.filter(slot -> !slot.isEmpty());

// Extraction
SlotRangeHandle edges = range.edges();  // First and last only
SlotHandle first = range.first();
SlotHandle last = range.last();

// Click handlers
range.onClick(ctx -> ctx.sendMessage("Border clicked!"));
range.disableAll();
range.enableAll();

// Info
int[] slots = range.getSlots();
int size = range.size();
```

### Multi-Slot Operations (Non-Contiguous)

```java
MultiSlotHandle corners = gui.slots(0, 8, 45, 53);

// Fill operations
corners.fill(cornerItem);
corners.fillEmpty(fillerItem);
corners.clear();

// Iteration
corners.forEach(slot -> slot.glow());
corners.forEachIndexed((i, slot) -> slot.setMeta("corner", i));

// Filtering
MultiSlotHandle emptyCorners = corners.emptySlots();

// Click handlers
corners.onClick(ctx -> ctx.playSound(Sound.BLOCK_NOTE_BLOCK_PLING));
corners.disable();
corners.enable();

// Combining
MultiSlotHandle combined = corners.combine(gui.slots(18, 26));
MultiSlotHandle moreCombined = corners.combine(1, 7);

// Info
int[] allSlots = corners.getSlots();
int count = corners.size();
```

### Rows and Columns

```java
// Rows (0-based, 0 = top row)
gui.row(0).fill(borderItem);  // Top row
gui.row(5).fill(borderItem);  // Bottom row (6-row GUI)

// Columns (0-based, 0 = leftmost)
gui.column(0).fill(borderItem);  // Left column
gui.column(8).fill(borderItem);  // Right column (9 slots wide)

// Combine for border
gui.row(0).fill(border);
gui.row(5).fill(border);
gui.column(0).fill(border);
gui.column(8).fill(border);
```

### Convenience Methods

```java
// Fill entire GUI
gui.fill(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

// Fill only border slots
gui.fillBorder(ItemBuilder.of(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());

// Clear everything
gui.clear();
```

---

## Click Handling

### ClickContext API

```java
gui.slot(13).onClick(ctx -> {
    // Primary accessors
    Player player = ctx.player();
    GUI gui = ctx.gui();
    int slot = ctx.slot();
    ItemStack item = ctx.item();
    ClickType clickType = ctx.clickType();
    InventoryClickEvent rawEvent = ctx.event();
    
    // Convenience checks
    if (ctx.isLeftClick()) {
        // Handle left click
    }
    if (ctx.isRightClick()) {
        // Handle right click
    }
    if (ctx.isShiftClick()) {
        // Handle shift click
    }
    
    // Actions
    ctx.cancel();  // Cancel the event
    ctx.close();   // Close the GUI
    ctx.playSound(Sound.UI_BUTTON_CLICK);
    ctx.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    ctx.sendMessage("&aYou clicked!");
    
    // Metadata (from slot)
    Integer price = ctx.meta("price", Integer.class);
    String itemId = ctx.meta("item-id", String.class);
    boolean hasMeta = ctx.hasMeta("price");
});
```

### Click Type Examples

```java
gui.slot(13).onClick(ctx -> {
    switch (ctx.clickType()) {
        case LEFT:
            ctx.sendMessage("&aLeft clicked");
            break;
        case RIGHT:
            ctx.sendMessage("&bRight clicked");
            break;
        case SHIFT_LEFT:
            ctx.sendMessage("&eShift + Left");
            break;
        case MIDDLE:
            ctx.sendMessage("&cMiddle clicked");
            break;
        case DROP:
            ctx.sendMessage("&7Dropped");
            break;
    }
});
```

### Global Click Handler

```java
// Handle ALL clicks in the GUI
gui.onGlobalClick(ctx -> {
    LoggerUtil.info(ctx.player().getName() + " clicked slot " + ctx.slot());
    
    // You can still cancel specific clicks
    if (ctx.slot() < 9) {
        ctx.cancel();
        ctx.sendMessage("&cTop row is disabled");
    }
});

// Specific slot handlers take priority over global
gui.slot(13).onClick(ctx -> {
    ctx.sendMessage("Specific handler for slot 13");
    // Global handler won't be called for this slot
});
```

### Open/Close Handlers

```java
gui.onOpen(player -> {
    player.sendMessage("&aWelcome to the shop!");
    SoundUtil.playOpenSound(player);
});

gui.onClose(player -> {
    player.sendMessage("&cThank you for visiting!");
    SoundUtil.playCloseSound(player);
    
    // IMPORTANT: Stop animations here
    if (animation != null && animation.isRunning()) {
        animation.stop();
    }
});
```

---

## ItemBuilder

### Basic Usage

```java
ItemStack item = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("&b&lLegendary Sword")
    .lore(
        "&7Damage: &c+15",
        "&7Durability: &a1000",
        "",
        "&eClick to equip"
    )
    .amount(1)
    .build();
```

### Complete API

```java
ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_PICKAXE);

// Display
builder.name("&b&lSuper Pickaxe");
builder.lore("&7Line 1", "&7Line 2");
builder.lore(Arrays.asList("&7Line 1", "&7Line 2"));
builder.addLore("&7Extra line");
builder.insertLore(0, "&6First Line");
builder.clearLore();

// Quantity
builder.amount(16);

// Enchantments
builder.enchant(Enchantment.DAMAGE_ALL, 5);
builder.enchant(enchantmentMap);
builder.removeEnchant(Enchantment.DAMAGE_ALL);
builder.clearEnchants();
builder.glow();  // Fake glow (adds dummy enchant + hides it)

// Item Flags
builder.flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
builder.hideAll();  // Hide all attributes
builder.removeFlags(ItemFlag.HIDE_ENCHANTS);

// Unbreakable
builder.unbreakable(true);
builder.unbreakable();  // Shorthand for true

// Custom Model Data
builder.customModelData(1001);

// Skulls (Player Heads) - Unified skull() method
builder.skull("Notch");  // Player name
builder.skull("069a79f4-44e9-4726-a5be-fca90e38aaf5");  // Player UUID
builder.skull("http://textures.minecraft.net/texture/...");  // Texture URL
builder.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvIn19fQ==");  // Base64

// Placeholder support
builder.replacePlaceholders(text -> 
    text.replace("%player%", player.getName()));

// Conditional modifications
builder.modify(b -> {
    if (player.hasPermission("vip")) {
        b.glow();
    }
});

// Build
ItemStack result = builder.build();
```

### Skull Formats

The `skull(String)` method automatically detects the format:

```java
// Player name (2-16 alphanumeric characters)
ItemStack head1 = ItemBuilder.of(Material.PLAYER_HEAD)
    .skull("Notch")
    .build();

// UUID
ItemStack head2 = ItemBuilder.of(Material.PLAYER_HEAD)
    .skull("069a79f4-44e9-4726-a5be-fca90e38aaf5")
    .build();

// Texture URL
ItemStack head3 = ItemBuilder.of(Material.PLAYER_HEAD)
    .skull("http://textures.minecraft.net/texture/abc123...")
    .build();

// Base64 (any other string)
ItemStack head4 = ItemBuilder.of(Material.PLAYER_HEAD)
    .skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6...")
    .build();
```

### Creating from Existing Item

```java
ItemStack existing = player.getInventory().getItemInMainHand();

ItemStack modified = ItemBuilder.of(existing)
    .addLore("&7Modified!")
    .glow()
    .build();
```

### Config-Driven Items

```java
// config.yml:
// my-item:
//   material: DIAMOND
//   name: "&bDiamond"
//   lore:
//     - "&7Value: 100"
//   glow: true

ConfigurationSection section = config.getConfigurationSection("my-item");
ItemStack item = ItemBuilder.fromConfig(section).build();

// With placeholders
ItemStack item = ItemBuilder.fromConfig(section)
    .replacePlaceholders(text -> text.replace("%player%", player.getName()))
    .build();
```

---

## Configuration Items

### ItemTemplate Overview

`ItemTemplate` allows defining items in YAML with placeholder support:

```yaml
# items.yml
shop-item:
  material: EMERALD
  name: "&a{item_name}"
  lore:
    - "&7Price: &6${price} coins"
    - "&7Stock: &e{stock}"
    - ""
    - "&eClick to purchase"
  amount: 1
  glow: true
  enchantments:
    luck: 1
  flags:
    - HIDE_ENCHANTS
  custom-model-data: 5001
  slot: 13
  
# Skull example - unified skull field
player-head:
  skull: "Notch"  # Auto-detects: player name, UUID, URL, or base64
  name: "&ePlayer Head"
  slot: 4

# Legacy format still supported
custom-head:
  material: PLAYER_HEAD
  skull-owner: "Notch"  # Old key name
  skull-texture: "eyJ0ZXh0dXJlcyI6..."  # Old key name
```

### Loading from Config

```java
import dev.khanh.plugin.kplugin.item.ItemTemplate;

ConfigurationSection section = config.getConfigurationSection("shop-item");
ItemTemplate template = ItemTemplate.fromConfig(section);

// Build with placeholders
ItemStack item = template.build(text ->
        text.replace("{item_name}", "Health Potion")
                .replace("{price}", "50")
                .replace("{stock}", "10")
);

// Convert to ItemBuilder for further customization
ItemBuilder builder = template.toBuilder();
builder.

addLore("&7Extra line");

ItemStack customized = builder.build();
```

### Slot Formats

#### Single Slot
```yaml
item:
  material: DIAMOND
  slot: 13
```

#### Range
```yaml
border:
  material: BLACK_STAINED_GLASS_PANE
  slot: "0-8"  # Slots 0 through 8
```

#### Comma-Separated
```yaml
decorations:
  material: EMERALD
  slot: "10,12,14,16"  # Specific slots
```

#### List
```yaml
corners:
  material: GOLD_BLOCK
  slots:
    - 0
    - 8
    - 45
    - 53
```

#### Mixed List
```yaml
complex:
  material: DIAMOND
  slots:
    - "0-8"      # Range
    - 18         # Single
    - "27,36,45" # Multiple
```

### Complete Schema

```yaml
my-item:
  # REQUIRED
  material: DIAMOND_SWORD
  
  # Display
  name: "&b&lMy Sword"
  lore:
    - "&7Line 1"
    - "&7Line 2"
  
  # Quantity
  amount: 1
  
  # Durability
  durability: 100
  
  # Enchantments
  enchants:
    sharpness: 5
    fire_aspect: 2
  
  # Flags
  glow: true
  unbreakable: true
  flags:
    - HIDE_ENCHANTS
    - HIDE_ATTRIBUTES
  
  # Custom Model
  custom-model-data: 1001
  
  # NBT Data
  persistent-data:
    item-id: "legendary_sword"
    level: 10
  
  # Skull (for PLAYER_HEAD)
  skull-owner: "Notch"
  skull-texture: "base64_texture_string"
  
  # Slot(s)
  slot: 13
  # OR
  slots: "0-8"
  # OR
  slots:
    - 10
    - 12
```

### Using with GUIBuilder

```java
ItemTemplate template = ItemTemplate.fromConfig(section);

GUI gui = GUI.builder(3)
    .title("&6Shop")
    .build();

// Place template in all its configured slots
for (int slot : template.getSlots()) {
    gui.slot(slot).set(template.build());
}
```

### Placeholder Resolver

```java
// Custom placeholder resolver
Function<String, String> resolver = text -> {
    return PlaceholderAPI.setPlaceholders(player, text)
        .replace("{custom}", "value");
};

ItemStack item = template.build(resolver);
```

### Map-Based Placeholders

```java
Map<String, String> placeholders = new HashMap<>();
placeholders.put("{player}", player.getName());
placeholders.put("{balance}", String.valueOf(economy.getBalance(player)));
placeholders.put("{rank}", getRank(player));

ItemStack item = template.build(placeholders);
```

---

## Pagination

### Overview

The Pagination system supports two modes:
1. **Eager** - Pre-load all items at once
2. **Async** - Load pages on-demand asynchronously

### Eager Pagination

```java
import dev.khanh.plugin.kplugin.gui.pagination.Pagination;

public void openPlayerList(Player viewer) {
    GUI gui = GUI.builder(6)
        .title("&bOnline Players")
        .viewOnly(true)
        .build();
    
    // Define content area (28 slots for items)
    int[] contentSlots = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    // Get all online players
    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
    
    // Create pagination
    Pagination<Player> pagination = Pagination.<Player>create(gui, contentSlots)
        .items(players)
        .itemRenderer(p -> ItemBuilder.of(Material.PLAYER_HEAD)
            .skullOwner(p.getName())
            .name("&b" + p.getName())
            .lore(
                "&7Health: &c" + p.getHealth() + "/" + p.getMaxHealth(),
                "&7Level: &a" + p.getLevel(),
                "",
                "&eClick to teleport"
            )
            .build())
        .onItemClick((player, ctx) -> {
            ctx.player().teleport(player);
            ctx.sendMessage("&aTeleported to " + player.getName());
            ctx.close();
        })
        .previousButton(48, 
            ItemBuilder.of(Material.ARROW).name("&aPrevious Page").build(),
            ItemBuilder.of(Material.GRAY_DYE).name("&7No Previous Page").build())
        .nextButton(50,
            ItemBuilder.of(Material.ARROW).name("&aNext Page").build(),
            ItemBuilder.of(Material.GRAY_DYE).name("&7No Next Page").build())
        .pageInfoButton(49, info -> ItemBuilder.of(Material.BOOK)
            .name("&6Page &e" + info.currentPage + "&6/&e" + info.totalPages)
            .lore("&7Showing " + info.itemsPerPage + " items per page")
            .build())
        .build();
    
    // Render and open
    pagination.render(viewer);
    gui.open(viewer);
}
```

### Async Pagination (Database Example)

```java
import java.util.concurrent.CompletableFuture;

public void openDatabaseItems(Player player) {
    GUI gui = GUI.builder(6)
        .title("&6Item Database")
        .viewOnly(true)
        .build();
    
    int[] contentSlots = {/* ... 28 slots ... */};
    
    Pagination<ItemData> pagination = Pagination.<ItemData>create(gui, contentSlots)
        // Async loader - runs off main thread
        .asyncLoader(request -> 
            CompletableFuture.supplyAsync(() -> {
                // Safe to do database queries here
                return database.loadItems(request.offset, request.itemsPerPage);
            })
        )
        // Total pages supplier
        .totalPages(() -> {
            int totalItems = database.countItems();
            return (int) Math.ceil(totalItems / 28.0);
        })
        // Render items
        .itemRenderer(data -> ItemBuilder.of(data.getMaterial())
            .name("&b" + data.getName())
            .lore("&7ID: " + data.getId())
            .build())
        // Click handler
        .onItemClick((data, ctx) -> {
            ctx.sendMessage("&aSelected: " + data.getName());
        })
        // Navigation
        .previousButton(48, prevItem, disabledPrevItem)
        .nextButton(50, nextItem, disabledNextItem)
        .build();
    
    // Initial render
    pagination.render(player);
    gui.open(player);
}
```

### Pagination API

```java
// Page control
int currentPage = pagination.getCurrentPage();  // 0-based
int totalPages = pagination.getTotalPages();
boolean hasPrev = pagination.hasPreviousPage();
boolean hasNext = pagination.hasNextPage();

// Navigate
pagination.previousPage(player);  // Auto-closes and re-opens
pagination.nextPage(player);
pagination.goToPage(2, player);   // 0-based page index
pagination.render(player);        // Re-render current page

// Page change callback
.onPageChange((page, player) -> {
    LoggerUtil.info(player.getName() + " went to page " + page);
})
```

### PageRequest & PageInfo

```java
// PageRequest (provided to async loader)
class PageRequest {
    int page;          // Current page (0-based)
    int itemsPerPage;  // Items per page (content slots count)
    int offset;        // Database offset (page * itemsPerPage)
}

// PageInfo (provided to page info renderer)
class PageInfo {
    int currentPage;   // 1-based for display
    int totalPages;
    int itemsPerPage;
}
```

---

## Animations

### FrameAnimation

Efficient diff-based animation system that only updates changed slots.

#### Basic Usage

```java
import dev.khanh.plugin.kplugin.gui.animation.FrameAnimation;

FrameAnimation loading = FrameAnimation.create(gui)
    .interval(5)  // Ticks between frames
    .loop(true)   // Loop animation
    .addFrame(frame -> {
        frame.slot(13, ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).name("&a●").build());
        frame.slot(14, ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("&8○").build());
    })
    .addFrame(frame -> {
        frame.slot(13, ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("&8○").build());
        frame.slot(14, ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).name("&a●").build());
    })
    .build();

// Start animation
loading.start();

// IMPORTANT: Stop on close
gui.onClose(p -> loading.stop());
```

#### Factory Methods

```java
// Alternating between two states
int[] slots = {10, 11, 12, 13, 14, 15, 16};
ItemStack lit = ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).name("&a●").build();
ItemStack dark = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("&8○").build();

FrameAnimation alternating = FrameAnimation.alternating(gui, slots, lit, dark, 5);

// Wave effect (one lit slot moving)
FrameAnimation wave = FrameAnimation.wave(gui, slots, lit, dark, 4);

// Pulse effect (cycle through items on one slot)
ItemStack[] pulseItems = {
    ItemBuilder.of(Material.RED_STAINED_GLASS).build(),
    ItemBuilder.of(Material.ORANGE_STAINED_GLASS).build(),
    ItemBuilder.of(Material.YELLOW_STAINED_GLASS).build()
};
FrameAnimation pulse = FrameAnimation.pulse(gui, 13, pulseItems, 10);
```

#### Advanced Frame Building

```java
FrameAnimation anim = FrameAnimation.create(gui)
    .interval(10)
    .loop(true)
    .addFrame(frame -> {
        // Set individual slot
        frame.slot(13, item1);
        
        // Set multiple slots at once
        frame.slots(new int[]{10, 11, 12}, item2);
        
        // Clear slots
        frame.clear(14, 15, 16);
    })
    .onFrameChange((frameIndex, frame) -> {
        LoggerUtil.info("Now showing frame " + frameIndex);
    })
    .onComplete(() -> {
        player.sendMessage("&aAnimation completed!");
    })
    .build();
```

#### Animation Control

```java
// Lifecycle
anim.start();
anim.stop();
anim.pause();
anim.resume();

// State
boolean running = anim.isRunning();
int currentFrame = anim.getCurrentFrameIndex();

// Manual control
anim.setFrame(2);  // Jump to frame 2
```

#### Complex Example: Loading Screen

```java
public void showLoadingScreen(Player player) {
    GUI gui = GUI.builder(5)
        .title("&b⏳ Loading...")
        .viewOnly(true)
        .build();
    
    // Background
    gui.fill(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
    
    // Spinner slots
    int[] spinner = {12, 13, 14, 23, 32, 31, 30, 21};
    
    // Wave animation
    FrameAnimation spinAnim = FrameAnimation.wave(
        gui, 
        spinner,
        ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).name("&a●").build(),
        ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("&8○").build(),
        4
    );
    
    // Progress bar
    int[] progressSlots = {37, 38, 39, 40, 41, 42, 43};
    ItemStack filled = ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).name("&a█").build();
    ItemStack empty = ItemBuilder.of(Material.WHITE_STAINED_GLASS_PANE).name("&7░").build();
    
    FrameAnimation progress = FrameAnimation.create(gui)
        .interval(10)
        .loop(false)
        // Frame 0: all empty
        .addFrame(f -> f.slots(progressSlots, empty))
        // Frames 1-7: progressive fill
        .addFrame(f -> {
            f.slot(37, filled);
            f.slots(new int[]{38, 39, 40, 41, 42, 43}, empty);
        })
        // ... more frames ...
        .onComplete(() -> {
            player.sendMessage("&aLoading complete!");
            gui.close(player);
        })
        .build();
    
    // Start both animations
    spinAnim.start();
    progress.start();
    
    // Cleanup
    gui.onClose(p -> {
        spinAnim.stop();
        progress.stop();
    });
    
    gui.open(player);
}
```

---

## Advanced Features

### Metadata System

Store arbitrary data on slots:

```java
// Set metadata
gui.slot(13)
    .setMeta("price", 100)
    .setMeta("item-id", "legendary_sword")
    .setMeta("rarity", Rarity.LEGENDARY);

// Get metadata in click handler
gui.slot(13).onClick(ctx -> {
    Integer price = ctx.meta("price", Integer.class);
    String itemId = ctx.meta("item-id", String.class);
    Rarity rarity = ctx.meta("rarity", Rarity.class);
    
    if (economy.getBalance(ctx.player()) >= price) {
        economy.withdraw(ctx.player(), price);
        giveItem(ctx.player(), itemId, rarity);
    }
});

// Direct access
Integer price = gui.getMeta(13, "price", Integer.class);
```

### View-Only Mode

Prevent all item manipulation:

```java
GUI gui = GUI.builder(3)
    .title("&cView Only Shop")
    .viewOnly(true)  // Players cannot take/move items
    .build();

// View-only mode blocks:
// ❌ Taking items
// ❌ Placing items
// ❌ Moving items
// ❌ Shift-clicking
// ❌ Drag events
// ✅ Click handlers still work
```

### Disabled Slots

```java
// Disable specific slots
gui.slot(13).disable();
gui.slots(0, 8, 45, 53).disable();

// Using builder
GUI gui = GUI.builder(3)
    .disabledSlots(0, 1, 2, 6, 7, 8)  // Top row corners
    .build();

// Check if disabled
if (gui.slot(13).isDisabled()) {
    // Slot is disabled
}

// Re-enable
gui.slot(13).enable();
```

### Dynamic Title Updates

Note: Minecraft doesn't support dynamic title updates without reopening:

```java
// To change title, you must reopen the GUI
gui.close(player);
GUI newGui = GUI.builder(3)
    .title("&aNew Title")
    // ... copy contents from old GUI ...
    .build();
newGui.open(player);
```

### Refresh Method

```java
// Refresh GUI for all viewers
gui.refresh();

// Use case: Update items based on external state
gui.slot(13).onClick(ctx -> {
    toggleSetting(ctx.player());
    
    // Update item to reflect new state
    gui.slot(13).set(createSettingItem(ctx.player()));
    gui.refresh();  // Update for all viewers
});
```

### GUI Accessors

```java
Inventory inventory = gui.getInventory();
int size = gui.getSize();
String title = gui.getTitle();
UUID guiId = gui.getGuiId();
Set<Player> viewers = gui.getViewers();

// Example: Broadcast to all viewers
gui.getViewers().forEach(p -> 
    p.sendMessage("&aGUI updated!")
);
```

---

## Security & Thread Safety

### Security Features

#### 1. GUIHolder Validation

```java
// Automatic validation prevents stale GUI exploits
// If plugin reloads, all old GUIs become invalid
public GUI getGUI(Inventory inventory) {
    if (inventory.getHolder() instanceof GUIHolder) {
        GUIHolder holder = (GUIHolder) inventory.getHolder();
        UUID managerUUID = GUIManager.getInstance().getManagerUUID();
        
        if (holder.getManagerUUID().equals(managerUUID)) {
            return holder.getGui();  // Valid GUI
        } else {
            // Stale GUI from before plugin reload
            return null;
        }
    }
    return null;
}
```

#### 2. Anti-Duplication

```java
// All drag events are cancelled automatically
// Items are cloned when set in inventory
// Shift-click is blocked in view-only mode
```

#### 3. View-Only Mode

```java
GUI gui = GUI.builder(3)
    .viewOnly(true)  // ALWAYS use for shops/display GUIs
    .build();
```

#### 4. Permission Checks

```java
gui.slot(13).onClick(ctx -> {
    if (!ctx.player().hasPermission("shop.buy")) {
        ctx.cancel();
        ctx.sendMessage("&cYou don't have permission!");
        ctx.playSound(Sound.ENTITY_VILLAGER_NO);
        return;
    }
    
    // Process purchase
    processPurchase(ctx.player());
});
```

### Thread Safety

#### Main Thread Operations

All GUI operations are main-thread only:

```java
// ❌ WRONG - calling from async thread
CompletableFuture.supplyAsync(() -> {
    gui.slot(13).set(item);  // WILL CRASH!
    return null;
});

// ✅ CORRECT - switch to main thread
CompletableFuture.supplyAsync(() -> {
    // Async work here (database, API calls, etc.)
    return database.loadData();
}).thenAccept(data -> {
    TaskUtil.runSync(() -> {
        // Main thread - safe for GUI operations
        gui.slot(13).set(renderItem(data));
    });
});
```

#### Folia Compatibility

```java
// Spigot/Paper - use global scheduler
TaskUtil.runSync(() -> {
    gui.open(player);
});

// Folia - use entity region
TaskUtil.runAtEntity(player, () -> {
    gui.open(player);
});

// Folia - use location region
TaskUtil.runAtLocation(location, () -> {
    // Update GUI at specific location
});
```

#### Async Data Loading Pattern

```java
public void openAsyncGUI(Player player) {
    // Show loading GUI
    GUI loading = createLoadingGUI();
    loading.open(player);
    
    // Load data async
    CompletableFuture.supplyAsync(() -> {
        return database.loadExpensiveData();
    }).thenAccept(data -> {
        TaskUtil.runSync(() -> {
            // Close loading GUI
            loading.close(player);
            
            // Open real GUI with data
            GUI realGUI = createDataGUI(data);
            realGUI.open(player);
        });
    }).exceptionally(ex -> {
        TaskUtil.runSync(() -> {
            loading.close(player);
            player.sendMessage("&cFailed to load data: " + ex.getMessage());
        });
        return null;
    });
}
```

---

## Best Practices

### 1. Always Initialize GUIManager

```java
@Override
public void enable() {
    GUIManager.init(this);  // FIRST THING!
    // ... rest of initialization
}
```

### 2. Use View-Only for Display GUIs

```java
// ✅ DO - for shops, menus, displays
GUI gui = GUI.builder(3)
    .viewOnly(true)
    .build();

// ❌ DON'T - unless you want players to take items
GUI gui = GUI.builder(3)
    .viewOnly(false)
    .build();
```

### 3. Always Stop Animations

```java
FrameAnimation anim = FrameAnimation.create(gui)
    .interval(5)
    .loop(true)
    .build();

anim.start();

// ✅ ALWAYS clean up
gui.onClose(p -> {
    if (anim.isRunning()) {
        anim.stop();
    }
});
```

### 4. Use TaskUtil, Not Bukkit Scheduler

```java
// ❌ DON'T
Bukkit.getScheduler().runTaskLater(plugin, () -> {}, 20L);

// ✅ DO
TaskUtil.runSync(() -> {}, 20);
```

### 5. Validate Permissions and Conditions

```java
gui.slot(13).onClick(ctx -> {
    // Validate permission
    if (!ctx.player().hasPermission("shop.buy")) {
        ctx.sendMessage("&cNo permission!");
        return;
    }
    
    // Validate economy
    if (economy.getBalance(ctx.player()) < price) {
        ctx.sendMessage("&cInsufficient funds!");
        return;
    }
    
    // Process
    processPurchase(ctx.player());
});
```

### 6. Use Config for Maintainability

```java
// ✅ Better - easy to modify without recompiling
ItemTemplate template = ItemTemplate.fromConfig(section);
gui.slot(13).set(template.build());

// ❌ Worse - hardcoded
ItemStack item = new ItemStack(Material.DIAMOND);
ItemMeta meta = item.getItemMeta();
meta.setDisplayName(ColorUtil.colorize("&bItem"));
// ... 10 more lines ...
item.setItemMeta(meta);
```

### 7. Log Important Actions

```java
gui.slot(13).onClick(ctx -> {
    LoggerUtil.info(ctx.player().getName() + " purchased item for $" + price);
    economy.withdraw(ctx.player(), price);
    giveItem(ctx.player());
});
```

### 8. Handle Errors Gracefully

```java
Pagination.create(gui, slots)
    .asyncLoader(req -> 
        CompletableFuture.supplyAsync(() -> database.load(req.offset, req.itemsPerPage))
            .exceptionally(ex -> {
                LoggerUtil.severe("Failed to load page: " + ex.getMessage());
                return Collections.emptyList();  // Return empty instead of crashing
            })
    )
    .build();
```

### 9. Use Metadata for Complex Data

```java
// ✅ Clean - use metadata
gui.slot(13)
    .setMeta("item-data", complexObject)
    .onClick(ctx -> {
        ComplexObject data = ctx.meta("item-data", ComplexObject.class);
        processData(data);
    });

// ❌ Messy - global map or static storage
static Map<Integer, ComplexObject> slotData = new HashMap<>();
slotData.put(13, complexObject);
```

### 10. Consistent Naming and Organization

```java
// ✅ Good organization
public class ShopGUI {
    private static final int[] CONTENT_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int PREV_BUTTON_SLOT = 48;
    private static final int NEXT_BUTTON_SLOT = 50;
    
    private final GUI gui;
    private final Pagination<ShopItem> pagination;
    
    public ShopGUI(List<ShopItem> items) {
        this.gui = createGUI();
        this.pagination = createPagination(items);
    }
    
    private GUI createGUI() { /* ... */ }
    private Pagination<ShopItem> createPagination(List<ShopItem> items) { /* ... */ }
    
    public void open(Player player) {
        pagination.render(player);
        gui.open(player);
    }
}
```

---

## API Reference

### GUI

```java
// Static Factory
static GUIBuilder builder(int rows)
static GUIBuilder builder(InventoryType type)

// Slot Access
SlotHandle slot(int slot)
SlotRangeHandle slotRange(int startSlot, int endSlot)
MultiSlotHandle slots(int... slots)
SlotRangeHandle row(int row)
SlotRangeHandle column(int column)

// Content Operations
void setItem(int slot, ItemStack item)
ItemStack getItem(int slot)
void clearSlot(int slot)
void fill(ItemStack item)
void fillBorder(ItemStack item)
void clear()

// Click Handlers
void setClickHandler(int slot, Consumer<ClickContext> handler)
void clearClickHandler(int slot)
void onGlobalClick(Consumer<ClickContext> handler)
void onOpen(Consumer<Player> handler)
void onClose(Consumer<Player> handler)

// Slot State
void disableSlot(int slot)
void enableSlot(int slot)
boolean isSlotDisabled(int slot)

// Metadata
void setMeta(int slot, String key, Object value)
<T> T getMeta(int slot, String key, Class<T> type)

// Lifecycle
void open(Player player)
void close(Player player)
void refresh()

// Accessors
Inventory getInventory()
int getSize()
String getTitle()
UUID getGuiId()
Set<Player> getViewers()
```

### GUIBuilder

```java
GUIBuilder title(String title)
GUIBuilder viewOnly(boolean viewOnly)
GUIBuilder slot(int slot, ItemStack item)
GUIBuilder slot(int slot, ItemStack item, Consumer<ClickContext> handler)
GUIBuilder disabledSlot(int slot)
GUIBuilder disabledSlots(int... slots)
GUIBuilder fill(ItemStack item)
GUIBuilder fillBorder(ItemStack item)
GUIBuilder onGlobalClick(Consumer<ClickContext> handler)
GUIBuilder onOpen(Consumer<Player> handler)
GUIBuilder onClose(Consumer<Player> handler)
GUI build()
```

### SlotHandle

```java
SlotHandle set(ItemStack item)
SlotHandle set(ItemBuilder builder)
SlotHandle clear()
ItemStack get()
boolean isEmpty()
SlotHandle onClick(Consumer<ClickContext> handler)
SlotHandle clearClickHandler()
SlotHandle disable()
SlotHandle enable()
boolean isDisabled()
SlotHandle setMeta(String key, Object value)
<T> T getMeta(String key, Class<T> type)
GUI gui()
int getSlot()
```

### SlotRangeHandle

```java
SlotRangeHandle fill(ItemStack item)
SlotRangeHandle fillEmpty(ItemStack item)
SlotRangeHandle fillAlternating(ItemStack item1, ItemStack item2)
SlotRangeHandle clear()
SlotRangeHandle forEach(Consumer<SlotHandle> action)
SlotRangeHandle forEachIndexed(BiConsumer<Integer, SlotHandle> action)
MultiSlotHandle filter(Predicate<SlotHandle> predicate)
MultiSlotHandle emptySlots()
SlotRangeHandle edges()
SlotHandle first()
SlotHandle last()
SlotRangeHandle onClick(Consumer<ClickContext> handler)
SlotRangeHandle disableAll()
SlotRangeHandle enableAll()
int[] getSlots()
int size()
GUI gui()
```

### MultiSlotHandle

```java
MultiSlotHandle fill(ItemStack item)
MultiSlotHandle fillEmpty(ItemStack item)
MultiSlotHandle clear()
MultiSlotHandle forEach(Consumer<SlotHandle> action)
MultiSlotHandle forEachIndexed(BiConsumer<Integer, SlotHandle> action)
MultiSlotHandle filter(Predicate<SlotHandle> predicate)
MultiSlotHandle emptySlots()
MultiSlotHandle onClick(Consumer<ClickContext> handler)
MultiSlotHandle disable()
MultiSlotHandle enable()
MultiSlotHandle combine(MultiSlotHandle other)
MultiSlotHandle combine(int... additionalSlots)
int[] getSlots()
int size()
GUI gui()
```

### ClickContext

```java
Player player()
GUI gui()
int slot()
ItemStack item()
ClickType clickType()
InventoryClickEvent event()
boolean isLeftClick()
boolean isRightClick()
boolean isShiftClick()
void cancel()
void close()
void playSound(Sound sound)
void playSound(Sound sound, float volume, float pitch)
void sendMessage(String message)
<T> T meta(String key, Class<T> type)
boolean hasMeta(String key)
```

### ItemBuilder

```java
static ItemBuilder of(Material material)
static ItemBuilder of(ItemStack base)
static ItemBuilder fromConfig(ConfigurationSection section)
ItemBuilder name(String name)
ItemBuilder lore(String... lines)
ItemBuilder lore(List<String> lines)
ItemBuilder addLore(String... lines)
ItemBuilder amount(int amount)
ItemBuilder durability(short durability)
ItemBuilder enchant(Enchantment enchant, int level)
ItemBuilder enchant(Enchantment enchant, int level, boolean ignoreLevelRestriction)
ItemBuilder glow()
ItemBuilder flags(ItemFlag... flags)
ItemBuilder hideAllFlags()
ItemBuilder unbreakable(boolean unbreakable)
ItemBuilder customModelData(int data)
ItemBuilder persistentData(NamespacedKey key, String value)
ItemBuilder persistentData(NamespacedKey key, int value)
ItemBuilder skullOwner(String playerName)
ItemBuilder skullTexture(String base64Texture)
ItemStack build()
```

### ItemTemplate

```java
static ItemTemplate fromConfig(ConfigurationSection section)
ItemStack build()
ItemStack build(Function<String, String> placeholderResolver)
ItemStack build(Map<String, String> placeholders)
ItemBuilder toBuilder()
ItemBuilder toBuilder(Function<String, String> placeholderResolver)
Material getMaterial()
String getName()
List<String> getLore()
List<Integer> getSlots()
boolean hasSlots()
```

### Pagination

```java
static <T> Builder<T> create(GUI gui, int[] contentSlots)
static Builder<ItemStack> createForItems(GUI gui, int[] contentSlots)
int getCurrentPage()
int getTotalPages()
boolean hasPreviousPage()
boolean hasNextPage()
void previousPage(Player player)
void nextPage(Player player)
void goToPage(int page, Player player)
void render(Player player)

// Builder
Builder<T> items(List<T> items)
Builder<T> asyncLoader(Function<PageRequest, CompletableFuture<List<T>>> loader)
Builder<T> totalPages(Supplier<Integer> supplier)
Builder<T> totalPages(int pages)
Builder<T> itemRenderer(Function<T, ItemStack> renderer)
Builder<T> onItemClick(BiConsumer<T, ClickContext> handler)
Builder<T> previousButton(int slot, ItemStack item)
Builder<T> previousButton(int slot, ItemStack item, ItemStack disabledItem)
Builder<T> nextButton(int slot, ItemStack item)
Builder<T> nextButton(int slot, ItemStack item, ItemStack disabledItem)
Builder<T> pageInfoButton(int slot, Function<PageInfo, ItemStack> renderer)
Builder<T> onPageChange(BiConsumer<Integer, Player> callback)
Pagination<T> build()
```

### FrameAnimation

```java
static Builder create(GUI gui)
static FrameAnimation alternating(GUI gui, int[] slots, ItemStack item1, ItemStack item2, long intervalTicks)
static FrameAnimation wave(GUI gui, int[] slots, ItemStack item, ItemStack empty, long intervalTicks)
static FrameAnimation pulse(GUI gui, int slot, ItemStack[] items, long intervalTicks)
void start()
void stop()
void pause()
void resume()
boolean isRunning()
int getCurrentFrameIndex()
void setFrame(int frameIndex)

// Builder
Builder interval(long ticks)
Builder loop(boolean loop)
Builder addFrame(Frame frame)
Builder addFrame(Consumer<Frame.Builder> frameBuilder)
Builder onFrameChange(BiConsumer<Integer, Frame> callback)
Builder onComplete(Runnable callback)
FrameAnimation build()
```

---

## Troubleshooting

### GUIManager not initialized

**Error**: `NullPointerException` when creating GUI

**Solution**: Initialize GUIManager in `enable()`:
```java
@Override
public void enable() {
    GUIManager.init(this);
}
```

### Items disappearing when clicked

**Cause**: View-only mode is disabled

**Solution**: Enable view-only mode:
```java
GUI gui = GUI.builder(3)
    .viewOnly(true)  // Add this
    .build();
```

### Animation not stopping

**Cause**: Missing cleanup in close handler

**Solution**: Stop animation on close:
```java
gui.onClose(p -> {
    if (animation != null && animation.isRunning()) {
        animation.stop();
    }
});
```

### GUI not opening after plugin reload

**Cause**: Old GUI references with stale GUIHolder

**Solution**: GUIs are automatically invalidated. Recreate them:
```java
// Don't store GUIs as static fields that persist across reloads
// Instead, create fresh GUIs each time
public void openShop(Player player) {
    GUI shop = createShopGUI();  // Create fresh instance
    shop.open(player);
}
```

### Pagination shows empty pages

**Cause**: Total pages calculation is incorrect

**Solution**: Ensure correct page count:
```java
int totalItems = items.size();
int itemsPerPage = contentSlots.length;
int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
```

### Async errors with Folia

**Cause**: Using wrong scheduler

**Solution**: Use TaskUtil for Folia compatibility:
```java
// ❌ Wrong
Bukkit.getScheduler().runTaskLater(plugin, () -> {}, 20L);

// ✅ Correct
TaskUtil.runSync(() -> {}, 20);
```

### Click handlers not firing

**Cause 1**: Global handler is used without specific handlers  
**Solution**: Use specific slot handlers or check slot in global handler

**Cause 2**: Slot is disabled  
**Solution**: Enable the slot:
```java
gui.slot(13).enable();
```

### Items have wrong colors

**Cause**: Not using color codes properly

**Solution**: Use `&` codes, they're auto-converted:
```java
ItemBuilder.of(Material.DIAMOND)
    .name("&b&lDiamond")  // Use & codes
    .build();
```

---

## Migration from v3.x

### Constructor Changes

**v3.x**:
```java
GUI gui = new GUI(plugin, 3, "&aMenu");
```

**v4.0**:
```java
GUI gui = GUI.builder(3)
    .title("&aMenu")
    .build();
```

### Setting Items

**v3.x**:
```java
gui.setItem(13, item);
gui.addClickHandler(13, event -> {
    Player p = (Player) event.getWhoClicked();
    p.sendMessage("Clicked");
});
```

**v4.0**:
```java
gui.slot(13)
    .set(item)
    .onClick(ctx -> ctx.sendMessage("Clicked"));
```

### Opening GUI

**v3.x**:
```java
gui.openInventory(player);
```

**v4.0**:
```java
gui.open(player);
```

### Pagination

**v3.x**:
```java
PaginatedGUI gui = new PaginatedGUI(plugin, 6, "&aList", contentSlots);
gui.setItems(items);
gui.open(player);
```

**v4.0**:
```java
GUI gui = GUI.builder(6).title("&aList").build();
Pagination<Item> pagination = Pagination.<Item>create(gui, contentSlots)
    .items(items)
    .itemRenderer(item -> render(item))
    .build();
pagination.render(player);
gui.open(player);
```

### Animations

**v3.x**:
```java
GlowAnimation anim = new GlowAnimation(gui, slot, 20);
anim.start();
```

**v4.0**:
```java
FrameAnimation anim = FrameAnimation.pulse(gui, slot, pulseItems, 20);
anim.start();
```

---

**End of Guide**

For complete examples, see the test implementation file.  
For API details, refer to Javadocs at `docs/index.html`.

**Questions or Issues?**  
- Check existing documentation
- Review example implementations
- Consult source code in `src/main/java/dev/khanh/plugin/kplugin/gui/`
