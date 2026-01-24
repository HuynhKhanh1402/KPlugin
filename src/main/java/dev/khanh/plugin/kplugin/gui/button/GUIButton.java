package dev.khanh.plugin.kplugin.gui.button;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents a clickable button in a GUI.
 * <p>
 * GUIButton is a simple abstraction that wraps an ItemStack with
 * click handling functionality. Buttons can be created from ItemStacks,
 * configuration sections, or using the {@link GUIButtonBuilder}.
 * </p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * GUIButton button = new GUIButton(
 *     new ItemStack(Material.DIAMOND),
 *     player -> player.sendMessage("You clicked the diamond!")
 * );
 * 
 * gui.setButton(10, button);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class GUIButton {
    
    private final ItemStack item;
    private final Consumer<Player> clickHandler;
    
    /**
     * Creates a new GUI button with the specified item and click handler.
     *
     * @param item the display item
     * @param clickHandler the click handler (may be null for no action)
     */
    public GUIButton(@NotNull ItemStack item, Consumer<Player> clickHandler) {
        this.item = item.clone();
        this.clickHandler = clickHandler;
    }
    
    /**
     * Creates a new GUI button with the specified item and no click handler.
     *
     * @param item the display item
     */
    public GUIButton(@NotNull ItemStack item) {
        this(item, null);
    }
    
    /**
     * Gets the display item for this button.
     * <p>
     * Returns a clone to prevent external modification.
     * </p>
     *
     * @return a cloned copy of the item
     */
    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }
    
    /**
     * Handles a click on this button.
     *
     * @param player the player who clicked
     */
    public void onClick(@NotNull Player player) {
        if (clickHandler != null) {
            clickHandler.accept(player);
        }
    }
    
    /**
     * Checks if this button has a click handler.
     *
     * @return true if a click handler exists
     */
    public boolean hasClickHandler() {
        return clickHandler != null;
    }
}
