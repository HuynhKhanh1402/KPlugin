package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.button.ConfigItem;
import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A simple GUI implementation for creating basic inventory menus.
 * <p>
 * SimpleGUI provides a straightforward way to create GUIs without
 * needing to extend the base {@link GUI} class. It can be used
 * directly or through the {@link GUIBuilder} for fluent construction.
 * </p>
 * 
 * <h3>Direct Usage</h3>
 * <pre>{@code
 * SimpleGUI gui = new SimpleGUI(GUIType.CHEST_3_ROWS, "&6&lShop Menu");
 * 
 * // Add items
 * gui.setItem(13, new ItemStack(Material.DIAMOND));
 * 
 * // Add buttons with click handlers
 * gui.setButton(22, new GUIButton(
 *     new ItemStack(Material.EMERALD),
 *     player -> player.sendMessage("You clicked!")
 * ));
 * 
 * // Open for player
 * gui.open(player);
 * }</pre>
 * 
 * <h3>Using GUIBuilder</h3>
 * <pre>{@code
 * GUI gui = new GUIBuilder(3, "&6&lShop Menu")
 *     .setItem(13, new ItemStack(Material.DIAMOND))
 *     .setButton(22, new GUIButton(item, p -> p.sendMessage("Clicked!")))
 *     .fillBorder(GUIUtil.createBlackFiller())
 *     .build();
 * gui.open(player);
 * }</pre>
 * 
 * <h3>Extending SimpleGUI</h3>
 * <pre>{@code
 * public class ShopGUI extends SimpleGUI {
 *     public ShopGUI() {
 *         super(GUIType.CHEST_6_ROWS, "&6&lItem Shop");
 *     }
 *     
 *     {@literal @}Override
 *     protected void setup(Player player) {
 *         fillBorder(GUIUtil.createBlackFiller());
 *         
 *         // Add shop items
 *         setItem(13, new ItemStack(Material.DIAMOND));
 *         setClickHandler(13, p -> {
 *             p.sendMessage("Buying diamond!");
 *         });
 *     }
 * }
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 * @see GUI
 * @see GUIBuilder
 */
public class SimpleGUI extends GUI {
    
    /**
     * Creates a new simple GUI with the specified type and title.
     *
     * @param type the GUI type
     * @param title the display title (supports color codes with &amp;)
     */
    public SimpleGUI(@NotNull GUIType type, @NotNull String title) {
        super(type, title);
    }
    
    /**
     * Creates a new simple GUI with the specified number of rows and title.
     * <p>
     * This is a convenience constructor for chest-type GUIs.
     * </p>
     *
     * @param rows the number of rows (1-6)
     * @param title the display title (supports color codes with &amp;)
     */
    public SimpleGUI(int rows, @NotNull String title) {
        super(GUIType.getByRows(rows), title);
    }
    
    // ==================== Fluent API Methods ====================
    
    /**
     * Sets an item and returns this GUI for chaining.
     *
     * @param slot the slot index
     * @param item the item to set
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withItem(int slot, @Nullable ItemStack item) {
        setItem(slot, item);
        return this;
    }
    
    /**
     * Sets a button and returns this GUI for chaining.
     *
     * @param slot the slot index
     * @param button the button to set
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withButton(int slot, @NotNull GUIButton button) {
        setButton(slot, button);
        return this;
    }
    
    /**
     * Sets a click handler and returns this GUI for chaining.
     *
     * @param slot the slot index
     * @param handler the click handler
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withClickHandler(int slot, @NotNull Consumer<Player> handler) {
        setClickHandler(slot, handler);
        return this;
    }
    
    /**
     * Sets the view-only mode and returns this GUI for chaining.
     *
     * @param viewOnly true for view-only, false for interactive
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withViewOnly(boolean viewOnly) {
        setViewOnly(viewOnly);
        return this;
    }
    
    /**
     * Sets the global click handler and returns this GUI for chaining.
     *
     * @param handler the global click handler
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withGlobalClickHandler(@NotNull BiConsumer<Player, Integer> handler) {
        setGlobalClickHandler(handler);
        return this;
    }
    
    /**
     * Sets the close handler and returns this GUI for chaining.
     *
     * @param handler the close handler
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withCloseHandler(@NotNull Consumer<Player> handler) {
        setCloseHandler(handler);
        return this;
    }
    
    /**
     * Fills empty slots and returns this GUI for chaining.
     *
     * @param filler the filler item
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withEmptyFill(@NotNull ItemStack filler) {
        fillEmpty(filler);
        return this;
    }
    
    /**
     * Fills border slots and returns this GUI for chaining.
     *
     * @param borderItem the border item
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withBorder(@NotNull ItemStack borderItem) {
        fillBorder(borderItem);
        return this;
    }
    
    /**
     * Places a ConfigItem in all its configured slots.
     * <p>
     * The ConfigItem's slots are read from its configuration and
     * the item is placed in each specified slot.
     * </p>
     *
     * @param configItem the config item to place
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withConfigItem(@NotNull ConfigItem configItem) {
        ItemStack item = configItem.build();
        for (Integer slot : configItem.getSlots()) {
            setItem(slot, item);
        }
        return this;
    }
    
    /**
     * Places a ConfigItem with a click handler in all its configured slots.
     *
     * @param configItem the config item to place
     * @param handler the click handler for all slots
     * @return this GUI for chaining
     */
    @NotNull
    public SimpleGUI withConfigItem(@NotNull ConfigItem configItem, @NotNull Consumer<Player> handler) {
        ItemStack item = configItem.build();
        for (Integer slot : configItem.getSlots()) {
            setButton(slot, new GUIButton(item, handler));
        }
        return this;
    }
}
