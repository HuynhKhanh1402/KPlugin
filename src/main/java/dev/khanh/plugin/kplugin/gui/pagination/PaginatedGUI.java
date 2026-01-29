package dev.khanh.plugin.kplugin.gui.pagination;

import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.gui.GUIManager;
import dev.khanh.plugin.kplugin.gui.GUIType;
import dev.khanh.plugin.kplugin.gui.button.ConfigItem;
import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import dev.khanh.plugin.kplugin.gui.placeholder.PlaceholderContext;
import dev.khanh.plugin.kplugin.gui.placeholder.PlaceholderResolver;
import dev.khanh.plugin.kplugin.util.TaskUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A paginated GUI that supports both eager and lazy loading of items.
 * <p>
 * This GUI provides automatic pagination with navigation buttons and
 * two loading modes:
 * <ul>
 *   <li><strong>Eager mode</strong>: All items are loaded immediately and divided into pages</li>
 *   <li><strong>Lazy mode</strong>: Pages are loaded on-demand when navigated to</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Important:</strong> Content slots must be provided explicitly via the
 * constructor or {@link #setContentSlots(int[])}. This gives full control over
 * where pagination items are placed.
 * </p>
 * 
 * <p>
 * In lazy mode, when loading a new page:
 * <ol>
 *   <li>The current inventory is closed</li>
 *   <li>Items are loaded asynchronously</li>
 *   <li>The inventory is reopened with the new page</li>
 * </ol>
 * This prevents players from seeing loading placeholders and provides
 * a smoother experience.
 * </p>
 * 
 * <h3>Eager Mode Example</h3>
 * <pre>{@code
 * // Define content slots (where items will be placed)
 * int[] contentSlots = {10, 11, 12, 13, 14, 15, 16,
 *                       19, 20, 21, 22, 23, 24, 25,
 *                       28, 29, 30, 31, 32, 33, 34};
 * 
 * PaginatedGUI gui = new PaginatedGUI(
 *     GUIType.CHEST_6_ROWS,
 *     "&6Item Shop",
 *     contentSlots
 * );
 * 
 * // Set navigation button positions
 * gui.setNavigationSlots(45, 53, 49); // prev, next, info
 * 
 * // Set items
 * List<ConfigItem> items = loadItemsFromConfig();
 * gui.setItems(items);
 * 
 * gui.open(player);
 * }</pre>
 * 
 * <h3>Lazy Mode Example</h3>
 * <pre>{@code
 * PaginatedGUI gui = new PaginatedGUI(
 *     GUIType.CHEST_6_ROWS,
 *     "&6Database Items",
 *     contentSlots
 * );
 * 
 * // Set lazy loader - loads items for each page on demand
 * gui.setLazyLoader(page -> CompletableFuture.supplyAsync(() -> {
 *     return database.loadItemsForPage(page);
 * }), totalPages);
 * 
 * gui.open(player);
 * }</pre>
 * 
 * <h3>Extending PaginatedGUI</h3>
 * <pre>{@code
 * public class ShopGUI extends PaginatedGUI {
 *     private static final int[] CONTENT_SLOTS = {10, 11, 12, 13, 14, 15, 16};
 *     
 *     public ShopGUI() {
 *         super(GUIType.CHEST_3_ROWS, "&6Shop", CONTENT_SLOTS);
 *         setNavigationSlots(18, 26, 22);
 *     }
 *     
 *     {@literal @}Override
 *     protected void setup(Player player) {
 *         // Add border or decorations
 *         fillBorder(GUIUtil.createBlackFiller());
 *         
 *         // Load items
 *         setItems(loadShopItems());
 *         
 *         // Call super to render the page
 *         super.setup(player);
 *     }
 * }
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 * @see GUI
 * @see ConfigItem
 */
public class PaginatedGUI extends GUI {
    
    // Content configuration
    private int[] contentSlots;
    private int previousButtonSlot;
    private int nextButtonSlot;
    private int pageInfoSlot;
    
    // Navigation buttons
    private ConfigItem previousButton;
    private ConfigItem nextButton;
    private ConfigItem pageInfoItem;
    
    // State
    private int currentPage;
    private int totalPages;
    private boolean loading;
    
    // Eager mode data
    private List<ConfigItem> allItems;
    
    // Lazy mode data
    private Function<Integer, CompletableFuture<List<ConfigItem>>> lazyLoader;
    private List<ConfigItem> currentPageItems;
    private long loadTimeout;
    
    // Placeholder support
    private PlaceholderResolver placeholderResolver;
    
    // Current viewer
    private Player currentViewer;
    
    /**
     * Creates a new paginated GUI with explicit content slots.
     * <p>
     * This is the recommended constructor that requires you to specify
     * exactly which slots will be used for pagination content.
     * </p>
     *
     * @param type the GUI type
     * @param title the GUI title (supports color codes with &amp;)
     * @param contentSlots the slots where paginated items will be placed
     */
    public PaginatedGUI(@NotNull GUIType type, @NotNull String title, @NotNull int[] contentSlots) {
        super(type, title);
        this.contentSlots = contentSlots.clone();
        this.currentPage = 0;
        this.totalPages = 1;
        this.previousButtonSlot = type.getSize() - 9;
        this.nextButtonSlot = type.getSize() - 1;
        this.pageInfoSlot = type.getSize() - 5;
        this.loadTimeout = 5000; // 5 seconds
        this.loading = false;
        this.placeholderResolver = new PlaceholderResolver();
        
        setupDefaultNavigationButtons();
        setupPaginationPlaceholders();
    }
    
    /**
     * Creates a new paginated GUI with explicit content slots.
     *
     * @param rows the number of rows (1-6)
     * @param title the GUI title (supports color codes with &amp;)
     * @param contentSlots the slots where paginated items will be placed
     */
    public PaginatedGUI(int rows, @NotNull String title, @NotNull int[] contentSlots) {
        this(GUIType.getByRows(rows), title, contentSlots);
    }
    
    /**
     * Creates a new paginated GUI without predefined content slots.
     * <p>
     * <strong>Warning:</strong> You must call {@link #setContentSlots(int[])}
     * before using this GUI, or content will not be displayed properly.
     * </p>
     *
     * @param type the GUI type
     * @param title the GUI title (supports color codes with &amp;)
     */
    public PaginatedGUI(@NotNull GUIType type, @NotNull String title) {
        super(type, title);
        this.contentSlots = new int[0];
        this.currentPage = 0;
        this.totalPages = 1;
        this.previousButtonSlot = type.getSize() - 9;
        this.nextButtonSlot = type.getSize() - 1;
        this.pageInfoSlot = type.getSize() - 5;
        this.loadTimeout = 5000;
        this.loading = false;
        this.placeholderResolver = new PlaceholderResolver();
        
        setupDefaultNavigationButtons();
        setupPaginationPlaceholders();
    }
    
    /**
     * Sets up pagination-specific placeholders.
     */
    private void setupPaginationPlaceholders() {
        placeholderResolver
            .addResolver("page", (player, ctx) -> String.valueOf(currentPage + 1))
            .addResolver("current_page", (player, ctx) -> String.valueOf(currentPage + 1))
            .addResolver("total_pages", (player, ctx) -> String.valueOf(totalPages))
            .addResolver("total", (player, ctx) -> String.valueOf(allItems != null ? allItems.size() : 0));
    }
    
    /**
     * Gets the placeholder resolver for this GUI.
     * <p>
     * You can add custom placeholders using this resolver.
     * </p>
     *
     * @return the placeholder resolver
     */
    @NotNull
    public PlaceholderResolver getPlaceholderResolver() {
        return placeholderResolver;
    }
    
    /**
     * Sets a custom placeholder resolver.
     *
     * @param resolver the new placeholder resolver
     */
    public void setPlaceholderResolver(@NotNull PlaceholderResolver resolver) {
        this.placeholderResolver = resolver;
        setupPaginationPlaceholders();
    }
    
    /**
     * Sets the items for eager loading mode.
     *
     * @param items the items to paginate
     */
    public void setItems(@NotNull List<ConfigItem> items) {
        this.allItems = new ArrayList<>(items);
        this.lazyLoader = null;
        this.totalPages = calculateTotalPages();
        this.currentPage = 0;
    }
    
    /**
     * Sets the lazy loader function.
     *
     * @param loader function that loads items for a given page (0-indexed)
     * @param totalPages the total number of pages
     */
    public void setLazyLoader(@NotNull Function<Integer, CompletableFuture<List<ConfigItem>>> loader, int totalPages) {
        this.lazyLoader = loader;
        this.allItems = null;
        this.totalPages = totalPages;
        this.currentPage = 0;
    }
    
    /**
     * Gets the number of items per page.
     *
     * @return the items per page (equals content slots length)
     */
    public int getItemsPerPage() {
        return contentSlots.length;
    }
    
    /**
     * Sets the slots where content items should be placed.
     *
     * @param slots the content slot indices
     */
    public void setContentSlots(@NotNull int[] slots) {
        this.contentSlots = slots.clone();
        // Recalculate total pages if items are set
        if (allItems != null) {
            this.totalPages = calculateTotalPages();
        }
    }
    
    /**
     * Gets the content slots.
     *
     * @return a copy of the content slots array
     */
    @NotNull
    public int[] getContentSlots() {
        return contentSlots.clone();
    }
    
    /**
     * Sets the navigation button slots.
     *
     * @param previousSlot the previous button slot
     * @param nextSlot the next button slot
     * @param pageInfoSlot the page info slot
     */
    public void setNavigationSlots(int previousSlot, int nextSlot, int pageInfoSlot) {
        this.previousButtonSlot = previousSlot;
        this.nextButtonSlot = nextSlot;
        this.pageInfoSlot = pageInfoSlot;
    }
    
    /**
     * Sets custom navigation button items.
     *
     * @param previousButton the previous button config item
     * @param nextButton the next button config item
     * @param pageInfoItem the page info item config
     */
    public void setNavigationButtons(@NotNull ConfigItem previousButton, @NotNull ConfigItem nextButton, @Nullable ConfigItem pageInfoItem) {
        this.previousButton = previousButton;
        this.nextButton = nextButton;
        this.pageInfoItem = pageInfoItem;
    }
    
    /**
     * Sets the timeout for lazy loading operations.
     *
     * @param timeout the timeout in milliseconds
     */
    public void setLoadTimeout(long timeout) {
        this.loadTimeout = timeout;
    }
    
    /**
     * Gets the current page number (0-indexed).
     *
     * @return the current page
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the total number of pages.
     *
     * @return the total pages
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Checks if currently loading a page.
     *
     * @return true if loading
     */
    public boolean isLoading() {
        return loading;
    }
    
    /**
     * Navigates to the next page.
     *
     * @param player the player viewing the GUI
     */
    public void nextPage(@NotNull Player player) {
        if (loading || currentPage >= totalPages - 1) {
            return;
        }
        
        currentPage++;
        
        if (lazyLoader != null) {
            loadPageLazy(player);
        } else {
            renderPage(player);
        }
    }
    
    /**
     * Navigates to the previous page.
     *
     * @param player the player viewing the GUI
     */
    public void previousPage(@NotNull Player player) {
        if (loading || currentPage <= 0) {
            return;
        }
        
        currentPage--;
        
        if (lazyLoader != null) {
            loadPageLazy(player);
        } else {
            renderPage(player);
        }
    }
    
    /**
     * Goes to a specific page.
     *
     * @param page the page number (0-indexed)
     * @param player the player viewing the GUI
     */
    public void goToPage(int page, @NotNull Player player) {
        if (loading || page < 0 || page >= totalPages || page == currentPage) {
            return;
        }
        
        currentPage = page;
        
        if (lazyLoader != null) {
            loadPageLazy(player);
        } else {
            renderPage(player);
        }
    }
    
    @Override
    protected void setup(@NotNull Player player) {
        this.currentViewer = player;
        // Render the current page during setup
        renderPage(player);
    }
    
    @Override
    public void open(@NotNull Player player) {
        this.currentViewer = player;
        
        if (lazyLoader != null && currentPageItems == null) {
            // Initial lazy load
            ensureInitialized();
            loadPageLazy(player);
        } else {
            // Use parent implementation which calls setup()
            super.open(player);
        }
    }
    
    /**
     * Loads a page using lazy loading.
     * <p>
     * Closes the inventory, loads items asynchronously, then reopens.
     * </p>
     */
    private void loadPageLazy(@NotNull Player player) {
        if (loading) {
            return;
        }
        
        loading = true;
        
        // Close inventory during load
        TaskUtil.runAtEntity(player, () -> player.closeInventory());
        
        CompletableFuture<List<ConfigItem>> future = lazyLoader.apply(currentPage);
        
        future.orTimeout(loadTimeout, TimeUnit.MILLISECONDS)
            .thenAccept(items -> {
                // Update on main thread
                TaskUtil.runAtEntity(player, () -> {
                    currentPageItems = items;
                    renderPage(player);
                    loading = false;
                    
                    // Reopen inventory
                    player.openInventory(getInventory());
                });
            })
            .exceptionally(throwable -> {
                TaskUtil.runAtEntity(player, () -> {
                    loading = false;
                    
                    if (throwable instanceof TimeoutException) {
                        player.sendMessage("§cFailed to load page: Timeout");
                    } else {
                        player.sendMessage("§cFailed to load page: " + throwable.getMessage());
                    }
                });
                return null;
            });
    }
    
    /**
     * Renders the current page.
     */
    private void renderPage(@NotNull Player player) {
        // Clear content area
        for (int slot : contentSlots) {
            setItem(slot, null);
        }
        
        // Get items for current page
        List<ConfigItem> pageItems = getPageItems();
        
        // Create placeholder function using the resolver
        Function<String, String> placeholderFunction = placeholderResolver.asFunction(player);
        
        // Place items
        for (int i = 0; i < pageItems.size() && i < contentSlots.length; i++) {
            ConfigItem configItem = pageItems.get(i);
            ItemStack item = configItem.build(placeholderFunction);
            setItem(contentSlots[i], item);
        }
        
        // Update navigation buttons
        updateNavigationButtons(player);
    }
    
    /**
     * Gets the items for the current page.
     */
    @NotNull
    private List<ConfigItem> getPageItems() {
        // In lazy mode, return current page items
        if (lazyLoader != null && currentPageItems != null) {
            return currentPageItems;
        }
        
        // In eager mode, slice from all items
        if (allItems == null) {
            return new ArrayList<>();
        }
        
        int itemsPerPage = contentSlots.length;
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allItems.size());
        
        if (start >= allItems.size()) {
            return new ArrayList<>();
        }
        
        return allItems.subList(start, end);
    }
    
    /**
     * Calculates total pages based on items and content slots.
     */
    private int calculateTotalPages() {
        if (allItems == null || contentSlots.length == 0) {
            return 1;
        }
        return (int) Math.ceil((double) allItems.size() / contentSlots.length);
    }
    
    /**
     * Updates the navigation buttons.
     */
    private void updateNavigationButtons(@NotNull Player player) {
        Function<String, String> placeholderFunction = placeholderResolver.asFunction(player);
        
        // Previous button
        if (currentPage > 0) {
            ItemStack prevItem = previousButton.build(placeholderFunction);
            setButton(previousButtonSlot, new GUIButton(prevItem, p -> previousPage(p)));
        } else {
            setItem(previousButtonSlot, null);
            removeButton(previousButtonSlot);
        }
        
        // Next button
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = nextButton.build(placeholderFunction);
            setButton(nextButtonSlot, new GUIButton(nextItem, p -> nextPage(p)));
        } else {
            setItem(nextButtonSlot, null);
            removeButton(nextButtonSlot);
        }
        
        // Page info
        if (pageInfoItem != null) {
            ItemStack infoItem = pageInfoItem.build(placeholderFunction);
            setItem(pageInfoSlot, infoItem);
        }
    }
    
    /**
     * Sets up default navigation buttons.
     */
    private void setupDefaultNavigationButtons() {
        this.previousButton = new ConfigItem.Builder()
            .material(Material.ARROW)
            .displayName("&e&l← Previous Page")
            .addLore("&7Page {current_page}/{total_pages}")
            .build();
        
        this.nextButton = new ConfigItem.Builder()
            .material(Material.ARROW)
            .displayName("&e&lNext Page →")
            .addLore("&7Page {current_page}/{total_pages}")
            .build();
        
        this.pageInfoItem = new ConfigItem.Builder()
            .material(Material.PAPER)
            .displayName("&6&lPage {current_page}/{total_pages}")
            .build();
    }
}
