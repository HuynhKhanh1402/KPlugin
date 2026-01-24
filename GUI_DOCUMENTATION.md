# KPlugin GUI Library - Complete Documentation

A comprehensive, SOLID-compliant GUI framework for Minecraft Bukkit/Spigot plugins (Java 8+, Minecraft 1.16.5+) with full Folia support.

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Requirements](#requirements)
4. [Installation & Setup](#installation--setup)
5. [Core Concepts](#core-concepts)
6. [Quick Start Guide](#quick-start-guide)
7. [GUI Types](#gui-types)
8. [GUIBuilder API](#guibuilder-api)
9. [ConfigItem System](#configitem-system)
10. [Animation Framework](#animation-framework)
11. [Pagination System](#pagination-system)
12. [Placeholder Resolution](#placeholder-resolution)
13. [Sound System](#sound-system)
14. [Security Features](#security-features)
15. [Configuration](#configuration)
16. [Folia Compatibility](#folia-compatibility)
17. [Advanced Examples](#advanced-examples)
18. [Best Practices](#best-practices)
19. [Troubleshooting](#troubleshooting)
20. [API Reference](#api-reference)

---

## Overview

The KPlugin GUI Library provides a powerful, production-ready framework for creating interactive inventory-based GUIs in Minecraft. Built on SOLID principles, it offers a fluent builder API, animation support, dual pagination modes (eager/lazy), multi-version sound compatibility, placeholder resolution, and robust security features to prevent item duplication exploits.

### Key Highlights

- **Zero boilerplate**: Create complex GUIs with minimal code
- **Type-safe**: Compile-time safety with fluent builders
- **Thread-safe**: Full Folia regional threading support
- **Secure by default**: PDC-based validation, anti-dupe protection
- **Highly configurable**: YAML-based configuration support
- **Performance-optimized**: Lazy loading, efficient item cloning

---

## Features

### ✨ Modern Architecture
- SOLID principles compliance
- Fluent builder API for intuitive GUI creation
- Thread-safe operations with concurrent collections
- Folia regional threading support via TaskUtil
- Singleton pattern for GUIManager

### 🎨 Rich Feature Set
- **Animations**: Frame-based, pulse, and transition animations
- **Pagination**: Eager (pre-loaded) and lazy (on-demand) modes
- **Sound System**: Multi-version compatibility with automatic mapping
- **Placeholders**: Internal placeholder system with chainable resolvers
- **ConfigItem**: Load items from YAML with flexible slot formats
- **Slot Formats**: Single slots, ranges (`"0-8"`), comma-separated (`"1,3,5"`), lists

### 🔒 Security First
- GUIHolder-based validation (no PDC overhead on items)
- Plugin reload detection via manager UUID
- Anti-duplication protection
- Automatic drag event cancellation
- Item cloning by default to prevent reference sharing
- Shift-click exploit prevention
- Transient item cleanup on GUI close
- Per-player GUI ownership tracking
- Automatic stale GUI detection and cleanup

### 🔧 Developer Experience
- Comprehensive Javadoc documentation
- Kotlin DSL friendly
- No reflection hacks (except for sound mapping)
- Graceful error handling
- Extensive logging with LoggerUtil

---

## Requirements

### Minimum Requirements
- **Java**: 8 or higher
- **Minecraft Server**: Bukkit/Spigot/Paper 1.16.5+
- **KPlugin Framework**: 3.1.0+

### Optional Dependencies
- **Folia**: For regional threading support (automatically detected)
- **PlaceholderAPI**: Not required - library uses internal placeholders

### Supported Platforms
- ✅ Spigot 1.16.5+
- ✅ Paper 1.16.5+
- ✅ Folia (latest)
- ✅ Purpur (1.16.5+)

---

## Installation & Setup

### 1. Add KPlugin Dependency

Add KPlugin to your project dependencies (see main README for Maven/Gradle configuration).

### 2. Initialize GUIManager

**CRITICAL**: You must initialize `GUIManager` in your plugin's `enable()` method:

```java
package com.example.myplugin;

import dev.khanh.plugin.kplugin.KPlugin;
import dev.khanh.plugin.kplugin.gui.GUIManager;

public class MyPlugin extends KPlugin {
    
    @Override
    public void enable() {
        // Initialize GUI Manager (REQUIRED - must be called first)
        new GUIManager(this);
        
        LoggerUtil.info("GUI Manager initialized successfully!");
        
        // Your other initialization code...
        registerCommands();
        loadConfiguration();
    }
    
    @Override
    public void disable() {
        // Cleanup is automatic - GUIManager handles it
        LoggerUtil.info("Plugin disabled, GUIs cleaned up.");
    }
}
```

### 3. Verify Installation

```java
// Check if GUIManager is initialized
GUIManager manager = GUIManager.getInstance();
if (manager != null) {
    LoggerUtil.info("GUIManager is ready!");
} else {
    LoggerUtil.severe("GUIManager not initialized!");
}
```

---

## Core Concepts

### GUI Lifecycle

1. **Creation**: Use `GUIBuilder` to construct GUI instance
2. **Registration**: GUI automatically registers with `GUIManager` on creation
3. **Opening**: Call `gui.open(player)` - inventory opens, events registered
4. **Interaction**: Player clicks trigger handlers
5. **Closing**: Player closes inventory, cleanup executed
6. **Unregistration**: GUI removed from `GUIManager` tracking

### Event Flow

```
Player Action → Bukkit Event → GUIManager → GUI Instance → Click Handler → Response
```

### GUI Validation (GUIHolder)

The GUI library uses a custom `InventoryHolder` implementation for validation:

```java
// Automatically created when GUI is instantiated
UUID managerUUID = GUIManager.getInstance().getManagerUUID();
GUIHolder holder = new GUIHolder(managerUUID, guiId, this);
Inventory inventory = Bukkit.createInventory(holder, type.getSize(), title);
```

This enables:
- **Manager UUID validation**: Detects if GUI is from previous plugin instance
- **GUI UUID tracking**: Unique identifier per GUI instance
- **Zero item overhead**: No PDC tags on items = better performance
- **Plugin reload detection**: Stale GUIs automatically closed

**How it works:**
1. GUIManager generates unique UUID on initialization
2. Each GUI stores this manager UUID in its holder
3. On interaction, manager UUID is validated
4. If plugin was reloaded (different UUID), GUI is closed
5. Player receives message: "This GUI is no longer valid (plugin was reloaded)"

### Threading Model

```java
// Main thread (Bukkit/Spigot/Paper)
TaskUtil.runSync(() -> { /* ... */ });

// Entity region (Folia)
TaskUtil.runAtEntity(player, () -> { /* ... */ });

// Async thread
TaskUtil.runAsync(() -> { /* ... */ });
```

---

## Quick Start Guide

### Example 1: Simple Menu

```java
import dev.khanh.plugin.kplugin.gui.*;
import dev.khanh.plugin.kplugin.util.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SimpleMenuExample {
    
    public void openMainMenu(Player player) {
        // Create a 3-row chest GUI
        GUI gui = new GUIBuilder(3, "&6&lMain Menu")
            .setViewOnly(true)  // Prevent item manipulation
            
            // Add items
            .setItem(10, createMenuItem(Material.DIAMOND, "&bShop", "&7Click to open shop"))
            .setItem(12, createMenuItem(Material.COMPASS, "&bTeleport", "&7Click to teleport"))
            .setItem(14, createMenuItem(Material.BOOK, "&bInfo", "&7View server info"))
            .setItem(16, createMenuItem(Material.BARRIER, "&cClose", "&7Close this menu"))
            
            // Add click handlers
            .setClickHandler(10, p -> openShop(p))
            .setClickHandler(12, p -> openTeleportMenu(p))
            .setClickHandler(14, p -> showInfo(p))
            .setClickHandler(16, p -> p.closeInventory())
            
            // Fill empty slots with glass pane
            .fillBorder(GUIUtil.createBlackFiller())
            
            // Build the GUI
            .build();
        
        // Open for player
        gui.open(player);
        SoundUtil.playOpenSound(player);
    }
    
    private ItemStack createMenuItem(Material material, String name, String lore) {
        return new ItemStackWrapper(material)
            .setDisplayName(ColorUtil.colorize(name))
            .addLore(ColorUtil.colorize(lore))
            .build();
    }
    
    private void openShop(Player player) {
        player.closeInventory();
        SoundUtil.playClickSound(player);
        player.sendMessage(ColorUtil.colorize("&aOpening shop..."));
        // Open shop GUI
    }
    
    private void openTeleportMenu(Player player) {
        player.closeInventory();
        SoundUtil.playClickSound(player);
        // Open teleport GUI
    }
    
    private void showInfo(Player player) {
        SoundUtil.playSuccessSound(player);
        player.sendMessage(ColorUtil.colorize("&eServer Info:"));
        player.sendMessage(ColorUtil.colorize("&7Players online: " + Bukkit.getOnlinePlayers().size()));
    }
}
```

### Example 2: Configuration-Based GUI

**config.yml:**
```yaml
shop-gui:
  title: "&6&lItem Shop"
  rows: 3
  view-only: true
  
  items:
    diamond-item:
      material: DIAMOND
      display-name: "&b&lDiamond"
      lore:
        - "&7Price: $100"
        - "&7Click to purchase"
      amount: 5
      slot: 10
      enchantments:
        DURABILITY: 1
      flags:
        - HIDE_ENCHANTS
      glow: true
    
    emerald-item:
      material: EMERALD
      display-name: "&a&lEmerald"
      lore:
        - "&7Price: $50"
      slot: 12
    
    border:
      material: BLACK_STAINED_GLASS_PANE
      display-name: " "
      slot: "0-8,18-26"  # Top and bottom rows
    
    close-button:
      material: BARRIER
      display-name: "&cClose"
      slot: 22
```

**Java Code:**
```java
import org.bukkit.configuration.ConfigurationSection;
import dev.khanh.plugin.kplugin.gui.button.ConfigItem;

public class ConfigShopExample {
    
    public void openShop(Player player, ConfigurationSection config) {
        ConfigurationSection shopSection = config.getConfigurationSection("shop-gui");
        
        String title = shopSection.getString("title", "&6Shop");
        int rows = shopSection.getInt("rows", 3);
        boolean viewOnly = shopSection.getBoolean("view-only", true);
        
        GUI gui = new GUIBuilder(rows, title)
            .setViewOnly(viewOnly)
            .build();
        
        // Load items from config
        ConfigurationSection itemsSection = shopSection.getConfigurationSection("items");
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            ConfigItem configItem = ConfigItem.fromConfig(itemSection);
            
            // Build item with placeholders
            ItemStack item = configItem.build(text -> 
                text.replace("{player}", player.getName())
                    .replace("{balance}", getBalance(player))
            );
            
            // Place in all configured slots
            for (int slot : configItem.getSlots()) {
                gui.setItem(slot, item);
                
                // Add click handler for specific items
                if (key.equals("diamond-item")) {
                    gui.setClickHandler(slot, p -> purchaseDiamond(p));
                } else if (key.equals("emerald-item")) {
                    gui.setClickHandler(slot, p -> purchaseEmerald(p));
                } else if (key.equals("close-button")) {
                    gui.setClickHandler(slot, Player::closeInventory);
                }
            }
        }
        
        gui.open(player);
    }
    
    private String getBalance(Player player) {
        // Get from economy plugin
        return "1000";
    }
    
    private void purchaseDiamond(Player player) {
        SoundUtil.playSuccessSound(player);
        player.sendMessage(ColorUtil.colorize("&aPurchased 5 diamonds for $100!"));
    }
    
    private void purchaseEmerald(Player player) {
        SoundUtil.playSuccessSound(player);
        player.sendMessage(ColorUtil.colorize("&aPurchased 1 emerald for $50!"));
    }
}
```

---

## GUI Types

The library provides predefined inventory types:

```java
public enum GUIType {
    CHEST_1_ROW(9, InventoryType.CHEST),      // 9 slots (1x9)
    CHEST_2_ROWS(18, InventoryType.CHEST),    // 18 slots (2x9)
    CHEST_3_ROWS(27, InventoryType.CHEST),    // 27 slots (3x9)
    CHEST_4_ROWS(36, InventoryType.CHEST),    // 36 slots (4x9)
    CHEST_5_ROWS(45, InventoryType.CHEST),    // 45 slots (5x9)
    CHEST_6_ROWS(54, InventoryType.CHEST),    // 54 slots (6x9)
    HOPPER(5, InventoryType.HOPPER),          // 5 slots (1x5)
    DISPENSER(9, InventoryType.DISPENSER);    // 9 slots (3x3)
}
```

### Usage

```java
// Using GUIType enum
GUI gui = new GUIBuilder(GUIType.CHEST_6_ROWS, "Large Menu").build();

// Using row count (only for chest types)
GUI gui = new GUIBuilder(6, "Large Menu").build(); // Same as CHEST_6_ROWS

// Using specific type
GUI hopperGUI = new GUIBuilder(GUIType.HOPPER, "Quick Menu").build();
```

### Slot Layouts

**CHEST_3_ROWS (27 slots):**
```
 0  1  2  3  4  5  6  7  8
 9 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
```

**HOPPER (5 slots):**
```
0  1  2  3  4
```

**DISPENSER (9 slots):**
```
0  1  2
3  4  5
6  7  8
```

---

## GUIBuilder API

### Constructor Options

```java
// Row-based (chest only, 1-6 rows)
new GUIBuilder(int rows, String title)

// Type-based (all inventory types)
new GUIBuilder(GUIType type, String title)
```

### Core Methods

```java
// Item Management
.setItem(int slot, ItemStack item)           // Set item at slot
.setButton(int slot, GUIButton button)       // Set button at slot
.fillEmpty(ItemStack filler)                 // Fill empty slots
.fillBorder(ItemStack border)                // Fill border slots
.fillSlots(List<Integer> slots, ItemStack item) // Fill specific slots

// Click Handlers
.setClickHandler(int slot, Consumer<Player> handler)  // Single slot
.setGlobalClickHandler(BiConsumer<Player, Integer> handler) // All slots

// GUI Configuration
.setViewOnly(boolean viewOnly)               // Prevent item manipulation
.setCloseHandler(Consumer<Player> handler)   // On close callback

// Build
.build()                                     // Create GUI instance
```

### Method Chaining Example

```java
GUI gui = new GUIBuilder(GUIType.CHEST_3_ROWS, "&6&lExample")
    .setViewOnly(true)
    .setItem(0, new ItemStack(Material.DIAMOND))
    .setItem(1, new ItemStack(Material.EMERALD))
    .setClickHandler(0, player -> player.sendMessage("Clicked diamond!"))
    .setClickHandler(1, player -> player.sendMessage("Clicked emerald!"))
    .fillBorder(GUIUtil.createBlackFiller())
    .setCloseHandler(player -> player.sendMessage("Closed!"))
    .build();
```

### Advanced Builder Usage

```java
public GUI createAdvancedGUI(Player player) {
    GUIBuilder builder = new GUIBuilder(6, "&e&lAdvanced Menu");
    
    // Set view-only mode
    builder.setViewOnly(true);
    
    // Add items in a loop
    for (int i = 0; i < 9; i++) {
        builder.setItem(i, new ItemStack(Material.GLASS_PANE));
    }
    
    // Add interactive buttons
    builder.setItem(22, createButton("Confirm", Material.LIME_WOOL));
    builder.setClickHandler(22, p -> {
        p.sendMessage(ColorUtil.colorize("&aConfirmed!"));
        SoundUtil.playSuccessSound(p);
        p.closeInventory();
    });
    
    builder.setItem(24, createButton("Cancel", Material.RED_WOOL));
    builder.setClickHandler(24, p -> {
        p.sendMessage(ColorUtil.colorize("&cCancelled!"));
        SoundUtil.playErrorSound(p);
        p.closeInventory();
    });
    
    // Global click handler (fallback)
    builder.setGlobalClickHandler((p, slot) -> {
        LoggerUtil.info("Player " + p.getName() + " clicked slot " + slot);
    });
    
    // Close handler
    builder.setCloseHandler(p -> {
        LoggerUtil.info("Player " + p.getName() + " closed the GUI");
    });
    
    return builder.build();
}

private ItemStack createButton(String name, Material material) {
    return new ItemStackWrapper(material)
        .setDisplayName(ColorUtil.colorize("&e" + name))
        .addLore(ColorUtil.colorize("&7Click to " + name.toLowerCase()))
        .build();
}
```

---

## ConfigItem System

`ConfigItem` provides a powerful way to define items in YAML configuration files with flexible slot placement options.

### Slot Configuration Formats

#### 1. Single Slot (Integer)

```yaml
item:
  material: DIAMOND
  slot: 13  # Single slot
```

#### 2. Slot Range (String)

```yaml
item:
  material: BLACK_STAINED_GLASS_PANE
  slot: "0-8"  # Fills slots 0 through 8 (inclusive)
```

#### 3. Comma-Separated Slots (String)

```yaml
item:
  material: EMERALD
  slot: "10,12,14,16"  # Four specific slots
```

#### 4. Multiple Slots (List)

```yaml
item:
  material: GOLD_INGOT
  slots:  # List format
    - 0
    - 8
    - 45
    - 53
```

#### 5. Mixed Formats (List)

```yaml
item:
  material: DIAMOND
  slots:
    - 10              # Single slot
    - "12-14"         # Range (12, 13, 14)
    - "20,22,24"      # Comma-separated
```

### Complete ConfigItem Schema

```yaml
example-item:
  # Required
  material: DIAMOND_SWORD
  
  # Slot placement (at least one required)
  slot: 13                    # Single slot
  # OR
  slots:                      # Multiple slots
    - 10
    - "12-14"
    - "20,22,24"
  
  # Display
  display-name: "&b&lDiamond Sword"
  lore:
    - "&7A powerful weapon"
    - "&7Damage: 10"
  amount: 1
  custom-model-data: 12345
  
  # Enchantments
  enchantments:
    SHARPNESS: 5
    UNBREAKING: 3
  
  # Item Flags
  flags:
    - HIDE_ENCHANTS
    - HIDE_ATTRIBUTES
  
  # Visual Effects
  glow: true  # Adds enchantment glint
  
  # Skull (for PLAYER_HEAD material)
  skull-owner: "Notch"        # Player name
  # OR
  skull-texture: "base64..."  # Base64 texture
```

### Java API

#### Loading from Configuration

```java
ConfigurationSection section = config.getConfigurationSection("example-item");
ConfigItem item = ConfigItem.fromConfig(section);

// Get configured slots
List<Integer> slots = item.getSlots();

// Get first slot (for backward compatibility)
Integer firstSlot = item.getSlot();

// Build ItemStack
ItemStack stack = item.build();

// Build with placeholders
ItemStack stack = item.build(text -> 
    text.replace("{player}", player.getName())
        .replace("{price}", "100")
);

// Place in GUI
for (int slot : item.getSlots()) {
    gui.setItem(slot, stack);
}
```

#### Builder Pattern

```java
ConfigItem item = new ConfigItem.Builder()
    .material(Material.DIAMOND)
    .displayName("&bDiamond")
    .addLore("&7Line 1", "&7Line 2")
    .amount(5)
    .slot(10)                    // Add slot
    .slot(11)                    // Add another slot (cumulative)
    .slot(12)
    .addEnchantment(Enchantment.DURABILITY, 3)
    .addFlag(ItemFlag.HIDE_ENCHANTS)
    .glow(true)
    .customModelData(12345)
    .build();

// Or set all slots at once
ConfigItem item2 = new ConfigItem.Builder()
    .material(Material.EMERALD)
    .slots(Arrays.asList(0, 8, 45, 53))  // Replace all slots
    .build();
```

#### Skull Configuration

```java
// Player name
ConfigItem skull1 = new ConfigItem.Builder()
    .material(Material.PLAYER_HEAD)
    .displayName("&eNotch's Head")
    .skullOwner("Notch")
    .slot(13)
    .build();

// Base64 texture
ConfigItem skull2 = new ConfigItem.Builder()
    .material(Material.PLAYER_HEAD)
    .displayName("&eCustom Head")
    .skullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6...")
    .slot(13)
    .build();

// URL (automatically converted to base64)
ConfigItem skull3 = new ConfigItem.Builder()
    .material(Material.PLAYER_HEAD)
    .skullTexture("https://textures.minecraft.net/texture/...")
    .slot(13)
    .build();
```

### Practical Examples

#### Example 1: Shop GUI with Borders

```yaml
shop:
  border:
    material: BLACK_STAINED_GLASS_PANE
    display-name: " "
    slot: "0-8,17-18,26-27,35-36,44-45,53"  # All border slots
  
  diamond:
    material: DIAMOND
    display-name: "&b&lDiamond"
    lore:
      - "&7Price: $100"
    slot: 20
  
  emerald:
    material: EMERALD
    display-name: "&a&lEmerald"
    lore:
      - "&7Price: $50"
    slot: 22
```

#### Example 2: Paginated Item Display

```yaml
items:
  player-head:
    material: PLAYER_HEAD
    display-name: "&e{player_name}"
    lore:
      - "&7Rank: {rank}"
      - "&7Join Date: {join_date}"
    skull-owner: "{player_name}"
    slots:  # Grid layout
      - 10
      - 11
      - 12
      - 19
      - 20
      - 21
      - 28
      - 29
      - 30
```

---

## Animation Framework

The GUI library provides a flexible animation system with multiple animation types.

### Animation Types

1. **FrameAnimation**: Cycle through predefined frames
2. **PulseAnimation**: Pulsing effect on specific slots
3. **TransitionAnimation**: Smooth transitions between states (custom implementation)

### Frame Animation

Cycle through multiple frames at a specified interval.

```java
import dev.khanh.plugin.kplugin.gui.animation.FrameAnimation;

public void createFrameAnimation(GUI gui) {
    // Create animation with 5 ticks per frame (0.25 seconds)
    FrameAnimation animation = new FrameAnimation(gui, 5);
    
    // Frame 0 - Red, White, Blue pattern
    animation.addFrame(frame -> {
        frame.put(0, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        frame.put(1, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        frame.put(2, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
    });
    
    // Frame 1 - White, Blue, Red pattern
    animation.addFrame(frame -> {
        frame.put(0, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        frame.put(1, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
        frame.put(2, new ItemStack(Material.RED_STAINED_GLASS_PANE));
    });
    
    // Frame 2 - Blue, Red, White pattern
    animation.addFrame(frame -> {
        frame.put(0, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
        frame.put(1, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        frame.put(2, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
    });
    
    // Set to loop continuously
    animation.setLoop(true);
    
    // Start animation
    animation.start();
    
    // Stop when GUI closes
    gui.onClose(player -> animation.stop());
}
```

### Pulse Animation

Create a pulsing effect on specific slots.

```java
import dev.khanh.plugin.kplugin.gui.animation.PulseAnimation;

public void createPulseAnimation(GUI gui) {
    // Create pulse animation with 10 tick interval (0.5 seconds)
    PulseAnimation pulse = new PulseAnimation(gui, 10);
    
    // Add slots to pulse
    pulse.addSlot(4);   // Center top
    pulse.addSlot(13);  // Center middle
    pulse.addSlot(22);  // Center bottom
    
    // Start pulsing
    pulse.start();
    
    // Stop when GUI closes
    gui.onClose(player -> pulse.stop());
}
```

### Advanced Animation Example

```java
public class AnimatedLoadingScreen {
    
    public void showLoadingScreen(Player player) {
        GUI gui = new GUIBuilder(3, "&e&lLoading...")
            .setViewOnly(true)
            .build();
        
        // Fill background
        gui.fillEmpty(GUIUtil.createBlackFiller());
        
        // Create loading bar animation
        FrameAnimation loadingBar = new FrameAnimation(gui, 3);
        
        // Frame 0 - Empty bar
        loadingBar.addFrame(frame -> {
            for (int i = 10; i <= 16; i++) {
                frame.put(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        });
        
        // Frame 1 - 25% loaded
        loadingBar.addFrame(frame -> {
            frame.put(10, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
            frame.put(11, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
            for (int i = 12; i <= 16; i++) {
                frame.put(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        });
        
        // Frame 2 - 50% loaded
        loadingBar.addFrame(frame -> {
            for (int i = 10; i <= 13; i++) {
                frame.put(i, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
            }
            for (int i = 14; i <= 16; i++) {
                frame.put(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        });
        
        // Frame 3 - 75% loaded
        loadingBar.addFrame(frame -> {
            for (int i = 10; i <= 15; i++) {
                frame.put(i, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
            }
            frame.put(16, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        });
        
        // Frame 4 - 100% loaded
        loadingBar.addFrame(frame -> {
            for (int i = 10; i <= 16; i++) {
                frame.put(i, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
            }
        });
        
        loadingBar.setLoop(false);
        loadingBar.start();
        
        // Add pulse effect on center
        PulseAnimation centerPulse = new PulseAnimation(gui, 8);
        centerPulse.addSlot(13);
        centerPulse.start();
        
        // Open GUI
        gui.open(player);
        
        // Auto-close after loading completes
        TaskUtil.runSync(() -> {
            player.closeInventory();
            SoundUtil.playSuccessSound(player);
            player.sendMessage(ColorUtil.colorize("&aLoading complete!"));
        }, 100); // 5 seconds (20 ticks per second * 5)
        
        // Cleanup on close
        gui.onClose(p -> {
            loadingBar.stop();
            centerPulse.stop();
        });
    }
}
```

### Animation Lifecycle

```java
// Create
FrameAnimation anim = new FrameAnimation(gui, ticksPerFrame);

// Configure
anim.addFrame(frameConsumer);
anim.setLoop(true/false);

// Control
anim.start();   // Begin animation
anim.stop();    // Stop animation
anim.pause();   // Pause (if implemented)
anim.resume();  // Resume (if implemented)

// Always clean up
gui.onClose(player -> anim.stop());
```

---

## Pagination System

The library provides two pagination modes: **Eager** (pre-loaded) and **Lazy** (on-demand).

### Eager Pagination

Load all items at once and paginate them.

```java
import dev.khanh.plugin.kplugin.gui.pagination.PaginatedGUI;

public class EagerPaginationExample {
    
    public void openPlayerListGUI(Player viewer) {
        // Create paginated GUI
        PaginatedGUI gui = new PaginatedGUI(GUIType.CHEST_6_ROWS, "&6&lOnline Players");
        
        // Convert all online players to ConfigItems
        List<ConfigItem> items = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ConfigItem item = new ConfigItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName("&e" + p.getName())
                .addLore("&7Health: " + p.getHealth() + "/20")
                .addLore("&7Level: " + p.getLevel())
                .addLore("&7Click to teleport")
                .skullOwner(p.getName())
                .build();
            items.add(item);
        }
        
        // Configure pagination
        gui.setItemsPerPage(28);  // 28 items per page (leave room for navigation)
        
        // Set items and open
        gui.setItems(items, viewer);
        gui.open(viewer);
    }
}
```

### Lazy Pagination

Load pages on-demand from database or async source.

```java
import java.util.concurrent.CompletableFuture;

public class LazyPaginationExample {
    
    private final Database database;
    
    public void openDatabaseItemsGUI(Player player) {
        PaginatedGUI gui = new PaginatedGUI(GUIType.CHEST_6_ROWS, "&6&lDatabase Items");
        
        // Configure pagination
        gui.setItemsPerPage(28);
        
        // Set lazy loader
        gui.setLazyLoader(
            page -> CompletableFuture.supplyAsync(() -> {
                // Load from database asynchronously
                List<ItemData> data = database.loadPage(page, 28);
                
                // Convert to ConfigItems
                List<ConfigItem> items = new ArrayList<>();
                for (ItemData itemData : data) {
                    ConfigItem item = new ConfigItem.Builder()
                        .material(Material.valueOf(itemData.getMaterial()))
                        .displayName(itemData.getName())
                        .addLore(itemData.getLore().toArray(new String[0]))
                        .build();
                    items.add(item);
                }
                
                return items;
            }),
            getTotalPages() // Total number of pages
        );
        
        // Open GUI
        gui.open(player);
        
        // Flow: Inventory closes → Items load async → Inventory reopens with new page
    }
    
    private int getTotalPages() {
        int totalItems = database.countTotalItems();
        return (int) Math.ceil(totalItems / 28.0);
    }
}
```

### Pagination Navigation

```java
// PaginatedGUI automatically adds navigation buttons:
// - Slot 48: Previous page (if not on first page)
// - Slot 49: Current page indicator
// - Slot 50: Next page (if not on last page)

// Custom navigation slots (optional)
gui.setPreviousPageSlot(45);
gui.setCurrentPageSlot(49);
gui.setNextPageSlot(53);
```

### Advanced Pagination Example

```java
public class AdvancedPaginationExample {
    
    public void openShopWithCategories(Player player, String category) {
        PaginatedGUI gui = new PaginatedGUI(GUIType.CHEST_6_ROWS, "&6&l" + category + " Shop");
        
        // Load items from config
        ConfigurationSection categorySection = config.getConfigurationSection("shop." + category);
        List<ConfigItem> items = new ArrayList<>();
        
        for (String key : categorySection.getKeys(false)) {
            ConfigurationSection itemSection = categorySection.getConfigurationSection(key);
            ConfigItem item = ConfigItem.fromConfig(itemSection);
            items.add(item);
        }
        
        // Set items per page
        gui.setItemsPerPage(28);
        
        // Add border
        ItemStack border = GUIUtil.createBlackFiller();
        gui.fillBorder(border);
        
        // Add category switcher buttons
        gui.setItem(45, createCategoryButton("Weapons", Material.DIAMOND_SWORD));
        gui.setClickHandler(45, p -> openShopWithCategories(p, "weapons"));
        
        gui.setItem(46, createCategoryButton("Armor", Material.DIAMOND_CHESTPLATE));
        gui.setClickHandler(46, p -> openShopWithCategories(p, "armor"));
        
        gui.setItem(47, createCategoryButton("Tools", Material.DIAMOND_PICKAXE));
        gui.setClickHandler(47, p -> openShopWithCategories(p, "tools"));
        
        // Set items and open
        gui.setItems(items, player);
        gui.open(player);
    }
    
    private ItemStack createCategoryButton(String name, Material material) {
        return new ItemStackWrapper(material)
            .setDisplayName(ColorUtil.colorize("&e" + name))
            .addLore(ColorUtil.colorize("&7Click to view " + name.toLowerCase()))
            .build();
    }
}
```

### Error Handling in Lazy Loading

```java
gui.setLazyLoader(
    page -> CompletableFuture.supplyAsync(() -> {
        try {
            return database.loadPage(page, 28);
        } catch (SQLException e) {
            LoggerUtil.severe("Failed to load page " + page + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return empty list on error
            return new ArrayList<>();
        }
    }),
    totalPages
);

// Optional: Add error handler
gui.setErrorHandler((player, throwable) -> {
    player.sendMessage(ColorUtil.colorize("&cFailed to load page!"));
    SoundUtil.playErrorSound(player);
});
```

---

## Placeholder Resolution

The library includes a built-in placeholder system for dynamic text replacement.

### PlaceholderResolver

```java
import dev.khanh.plugin.kplugin.gui.placeholder.PlaceholderResolver;
import dev.khanh.plugin.kplugin.gui.placeholder.PlaceholderContext;

public class PlaceholderExample {
    
    public PlaceholderResolver createResolver() {
        PlaceholderResolver resolver = new PlaceholderResolver();
        
        // Add custom resolvers
        resolver.addResolver("player", Player::getName);
        
        resolver.addResolver("health", player -> 
            String.valueOf(player.getHealth())
        );
        
        resolver.addResolver("level", player -> 
            String.valueOf(player.getLevel())
        );
        
        resolver.addResolver("balance", player -> 
            String.valueOf(economy.getBalance(player))
        );
        
        resolver.addResolver("rank", player -> 
            permissionSystem.getRank(player)
        );
        
        return resolver;
    }
    
    public void applyPlaceholders(Player player) {
        PlaceholderResolver resolver = createResolver();
        
        // Create context for non-player placeholders
        PlaceholderContext context = new PlaceholderContext()
            .set("server", "MyServer")
            .set("page", "1")
            .set("total_pages", "5")
            .set("date", "2026-01-23");
        
        String template = "&eHello {player}!\n" +
                         "&7Rank: {rank}\n" +
                         "&7Balance: ${balance}\n" +
                         "&7Server: {server}\n" +
                         "&7Page: {page}/{total_pages}";
        
        String result = resolver.apply(player, template, context);
        
        player.sendMessage(ColorUtil.colorize(result));
    }
}
```

### Using with ConfigItem

```java
PlaceholderResolver resolver = new PlaceholderResolver()
    .addResolver("player", Player::getName)
    .addResolver("balance", p -> economy.getBalance(p));

ConfigItem item = ConfigItem.fromConfig(section);

// Build item with placeholders
ItemStack stack = item.build(resolver.asFunction(player));

// Or manually
ItemStack stack = item.build(text -> 
    resolver.apply(player, text, new PlaceholderContext())
);
```

### Chainable Resolvers

```java
PlaceholderResolver baseResolver = new PlaceholderResolver()
    .addResolver("player", Player::getName);

PlaceholderResolver economyResolver = new PlaceholderResolver()
    .addResolver("balance", p -> economy.getBalance(p))
    .addResolver("rank", p -> economy.getRank(p));

// Chain resolvers
PlaceholderResolver combined = baseResolver.chain(economyResolver);

String result = combined.apply(player, "Balance: {balance}", null);
```

### Built-in Placeholders

The library does not include PlaceholderAPI integration by default. All placeholders must be registered manually using `addResolver()`.

**Common pattern:**
```java
PlaceholderResolver resolver = new PlaceholderResolver()
    // Player info
    .addResolver("player", Player::getName)
    .addResolver("uuid", p -> p.getUniqueId().toString())
    .addResolver("world", p -> p.getWorld().getName())
    .addResolver("x", p -> String.valueOf(p.getLocation().getBlockX()))
    .addResolver("y", p -> String.valueOf(p.getLocation().getBlockY()))
    .addResolver("z", p -> String.valueOf(p.getLocation().getBlockZ()))
    
    // Player stats
    .addResolver("health", p -> String.valueOf((int)p.getHealth()))
    .addResolver("max_health", p -> String.valueOf((int)p.getMaxHealth()))
    .addResolver("food", p -> String.valueOf(p.getFoodLevel()))
    .addResolver("level", p -> String.valueOf(p.getLevel()))
    .addResolver("exp", p -> String.valueOf((int)(p.getExp() * 100)) + "%")
    
    // Custom data
    .addResolver("balance", p -> economy.getBalance(p))
    .addResolver("rank", p -> permissions.getRank(p));
```

---

## Sound System

Multi-version sound compatibility with automatic mapping.

### Predefined Sounds

```java
import dev.khanh.plugin.kplugin.util.SoundUtil;

// Click sound (UI button click)
SoundUtil.playClickSound(player);

// Open sound (chest open)
SoundUtil.playOpenSound(player);

// Close sound (chest close)
SoundUtil.playCloseSound(player);

// Navigate sound (experience orb pickup)
SoundUtil.playNavigateSound(player);

// Success sound (level up)
SoundUtil.playSuccessSound(player);

// Error sound (villager no)
SoundUtil.playErrorSound(player);
```

### Custom Sounds

```java
// Modern format (1.9+)
SoundUtil.play(player, "entity.player.levelup", 1.0f, 1.0f);
SoundUtil.play(player, "ui.button.click", 0.5f, 1.2f);

// Legacy format (auto-converts to modern)
SoundUtil.play(player, "LEVEL_UP", 1.0f, 1.0f);
SoundUtil.play(player, "CLICK", 0.5f, 1.2f);

// With custom volume and pitch
SoundUtil.play(player, "block.note_block.pling", 1.0f, 2.0f); // High pitch
SoundUtil.play(player, "entity.wither.spawn", 0.3f, 0.8f);    // Low volume
```

### Sound Configuration

**gui.yml:**
```yaml
sounds:
  enabled: true
  volume: 1.0
  pitch: 1.0
  
  click: "ui.button.click"
  open: "block.chest.open"
  close: "block.chest.close"
  navigate: "entity.experience_orb.pickup"
  error: "entity.villager.no"
  success: "entity.player.levelup"
```

**Usage:**
```java
GUIConfigFile config = new GUIConfigFile(plugin);

if (config.areSoundsEnabled()) {
    SoundUtil.play(player, 
        config.getClickSound(), 
        config.getSoundVolume(), 
        config.getSoundPitch()
    );
}
```

### Sound Patterns

```java
public class SoundPatterns {
    
    // Success pattern
    public void playSuccessPattern(Player player) {
        SoundUtil.play(player, "entity.player.levelup", 1.0f, 1.0f);
        TaskUtil.runSync(() -> {
            SoundUtil.play(player, "entity.experience_orb.pickup", 1.0f, 1.5f);
        }, 5);
    }
    
    // Error pattern
    public void playErrorPattern(Player player) {
        SoundUtil.play(player, "entity.villager.no", 1.0f, 0.8f);
        TaskUtil.runSync(() -> {
            SoundUtil.play(player, "entity.villager.no", 1.0f, 0.6f);
        }, 5);
    }
    
    // Warning pattern
    public void playWarningPattern(Player player) {
        for (int i = 0; i < 3; i++) {
            int delay = i * 10;
            TaskUtil.runSync(() -> {
                SoundUtil.play(player, "block.note_block.bass", 1.0f, 0.5f);
            }, delay);
        }
    }
}
```

---

## Security Features

The GUI library includes multiple security layers to prevent exploits.

### Automatic Protection

1. **GUIHolder Validation**: Manager and GUI UUID verification
2. **Drag Cancellation**: All drag events automatically cancelled
3. **Item Cloning**: Items cloned by default to prevent reference sharing
4. **Shift-Click Prevention**: Blocked in view-only mode
5. **Stale GUI Detection**: Plugin reload detection via manager UUID
6. **Ownership Tracking**: Each player can only interact with their own GUI

### View-Only Mode

```java
GUI gui = new GUIBuilder(3, "View Only")
    .setViewOnly(true)  // Prevents ALL item manipulation
    .build();

// In view-only mode:
// ✅ Players can click items (triggers handlers)
// ❌ Players cannot move items
// ❌ Players cannot take items
// ❌ Players cannot place items
// ❌ Shift-click is blocked
```

### GUIHolder Validation

```java
// Automatic validation (done by GUIManager on click)
public GUI getGUI(Inventory inventory) {
    if (inventory.getHolder() instanceof GUIHolder) {
        GUIHolder holder = (GUIHolder) inventory.getHolder();
        
        // Validate holder belongs to current manager instance
        if (!holder.isValid(managerUUID)) {
            LoggerUtil.warning("Detected stale GUI from previous plugin instance");
            return null; // Triggers GUI close
        }
        
        return holder.getGui();
    }
    return null;
}

// On click event
GUI gui = getGUI(event.getInventory());
if (gui == null && event.getInventory().getHolder() instanceof GUIHolder) {
    // Stale GUI detected
    event.setCancelled(true);
    player.closeInventory();
    player.sendMessage("§cThis GUI is no longer valid (plugin was reloaded).");
}
```

### Anti-Duplication

```java
// Example: Player tries to duplicate items
@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    // GUI library automatically handles this:
    
    // 1. Validate clicked item has correct PDC tags
    if (!isValidItem(event.getCurrentItem(), event.getSlot())) {
        event.setCancelled(true);
        return;
    }
    
    // 2. Cancel if view-only mode
    if (isViewOnly() && event.getClick() != ClickType.LEFT) {
        event.setCancelled(true);
        return;
    }
    
    // 3. Cancel all drag events
    // (handled in separate InventoryDragEvent listener)
}

@EventHandler
public void onInventoryDrag(InventoryDragEvent event) {
    // ALWAYS cancel drag events in GUIs
    event.setCancelled(true);
}
```

### Transient Item Cleanup

```java
// Items are automatically removed when GUI closes
gui.onClose(player -> {
    // Remove any items that might have been placed in player inventory
    for (ItemStack item : player.getInventory().getContents()) {
        if (item != null && isGUIItem(item)) {
            player.getInventory().remove(item);
        }
    }
});
```

### Security Best Practices

```java
public class SecureGUIExample {
    
    public GUI createSecureShop(Player player) {
        GUI gui = new GUIBuilder(6, "&6&lSecure Shop")
            .setViewOnly(true)  // ALWAYS use for shops
            .build();
        
        // Add items
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, shopItem);
        }
        
        // Add click handler with validation
        gui.setGlobalClickHandler((p, slot) -> {
            // Additional validation
            if (!p.equals(player)) {
                LoggerUtil.warning("Player " + p.getName() + 
                    " tried to click " + player.getName() + "'s GUI!");
                p.closeInventory();
                return;
            }
            
            // Process purchase
            processPurchase(p, slot);
        });
        
        // Close handler with cleanup
        gui.setCloseHandler(p -> {
            // Log close
            LoggerUtil.info("Player " + p.getName() + " closed shop GUI");
            
            // Additional cleanup if needed
            cleanupPlayerData(p);
        });
        
        return gui;
    }
    
    private void processPurchase(Player player, int slot) {
        // Validate player still has permission
        if (!player.hasPermission("shop.buy")) {
            SoundUtil.playErrorSound(player);
            player.sendMessage(ColorUtil.colorize("&cNo permission!"));
            return;
        }
        
        // Validate balance
        double price = getPriceForSlot(slot);
        if (economy.getBalance(player) < price) {
            SoundUtil.playErrorSound(player);
            player.sendMessage(ColorUtil.colorize("&cInsufficient funds!"));
            return;
        }
        
        // Process transaction
        economy.withdrawPlayer(player, price);
        giveItem(player, slot);
        
        SoundUtil.playSuccessSound(player);
        player.sendMessage(ColorUtil.colorize("&aPurchase successful!"));
    }
}
```

---

## Configuration

The GUI library can be configured via `gui.yml` in your plugin's data folder.

### GUIConfigFile

```java
import dev.khanh.plugin.kplugin.gui.file.GUIConfigFile;

public class MyPlugin extends KPlugin {
    
    private GUIConfigFile guiConfig;
    
    @Override
    public void enable() {
        new GUIManager(this);
        
        // Load GUI configuration
        guiConfig = new GUIConfigFile(this);
        
        LoggerUtil.info("GUI config loaded!");
    }
    
    public GUIConfigFile getGuiConfig() {
        return guiConfig;
    }
}
```

### Default Configuration

**gui.yml:**
```yaml
gui-version: 1

# Sound settings
sounds:
  enabled: true
  volume: 1.0
  pitch: 1.0
  
  # Sound mappings (modern format)
  click: "ui.button.click"
  open: "block.chest.open"
  close: "block.chest.close"
  navigate: "entity.experience_orb.pickup"
  error: "entity.villager.no"
  success: "entity.player.levelup"

# Pagination settings
pagination:
  items-per-page: 28
  lazy-load-timeout: 5000  # Milliseconds
  
# Security settings
security:
  enable-pdc-validation: true
  enable-drag-cancellation: true
  enable-item-cloning: true
```

### Accessing Configuration

```java
GUIConfigFile config = plugin.getGuiConfig();

// Sound settings
boolean soundsEnabled = config.areSoundsEnabled();
float volume = config.getSoundVolume();
float pitch = config.getSoundPitch();
String clickSound = config.getClickSound();

// Pagination settings
int itemsPerPage = config.getItemsPerPage();
int timeout = config.getLazyLoadTimeout();

// Use in GUI
if (soundsEnabled) {
    SoundUtil.play(player, clickSound, volume, pitch);
}
```

### Custom Configuration Sections

```yaml
# Add custom sections to gui.yml
gui-version: 1

sounds:
  enabled: true
  # ... sound config

custom-guis:
  main-menu:
    title: "&6&lMain Menu"
    rows: 3
    view-only: true
    
  shop:
    title: "&e&lShop"
    rows: 6
    view-only: true
    items-per-page: 28
```

**Access custom sections:**
```java
ConfigurationSection customSection = config.getConfigurationSection("custom-guis.main-menu");
String title = customSection.getString("title");
int rows = customSection.getInt("rows");
boolean viewOnly = customSection.getBoolean("view-only");
```

---

## Folia Compatibility

The GUI library is fully compatible with Folia's regional threading model.

### Automatic Folia Support

```java
// GUIManager automatically detects Folia and uses appropriate scheduler
public void openGUI(Player player, GUI gui) {
    // Uses TaskUtil which handles Folia/Spigot/Paper automatically
    TaskUtil.runAtEntity(player, () -> {
        player.openInventory(gui.getInventory());
    });
}
```

### TaskUtil Integration

```java
// Global region (Spigot/Paper) or entity region (Folia)
TaskUtil.runSync(() -> {
    // Update GUI contents
    gui.setItem(13, newItem);
});

// Entity-specific region (Folia-optimized)
TaskUtil.runAtEntity(player, () -> {
    gui.open(player);
});

// Location-specific region (Folia-optimized)
TaskUtil.runAtLocation(location, () -> {
    // Update nearby GUIs
});

// Async (works on all platforms)
TaskUtil.runAsync(() -> {
    // Load data from database
    List<ConfigItem> items = database.loadItems();
    
    // Switch back to entity region
    TaskUtil.runAtEntity(player, () -> {
        gui.setItems(items, player);
    });
});
```

### Folia Best Practices

```java
public class FoliaGUIExample {
    
    public void openDatabaseGUI(Player player) {
        // 1. Create GUI on entity region
        TaskUtil.runAtEntity(player, () -> {
            GUI gui = new GUIBuilder(6, "&6&lDatabase Items").build();
            
            // 2. Load data asynchronously
            TaskUtil.runAsync(() -> {
                List<ItemData> data = database.loadData();
                
                // 3. Switch back to entity region for GUI updates
                TaskUtil.runAtEntity(player, () -> {
                    for (int i = 0; i < data.size(); i++) {
                        ItemStack item = convertToItem(data.get(i));
                        gui.setItem(i, item);
                    }
                    
                    // 4. Open GUI
                    gui.open(player);
                });
            });
        });
    }
    
    public void updateGUIAsync(Player player, GUI gui) {
        // Update GUI from async task
        TaskUtil.runAsync(() -> {
            ItemStack newItem = generateExpensiveItem();
            
            // Switch to entity region for GUI modification
            TaskUtil.runAtEntity(player, () -> {
                gui.setItem(13, newItem);
            });
        });
    }
}
```

---

## Advanced Examples

### Example 1: Confirmation Dialog

```java
public class ConfirmationDialog {
    
    public void showConfirmation(Player player, String message, Runnable onConfirm) {
        GUI gui = new GUIBuilder(3, "&e&lConfirmation")
            .setViewOnly(true)
            .build();
        
        // Message display
        ItemStack messageItem = new ItemStackWrapper(Material.PAPER)
            .setDisplayName("&e" + message)
            .addLore("&7Click Confirm or Cancel")
            .build();
        gui.setItem(13, messageItem);
        
        // Confirm button (green)
        ItemStack confirmItem = new ItemStackWrapper(Material.LIME_WOOL)
            .setDisplayName("&a&lCONFIRM")
            .addLore("&7Click to confirm")
            .build();
        gui.setItem(11, confirmItem);
        gui.setClickHandler(11, p -> {
            p.closeInventory();
            SoundUtil.playSuccessSound(p);
            onConfirm.run();
        });
        
        // Cancel button (red)
        ItemStack cancelItem = new ItemStackWrapper(Material.RED_WOOL)
            .setDisplayName("&c&lCANCEL")
            .addLore("&7Click to cancel")
            .build();
        gui.setItem(15, cancelItem);
        gui.setClickHandler(15, p -> {
            p.closeInventory();
            SoundUtil.playErrorSound(p);
            p.sendMessage(ColorUtil.colorize("&cCancelled!"));
        });
        
        // Fill border
        gui.fillBorder(GUIUtil.createBlackFiller());
        
        gui.open(player);
    }
}
```

### Example 2: Player Selector

```java
public class PlayerSelector {
    
    public void openPlayerSelector(Player viewer, Consumer<Player> onSelect) {
        PaginatedGUI gui = new PaginatedGUI(GUIType.CHEST_6_ROWS, "&6&lSelect Player");
        
        List<ConfigItem> items = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(viewer)) continue; // Skip self
            
            ConfigItem item = new ConfigItem.Builder()
                .material(Material.PLAYER_HEAD)
                .displayName("&e" + p.getName())
                .addLore("&7Health: " + (int)p.getHealth() + "/20")
                .addLore("&7World: " + p.getWorld().getName())
                .addLore("&7Click to select")
                .skullOwner(p.getName())
                .build();
            items.add(item);
        }
        
        gui.setItemsPerPage(28);
        gui.setItems(items, viewer);
        
        // Add click handlers
        gui.setGlobalClickHandler((clicker, slot) -> {
            ItemStack clicked = gui.getInventory().getItem(slot);
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                if (meta != null && meta.hasOwner()) {
                    Player selected = Bukkit.getPlayer(meta.getOwner().getName());
                    if (selected != null) {
                        clicker.closeInventory();
                        SoundUtil.playSuccessSound(clicker);
                        onSelect.accept(selected);
                    }
                }
            }
        });
        
        gui.open(viewer);
    }
}
```

### Example 3: Animated Loading Screen with Progress

```java
public class LoadingScreen {
    
    public void showLoadingScreen(Player player, CompletableFuture<?> task) {
        GUI gui = new GUIBuilder(3, "&e&lLoading...")
            .setViewOnly(true)
            .build();
        
        gui.fillEmpty(GUIUtil.createBlackFiller());
        
        // Progress bar slots: 10-16
        FrameAnimation loading = new FrameAnimation(gui, 5);
        
        for (int progress = 0; progress <= 7; progress++) {
            final int finalProgress = progress;
            loading.addFrame(frame -> {
                for (int i = 10; i <= 16; i++) {
                    Material mat = (i - 10) < finalProgress ? 
                        Material.LIME_STAINED_GLASS_PANE : 
                        Material.GRAY_STAINED_GLASS_PANE;
                    frame.put(i, new ItemStack(mat));
                }
            });
        }
        
        loading.setLoop(false);
        loading.start();
        
        gui.open(player);
        
        // When task completes
        task.thenRun(() -> {
            TaskUtil.runAtEntity(player, () -> {
                loading.stop();
                player.closeInventory();
                SoundUtil.playSuccessSound(player);
                player.sendMessage(ColorUtil.colorize("&aLoaded successfully!"));
            });
        }).exceptionally(throwable -> {
            TaskUtil.runAtEntity(player, () -> {
                loading.stop();
                player.closeInventory();
                SoundUtil.playErrorSound(player);
                player.sendMessage(ColorUtil.colorize("&cLoading failed!"));
            });
            return null;
        });
        
        gui.onClose(p -> loading.stop());
    }
}
```

### Example 4: Multi-Page Shop with Categories

```java
public class CategoryShop {
    
    private final ConfigurationSection shopConfig;
    
    public void openCategoryShop(Player player, String category) {
        GUI gui = new GUIBuilder(6, "&6&l" + category + " Shop")
            .setViewOnly(true)
            .build();
        
        // Category buttons (bottom row)
        addCategoryButton(gui, 45, "Weapons", Material.DIAMOND_SWORD, player);
        addCategoryButton(gui, 46, "Armor", Material.DIAMOND_CHESTPLATE, player);
        addCategoryButton(gui, 47, "Tools", Material.DIAMOND_PICKAXE, player);
        addCategoryButton(gui, 48, "Food", Material.COOKED_BEEF, player);
        addCategoryButton(gui, 49, "Blocks", Material.STONE, player);
        
        // Close button
        ItemStack closeButton = new ItemStackWrapper(Material.BARRIER)
            .setDisplayName("&cClose")
            .build();
        gui.setItem(53, closeButton);
        gui.setClickHandler(53, Player::closeInventory);
        
        // Load category items
        ConfigurationSection categorySection = shopConfig.getConfigurationSection(category);
        if (categorySection != null) {
            int slot = 0;
            for (String key : categorySection.getKeys(false)) {
                if (slot >= 45) break; // Don't override bottom row
                
                ConfigItem item = ConfigItem.fromConfig(
                    categorySection.getConfigurationSection(key)
                );
                
                double price = categorySection.getDouble(key + ".price", 0.0);
                
                ItemStack stack = item.build(text -> 
                    text.replace("{price}", String.valueOf(price))
                        .replace("{player}", player.getName())
                );
                
                gui.setItem(slot, stack);
                gui.setClickHandler(slot, p -> purchaseItem(p, key, price));
                
                slot++;
            }
        }
        
        gui.open(player);
    }
    
    private void addCategoryButton(GUI gui, int slot, String category, 
                                   Material icon, Player player) {
        ItemStack button = new ItemStackWrapper(icon)
            .setDisplayName("&e" + category)
            .addLore("&7Click to view " + category.toLowerCase())
            .build();
        gui.setItem(slot, button);
        gui.setClickHandler(slot, p -> openCategoryShop(p, category.toLowerCase()));
    }
    
    private void purchaseItem(Player player, String itemKey, double price) {
        if (economy.getBalance(player) >= price) {
            economy.withdrawPlayer(player, price);
            // Give item logic
            SoundUtil.playSuccessSound(player);
            player.sendMessage(ColorUtil.colorize("&aPurchased " + itemKey + "!"));
        } else {
            SoundUtil.playErrorSound(player);
            player.sendMessage(ColorUtil.colorize("&cInsufficient funds!"));
        }
    }
}
```

---

## Best Practices

### 1. Always Initialize GUIManager

```java
@Override
public void enable() {
    // FIRST thing to do
    new GUIManager(this);
    
    // Then other initialization
}
```

### 2. Use View-Only Mode for Shops

```java
// ALWAYS set view-only for shops to prevent duplication
GUI shop = new GUIBuilder(6, "&6Shop")
    .setViewOnly(true)
    .build();
```

### 3. Clean Up Animations

```java
FrameAnimation anim = new FrameAnimation(gui, 5);
anim.start();

// ALWAYS stop animations when GUI closes
gui.onClose(player -> anim.stop());
```

### 4. Handle Async Errors

```java
gui.setLazyLoader(
    page -> CompletableFuture.supplyAsync(() -> {
        try {
            return database.loadPage(page);
        } catch (Exception e) {
            LoggerUtil.severe("Error loading page: " + e.getMessage());
            return new ArrayList<>(); // Return empty list
        }
    }),
    totalPages
);
```

### 5. Use Placeholders Efficiently

```java
// Create resolver ONCE
PlaceholderResolver resolver = new PlaceholderResolver()
    .addResolver("player", Player::getName)
    .addResolver("balance", p -> economy.getBalance(p));

// Reuse for all items
for (ConfigItem item : items) {
    ItemStack built = item.build(resolver.asFunction(player));
}
```

### 6. Validate Player Permissions

```java
gui.setClickHandler(slot, player -> {
    if (!player.hasPermission("shop.buy")) {
        SoundUtil.playErrorSound(player);
        player.sendMessage("§cNo permission!");
        return;
    }
    
    // Process purchase
});
```

### 7. Use TaskUtil for Scheduling

```java
// DON'T use Bukkit scheduler directly
// Bukkit.getScheduler().runTaskLater(plugin, () -> {}, 20L);

// DO use TaskUtil (Folia-compatible)
TaskUtil.runSync(() -> {
    // Update GUI
}, 20);
```

### 8. Clone Items When Needed

```java
// Items are auto-cloned by GUI.setItem()
// But if you're manually handling items:
ItemStack original = someItem;
ItemStack cloned = original.clone(); // Always clone
gui.setItem(slot, cloned);
```

### 9. Log Important Events

```java
gui.setClickHandler(slot, player -> {
    LoggerUtil.info("Player " + player.getName() + " clicked slot " + slot);
    processPurchase(player, slot);
});

gui.setCloseHandler(player -> {
    LoggerUtil.info("Player " + player.getName() + " closed GUI");
});
```

### 10. Use ConfigItem for Maintainability

```java
// DON'T hardcode items in Java
ItemStack item = new ItemStack(Material.DIAMOND);
// ...

// DO use ConfigItem from YAML
ConfigItem item = ConfigItem.fromConfig(config.getConfigurationSection("item"));
```

---

## Troubleshooting

### GUI Not Opening

**Symptom**: Calling `gui.open(player)` does nothing

**Solutions**:
1. Check if `GUIManager` is initialized:
   ```java
   if (GUIManager.getInstance() == null) {
       LoggerUtil.severe("GUIManager not initialized!");
   }
   ```

2. Check console for errors

3. Verify player is online:
   ```java
   if (!player.isOnline()) {
       LoggerUtil.warning("Player is offline!");
   }
   ```

### Items Disappearing

**Symptom**: Items placed in GUI disappear

**Solutions**:
1. Verify items are not null:
   ```java
   if (item != null) {
       gui.setItem(slot, item);
   }
   ```

2. Check slot is within bounds:
   ```java
   if (slot >= 0 && slot < gui.getSize()) {
       gui.setItem(slot, item);
   }
   ```

3. Ensure GUI is not being cleared elsewhere

### Sounds Not Playing

**Symptom**: `SoundUtil.playClickSound(player)` doesn't play sound

**Solutions**:
1. Check `gui.yml`:
   ```yaml
   sounds:
     enabled: true
   ```

2. Verify sound name is correct for your Minecraft version

3. Check player's sound settings (client-side)

4. Use debug logging:
   ```java
   LoggerUtil.info("Playing sound: " + soundName);
   SoundUtil.play(player, soundName, 1.0f, 1.0f);
   ```

### Pagination Not Working

**Symptom**: Pages don't change or items don't load

**Solutions**:
1. Verify items per page is set:
   ```java
   gui.setItemsPerPage(28);
   ```

2. Check total items count:
   ```java
   LoggerUtil.info("Total items: " + items.size());
   ```

3. For lazy loading, check async task:
   ```java
   gui.setLazyLoader(
       page -> CompletableFuture.supplyAsync(() -> {
           LoggerUtil.info("Loading page " + page);
           return database.loadPage(page);
       }),
       totalPages
   );
   ```

### Click Handlers Not Triggering

**Symptom**: Clicking items doesn't execute handler

**Solutions**:
1. Verify handler is set:
   ```java
   gui.setClickHandler(slot, player -> {
       LoggerUtil.info("Handler triggered!");
   });
   ```

2. Check if global handler is overriding:
   ```java
   // Specific handlers take priority over global
   ```

3. Verify slot number is correct:
   ```java
   LoggerUtil.info("Setting handler for slot: " + slot);
   ```

### Memory Leaks

**Symptom**: Server memory usage increases over time

**Solutions**:
1. Always stop animations:
   ```java
   gui.onClose(player -> {
       animation.stop();
   });
   ```

2. Unregister event listeners (GUIManager handles this automatically)

3. Clear large data structures:
   ```java
   gui.setCloseHandler(player -> {
       largeDataList.clear();
   });
   ```

### Folia Compatibility Issues

**Symptom**: Errors on Folia server

**Solutions**:
1. Use `TaskUtil` instead of Bukkit scheduler:
   ```java
   // DON'T
   Bukkit.getScheduler().runTask(plugin, () -> {});
   
   // DO
   TaskUtil.runSync(() -> {});
   ```

2. Use entity regions for player-specific tasks:
   ```java
   TaskUtil.runAtEntity(player, () -> {
       gui.open(player);
   });
   ```

---

## API Reference

### Package Structure

```
dev.khanh.plugin.kplugin.gui/
├── GUI.java                      # Abstract base class
├── GUIBuilder.java               # Fluent builder
├── GUIManager.java               # Singleton manager
├── GUIType.java                  # Inventory types enum
├── animation/
│   ├── Animation.java            # Animation interface
│   ├── AbstractAnimation.java    # Base implementation
│   ├── FrameAnimation.java       # Frame-based animation
│   └── PulseAnimation.java       # Pulsing effect
├── button/
│   ├── GUIButton.java            # Interactive button
│   └── ConfigItem.java           # YAML-based items
├── file/
│   └── GUIConfigFile.java        # gui.yml handler
├── pagination/
│   └── PaginatedGUI.java         # Pagination support
└── placeholder/
    ├── PlaceholderResolver.java  # Placeholder resolver
    └── PlaceholderContext.java   # Context data
```

### Core Classes

#### GUI (Abstract)

```java
public abstract class GUI {
    public abstract void open(@NotNull Player player);
    public abstract void close(@NotNull Player player);
    public void setItem(int slot, ItemStack item);
    public ItemStack getItem(int slot);
    public void setClickHandler(int slot, Consumer<Player> handler);
    public void setGlobalClickHandler(BiConsumer<Player, Integer> handler);
    public void setCloseHandler(Consumer<Player> handler);
    public void onClose(Consumer<Player> handler);
    public Inventory getInventory();
    public int getSize();
    public String getTitle();
    public boolean isViewOnly();
}
```

#### GUIBuilder

```java
public class GUIBuilder {
    public GUIBuilder(int rows, String title);
    public GUIBuilder(GUIType type, String title);
    public GUIBuilder setItem(int slot, ItemStack item);
    public GUIBuilder setButton(int slot, GUIButton button);
    public GUIBuilder setClickHandler(int slot, Consumer<Player> handler);
    public GUIBuilder setGlobalClickHandler(BiConsumer<Player, Integer> handler);
    public GUIBuilder setViewOnly(boolean viewOnly);
    public GUIBuilder setCloseHandler(Consumer<Player> handler);
    public GUIBuilder fillEmpty(ItemStack filler);
    public GUIBuilder fillBorder(ItemStack border);
    public GUIBuilder fillSlots(List<Integer> slots, ItemStack item);
    public GUI build();
}
```

#### ConfigItem

```java
public class ConfigItem {
    public static ConfigItem fromConfig(ConfigurationSection section);
    public ItemStack build();
    public ItemStack build(Function<String, String> placeholderReplacer);
    public List<Integer> getSlots();
    public Integer getSlot();
    
    public static class Builder {
        public Builder material(Material material);
        public Builder displayName(String displayName);
        public Builder addLore(String... lines);
        public Builder amount(int amount);
        public Builder slot(int slot);
        public Builder slots(List<Integer> slots);
        public Builder addEnchantment(Enchantment enchantment, int level);
        public Builder addFlag(ItemFlag flag);
        public Builder glow(boolean glow);
        public Builder customModelData(Integer data);
        public Builder skullOwner(String owner);
        public Builder skullTexture(String texture);
        public ConfigItem build();
    }
}
```

### Full Javadoc

Complete API documentation is available in:
- **Directory**: `docs/` (HTML format)
- **Online**: https://yourusername.github.io/KPlugin/ (if published)

---

## License

This library is part of the KPlugin framework and is subject to the same license.

See [LICENSE](../LICENSE) file for details.

---

## Author & Support

**Author**: KhanhHuynh
- **Discord**: khanhhuynh
- **GitHub**: [@HuynhKhanh1402](https://github.com/HuynhKhanh1402)

**Support**:
- Open an issue on GitHub for bug reports
- Join our Discord for questions
- Contributions welcome via pull requests

---

## Version History

### Version 3.1.0
- Initial GUI library release
- Frame and pulse animations
- Eager and lazy pagination
- ConfigItem with flexible slot formats
- Multi-version sound support
- Full Folia compatibility
- PDC-based security

---

**Happy coding!** 🎨✨
