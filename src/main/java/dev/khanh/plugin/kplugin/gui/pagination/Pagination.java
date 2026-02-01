package dev.khanh.plugin.kplugin.gui.pagination;

import dev.khanh.plugin.kplugin.gui.context.ClickContext;
import dev.khanh.plugin.kplugin.gui.GUI;
import dev.khanh.plugin.kplugin.item.ItemBuilder;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import dev.khanh.plugin.kplugin.util.TaskUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Pagination helper for displaying paginated content in GUIs.
 * <p>
 * Supports synchronous and asynchronous data loading, customizable navigation,
 * and placeholder support for page info.
 * </p>
 *
 * @param <T> the item type
 */
public final class Pagination<T> {
    
    private final GUI gui;
    private final int[] contentSlots;
    private final int itemsPerPage;
    
    // Data source
    private List<T> items;
    private Function<PageRequest, CompletableFuture<List<T>>> asyncLoader;
    private Supplier<Integer> totalPagesSupplier;
    
    // Rendering
    private Function<T, ItemStack> itemRenderer;
    private BiConsumer<T, ClickContext> itemClickHandler;
    
    // Navigation
    private int previousButtonSlot = -1;
    private int nextButtonSlot = -1;
    private int pageInfoSlot = -1;
    private ItemStack previousButtonItem;
    private ItemStack nextButtonItem;
    private ItemStack previousButtonDisabledItem;
    private ItemStack nextButtonDisabledItem;
    private Function<PageInfo, ItemStack> pageInfoRenderer;

    // State
    private int currentPage;
    private int totalPages;
    private boolean loading;
    private Player currentViewer;
    
    // Callbacks
    private BiConsumer<Integer, Player> pageChangeCallback;
    
    private Pagination(@NotNull GUI gui, int @NotNull [] contentSlots) {
        this.gui = gui;
        this.contentSlots = contentSlots.clone();
        this.itemsPerPage = contentSlots.length;
        this.currentPage = 0;
        this.totalPages = 1;
        this.loading = false;
        
        // Default item renderer
        this.itemRenderer = item -> {
            if (item instanceof ItemStack) {
                return ((ItemStack) item).clone();
            }
            return new ItemStack(Material.STONE);
        };
        
        // Default page info renderer
        this.pageInfoRenderer = info -> ItemBuilder.of(Material.PAPER)
            .name("&ePage " + info.currentPage + "/" + info.totalPages)
            .lore("&7Click to jump to page")
            .build();
    }
    
    // ==================== Static Factory ====================
    
    /**
     * Creates a new Pagination builder for the specified GUI and content slots.
     *
     * @param gui the GUI to paginate
     * @param contentSlots the slots where items will be displayed
     * @param <T> the item type
     * @return a new Pagination builder
     */
    @NotNull
    public static <T> Builder<T> create(@NotNull GUI gui, int @NotNull [] contentSlots) {
        return new Builder<>(gui, contentSlots);
    }
    
    /**
     * Creates a new Pagination builder for ItemStacks.
     *
     * @param gui the GUI
     * @param contentSlots the content slots
     * @return a new builder for ItemStack pagination
     */
    @NotNull
    public static Builder<ItemStack> createForItems(@NotNull GUI gui, int @NotNull [] contentSlots) {
        Builder<ItemStack> builder = new Builder<>(gui, contentSlots);
        builder.itemRenderer(ItemStack::clone);
        return builder;
    }
    
    // ==================== Pagination Control ====================

    /**
     * Gets the current page (0-indexed).
     *
     * @return the current page
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Gets the total number of pages.
     * <p>
     * <b>Important:</b> When using async loading ({@link Builder#asyncLoader(Function)}),
     * you must call {@link Builder#totalPages(int)} or {@link Builder#totalPages(Supplier)}
     * before this method can be used. Otherwise, an {@link IllegalStateException} will be thrown.
     * </p>
     *
     * @return the total pages
     * @throws IllegalStateException if async loader is used without setting total pages
     */
    public int getTotalPages() {
        if (asyncLoader != null && totalPagesSupplier == null && items == null) {
            throw new IllegalStateException(
                "Total pages not set for async pagination. " +
                "When using asyncLoader(), you must call totalPages() to set the total page count."
            );
        }
        return totalPages;
    }
    
    /**
     * Checks if there is a previous page.
     *
     * @return true if previous page exists
     */
    public boolean hasPreviousPage() {
        return currentPage > 0;
    }
    
    /**
     * Checks if there is a next page.
     *
     * @return true if next page exists
     */
    public boolean hasNextPage() {
        return currentPage < totalPages - 1;
    }
    
    /**
     * Navigates to the previous page.
     *
     * @param player the viewer
     */
    public void previousPage(@NotNull Player player) {
        if (hasPreviousPage() && !loading) {
            goToPage(currentPage - 1, player);
        }
    }
    
    /**
     * Navigates to the next page.
     *
     * @param player the viewer
     */
    public void nextPage(@NotNull Player player) {
        if (hasNextPage() && !loading) {
            goToPage(currentPage + 1, player);
        }
    }
    
    /**
     * Navigates to a specific page.
     *
     * @param page the page index (0-based)
     * @param player the viewer
     */
    public void goToPage(int page, @NotNull Player player) {
        if (page < 0 || page >= totalPages || loading) {
            return;
        }
        
        currentPage = page;
        currentViewer = player;
        
        if (asyncLoader != null) {
            loadPageAsync(page, player);
        } else {
            renderPage(player);
        }
        
        if (pageChangeCallback != null) {
            pageChangeCallback.accept(page, player);
        }
    }
    
    /**
     * Renders the current page.
     * <p>
     * For async pagination, ensures that total pages is set before rendering.
     * </p>
     *
     * @param player the viewer
     * @throws IllegalStateException if async loader is used without setting total pages
     */
    public void render(@NotNull Player player) {
        currentViewer = player;
        
        // Validate async pagination setup
        if (asyncLoader != null && totalPagesSupplier == null && items == null) {
            throw new IllegalStateException(
                "Total pages not set for async pagination. " +
                "When using asyncLoader(), you must call totalPages() to set the total page count."
            );
        }
        
        // Calculate total pages for sync data
        if (items != null && totalPagesSupplier == null) {
            totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
        } else if (totalPagesSupplier != null) {
            totalPages = Math.max(1, totalPagesSupplier.get());
        }
        
        if (asyncLoader != null) {
            loadPageAsync(currentPage, player);
        } else {
            renderPage(player);
        }
    }
    
    // ==================== Internal Rendering ====================
    
    private void loadPageAsync(int page, @NotNull Player player) {
        if (loading) {
            return;
        }
        
        loading = true;
        
        // Show loading state
        for (int slot : contentSlots) {
            gui.slot(slot).set(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name("&7Loading...")
                .build());
        }
        
        PageRequest request = new PageRequest(page, itemsPerPage);
        
        asyncLoader.apply(request).thenAccept(loadedItems -> {
            // Schedule to main thread
            TaskUtil.runSync(() -> {
                items = loadedItems != null ? new ArrayList<>(loadedItems) : new ArrayList<>();
                loading = false;
                renderPage(player);
            });
        }).exceptionally(throwable -> {
            TaskUtil.runSync(() -> {
                loading = false;
                player.sendMessage(ColorUtil.colorize("&cFailed to load page data."));
            });
            return null;
        });
    }
    
    private void renderPage(@NotNull Player player) {
        // Clear content slots
        for (int slot : contentSlots) {
            gui.slot(slot).clear().clearClickHandler();
        }
        
        // Calculate items for current page
        List<T> pageItems;
        if (asyncLoader != null) {
            pageItems = items != null ? items : new ArrayList<>();
        } else {
            int startIndex = currentPage * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, items != null ? items.size() : 0);
            pageItems = items != null && startIndex < items.size() 
                ? items.subList(startIndex, endIndex) 
                : new ArrayList<>();
        }
        
        // Render items
        for (int i = 0; i < pageItems.size() && i < contentSlots.length; i++) {
            final T item = pageItems.get(i);
            final int slot = contentSlots[i];
            
            ItemStack rendered = itemRenderer.apply(item);
            gui.slot(slot).set(rendered);
            
            if (itemClickHandler != null) {
                gui.slot(slot).onClick(ctx -> itemClickHandler.accept(item, ctx));
            }
        }
        
        // Render navigation
        renderNavigation(player);
        
        // Refresh for viewers
        gui.refresh();
    }
    
    private void renderNavigation(@NotNull Player player) {
        // Previous button
        if (previousButtonSlot >= 0) {
            if (hasPreviousPage()) {
                gui.slot(previousButtonSlot)
                    .set(previousButtonItem)
                    .onClick(ctx -> previousPage(ctx.player()));
            } else if (previousButtonDisabledItem != null) {
                gui.slot(previousButtonSlot)
                    .set(previousButtonDisabledItem)
                    .clearClickHandler();
            } else {
                gui.slot(previousButtonSlot).clear().clearClickHandler();
            }
        }
        
        // Next button
        if (nextButtonSlot >= 0) {
            if (hasNextPage()) {
                gui.slot(nextButtonSlot)
                    .set(nextButtonItem)
                    .onClick(ctx -> nextPage(ctx.player()));
            } else if (nextButtonDisabledItem != null) {
                gui.slot(nextButtonSlot)
                    .set(nextButtonDisabledItem)
                    .clearClickHandler();
            } else {
                gui.slot(nextButtonSlot).clear().clearClickHandler();
            }
        }
        
        // Page info button
        if (pageInfoSlot >= 0 && pageInfoRenderer != null) {
            PageInfo info = new PageInfo(currentPage + 1, totalPages, itemsPerPage);
            gui.slot(pageInfoSlot).set(pageInfoRenderer.apply(info));
        }
    }
    
    // ==================== Builder ====================
    
    /**
     * Builder for creating Pagination instances.
     *
     * @param <T> the item type
     */
    public static final class Builder<T> {
        private final GUI gui;
        private final int[] contentSlots;
        
        private List<T> items;
        private Function<PageRequest, CompletableFuture<List<T>>> asyncLoader;
        private Supplier<Integer> totalPagesSupplier;
        private Function<T, ItemStack> itemRenderer;
        private BiConsumer<T, ClickContext> itemClickHandler;
        
        private int previousButtonSlot = -1;
        private int nextButtonSlot = -1;
        private int pageInfoSlot = -1;
        private ItemStack previousButtonItem;
        private ItemStack nextButtonItem;
        private ItemStack previousButtonDisabledItem;
        private ItemStack nextButtonDisabledItem;
        private Function<PageInfo, ItemStack> pageInfoRenderer;
        
        private BiConsumer<Integer, Player> pageChangeCallback;
        
        private Builder(@NotNull GUI gui, int @NotNull [] contentSlots) {
            this.gui = gui;
            this.contentSlots = contentSlots;
        }
        
        /**
         * Sets the items to paginate (synchronous).
         *
         * @param items the items
         * @return this builder
         */
        @NotNull
        public Builder<T> items(@NotNull List<T> items) {
            this.items = new ArrayList<>(items);
            return this;
        }
        
        /**
         * Sets an async loader for items.
         * <p>
         * The loader receives a {@link PageRequest} and returns a
         * {@link CompletableFuture} containing the items for that page.
         * </p>
         *
         * @param loader the async loader
         * @return this builder
         */
        @NotNull
        public Builder<T> asyncLoader(@NotNull Function<PageRequest, CompletableFuture<List<T>>> loader) {
            this.asyncLoader = loader;
            return this;
        }
        
        /**
         * Sets a supplier for the total number of pages.
         * <p>
         * Required for async loading to know the page count.
         * </p>
         *
         * @param supplier the total pages supplier
         * @return this builder
         */
        @NotNull
        public Builder<T> totalPages(@NotNull Supplier<Integer> supplier) {
            this.totalPagesSupplier = supplier;
            return this;
        }
        
        /**
         * Sets a fixed total page count.
         *
         * @param pages the total pages
         * @return this builder
         */
        @NotNull
        public Builder<T> totalPages(int pages) {
            this.totalPagesSupplier = () -> pages;
            return this;
        }
        
        /**
         * Sets the item renderer.
         *
         * @param renderer function that converts an item to an ItemStack
         * @return this builder
         */
        @NotNull
        public Builder<T> itemRenderer(@NotNull Function<T, ItemStack> renderer) {
            this.itemRenderer = renderer;
            return this;
        }
        
        /**
         * Sets the item click handler.
         *
         * @param handler handler called when an item is clicked
         * @return this builder
         */
        @NotNull
        public Builder<T> onItemClick(@NotNull BiConsumer<T, ClickContext> handler) {
            this.itemClickHandler = handler;
            return this;
        }
        
        /**
         * Sets the previous page button.
         *
         * @param slot the slot for the button
         * @param item the button item
         * @return this builder
         */
        @NotNull
        public Builder<T> previousButton(int slot, @NotNull ItemStack item) {
            this.previousButtonSlot = slot;
            this.previousButtonItem = item;
            return this;
        }
        
        /**
         * Sets the previous page button with disabled state.
         *
         * @param slot the slot
         * @param item the active button item
         * @param disabledItem the disabled button item
         * @return this builder
         */
        @NotNull
        public Builder<T> previousButton(int slot, @NotNull ItemStack item, @Nullable ItemStack disabledItem) {
            this.previousButtonSlot = slot;
            this.previousButtonItem = item;
            this.previousButtonDisabledItem = disabledItem;
            return this;
        }
        
        /**
         * Sets the next page button.
         *
         * @param slot the slot
         * @param item the button item
         * @return this builder
         */
        @NotNull
        public Builder<T> nextButton(int slot, @NotNull ItemStack item) {
            this.nextButtonSlot = slot;
            this.nextButtonItem = item;
            return this;
        }
        
        /**
         * Sets the next page button with disabled state.
         *
         * @param slot the slot
         * @param item the active button item
         * @param disabledItem the disabled button item
         * @return this builder
         */
        @NotNull
        public Builder<T> nextButton(int slot, @NotNull ItemStack item, @Nullable ItemStack disabledItem) {
            this.nextButtonSlot = slot;
            this.nextButtonItem = item;
            this.nextButtonDisabledItem = disabledItem;
            return this;
        }
        
        /**
         * Sets the page info button.
         *
         * @param slot the slot
         * @param renderer function that creates the info item from page info
         * @return this builder
         */
        @NotNull
        public Builder<T> pageInfoButton(int slot, @NotNull Function<PageInfo, ItemStack> renderer) {
            this.pageInfoSlot = slot;
            this.pageInfoRenderer = renderer;
            return this;
        }
        
        /**
         * Sets the page info button with a simple item.
         *
         * @param slot the slot
         * @param baseItem the base item (will have page info added to lore)
         * @return this builder
         */
        @NotNull
        public Builder<T> pageInfoButton(int slot, @NotNull ItemStack baseItem) {
            this.pageInfoSlot = slot;
            this.pageInfoRenderer = info -> ItemBuilder.of(baseItem)
                .addLore("", "&7Page &f" + info.currentPage + "&7/&f" + info.totalPages)
                .build();
            return this;
        }
        
        /**
         * Sets a callback for page changes.
         *
         * @param callback callback receiving page number and player
         * @return this builder
         */
        @NotNull
        public Builder<T> onPageChange(@NotNull BiConsumer<Integer, Player> callback) {
            this.pageChangeCallback = callback;
            return this;
        }
        
        /**
         * Builds the Pagination instance.
         *
         * @return the configured Pagination
         */
        @NotNull
        public Pagination<T> build() {
            Pagination<T> pagination = new Pagination<>(gui, contentSlots);
            pagination.items = items;
            pagination.asyncLoader = asyncLoader;
            pagination.totalPagesSupplier = totalPagesSupplier;
            pagination.itemRenderer = itemRenderer != null ? itemRenderer : pagination.itemRenderer;
            pagination.itemClickHandler = itemClickHandler;
            pagination.previousButtonSlot = previousButtonSlot;
            pagination.nextButtonSlot = nextButtonSlot;
            pagination.pageInfoSlot = pageInfoSlot;
            pagination.previousButtonItem = previousButtonItem;
            pagination.nextButtonItem = nextButtonItem;
            pagination.previousButtonDisabledItem = previousButtonDisabledItem;
            pagination.nextButtonDisabledItem = nextButtonDisabledItem;
            pagination.pageInfoRenderer = pageInfoRenderer != null ? pageInfoRenderer : pagination.pageInfoRenderer;
            pagination.pageChangeCallback = pageChangeCallback;
            return pagination;
        }
    }
    
    // ==================== Helper Classes ====================
    
    /**
     * Request object for async page loading.
     */
    public static final class PageRequest {
        /** The page index (0-based) */
        public final int page;
        /** The number of items per page */
        public final int itemsPerPage;
        /** The starting offset */
        public final int offset;
        
        PageRequest(int page, int itemsPerPage) {
            this.page = page;
            this.itemsPerPage = itemsPerPage;
            this.offset = page * itemsPerPage;
        }
    }
    
    /**
     * Page information for rendering page info displays.
     */
    public static final class PageInfo {
        /** The current page (1-based for display) */
        public final int currentPage;
        /** The total number of pages */
        public final int totalPages;
        /** Items per page */
        public final int itemsPerPage;
        
        PageInfo(int currentPage, int totalPages, int itemsPerPage) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.itemsPerPage = itemsPerPage;
        }
    }
}
