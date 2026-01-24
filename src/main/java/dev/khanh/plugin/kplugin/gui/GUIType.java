package dev.khanh.plugin.kplugin.gui;

/**
 * Represents the different types of inventories that can be used for GUIs.
 * <p>
 * This enum provides mappings to Bukkit's inventory types with their
 * corresponding row sizes and display titles.
 * </p>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public enum GUIType {
    /**
     * Standard chest inventory with 1 row (9 slots)
     */
    CHEST_1_ROW(9, "Chest"),
    
    /**
     * Standard chest inventory with 2 rows (18 slots)
     */
    CHEST_2_ROWS(18, "Chest"),
    
    /**
     * Standard chest inventory with 3 rows (27 slots)
     */
    CHEST_3_ROWS(27, "Chest"),
    
    /**
     * Standard chest inventory with 4 rows (36 slots)
     */
    CHEST_4_ROWS(36, "Chest"),
    
    /**
     * Standard chest inventory with 5 rows (45 slots)
     */
    CHEST_5_ROWS(45, "Chest"),
    
    /**
     * Standard chest inventory with 6 rows (54 slots)
     */
    CHEST_6_ROWS(54, "Chest"),
    
    /**
     * Hopper inventory (5 slots in a single row)
     */
    HOPPER(5, "Hopper"),
    
    /**
     * Dispenser/Dropper inventory (3x3 grid, 9 slots)
     */
    DISPENSER(9, "Dispenser");
    
    private final int size;
    private final String defaultTitle;
    
    GUIType(int size, String defaultTitle) {
        this.size = size;
        this.defaultTitle = defaultTitle;
    }
    
    /**
     * Gets the number of slots in this inventory type.
     *
     * @return the inventory size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Gets the default title for this inventory type.
     *
     * @return the default title
     */
    public String getDefaultTitle() {
        return defaultTitle;
    }
    
    /**
     * Gets the number of rows for chest-type inventories.
     *
     * @return the number of rows, or 1 for non-chest types
     */
    public int getRows() {
        if (this == HOPPER || this == DISPENSER) {
            return 1;
        }
        return size / 9;
    }
    
    /**
     * Gets a chest GUI type by number of rows.
     *
     * @param rows the number of rows (1-6)
     * @return the corresponding GUIType
     * @throws IllegalArgumentException if rows is not between 1 and 6
     */
    public static GUIType getByRows(int rows) {
        switch (rows) {
            case 1: return CHEST_1_ROW;
            case 2: return CHEST_2_ROWS;
            case 3: return CHEST_3_ROWS;
            case 4: return CHEST_4_ROWS;
            case 5: return CHEST_5_ROWS;
            case 6: return CHEST_6_ROWS;
            default: throw new IllegalArgumentException("Rows must be between 1 and 6, got: " + rows);
        }
    }
    
    /**
     * Gets a GUI type by size.
     *
     * @param size the inventory size
     * @return the corresponding GUIType
     * @throws IllegalArgumentException if size doesn't match any type
     */
    public static GUIType getBySize(int size) {
        for (GUIType type : values()) {
            if (type.size == size) {
                return type;
            }
        }
        throw new IllegalArgumentException("No GUIType found for size: " + size);
    }
}
