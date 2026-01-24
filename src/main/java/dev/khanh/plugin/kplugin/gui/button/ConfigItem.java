package dev.khanh.plugin.kplugin.gui.button;

import dev.khanh.plugin.kplugin.item.ItemStackWrapper;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import dev.khanh.plugin.kplugin.util.LoggerUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a GUI item that can be loaded from configuration.
 * <p>
 * ConfigItem provides a comprehensive abstraction for defining GUI items
 * in YAML configuration files. It supports all standard item properties
 * including material, display name, lore, enchantments, flags, custom model
 * data, skull textures, and more. Additionally, it supports multiple slot
 * placement through single slots, ranges, and comma-separated lists.
 * </p>
 * 
 * <p><strong>YAML Example:</strong></p>
 * <pre>{@code
 * back-button:
 *   material: ARROW
 *   display-name: "&c&lGo Back"
 *   lore:
 *     - "&7Click to return"
 *     - "&7to the previous menu"
 *   slot: 0  # Single slot
 *   amount: 1
 *   enchantments:
 *     DURABILITY: 1
 *   flags:
 *     - HIDE_ENCHANTS
 *   custom-model-data: 1001
 * 
 * border-item:
 *   material: BLACK_STAINED_GLASS_PANE
 *   display-name: " "
 *   slot: "0-8"  # Range: fills slots 0 through 8
 * 
 * decoration:
 *   material: RED_STAINED_GLASS_PANE
 *   slot: "1,3,5,7"  # Comma-separated slots
 * 
 * filler:
 *   material: GRAY_STAINED_GLASS_PANE
 *   slots:  # List format (can mix numbers, ranges, comma-separated)
 *     - 10
 *     - "12-14"
 *     - "20,22,24"
 * }</pre>
 * 
 * <p><strong>Java Usage:</strong></p>
 * <pre>{@code
 * ConfigurationSection section = config.getConfigurationSection("back-button");
 * ConfigItem configItem = ConfigItem.fromConfig(section);
 * ItemStack item = configItem.build(player); // With placeholder replacement
 * 
 * // Place item in all configured slots
 * for (int slot : configItem.getSlots()) {
 *     gui.setItem(slot, item);
 * }
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class ConfigItem {
    
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final int amount;
    private final List<Integer> slots;
    private final Map<Enchantment, Integer> enchantments;
    private final Set<ItemFlag> flags;
    private final Integer customModelData;
    private final String skullOwner;
    private final String skullTexture;
    
    private ConfigItem(@NotNull Builder builder) {
        this.material = builder.material;
        this.displayName = builder.displayName;
        this.lore = builder.lore != null ? new ArrayList<>(builder.lore) : new ArrayList<>();
        this.amount = builder.amount;
        this.slots = builder.slots != null ? new ArrayList<>(builder.slots) : new ArrayList<>();
        this.enchantments = builder.enchantments != null ? new HashMap<>(builder.enchantments) : new HashMap<>();
        this.flags = builder.flags != null ? new HashSet<>(builder.flags) : new HashSet<>();
        this.customModelData = builder.customModelData;
        this.skullOwner = builder.skullOwner;
        this.skullTexture = builder.skullTexture;
    }
    
    /**
     * Gets the material type.
     *
     * @return the material
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }
    
    /**
     * Gets the display name.
     *
     * @return the display name, or null if not set
     */
    @Nullable
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the lore lines.
     *
     * @return the lore list
     */
    @NotNull
    public List<String> getLore() {
        return new ArrayList<>(lore);
    }
    
    /**
     * Gets the item amount.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }
    
    /**
     * Gets the slot indices.
     *
     * @return the slots list
     */
    @NotNull
    public List<Integer> getSlots() {
        return new ArrayList<>(slots);
    }
    
    /**
     * Gets the first slot index (for backward compatibility).
     *
     * @return the first slot, or null if no slots specified
     */
    @Nullable
    public Integer getSlot() {
        return slots.isEmpty() ? null : slots.get(0);
    }
    
    /**
     * Gets the enchantments.
     *
     * @return the enchantments map
     */
    @NotNull
    public Map<Enchantment, Integer> getEnchantments() {
        return new HashMap<>(enchantments);
    }
    
    /**
     * Gets the item flags.
     *
     * @return the flags set
     */
    @NotNull
    public Set<ItemFlag> getFlags() {
        return new HashSet<>(flags);
    }
    
    /**
     * Gets the custom model data.
     *
     * @return the custom model data, or null if not set
     */
    @Nullable
    public Integer getCustomModelData() {
        return customModelData;
    }
    
    /**
     * Gets the skull owner.
     *
     * @return the skull owner name, or null if not set
     */
    @Nullable
    public String getSkullOwner() {
        return skullOwner;
    }
    
    /**
     * Gets the skull texture.
     *
     * @return the skull texture (URL or base64), or null if not set
     */
    @Nullable
    public String getSkullTexture() {
        return skullTexture;
    }
    
    /**
     * Builds an ItemStack from this configuration.
     *
     * @return the constructed ItemStack
     */
    @NotNull
    public ItemStack build() {
        return build(text -> text);
    }
    
    /**
     * Builds an ItemStack from this configuration with placeholder replacement.
     *
     * @param placeholderReplacer function to replace placeholders in text
     * @return the constructed ItemStack
     */
    @NotNull
    public ItemStack build(@NotNull Function<String, String> placeholderReplacer) {
        ItemStackWrapper wrapper = new ItemStackWrapper(material)
            .setAmount(amount);
        
        if (displayName != null) {
            wrapper.setDisplayName(ColorUtil.colorize(placeholderReplacer.apply(displayName)));
        }
        
        if (!lore.isEmpty()) {
            List<String> processedLore = lore.stream()
                .map(placeholderReplacer)
                .map(ColorUtil::colorize)
                .collect(Collectors.toList());
            wrapper.setLore(processedLore.toArray(new String[0]));
        }
        
        if (!enchantments.isEmpty()) {
            enchantments.forEach((enchantment, level) -> 
                wrapper.addEnchant(enchantment, level));
        }
        
        if (!flags.isEmpty()) {
            wrapper.addItemFlags(flags.toArray(new ItemFlag[0]));
        }
        
        if (customModelData != null) {
            wrapper.setCustomModelData(customModelData);
        }
        
        if (skullOwner != null) {
            wrapper.setSkull(skullOwner);
        } else if (skullTexture != null) {
            wrapper.setSkull(skullTexture);
        }
        
        return wrapper.build();
    }
    
    /**
     * Creates a ConfigItem from a configuration section.
     *
     * @param section the configuration section
     * @return the parsed ConfigItem
     * @throws IllegalArgumentException if material is missing or invalid
     */
    @NotNull
    public static ConfigItem fromConfig(@NotNull ConfigurationSection section) {
        Builder builder = new Builder();
        
        // Material (required)
        String materialName = section.getString("material");
        if (materialName == null) {
            throw new IllegalArgumentException("Missing required field 'material' in config section: " + section.getCurrentPath());
        }
        
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material '" + materialName + "' in config section: " + section.getCurrentPath());
        }
        builder.material(material);
        
        // Display name (optional)
        if (section.contains("display-name")) {
            builder.displayName(section.getString("display-name"));
        }
        
        // Lore (optional)
        if (section.contains("lore")) {
            builder.lore(section.getStringList("lore"));
        }
        
        // Amount (optional, default 1)
        builder.amount(section.getInt("amount", 1));
        
        // Slot/Slots (optional) - supports single, multiple, and ranges
        if (section.contains("slot")) {
            Object slotValue = section.get("slot");
            if (slotValue instanceof Number) {
                // Single slot: slot: 0
                builder.slot(((Number) slotValue).intValue());
            } else if (slotValue instanceof String) {
                // String format: "1-3" or "1,2,3"
                parseSlotString((String) slotValue, builder, section.getCurrentPath());
            } else if (slotValue instanceof List) {
                // List format: [0, 1, 2]
                List<?> slotList = (List<?>) slotValue;
                for (Object obj : slotList) {
                    if (obj instanceof Number) {
                        builder.slot(((Number) obj).intValue());
                    } else if (obj instanceof String) {
                        parseSlotString((String) obj, builder, section.getCurrentPath());
                    }
                }
            }
        } else if (section.contains("slots")) {
            Object slotsValue = section.get("slots");
            if (slotsValue instanceof List) {
                List<?> slotList = (List<?>) slotsValue;
                for (Object obj : slotList) {
                    if (obj instanceof Number) {
                        builder.slot(((Number) obj).intValue());
                    } else if (obj instanceof String) {
                        parseSlotString((String) obj, builder, section.getCurrentPath());
                    }
                }
            } else if (slotsValue instanceof String) {
                parseSlotString((String) slotsValue, builder, section.getCurrentPath());
            }
        }
        
        // Enchantments (optional)
        if (section.contains("enchantments")) {
            ConfigurationSection enchSection = section.getConfigurationSection("enchantments");
            if (enchSection != null) {
                for (String enchName : enchSection.getKeys(false)) {
                    try {
                        Enchantment enchantment = Enchantment.getByName(enchName);
                        if (enchantment != null) {
                            int level = enchSection.getInt(enchName);
                            builder.enchantment(enchantment, level);
                        } else {
                            LoggerUtil.warning("Unknown enchantment '" + enchName + "' in config section: " + section.getCurrentPath());
                        }
                    } catch (Exception e) {
                        LoggerUtil.warning("Failed to parse enchantment '" + enchName + "': " + e.getMessage());
                    }
                }
            }
        }
        
        // Flags (optional)
        if (section.contains("flags")) {
            List<String> flagNames = section.getStringList("flags");
            for (String flagName : flagNames) {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagName);
                    builder.flag(flag);
                } catch (IllegalArgumentException e) {
                    LoggerUtil.warning("Unknown item flag '" + flagName + "' in config section: " + section.getCurrentPath());
                }
            }
        }
        
        // Custom model data (optional)
        if (section.contains("custom-model-data")) {
            builder.customModelData(section.getInt("custom-model-data"));
        }
        
        // Skull owner (optional)
        if (section.contains("skull-owner")) {
            builder.skullOwner(section.getString("skull-owner"));
        }
        
        // Skull texture (optional)
        if (section.contains("skull-texture")) {
            builder.skullTexture(section.getString("skull-texture"));
        }
        
        return builder.build();
    }
    
    /**
     * Parses a slot string that can be a range (1-3) or comma-separated values (1,2,3).
     *
     * @param slotString the slot string to parse
     * @param builder the builder to add slots to
     * @param path the config path for error messages
     */
    private static void parseSlotString(@NotNull String slotString, @NotNull Builder builder, @NotNull String path) {
        slotString = slotString.trim();
        
        // Check for range format: "1-3"
        if (slotString.contains("-")) {
            String[] parts = slotString.split("-");
            if (parts.length == 2) {
                try {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    if (start > end) {
                        LoggerUtil.warning("Invalid slot range '" + slotString + "' (start > end) in config section: " + path);
                        return;
                    }
                    for (int i = start; i <= end; i++) {
                        builder.slot(i);
                    }
                } catch (NumberFormatException e) {
                    LoggerUtil.warning("Invalid slot range format '" + slotString + "' in config section: " + path);
                }
            } else {
                LoggerUtil.warning("Invalid slot range format '" + slotString + "' in config section: " + path);
            }
        }
        // Check for comma-separated format: "1,2,3"
        else if (slotString.contains(",")) {
            String[] parts = slotString.split(",");
            for (String part : parts) {
                try {
                    int slot = Integer.parseInt(part.trim());
                    builder.slot(slot);
                } catch (NumberFormatException e) {
                    LoggerUtil.warning("Invalid slot number '" + part.trim() + "' in config section: " + path);
                }
            }
        }
        // Single slot as string: "5"
        else {
            try {
                int slot = Integer.parseInt(slotString);
                builder.slot(slot);
            } catch (NumberFormatException e) {
                LoggerUtil.warning("Invalid slot number '" + slotString + "' in config section: " + path);
            }
        }
    }
    
    /**
     * Creates a ConfigItem from an ItemStack.
     *
     * @param item the ItemStack
     * @return the ConfigItem
     */
    @NotNull
    public static ConfigItem fromItemStack(@NotNull ItemStack item) {
        Builder builder = new Builder()
            .material(item.getType())
            .amount(item.getAmount());
        
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                builder.displayName(item.getItemMeta().getDisplayName());
            }
            
            if (item.getItemMeta().hasLore()) {
                builder.lore(item.getItemMeta().getLore());
            }
            
            if (item.getItemMeta().hasCustomModelData()) {
                builder.customModelData(item.getItemMeta().getCustomModelData());
            }
            
            if (!item.getItemMeta().getItemFlags().isEmpty()) {
                builder.flags(item.getItemMeta().getItemFlags());
            }
        }
        
        if (item.hasItemMeta() && !item.getItemMeta().getEnchants().isEmpty()) {
            builder.enchantments(item.getItemMeta().getEnchants());
        }
        
        return builder.build();
    }
    
    /**
     * Builder for creating ConfigItem instances.
     */
    public static class Builder {
        private Material material;
        private String displayName;
        private List<String> lore;
        private int amount = 1;
        private List<Integer> slots;
        private Map<Enchantment, Integer> enchantments;
        private Set<ItemFlag> flags;
        private Integer customModelData;
        private String skullOwner;
        private String skullTexture;
        
        /**
         * Sets the material.
         *
         * @param material the material
         * @return this builder
         */
        @NotNull
        public Builder material(@NotNull Material material) {
            this.material = material;
            return this;
        }
        
        /**
         * Sets the display name.
         *
         * @param displayName the display name
         * @return this builder
         */
        @NotNull
        public Builder displayName(@Nullable String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        /**
         * Sets the lore.
         *
         * @param lore the lore lines
         * @return this builder
         */
        @NotNull
        public Builder lore(@NotNull List<String> lore) {
            this.lore = new ArrayList<>(lore);
            return this;
        }
        
        /**
         * Adds a lore line.
         *
         * @param line the lore line
         * @return this builder
         */
        @NotNull
        public Builder addLore(@NotNull String line) {
            if (this.lore == null) {
                this.lore = new ArrayList<>();
            }
            this.lore.add(line);
            return this;
        }
        
        /**
         * Sets the amount.
         *
         * @param amount the amount
         * @return this builder
         */
        @NotNull
        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }
        
        /**
         * Adds a slot.
         *
         * @param slot the slot index
         * @return this builder
         */
        @NotNull
        public Builder slot(int slot) {
            if (this.slots == null) {
                this.slots = new ArrayList<>();
            }
            this.slots.add(slot);
            return this;
        }
        
        /**
         * Sets the slots.
         *
         * @param slots the slot indices
         * @return this builder
         */
        @NotNull
        public Builder slots(@NotNull List<Integer> slots) {
            this.slots = new ArrayList<>(slots);
            return this;
        }
        
        /**
         * Sets the enchantments.
         *
         * @param enchantments the enchantments map
         * @return this builder
         */
        @NotNull
        public Builder enchantments(@NotNull Map<Enchantment, Integer> enchantments) {
            this.enchantments = new HashMap<>(enchantments);
            return this;
        }
        
        /**
         * Adds an enchantment.
         *
         * @param enchantment the enchantment
         * @param level the level
         * @return this builder
         */
        @NotNull
        public Builder enchantment(@NotNull Enchantment enchantment, int level) {
            if (this.enchantments == null) {
                this.enchantments = new HashMap<>();
            }
            this.enchantments.put(enchantment, level);
            return this;
        }
        
        /**
         * Sets the flags.
         *
         * @param flags the flags set
         * @return this builder
         */
        @NotNull
        public Builder flags(@NotNull Set<ItemFlag> flags) {
            this.flags = new HashSet<>(flags);
            return this;
        }
        
        /**
         * Adds a flag.
         *
         * @param flag the flag
         * @return this builder
         */
        @NotNull
        public Builder flag(@NotNull ItemFlag flag) {
            if (this.flags == null) {
                this.flags = new HashSet<>();
            }
            this.flags.add(flag);
            return this;
        }
        
        /**
         * Sets the custom model data.
         *
         * @param customModelData the custom model data
         * @return this builder
         */
        @NotNull
        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }
        
        /**
         * Sets the skull owner.
         *
         * @param skullOwner the skull owner name
         * @return this builder
         */
        @NotNull
        public Builder skullOwner(@NotNull String skullOwner) {
            this.skullOwner = skullOwner;
            return this;
        }
        
        /**
         * Sets the skull texture.
         *
         * @param skullTexture the skull texture (URL or base64)
         * @return this builder
         */
        @NotNull
        public Builder skullTexture(@NotNull String skullTexture) {
            this.skullTexture = skullTexture;
            return this;
        }
        
        /**
         * Builds the ConfigItem.
         *
         * @return the ConfigItem
         * @throws IllegalStateException if material is not set
         */
        @NotNull
        public ConfigItem build() {
            if (material == null) {
                throw new IllegalStateException("Material must be set");
            }
            return new ConfigItem(this);
        }
    }
}
