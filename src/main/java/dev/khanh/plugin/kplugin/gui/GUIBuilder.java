package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.button.ConfigItem;
import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import dev.khanh.plugin.kplugin.util.GUIUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Fluent builder for creating GUI instances.
 * <p>
 * This class provides a convenient builder pattern for constructing GUIs
 * with items, buttons, click handlers, and configuration options.
 * </p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * GUI gui = new GUIBuilder(GUIType.CHEST_3_ROWS, "&6&lMy Menu")
 *     .setItem(0, new ItemStack(Material.DIAMOND))
 *     .setButton(4, myButton)
 *     .setClickHandler(8, player -> player.sendMessage("Clicked!"))
 *     .setViewOnly(true)
 *     .fillBorder(GUIUtil.createBlackFiller())
 *     .build();
 * 
 * gui.open(player);
 * }</pre>
 * 
 * <p><strong>Row-based construction:</strong></p>
 * <pre>{@code
 * GUI gui = new GUIBuilder(3, "&6Menu") // 3 rows = 27 slots
 *     .fillBorder(GUIUtil.createGrayFiller())
 *     .setButton(13, closeButton)
 *     .build();
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 * @see SimpleGUI
 * @see GUI
 */
public class GUIBuilder {
    
    private final SimpleGUI gui;
    
    /**
     * Creates a new GUI builder with the specified type and title.
     *
     * @param type the GUI type
     * @param title the display title (supports color codes with &amp;)
     */
    public GUIBuilder(@NotNull GUIType type, @NotNull String title) {
        this.gui = new SimpleGUI(type, title);
    }
    
    /**
     * Creates a new GUI builder with the specified number of rows and title.
     * <p>
     * This is a convenience constructor for chest-type GUIs.
     * </p>
     *
     * @param rows the number of rows (1-6)
     * @param title the display title (supports color codes with &amp;)
     */
    public GUIBuilder(int rows, @NotNull String title) {
        this.gui = new SimpleGUI(rows, title);
    }
    
    /**
     * Sets an item in the specified slot.
     *
     * @param slot the slot index
     * @param item the item to set
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setItem(int slot, @NotNull ItemStack item) {
        gui.setItem(slot, item);
        return this;
    }
    
    /**
     * Sets a button in the specified slot.
     *
     * @param slot the slot index
     * @param button the button to set
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setButton(int slot, @NotNull GUIButton button) {
        gui.setButton(slot, button);
        return this;
    }
    
    /**
     * Sets a click handler for the specified slot.
     *
     * @param slot the slot index
     * @param handler the click handler
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setClickHandler(int slot, @NotNull Consumer<Player> handler) {
        gui.setClickHandler(slot, handler);
        return this;
    }
    
    /**
     * Sets a global click handler that is called for any slot click.
     *
     * @param handler the handler that receives player and slot
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setGlobalClickHandler(@NotNull BiConsumer<Player, Integer> handler) {
        gui.setGlobalClickHandler(handler);
        return this;
    }
    
    /**
     * Sets whether the GUI is view-only.
     *
     * @param viewOnly true for view-only, false for interactive
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setViewOnly(boolean viewOnly) {
        gui.setViewOnly(viewOnly);
        return this;
    }
    
    /**
     * Sets a close handler for the GUI.
     *
     * @param handler the close handler
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setCloseHandler(@NotNull Consumer<Player> handler) {
        gui.setCloseHandler(handler);
        return this;
    }
    
    /**
     * Fills all empty slots with the specified filler item.
     *
     * @param filler the filler item
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillEmpty(@NotNull ItemStack filler) {
        gui.fillEmpty(filler);
        return this;
    }
    
    /**
     * Fills all empty slots with a glass pane of the specified material.
     *
     * @param glassPaneMaterial the glass pane material
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillEmptyWithGlass(@NotNull Material glassPaneMaterial) {
        return fillEmpty(GUIUtil.createFiller(glassPaneMaterial));
    }
    
    /**
     * Fills the border (edges) of the GUI with the specified item.
     *
     * @param borderItem the border item
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillBorder(@NotNull ItemStack borderItem) {
        gui.fillBorder(borderItem);
        return this;
    }
    
    /**
     * Fills the specified slots with an item.
     *
     * @param slots the list of slots to fill
     * @param item the item to place
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillSlots(@NotNull List<Integer> slots, @NotNull ItemStack item) {
        for (int slot : slots) {
            gui.setItem(slot, item);
        }
        return this;
    }
    
    /**
     * Fills the specified slots with an item.
     *
     * @param slots the array of slots to fill
     * @param item the item to place
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillSlots(@NotNull int[] slots, @NotNull ItemStack item) {
        gui.fillSlots(slots, item);
        return this;
    }
    
    /**
     * Places a ConfigItem in all its configured slots.
     *
     * @param configItem the config item to place
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setConfigItem(@NotNull ConfigItem configItem) {
        gui.withConfigItem(configItem);
        return this;
    }
    
    /**
     * Places a ConfigItem with a click handler in all its configured slots.
     *
     * @param configItem the config item to place
     * @param handler the click handler for all slots
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder setConfigItem(@NotNull ConfigItem configItem, @NotNull Consumer<Player> handler) {
        gui.withConfigItem(configItem, handler);
        return this;
    }
    
    /**
     * Builds and returns the GUI instance.
     *
     * @return the constructed GUI
     */
    @NotNull
    public SimpleGUI build() {
        return gui;
    }
}
