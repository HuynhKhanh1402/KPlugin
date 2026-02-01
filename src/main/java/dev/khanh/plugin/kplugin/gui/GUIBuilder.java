package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for creating GUI instances.
 *
 * @see GUI
 */
public final class GUIBuilder {
    
    private final int rows;
    private String title = "Menu";
    private boolean viewOnly = true;
    private Consumer<ClickContext> globalClickHandler;
    private Consumer<Player> closeHandler;
    private Consumer<Player> openHandler;
    
    // Pre-build configurations
    private final List<SlotConfig> slotConfigs = new ArrayList<>();
    private ItemStack borderItem;
    private ItemStack fillItem;
    
    /**
     * Creates a GUI builder.
     *
     * @param rows the number of rows (1-6)
     */
    GUIBuilder(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6, got: " + rows);
        }
        this.rows = rows;
    }
    
    /**
     * Creates a GUIBuilder from an existing GUI.
     * <p>
     * Copies all configuration including title, view-only mode, handlers,
     * items, and slot configurations. This is useful for creating modified
     * copies of existing GUIs.
     * </p>
     * <p>
     * <b>Note:</b> This method captures the current state of the GUI.
     * Slot items are cloned, but handlers are referenced directly.
     * </p>
     *
     * @param gui the source GUI
     * @return a new GUIBuilder with the GUI's configuration
     */
    @NotNull
    public static GUIBuilder from(@NotNull GUI gui) {
        GUIBuilder builder = new GUIBuilder(gui.getRows());
        
        // Copy basic properties
        builder.title(gui.getTitle());
        builder.viewOnly(gui.isViewOnly());
        
        // Copy handlers (these are referenced, not cloned)
        if (gui.globalClickHandler != null) {
            builder.onGlobalClick(gui.globalClickHandler);
        }
        if (gui.closeHandler != null) {
            builder.onClose(gui.closeHandler);
        }
        if (gui.openHandler != null) {
            builder.onOpen(gui.openHandler);
        }
        
        // Copy all slot configurations
        gui.ensureInitialized();
        for (int slot = 0; slot < gui.getSize(); slot++) {
            ItemStack item = gui.getSlotItem(slot);
            
            // Only add slots that have items
            if (item != null && !item.getType().isAir()) {
                Consumer<ClickContext> handler = gui.getSlotClickHandler(slot);
                String disabledMsg = gui.getDisabledMessage(slot);
                
                if (disabledMsg != null) {
                    // Disabled slot
                    builder.slotConfigs.add(new SlotConfig(slot, item.clone(), null, disabledMsg));
                } else if (handler != null) {
                    // Slot with click handler
                    builder.slotConfigs.add(new SlotConfig(slot, item.clone(), handler, null));
                } else {
                    // Regular slot
                    builder.slotConfigs.add(new SlotConfig(slot, item.clone(), null, null));
                }
            }
        }
        
        return builder;
    }
    
    /**
     * Sets the title.
     *
     * @param title the title (supports &amp; codes)
     * @return this builder
     */
    @NotNull
    public GUIBuilder title(@NotNull String title) {
        this.title = title;
        return this;
    }
    
    /**
     * Sets whether the GUI is view-only.
     *
     * @param viewOnly true for view-only
     * @return this builder
     */
    @NotNull
    public GUIBuilder viewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
        return this;
    }
    
    /**
     * Sets a global click handler.
     *
     * @param handler the handler
     * @return this builder
     */
    @NotNull
    public GUIBuilder onGlobalClick(@Nullable Consumer<ClickContext> handler) {
        this.globalClickHandler = handler;
        return this;
    }
    
    /**
     * Sets the close handler.
     *
     * @param handler the handler
     * @return this builder
     */
    @NotNull
    public GUIBuilder onClose(@Nullable Consumer<Player> handler) {
        this.closeHandler = handler;
        return this;
    }
    
    /**
     * Sets the open handler.
     *
     * @param handler the handler
     * @return this builder
     */
    @NotNull
    public GUIBuilder onOpen(@Nullable Consumer<Player> handler) {
        this.openHandler = handler;
        return this;
    }
    
    /**
     * Configures a slot with an item.
     *
     * @param slot the slot index
     * @param item the item
     * @return this builder
     */
    @NotNull
    public GUIBuilder slot(int slot, @NotNull ItemStack item) {
        slotConfigs.add(new SlotConfig(slot, item, null, null));
        return this;
    }
    
    /**
     * Configures a slot with an ItemBuilder.
     *
     * @param slot the slot index
     * @param builder the item builder
     * @return this builder
     */
    @NotNull
    public GUIBuilder slot(int slot, @NotNull ItemBuilder builder) {
        return slot(slot, builder.build());
    }
    
    /**
     * Configures a slot with an item and click handler.
     *
     * @param slot the slot index
     * @param item the item
     * @param handler the click handler
     * @return this builder
     */
    @NotNull
    public GUIBuilder slot(int slot, @NotNull ItemStack item, @NotNull Consumer<ClickContext> handler) {
        slotConfigs.add(new SlotConfig(slot, item, handler, null));
        return this;
    }
    
    /**
     * Configures a slot with an ItemBuilder and click handler.
     *
     * @param slot the slot index
     * @param builder the item builder
     * @param handler the click handler
     * @return this builder
     */
    @NotNull
    public GUIBuilder slot(int slot, @NotNull ItemBuilder builder, @NotNull Consumer<ClickContext> handler) {
        return slot(slot, builder.build(), handler);
    }
    
    /**
     * Configures a disabled slot.
     *
     * @param slot the slot index
     * @param item the item
     * @param disabledMessage the message shown when clicked
     * @return this builder
     */
    @NotNull
    public GUIBuilder disabledSlot(int slot, @NotNull ItemStack item, @NotNull String disabledMessage) {
        slotConfigs.add(new SlotConfig(slot, item, null, disabledMessage));
        return this;
    }
    
    /**
     * Configures a disabled slot with an ItemBuilder.
     *
     * @param slot the slot index
     * @param builder the item builder
     * @param disabledMessage the message shown when clicked
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder disabledSlot(int slot, @NotNull ItemBuilder builder, @NotNull String disabledMessage) {
        return disabledSlot(slot, builder.build(), disabledMessage);
    }
    
    /**
     * Fills the border with an item.
     *
     * @param item the border item
     * @return this builder
     */
    @NotNull
    public GUIBuilder fillBorder(@NotNull ItemStack item) {
        this.borderItem = item;
        return this;
    }
    
    /**
     * Fills the border with an ItemBuilder.
     *
     * @param builder the item builder
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillBorder(@NotNull ItemBuilder builder) {
        return fillBorder(builder.build());
    }
    
    /**
     * Fills all empty slots with an item.
     *
     * @param item the filler item
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillEmpty(@NotNull ItemStack item) {
        this.fillItem = item;
        return this;
    }
    
    /**
     * Fills all empty slots with an ItemBuilder.
     *
     * @param builder the item builder
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillEmpty(@NotNull ItemBuilder builder) {
        return fillEmpty(builder.build());
    }
    
    /**
     * Removes a slot configuration at the specified index.
     *
     * @param slot the slot index to remove
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder removeSlot(int slot) {
        slotConfigs.removeIf(config -> config.slot == slot);
        return this;
    }
    
    /**
     * Removes all slot configurations in the specified range (inclusive).
     *
     * @param start the start slot
     * @param end the end slot
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder removeSlotRange(int start, int end) {
        slotConfigs.removeIf(config -> config.slot >= start && config.slot <= end);
        return this;
    }
    
    /**
     * Removes all slot configurations.
     *
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder clearSlots() {
        slotConfigs.clear();
        return this;
    }
    
    /**
     * Creates a copy of this builder.
     * <p>
     * Creates a new GUIBuilder with the same configuration.
     * Slot items are cloned, but handlers are referenced directly.
     * </p>
     *
     * @return a new GUIBuilder copy
     */
    @NotNull
    public GUIBuilder copy() {
        GUIBuilder copy = new GUIBuilder(rows);
        copy.title = this.title;
        copy.viewOnly = this.viewOnly;
        copy.globalClickHandler = this.globalClickHandler;
        copy.closeHandler = this.closeHandler;
        copy.openHandler = this.openHandler;
        copy.borderItem = this.borderItem != null ? this.borderItem.clone() : null;
        copy.fillItem = this.fillItem != null ? this.fillItem.clone() : null;
        
        // Deep copy slot configs
        for (SlotConfig config : this.slotConfigs) {
            copy.slotConfigs.add(new SlotConfig(
                config.slot,
                config.item.clone(),
                config.clickHandler,
                config.disabledMessage
            ));
        }
        
        return copy;
    }
    
    /**
     * Builds and returns the configured GUI.
     *
     * @return the new GUI instance
     */
    @NotNull
    public GUI build() {
        GUI gui = new GUI(rows, title);
        gui.setViewOnly(viewOnly);
        
        if (globalClickHandler != null) {
            gui.onGlobalClick(globalClickHandler);
        }
        if (closeHandler != null) {
            gui.onClose(closeHandler);
        }
        if (openHandler != null) {
            gui.onOpen(openHandler);
        }
        
        // Apply border first
        if (borderItem != null) {
            gui.fillBorder(borderItem);
        }
        
        // Apply slot configurations
        for (SlotConfig config : slotConfigs) {
            gui.slot(config.slot).set(config.item);
            if (config.clickHandler != null) {
                gui.slot(config.slot).onClick(config.clickHandler);
            }
            if (config.disabledMessage != null) {
                gui.slot(config.slot).disable(config.disabledMessage);
            }
        }
        
        // Apply fill last (only empty slots)
        if (fillItem != null) {
            gui.fillEmpty(fillItem);
        }
        
        return gui;
    }
    
    /**
     * Internal class for storing slot configuration.
     */
    private static class SlotConfig {
        final int slot;
        final ItemStack item;
        final Consumer<ClickContext> clickHandler;
        final String disabledMessage;
        
        SlotConfig(int slot, ItemStack item, Consumer<ClickContext> clickHandler, String disabledMessage) {
            this.slot = slot;
            this.item = item;
            this.clickHandler = clickHandler;
            this.disabledMessage = disabledMessage;
        }
    }
}
