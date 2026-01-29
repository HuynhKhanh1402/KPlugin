package dev.khanh.plugin.kplugin.gui.pagination;

import dev.khanh.plugin.kplugin.gui.GUIType;
import dev.khanh.plugin.kplugin.gui.button.ConfigItem;
import dev.khanh.plugin.kplugin.gui.placeholder.PlaceholderResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fluent builder for creating PaginatedGUI instances.
 * <p>
 * This builder provides a convenient way to construct paginated GUIs
 * with navigation buttons, content slots, and pagination configuration.
 * </p>
 * 
 * <h3>Eager Mode Example</h3>
 * <pre>{@code
 * int[] contentSlots = {10, 11, 12, 13, 14, 15, 16,
 *                       19, 20, 21, 22, 23, 24, 25};
 * 
 * PaginatedGUI gui = new PaginatedGUIBuilder(6, "&6&lItem Shop", contentSlots)
 *     .setNavigationSlots(45, 53, 49)
 *     .setItems(loadAllItems())
 *     .fillBorder(GUIUtil.createBlackFiller())
 *     .build();
 * 
 * gui.open(player);
 * }</pre>
 * 
 * <h3>Lazy Mode Example</h3>
 * <pre>{@code
 * PaginatedGUI gui = new PaginatedGUIBuilder(6, "&6&lDatabase Items", contentSlots)
 *     .setLazyLoader(page -> database.loadItemsAsync(page), totalPages)
 *     .setLoadTimeout(10000)
 *     .build();
 * 
 * gui.open(player);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 * @see PaginatedGUI
 */
public class PaginatedGUIBuilder {
    
    private final PaginatedGUI gui;
    
    /**
     * Creates a new builder with the specified type, title, and content slots.
     *
     * @param type the GUI type
     * @param title the display title (supports color codes with &amp;)
     * @param contentSlots the slots where paginated items will be placed
     */
    public PaginatedGUIBuilder(@NotNull GUIType type, @NotNull String title, @NotNull int[] contentSlots) {
        this.gui = new PaginatedGUI(type, title, contentSlots);
    }
    
    /**
     * Creates a new builder with the specified rows, title, and content slots.
     *
     * @param rows the number of rows (1-6)
     * @param title the display title (supports color codes with &amp;)
     * @param contentSlots the slots where paginated items will be placed
     */
    public PaginatedGUIBuilder(int rows, @NotNull String title, @NotNull int[] contentSlots) {
        this.gui = new PaginatedGUI(rows, title, contentSlots);
    }
    
    /**
     * Sets the navigation button slots.
     *
     * @param previousSlot the previous button slot
     * @param nextSlot the next button slot
     * @param pageInfoSlot the page info slot
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setNavigationSlots(int previousSlot, int nextSlot, int pageInfoSlot) {
        gui.setNavigationSlots(previousSlot, nextSlot, pageInfoSlot);
        return this;
    }
    
    /**
     * Sets custom navigation button items.
     *
     * @param previousButton the previous button
     * @param nextButton the next button
     * @param pageInfoItem the page info item (can be null)
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setNavigationButtons(@NotNull ConfigItem previousButton, 
                                                     @NotNull ConfigItem nextButton, 
                                                     @Nullable ConfigItem pageInfoItem) {
        gui.setNavigationButtons(previousButton, nextButton, pageInfoItem);
        return this;
    }
    
    /**
     * Sets items for eager loading mode.
     *
     * @param items the items to paginate
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setItems(@NotNull List<ConfigItem> items) {
        gui.setItems(items);
        return this;
    }
    
    /**
     * Sets the lazy loader function.
     *
     * @param loader function that loads items for a given page (0-indexed)
     * @param totalPages the total number of pages
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setLazyLoader(@NotNull Function<Integer, CompletableFuture<List<ConfigItem>>> loader, 
                                              int totalPages) {
        gui.setLazyLoader(loader, totalPages);
        return this;
    }
    
    /**
     * Sets the timeout for lazy loading operations.
     *
     * @param timeoutMs the timeout in milliseconds
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setLoadTimeout(long timeoutMs) {
        gui.setLoadTimeout(timeoutMs);
        return this;
    }
    
    /**
     * Sets the placeholder resolver.
     *
     * @param resolver the placeholder resolver
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setPlaceholderResolver(@NotNull PlaceholderResolver resolver) {
        gui.setPlaceholderResolver(resolver);
        return this;
    }
    
    /**
     * Sets whether the GUI is view-only.
     *
     * @param viewOnly true for view-only
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setViewOnly(boolean viewOnly) {
        gui.setViewOnly(viewOnly);
        return this;
    }
    
    /**
     * Sets a global click handler.
     *
     * @param handler the global click handler
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setGlobalClickHandler(@NotNull BiConsumer<Player, Integer> handler) {
        gui.setGlobalClickHandler(handler);
        return this;
    }
    
    /**
     * Sets a close handler.
     *
     * @param handler the close handler
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setCloseHandler(@NotNull Consumer<Player> handler) {
        gui.setCloseHandler(handler);
        return this;
    }
    
    /**
     * Fills empty slots with the specified item.
     *
     * @param filler the filler item
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder fillEmpty(@NotNull ItemStack filler) {
        gui.fillEmpty(filler);
        return this;
    }
    
    /**
     * Fills the border with the specified item.
     *
     * @param borderItem the border item
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder fillBorder(@NotNull ItemStack borderItem) {
        gui.fillBorder(borderItem);
        return this;
    }
    
    /**
     * Sets an item in the specified slot.
     *
     * @param slot the slot index
     * @param item the item to set
     * @return this builder for chaining
     */
    @NotNull
    public PaginatedGUIBuilder setItem(int slot, @NotNull ItemStack item) {
        gui.setItem(slot, item);
        return this;
    }
    
    /**
     * Builds and returns the PaginatedGUI.
     *
     * @return the constructed PaginatedGUI
     */
    @NotNull
    public PaginatedGUI build() {
        return gui;
    }
}
