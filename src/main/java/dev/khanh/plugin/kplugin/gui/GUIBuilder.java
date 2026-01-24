package dev.khanh.plugin.kplugin.gui;

import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import dev.khanh.plugin.kplugin.util.GUIUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
 *     .build();
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class GUIBuilder {
    
    private final GUIType type;
    private final String title;
    private boolean viewOnly = true;
    private final SimpleGUI gui;
    
    /**
     * Creates a new GUI builder with the specified type and title.
     *
     * @param type the GUI type
     * @param title the display title (supports color codes with &amp;)
     */
    public GUIBuilder(@NotNull GUIType type, @NotNull String title) {
        this.type = type;
        this.title = ColorUtil.colorize(title);
        this.gui = new SimpleGUI(type, this.title);
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
        this(GUIType.getByRows(rows), title);
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
     * Fills all empty slots with the specified filler item.
     *
     * @param filler the filler item
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillEmpty(@NotNull ItemStack filler) {
        for (int i = 0; i < type.getSize(); i++) {
            if (gui.getInventory().getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
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
        ItemStack glassPane = new ItemStack(glassPaneMaterial);
        return fillEmpty(glassPane);
    }
    
    /**
     * Fills the border (edges) of the GUI with the specified item.
     *
     * @param borderItem the border item
     * @return this builder for chaining
     */
    @NotNull
    public GUIBuilder fillBorder(@NotNull ItemStack borderItem) {
        int rows = type.getRows();
        int size = type.getSize();
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, borderItem);
            if (rows > 1) {
                gui.setItem(size - 9 + i, borderItem);
            }
        }
        
        // Side columns
        if (rows > 2) {
            for (int row = 1; row < rows - 1; row++) {
                gui.setItem(row * 9, borderItem);
                gui.setItem(row * 9 + 8, borderItem);
            }
        }
        
        return this;
    }
    
    /**
     * Builds and returns the GUI instance.
     *
     * @return the constructed GUI
     */
    @NotNull
    public GUI build() {
        return gui;
    }
    
    /**
     * Simple GUI implementation used by the builder.
     */
    private static class SimpleGUI extends GUI {
        
        protected SimpleGUI(@NotNull GUIType type, @NotNull String title) {
            super(type, title);
        }
        
        @Override
        public void open(@NotNull Player player) {
            GUIManager.getInstance().openGUI(player, this);
        }
    }
}
