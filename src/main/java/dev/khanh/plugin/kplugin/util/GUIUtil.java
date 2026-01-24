package dev.khanh.plugin.kplugin.util;

import dev.khanh.plugin.kplugin.gui.GUIType;
import dev.khanh.plugin.kplugin.item.ItemStackWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for GUI-related helper methods.
 * <p>
 * Provides convenience methods for creating common GUI elements
 * like filler items, borders, and navigation buttons.
 * </p>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class GUIUtil {
    
    /**
     * Creates a glass pane filler item.
     *
     * @param material the glass pane material
     * @param name the display name (supports color codes)
     * @return the filler ItemStack
     */
    @NotNull
    public static ItemStack createFiller(@NotNull Material material, @NotNull String name) {
        return new ItemStackWrapper(material)
            .setDisplayName(name)
            .build();
    }
    
    /**
     * Creates a glass pane filler item with no name.
     *
     * @param material the glass pane material
     * @return the filler ItemStack
     */
    @NotNull
    public static ItemStack createFiller(@NotNull Material material) {
        return createFiller(material, " ");
    }
    
    /**
     * Creates a black glass pane filler.
     *
     * @return the filler ItemStack
     */
    @NotNull
    public static ItemStack createBlackFiller() {
        return createFiller(Material.BLACK_STAINED_GLASS_PANE, " ");
    }
    
    /**
     * Creates a gray glass pane filler.
     *
     * @return the filler ItemStack
     */
    @NotNull
    public static ItemStack createGrayFiller() {
        return createFiller(Material.GRAY_STAINED_GLASS_PANE, " ");
    }
    
    /**
     * Creates a white glass pane filler.
     *
     * @return the filler ItemStack
     */
    @NotNull
    public static ItemStack createWhiteFiller() {
        return createFiller(Material.WHITE_STAINED_GLASS_PANE, " ");
    }
    
    /**
     * Calculates the center slot for a GUI type.
     *
     * @param type the GUI type
     * @return the center slot index
     */
    public static int getCenterSlot(@NotNull GUIType type) {
        int size = type.getSize();
        int rows = type.getRows();
        
        if (rows == 1) {
            return size / 2;
        }
        
        int centerRow = rows / 2;
        return centerRow * 9 + 4;
    }
    
    /**
     * Gets the slot indices for a GUI border.
     *
     * @param type the GUI type
     * @return array of border slot indices
     */
    @NotNull
    public static int[] getBorderSlots(@NotNull GUIType type) {
        int rows = type.getRows();
        int size = type.getSize();
        
        if (rows == 1) {
            // For single row, use first and last slots
            return new int[]{0, size - 1};
        }
        
        // Calculate border slots
        int borderSize = (rows * 2 + 7) * 2 - 4;
        int[] slots = new int[borderSize];
        int index = 0;
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            slots[index++] = i;
            if (rows > 1) {
                slots[index++] = size - 9 + i;
            }
        }
        
        // Side columns
        if (rows > 2) {
            for (int row = 1; row < rows - 1; row++) {
                slots[index++] = row * 9;
                slots[index++] = row * 9 + 8;
            }
        }
        
        return slots;
    }
    
    /**
     * Gets the slot indices for GUI content area (excluding borders).
     *
     * @param type the GUI type
     * @return array of content slot indices
     */
    @NotNull
    public static int[] getContentSlots(@NotNull GUIType type) {
        int rows = type.getRows();
        int size = type.getSize();
        
        if (rows <= 2) {
            // For small GUIs, use all except first and last
            int[] slots = new int[size - 2];
            for (int i = 1; i < size - 1; i++) {
                slots[i - 1] = i;
            }
            return slots;
        }
        
        // For larger GUIs, exclude border
        int contentRows = rows - 2;
        int contentCols = 7;
        int contentSize = contentRows * contentCols;
        int[] slots = new int[contentSize];
        int index = 0;
        
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                slots[index++] = row * 9 + col;
            }
        }
        
        return slots;
    }
    
    /**
     * Converts a row and column to a slot index.
     *
     * @param row the row (0-based)
     * @param col the column (0-based)
     * @return the slot index
     */
    public static int getSlot(int row, int col) {
        return row * 9 + col;
    }
    
    /**
     * Gets the row for a slot index.
     *
     * @param slot the slot index
     * @return the row (0-based)
     */
    public static int getRow(int slot) {
        return slot / 9;
    }
    
    /**
     * Gets the column for a slot index.
     *
     * @param slot the slot index
     * @return the column (0-based)
     */
    public static int getColumn(int slot) {
        return slot % 9;
    }
}
