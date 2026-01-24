package dev.khanh.plugin.kplugin.gui.pagination;

import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.gui.GUIManager;
import dev.khanh.plugin.kplugin.gui.GUIType;
import dev.khanh.plugin.kplugin.gui.button.ConfigItem;
import dev.khanh.plugin.kplugin.gui.button.GUIButton;
import dev.khanh.plugin.kplugin.util.GUIUtil;
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
 * <p><strong>Eager mode example:</strong></p>
 * <pre>{@code
 * List<ConfigItem> allItems = loadItemsFromConfig();
 * PaginatedGUI gui = new PaginatedGUI(GUIType.CHEST_6_ROWS, "&6Item Shop");
 * gui.setItemsPerPage(28);
 * gui.setItems(allItems, player);
 * gui.open(player);
 * }</pre>
 * 
 * <p><strong>Lazy mode example:</strong></p>
 * <pre>{@code
 * PaginatedGUI gui = new PaginatedGUI(GUIType.CHEST_6_ROWS, "&6Database Items");
 * gui.setItemsPerPage(28);
 * gui.setLazyLoader(page -> CompletableFuture.supplyAsync(() -> {
 *     return database.loadItemsForPage(page);
 * }));
 * gui.open(player);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class PaginatedGUI extends GUI {
    
    private int currentPage;
    private int itemsPerPage;
    private int[] contentSlots;
    private int previousButtonSlot;
    private int nextButtonSlot;
    private int pageInfoSlot;
    
    private ConfigItem previousButton;
    private ConfigItem nextButton;
    private ConfigItem pageInfoItem;
    
    // Eager mode
    private List<ConfigItem> allItems;
    
    // Lazy mode
    private Function<Integer, CompletableFuture<List<ConfigItem>>> lazyLoader;
    private int totalPages;
    private long loadTimeout;
    
    private boolean loading;
    
    /**
     * Creates a new paginated GUI.
     *
     * @param type the GUI type
     * @param title the GUI title
     */
    public PaginatedGUI(@NotNull GUIType type, @NotNull String title) {
        super(type, title);
        this.currentPage = 0;
        this.itemsPerPage = calculateDefaultItemsPerPage();
        this.contentSlots = generateDefaultContentSlots();
        this.previousButtonSlot = type.getSize() - 9;
        this.nextButtonSlot = type.getSize() - 1;
        this.pageInfoSlot = type.getSize() - 5;
        this.loadTimeout = 5000; // 5 seconds
        this.loading = false;
        
        setupDefaultNavigationButtons();
    }
    
    /**
     * Sets the items for eager loading mode.
     *
     * @param items the items to paginate
     * @param player the player to render for (for placeholder replacement)
     */
    public void setItems(@NotNull List<ConfigItem> items, @NotNull Player player) {
        this.allItems = new ArrayList<>(items);
        this.lazyLoader = null;
        this.totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        this.currentPage = 0;
        
        renderPage(player);
    }
    
    /**
     * Sets the lazy loader function.
     *
     * @param loader function that loads items for a given page
     * @param totalPages the total number of pages
     */
    public void setLazyLoader(@NotNull Function<Integer, CompletableFuture<List<ConfigItem>>> loader, int totalPages) {
        this.lazyLoader = loader;
        this.allItems = null;
        this.totalPages = totalPages;
        this.currentPage = 0;
    }
    
    /**
     * Sets the number of items per page.
     *
     * @param itemsPerPage the items per page
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
    
    /**
     * Sets the slots where content items should be placed.
     *
     * @param slots the content slot indices
     */
    public void setContentSlots(@NotNull int[] slots) {
        this.contentSlots = slots.clone();
        this.itemsPerPage = slots.length;
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
    public void open(@NotNull Player player) {
        if (lazyLoader != null && allItems == null) {
            // Initial lazy load
            loadPageLazy(player);
        } else {
            renderPage(player);
            GUIManager.getInstance().openGUI(player, this);
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
                    allItems = items;
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
        
        // Place items
        for (int i = 0; i < pageItems.size() && i < contentSlots.length; i++) {
            ConfigItem configItem = pageItems.get(i);
            ItemStack item = configItem.build(text -> replacePlaceholders(text, player));
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
        if (allItems == null) {
            return new ArrayList<>();
        }
        
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allItems.size());
        
        if (start >= allItems.size()) {
            return new ArrayList<>();
        }
        
        return allItems.subList(start, end);
    }
    
    /**
     * Updates the navigation buttons.
     */
    private void updateNavigationButtons(@NotNull Player player) {
        // Previous button
        if (currentPage > 0) {
            ItemStack prevItem = previousButton.build(text -> replacePlaceholders(text, player));
            setButton(previousButtonSlot, new GUIButton(prevItem, p -> previousPage(p)));
        } else {
            setItem(previousButtonSlot, null);
        }
        
        // Next button
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = nextButton.build(text -> replacePlaceholders(text, player));
            setButton(nextButtonSlot, new GUIButton(nextItem, p -> nextPage(p)));
        } else {
            setItem(nextButtonSlot, null);
        }
        
        // Page info
        if (pageInfoItem != null) {
            ItemStack infoItem = pageInfoItem.build(text -> replacePlaceholders(text, player));
            setItem(pageInfoSlot, infoItem);
        }
    }
    
    /**
     * Replaces placeholders in text.
     */
    @NotNull
    private String replacePlaceholders(@NotNull String text, @NotNull Player player) {
        return text
            .replace("{player}", player.getName())
            .replace("{page}", String.valueOf(currentPage + 1))
            .replace("{total_pages}", String.valueOf(totalPages))
            .replace("{current_page}", String.valueOf(currentPage + 1));
    }
    
    /**
     * Calculates the default number of items per page based on GUI size.
     */
    private int calculateDefaultItemsPerPage() {
        int rows = type.getRows();
        if (rows <= 2) {
            return type.getSize() - 3; // Reserve 3 slots for navigation
        }
        return (rows - 1) * 9 - 2; // Reserve bottom row for navigation
    }
    
    /**
     * Generates default content slots.
     */
    @NotNull
    private int[] generateDefaultContentSlots() {
        int size = type.getSize();
        int rows = type.getRows();
        
        if (rows <= 2) {
            // Use all slots except last 3 for navigation
            int[] slots = new int[size - 3];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = i;
            }
            return slots;
        }
        
        // Use all rows except the last one
        int contentSize = (rows - 1) * 9;
        int[] slots = new int[contentSize];
        for (int i = 0; i < contentSize; i++) {
            slots[i] = i;
        }
        return slots;
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
