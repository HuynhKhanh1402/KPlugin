package dev.khanh.plugin.kplugin.gui.test;

import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.gui.animation.FrameAnimation;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import dev.khanh.plugin.kplugin.gui.pagination.Pagination;
import dev.khanh.plugin.kplugin.util.MessageUtil;
import dev.khanh.plugin.kplugin.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Comprehensive test suite for KPlugin GUI System.
 * <p>
 * Demonstrates major features:
 * - Basic GUI creation with GUIBuilder
 * - Slot operations (single, range, multi, row, column)
 * - Click handlers and ClickContext
 * - ItemBuilder usage
 * - Metadata system (slot and GUI-level)
 * - View-only mode
 * - Pagination (eager and async)
 * - FrameAnimation
 * - Fill methods
 * - Disabled slots
 * </p>
 * 
 * Usage: Call openTestMenu(player) to see all test options
 */
public class TestGUI {

    /**
     * Opens the main test menu.
     *
     * @param player the player to show the menu to
     */
    public static void openTestMenu(Player player) {
        GUI gui = GUI.builder(6)
            .title("&6&lGUI System - Test Menu")
            .viewOnly(true)
            .build();

        // Background
        ItemStack bg = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        gui.slots(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53)
            .fill(bg);

        // Test 1: Basic GUI Operations
        gui.slot(10).set(ItemBuilder.of(Material.CRAFTING_TABLE)
                .name("&aTest 1: Basic GUI Operations")
                .lore(
                    "&7Tests GUIBuilder, slot operations,",
                    "&7fill methods, and basic features",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testBasicOperations(ctx.player());
            });

        // Test 2: Click Context
        gui.slot(11).set(ItemBuilder.of(Material.OAK_BUTTON)
                .name("&bTest 2: Click Context")
                .lore(
                    "&7Tests ClickContext features:",
                    "&7- Click types detection",
                    "&7- Context metadata",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testClickContext(ctx.player());
            });

        // Test 3: ItemBuilder
        gui.slot(12).set(ItemBuilder.of(Material.DIAMOND_SWORD)
                .name("&9Test 3: ItemBuilder")
                .lore(
                    "&7Tests ItemBuilder API:",
                    "&7- Enchantments, flags, glow",
                    "&7- Unbreakable items",
                    "",
                    "&eClick to test"
                )
                .enchant(Enchantment.DAMAGE_ALL, 1)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testItemBuilder(ctx.player());
            });

        // Test 4: Metadata System
        gui.slot(13).set(ItemBuilder.of(Material.NAME_TAG)
                .name("&dTest 4: Metadata System")
                .lore(
                    "&7Tests slot and GUI metadata:",
                    "&7- Store/retrieve data",
                    "&7- Persistence within session",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testMetadataSystem(ctx.player());
            });

        // Test 5: Pagination (Eager)
        gui.slot(14).set(ItemBuilder.of(Material.BOOK)
                .name("&eTest 5: Pagination (Eager)")
                .lore(
                    "&7Tests eager pagination:",
                    "&7- Pre-loaded items",
                    "&7- Navigation buttons",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testEagerPagination(ctx.player());
            });

        // Test 6: Frame Animation
        gui.slot(15).set(ItemBuilder.of(Material.BLAZE_POWDER)
                .name("&cTest 6: Frame Animation")
                .lore(
                    "&7Tests diff-based animations:",
                    "&7- Multiple patterns",
                    "&7- Play/pause/stop",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testFrameAnimation(ctx.player());
            });

        // Test 7: Advanced Features
        gui.slot(16).set(ItemBuilder.of(Material.REDSTONE)
                .name("&5Test 7: Advanced Features")
                .lore(
                    "&7Tests advanced features:",
                    "&7- Disabled slots",
                    "&7- Global handlers",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testAdvancedFeatures(ctx.player());
            });

        // Test 8: Slot Handles
        gui.slot(19).set(ItemBuilder.of(Material.COMPARATOR)
                .name("&3Test 8: Slot Handles")
                .lore(
                    "&7Tests SlotHandle operations:",
                    "&7- Single, range, multi",
                    "&7- Row and column operations",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testSlotHandles(ctx.player());
            });

        // Test 9: Fill Methods
        gui.slot(20).set(ItemBuilder.of(Material.PURPLE_STAINED_GLASS_PANE)
                .name("&5Test 9: Fill Methods")
                .lore(
                    "&7Tests various fill methods:",
                    "&7- fillBorder, fillEmpty",
                    "&7- fillAlternating",
                    "",
                    "&eClick to test"
                )
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testFillMethods(ctx.player());
            });

        // Info item
        gui.slot(49).set(ItemBuilder.of(Material.NETHER_STAR)
                .name("&e&lKPlugin GUI System v4.0")
                .lore(
                    "&7Framework: &fKPlugin",
                    "&7Tests: &f9 available",
                    "",
                    "&7Select a test above to begin"
                )
                .build());

        gui.open(player);
    }

    // ===== TEST 1: BASIC GUI OPERATIONS =====

    public static void testBasicOperations(Player player) {
        GUI gui = GUI.builder(5)
            .title("&aBasic GUI Operations")
            .viewOnly(false)
            .onOpen(p -> MessageUtil.sendMessageWithPrefix(p, "&aGUI opened!"))
            .onClose(p -> MessageUtil.sendMessageWithPrefix(p, "&cGUI closed!"))
            .build();

        // Border
        ItemStack border = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        gui.slotRange(0, 8).fill(border);
        gui.slotRange(36, 44).fill(border);
        gui.slots(9, 18, 27).fill(border);
        gui.slots(17, 26, 35).fill(border);

        // Center item
        gui.slot(22).set(ItemBuilder.of(Material.EMERALD)
                .name("&a&lCenter Item")
                .lore("&7This is the center of the GUI")
                .build())
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&aYou clicked the center!"));

        // Corner items
        int[] corners = {10, 16, 28, 34};
        for (int i = 0; i < corners.length; i++) {
            final int corner = i + 1;
            gui.slot(corners[i]).set(ItemBuilder.of(Material.GOLD_INGOT)
                    .name("&6Corner " + corner)
                    .build())
                .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&6Clicked corner " + corner + "!"));
        }

        // Close button
        gui.slot(40).set(ItemBuilder.of(Material.BARRIER)
                .name("&cClose")
                .build())
            .onClick(ctx -> {
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&cClosing...");
                ctx.close();
            });

        gui.open(player);
    }

    // ===== TEST 2: CLICK CONTEXT =====

    public static void testClickContext(Player player) {
        GUI gui = GUI.builder(4)
            .title("&bClick Context Test")
            .viewOnly(true)
            .build();

        // Background
        gui.slotRange(0, 35).fill(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

        // Click type detection
        gui.slot(11).set(ItemBuilder.of(Material.STONE_BUTTON)
                .name("&eClick Type Detector")
                .lore(
                    "&7Try different click types:",
                    "&7- Left click",
                    "&7- Right click",
                    "&7- Shift click"
                )
                .build())
            .onClick(ctx -> {
                String clickType = ctx.clickType().name();
                if (ctx.isLeftClick()) {
                    MessageUtil.sendMessageWithPrefix(ctx.player(), "&aLeft click detected!");
                } else if (ctx.isRightClick()) {
                    MessageUtil.sendMessageWithPrefix(ctx.player(), "&bRight click detected!");
                } else if (ctx.isShiftClick()) {
                    MessageUtil.sendMessageWithPrefix(ctx.player(), "&eShift click detected!");
                } else {
                    MessageUtil.sendMessageWithPrefix(ctx.player(), "&dClick type: " + clickType);
                }
            });

        // Context metadata
        gui.slot(13).set(ItemBuilder.of(Material.CHEST)
                .name("&dContext Metadata")
                .lore(
                    "&7Stores click count in",
                    "&7context metadata",
                    "",
                    "&7Clicks: &e0"
                )
                .build())
            .onClick(ctx -> {
                int clicks = ctx.getMeta("clicks", 0) + 1;
                ctx.setMeta("clicks", clicks);
                
                ItemStack item = ItemBuilder.of(Material.CHEST)
                    .name("&dContext Metadata")
                    .lore(
                        "&7Stores click count in",
                        "&7context metadata",
                        "",
                        "&7Clicks: &e" + clicks
                    )
                    .build();
                ctx.gui().slot(13).set(item);
                
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&dClick count: &e" + clicks);
            });

        // Sound helper
        gui.slot(15).set(ItemBuilder.of(Material.NOTE_BLOCK)
                .name("&6Sound Test")
                .lore("&7Tests playSound method")
                .build())
            .onClick(ctx -> {
                ctx.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&6Played sound!");
            });

        // Back button
        gui.slot(31).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }

    // ===== TEST 3: ITEM BUILDER =====

    public static void testItemBuilder(Player player) {
        GUI gui = GUI.builder(4)
            .title("&9ItemBuilder Test")
            .viewOnly(true)
            .build();

        // Background
        gui.slotRange(0, 35).fill(ItemBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(" ").build());

        // Enchanted item
        gui.slot(10).set(ItemBuilder.of(Material.DIAMOND_SWORD)
                .name("&b&lEnchanted Sword")
                .lore("&7Sharpness V", "&7Fire Aspect II")
                .enchant(Enchantment.DAMAGE_ALL, 5)
                .enchant(Enchantment.FIRE_ASPECT, 2)
                .build());

        // Glowing item
        gui.slot(12).set(ItemBuilder.of(Material.STICK)
                .name("&e&lGlowing Stick")
                .lore("&7Uses glow() method")
                .glow()
                .build());

        // Unbreakable item
        gui.slot(14).set(ItemBuilder.of(Material.IRON_PICKAXE)
                .name("&7&lUnbreakable Pickaxe")
                .lore("&7Unbreakable: &atrue")
                .unbreakable(true)
                .flags(ItemFlag.HIDE_UNBREAKABLE)
                .build());

        // Multiple flags
        gui.slot(16).set(ItemBuilder.of(Material.GOLDEN_APPLE)
                .name("&6Clean Golden Apple")
                .lore("&7All flags hidden")
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                .build());

        // Amount
        gui.slot(20).set(ItemBuilder.of(Material.DIAMOND)
                .name("&b&lDiamond Stack")
                .lore("&7Amount: &f32")
                .amount(32)
                .build());

        // Back button
        gui.slot(31).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }

    // ===== TEST 4: METADATA SYSTEM =====

    public static void testMetadataSystem(Player player) {
        GUI gui = GUI.builder(4)
            .title("&dMetadata System")
            .viewOnly(true)
            .build();

        // Background
        gui.slotRange(0, 35).fill(ItemBuilder.of(Material.PINK_STAINED_GLASS_PANE).name(" ").build());

        // Slot metadata - String
        gui.slot(10).set(ItemBuilder.of(Material.NAME_TAG)
                .name("&aSlot Metadata: String")
                .lore("&7Click to see stored name")
                .build())
            .setMeta("playerName", player.getName())
            .onClick(ctx -> {
                String name = ctx.gui().slot(10).getMeta("playerName");
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&aStored name: &f" + name);
            });

        // Slot metadata - Integer
        gui.slot(12).set(ItemBuilder.of(Material.GOLD_NUGGET)
                .name("&6Slot Metadata: Integer")
                .lore("&7Click to see stored price")
                .build())
            .setMeta("price", 100)
            .onClick(ctx -> {
                Integer price = ctx.gui().slot(12).getMeta("price");
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&6Price: &e$" + price);
            });

        // Slot metadata - Boolean
        gui.slot(14).set(ItemBuilder.of(Material.LEVER)
                .name("&eSlot Metadata: Boolean")
                .lore("&7Click to toggle")
                .build())
            .setMeta("enabled", true)
            .onClick(ctx -> {
                Boolean enabled = ctx.gui().slot(14).getMeta("enabled");
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&eEnabled: &f" + enabled);
                ctx.gui().slot(14).setMeta("enabled", !enabled);
            });

        // GUI metadata
        gui.setMeta("guiId", UUID.randomUUID().toString());
        gui.setMeta("createdAt", System.currentTimeMillis());
        gui.setMeta("owner", player.getName());

        gui.slot(16).set(ItemBuilder.of(Material.BOOK)
                .name("&bGUI Metadata")
                .lore("&7Click to see GUI metadata")
                .build())
            .onClick(ctx -> {
                String id = ctx.gui().getMeta("guiId");
                Long timestamp = ctx.gui().getMeta("createdAt");
                String owner = ctx.gui().getMeta("owner");
                
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&bGUI ID: &f" + id.substring(0, 8) + "...");
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&bCreated: &f" + timestamp);
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&bOwner: &f" + owner);
            });

        // Back button
        gui.slot(31).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }

    // ===== TEST 5: PAGINATION (EAGER) =====

    public static void testEagerPagination(Player player) {
        GUI gui = GUI.builder(6)
            .title("&ePagination - Eager")
            .viewOnly(true)
            .build();

        // Generate test items
        List<Material> materials = Arrays.asList(
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.COAL, Material.REDSTONE, Material.LAPIS_LAZULI, Material.QUARTZ,
            Material.NETHERITE_INGOT, Material.NETHERITE_SCRAP, Material.IRON_NUGGET,
            Material.GLOWSTONE_DUST, Material.PRISMARINE_SHARD, Material.ENDER_PEARL,
            Material.BLAZE_ROD, Material.GHAST_TEAR, Material.MAGMA_CREAM,
            Material.SLIME_BALL, Material.SPIDER_EYE, Material.BONE,
            Material.GUNPOWDER, Material.RABBIT_FOOT, Material.PHANTOM_MEMBRANE
        );

        // Content slots (center area)
        int[] contentSlots = IntStream.rangeClosed(10, 34)
            .filter(i -> i % 9 != 0 && i % 9 != 8)
            .toArray();

        // Create pagination
        Pagination<Material> pagination = Pagination.<Material>create(gui, contentSlots)
            .items(materials)
            .itemRenderer(mat -> ItemBuilder.of(mat)
                .name("&e" + mat.name())
                .lore("&7Click for info")
                .build())
            .onItemClick((mat, ctx) -> {
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&eYou clicked: &f" + mat.name());
                ctx.playSound(Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            })
            .previousButton(48, 
                ItemBuilder.of(Material.ARROW).name("&aPrevious Page").build(),
                ItemBuilder.of(Material.BARRIER).name("&cNo Previous Page").build())
            .nextButton(50,
                ItemBuilder.of(Material.ARROW).name("&aNext Page").build(),
                ItemBuilder.of(Material.BARRIER).name("&cNo Next Page").build())
            .pageInfoButton(49, info -> ItemBuilder.of(Material.PAPER)
                .name("&6Page " + (info.currentPage + 1) + "/" + info.totalPages)
                .build())
            .onPageChange((page, p) -> {
                SoundUtil.playNavigateSound(p);
                MessageUtil.sendMessageWithPrefix(p, "&ePage: &f" + (page + 1));
            })
            .build();

        // Back button
        gui.slot(45).set(ItemBuilder.of(Material.BARRIER)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        pagination.render(player);
        gui.open(player);
    }

    // ===== TEST 6: FRAME ANIMATION =====

    public static void testFrameAnimation(Player player) {
        GUI gui = GUI.builder(5)
            .title("&cFrame Animation")
            .viewOnly(true)
            .build();

        // Background
        gui.slotRange(0, 44).fill(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

        // Create wave animation
        FrameAnimation wave = FrameAnimation.create(gui)
            .interval(5) // 5 ticks
            .loop(true);

        // Add wave frames
        for (int i = 0; i < 7; i++) {
            final int pos = i;
            wave.addFrame(frame -> {
                frame.put(10 + pos, ItemBuilder.of(Material.CYAN_STAINED_GLASS_PANE)
                    .name("&b~Wave~")
                    .build());
            });
        }

        // Create pulse animation
        FrameAnimation pulse = FrameAnimation.create(gui)
            .interval(10)
            .loop(true);

        Material[] colors = {Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
                             Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE};
        for (Material color : colors) {
            pulse.addFrame(frame -> {
                frame.put(22, ItemBuilder.of(color).name("&ePulse").build());
            });
        }

        // Control buttons
        gui.slot(36).set(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                .name("&aStart Animations")
                .build())
            .onClick(ctx -> {
                wave.start();
                pulse.start();
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&aAnimations started!");
            });

        gui.slot(37).set(ItemBuilder.of(Material.ORANGE_STAINED_GLASS_PANE)
                .name("&6Pause Animations")
                .build())
            .onClick(ctx -> {
                wave.pause();
                pulse.pause();
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&6Animations paused!");
            });

        gui.slot(38).set(ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                .name("&cStop Animations")
                .build())
            .onClick(ctx -> {
                wave.stop();
                pulse.stop();
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&cAnimations stopped!");
            });

        // Back button
        gui.slot(40).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                wave.stop();
                pulse.stop();
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }

    // ===== TEST 7: ADVANCED FEATURES =====

    public static void testAdvancedFeatures(Player player) {
        GUI gui = GUI.builder(5)
            .title("&5Advanced Features")
            .viewOnly(true)
            .onOpen(p -> MessageUtil.sendMessageWithPrefix(p, "&5GUI opened!"))
            .onClose(p -> MessageUtil.sendMessageWithPrefix(p, "&5GUI closed!"))
            .build();

        // Background
        gui.slotRange(0, 44).fill(ItemBuilder.of(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());

        // Disabled slot
        gui.slot(10).set(ItemBuilder.of(Material.BARRIER)
                .name("&cDisabled Slot")
                .lore("&7This slot is disabled")
                .build())
            .disable("&cThis slot is disabled!");

        // Toggle button
        gui.slot(12).set(ItemBuilder.of(Material.LEVER)
                .name("&eToggle Slot 14")
                .lore("&7Current: &cDisabled")
                .build())
            .onClick(ctx -> {
                Boolean enabledMeta = ctx.gui().slot(14).getMeta("enabled");
                boolean enabled = enabledMeta != null ? enabledMeta : false;
                enabled = !enabled;
                ctx.gui().slot(14).setMeta("enabled", enabled);
                
                if (enabled) {
                    ctx.gui().slot(14).enable();
                    MessageUtil.sendMessageWithPrefix(ctx.player(), "&aSlot 14 enabled!");
                } else {
                    ctx.gui().slot(14).disable("&cSlot 14 is disabled!");
                    MessageUtil.sendMessageWithPrefix(ctx.player(), "&cSlot 14 disabled!");
                }
                
                ctx.gui().slot(12).set(ItemBuilder.of(Material.LEVER)
                    .name("&eToggle Slot 14")
                    .lore("&7Current: " + (enabled ? "&aEnabled" : "&cDisabled"))
                    .build());
            });

        // Toggleable slot
        gui.slot(14).set(ItemBuilder.of(Material.DIAMOND)
                .name("&bToggleable Slot")
                .lore("&7Use lever to enable/disable")
                .build())
            .setMeta("enabled", false)
            .disable("&cSlot 14 is disabled!")
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&bYou clicked the enabled slot!"));

        // Global click handler
        gui.onGlobalClick(ctx -> {
            if (ctx.slot() >= 27 && ctx.slot() <= 35) {
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&dGlobal handler: slot " + ctx.slot());
            }
        });

        // Info
        gui.slot(22).set(ItemBuilder.of(Material.BOOK)
                .name("&e&lInfo")
                .lore(
                    "&7This GUI demonstrates:",
                    "&7- Disabled slots",
                    "&7- Enable/disable at runtime",
                    "&7- Global click handlers"
                )
                .build());

        // Back button
        gui.slot(40).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }

    // ===== TEST 8: SLOT HANDLES =====

    public static void testSlotHandles(Player player) {
        GUI gui = GUI.builder(6)
            .title("&3Slot Handles Test")
            .viewOnly(true)
            .build();

        // Single slot
        gui.slot(4).set(ItemBuilder.of(Material.DIAMOND)
                .name("&bSingle Slot")
                .lore("&7Set via slot()")
                .build())
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&bSingle slot clicked!"));

        // Range of slots
        gui.slotRange(10, 16).fill(ItemBuilder.of(Material.EMERALD)
                .name("&aRange Slot")
                .lore("&7Set via slotRange()")
                .build())
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&aRange slot " + ctx.slot() + "!"));

        // Multiple specific slots
        gui.slots(20, 22, 24).fill(ItemBuilder.of(Material.GOLD_INGOT)
                .name("&6Multi Slot")
                .lore("&7Set via slots()")
                .build())
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&6Multi slot " + ctx.slot() + "!"));

        // Row
        gui.row(3).fill(ItemBuilder.of(Material.IRON_INGOT)
                .name("&7Row 3")
                .lore("&7Set via row()")
                .build())
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&7Row slot " + ctx.slot() + "!"));

        // Column
        gui.column(8).fill(ItemBuilder.of(Material.REDSTONE)
                .name("&cColumn 8")
                .lore("&7Set via column()")
                .build())
            .onClick(ctx -> MessageUtil.sendMessageWithPrefix(ctx.player(), "&cColumn slot " + ctx.slot() + "!"));

        // Back button
        gui.slot(49).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }

    // ===== TEST 9: FILL METHODS =====

    public static void testFillMethods(Player player) {
        GUI gui = GUI.builder(6)
            .title("&5Fill Methods Test")
            .viewOnly(true)
            .build();

        // Fill border
        gui.fillBorder(ItemBuilder.of(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());

        // Checkerboard pattern in center
        ItemStack white = ItemBuilder.of(Material.WHITE_STAINED_GLASS_PANE).name(" ").build();
        ItemStack black = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        
        for (int row = 1; row < 5; row++) {
            for (int col = 1; col < 8; col++) {
                int slot = row * 9 + col;
                gui.slot(slot).set((row + col) % 2 == 0 ? white : black);
            }
        }

        // Center info
        gui.slot(22).set(ItemBuilder.of(Material.NETHER_STAR)
                .name("&e&lFill Methods")
                .lore(
                    "&7Border: &5Purple pane",
                    "&7Pattern: &fCheckerboard",
                    "",
                    "&7Methods used:",
                    "&7- fillBorder()",
                    "&7- Custom pattern"
                )
                .build());

        // Clear button
        gui.slot(49).set(ItemBuilder.of(Material.SPONGE)
                .name("&eClear All")
                .build())
            .onClick(ctx -> {
                ctx.gui().clear();
                MessageUtil.sendMessageWithPrefix(ctx.player(), "&eGUI cleared!");
                SoundUtil.playSuccessSound(ctx.player());
            });

        // Refill button
        gui.slot(50).set(ItemBuilder.of(Material.PAINTING)
                .name("&aRefill")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                testFillMethods(ctx.player());
            });

        // Back button
        gui.slot(48).set(ItemBuilder.of(Material.ARROW)
                .name("&cBack to Menu")
                .build())
            .onClick(ctx -> {
                SoundUtil.playClickSound(ctx.player());
                openTestMenu(ctx.player());
            });

        gui.open(player);
    }
}
