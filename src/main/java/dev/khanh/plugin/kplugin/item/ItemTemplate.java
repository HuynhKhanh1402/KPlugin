package dev.khanh.plugin.kplugin.item;

import dev.khanh.plugin.kplugin.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Template for creating items from configuration with placeholder support.
 * <p>
 * Stores raw configuration strings and applies placeholders at build time.
 * Includes slot configuration for GUI placement.
 * </p>
 *
 * @see ItemBuilder
 */
public final class ItemTemplate {
    
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int amount;
    private final List<Integer> slots;
    private final Map<Enchantment, Integer> enchantments;
    private final Set<ItemFlag> flags;
    private final Integer customModelData;
    private final boolean glow;
    private final boolean unbreakable;
    private final String skullValue;
    
    private ItemTemplate(Builder builder) {
        this.material = builder.material;
        this.name = builder.name;
        this.lore = builder.lore != null ? new ArrayList<>(builder.lore) : new ArrayList<>();
        this.amount = builder.amount;
        this.slots = builder.slots != null ? new ArrayList<>(builder.slots) : new ArrayList<>();
        this.enchantments = builder.enchantments != null ? new HashMap<>(builder.enchantments) : new HashMap<>();
        this.flags = builder.flags != null ? new HashSet<>(builder.flags) : new HashSet<>();
        this.customModelData = builder.customModelData;
        this.glow = builder.glow;
        this.unbreakable = builder.unbreakable;
        this.skullValue = builder.skullValue;
    }
    
    // ==================== Static Factory Methods ====================
    
    /**
     * Creates an ItemTemplate from a configuration section.
     *
     * @param section the configuration section
     * @return a new ItemTemplate, or null if invalid
     */
    @Nullable
    public static ItemTemplate fromConfig(@Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        String materialName = section.getString("material");
        if (materialName == null) {
            return null;
        }
        
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            return null;
        }
        
        Builder builder = new Builder(material);
        
        // Display name
        String name = section.getString("name");
        if (name == null) {
            name = section.getString("display-name");
        }
        builder.name = name;
        
        // Lore
        builder.lore = section.getStringList("lore");
        
        // Amount
        builder.amount = section.getInt("amount", 1);
        
        // Slots parsing
        builder.slots = parseSlots(section);
        
        // Enchantments
        ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            builder.enchantments = new HashMap<>();
            for (String key : enchantSection.getKeys(false)) {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                if (enchant == null) {
                    enchant = Enchantment.getByName(key.toUpperCase());
                }
                if (enchant != null) {
                    builder.enchantments.put(enchant, enchantSection.getInt(key));
                }
            }
        }
        
        // Flags
        List<String> flagList = section.getStringList("flags");
        if (!flagList.isEmpty()) {
            builder.flags = new HashSet<>();
            for (String flagName : flagList) {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                    builder.flags.add(flag);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        
        // Other properties
        if (section.contains("custom-model-data")) {
            builder.customModelData = section.getInt("custom-model-data");
        }
        builder.glow = section.getBoolean("glow", false);
        builder.unbreakable = section.getBoolean("unbreakable", false);
        // Skull handling (supports "skull", "skull-owner", "skull-texture")
        String skullValue = section.getString("skull");
        if (skullValue == null || skullValue.isEmpty()) {
            skullValue = section.getString("skull-owner");
        }
        if (skullValue == null || skullValue.isEmpty()) {
            skullValue = section.getString("skull-texture");
        }
        builder.skullValue = skullValue;
        
        return builder.build();
    }
    
    /**
     * Creates a builder for programmatic template construction.
     *
     * @param material the material
     * @return a new builder
     */
    @NotNull
    public static Builder builder(@NotNull Material material) {
        return new Builder(material);
    }
    
    // ==================== Getters ====================
    
    /**
     * Gets the material.
     *
     * @return the material
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }
    
    /**
     * Gets the display name (raw, with placeholders).
     *
     * @return the display name, or null if not set
     */
    @Nullable
    public String getName() {
        return name;
    }
    
    /**
     * Gets the lore lines (raw, with placeholders).
     *
     * @return a copy of the lore list
     */
    @NotNull
    public List<String> getLore() {
        return new ArrayList<>(lore);
    }
    
    /**
     * Gets the stack amount.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }
    
    /**
     * Gets the configured slots.
     *
     * @return a copy of the slots list
     */
    @NotNull
    public List<Integer> getSlots() {
        return new ArrayList<>(slots);
    }
    
    /**
     * Gets the first configured slot.
     *
     * @return the first slot, or -1 if none configured
     */
    public int getSlot() {
        return slots.isEmpty() ? -1 : slots.get(0);
    }
    
    /**
     * Checks if this template has configured slots.
     *
     * @return true if at least one slot is configured
     */
    public boolean hasSlots() {
        return !slots.isEmpty();
    }
    
    /**
     * Gets whether glow is enabled.
     *
     * @return true if glow is enabled
     */
    public boolean isGlow() {
        return glow;
    }
    
    /**
     * Gets whether the item is unbreakable.
     *
     * @return true if unbreakable
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }
    
    // ==================== Build Methods ====================
    
    /**
     * Builds an ItemStack without placeholder replacement.
     *
     * @return a new ItemStack
     */
    @NotNull
    public ItemStack build() {
        return build(Function.identity());
    }
    
    /**
     * Builds an ItemStack with placeholder replacement using a map.
     * <p>
     * Simple map-based replacement for quick use cases.
     * </p>
     *
     * @param placeholders map of placeholder key to value
     * @return a new ItemStack
     */
    @NotNull
    public ItemStack build(@NotNull Map<String, String> placeholders) {
        return build(text -> {
            String result = text;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
            return result;
        });
    }
    
    /**
     * Builds an ItemStack with Placeholders system.
     * <p>
     * Uses the flexible {@link dev.khanh.plugin.kplugin.placeholder.Placeholders} system.
     * </p>
     *
     * @param placeholders the Placeholders instance
     * @return a new ItemStack
     * @see dev.khanh.plugin.kplugin.placeholder.Placeholders
     */
    @NotNull
    public ItemStack build(@NotNull dev.khanh.plugin.kplugin.placeholder.Placeholders placeholders) {
        return build(placeholders.toFunction());
    }
    
    /**
     * Builds an ItemStack with Placeholders system and player context.
     *
     * @param placeholders the Placeholders instance
     * @param player the player for player-aware placeholders
     * @return a new ItemStack
     * @see dev.khanh.plugin.kplugin.placeholder.Placeholders
     */
    @NotNull
    public ItemStack build(@NotNull dev.khanh.plugin.kplugin.placeholder.Placeholders placeholders,
                           @Nullable org.bukkit.entity.Player player) {
        return build(placeholders.toFunction(player));
    }
    
    /**
     * Builds an ItemStack with placeholder replacement.
     *
     * @param placeholderReplacer function to replace placeholders in strings
     * @return a new ItemStack
     */
    @NotNull
    public ItemStack build(@NotNull Function<String, String> placeholderReplacer) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Display name
            if (name != null) {
                String processedName = ColorUtil.colorize(placeholderReplacer.apply(name));
                meta.setDisplayName(processedName);
            }
            
            // Lore
            if (!lore.isEmpty()) {
                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    processedLore.add(ColorUtil.colorize(placeholderReplacer.apply(line)));
                }
                meta.setLore(processedLore);
            }
            
            // Flags
            if (!flags.isEmpty()) {
                meta.addItemFlags(flags.toArray(new ItemFlag[0]));
            }
            
            // Glow effect
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            // Custom model data
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            
            // Unbreakable
            meta.setUnbreakable(unbreakable);
            
            item.setItemMeta(meta);
        }
        
        // Enchantments (after meta to allow unsafe enchants)
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
        
        return item;
    }
    
    /**
     * Creates an ItemBuilder from this template.
     * <p>
     * This is useful when you need to make additional modifications
     * beyond what the template provides.
     * </p>
     *
     * @return a new ItemBuilder with this template's configuration
     */
    @NotNull
    public ItemBuilder toBuilder() {
        ItemBuilder builder = ItemBuilder.of(material).amount(amount);
        
        if (name != null) {
            builder.name(name);
        }
        if (!lore.isEmpty()) {
            builder.lore(lore);
        }
        if (!enchantments.isEmpty()) {
            builder.enchant(enchantments);
        }
        if (!flags.isEmpty()) {
            for (ItemFlag flag : flags) {
                builder.flags(flag);
            }
        }
        if (customModelData != null) {
            builder.customModelData(customModelData);
        }
        if (glow) {
            builder.glow();
        }
        if (unbreakable) {
            builder.unbreakable();
        }
        if (skullValue != null) {
            builder.skull(skullValue);
        }
        
        return builder;
    }
    
    // ==================== Slot Parsing ====================
    
    /**
     * Parses slots from a configuration section.
     * <p>
     * Supports multiple formats:
     * <ul>
     *   <li>Single integer: {@code slot: 13}</li>
     *   <li>Range notation: {@code slot: "0-8"}</li>
     *   <li>Comma-separated: {@code slot: "1,3,5,7"}</li>
     *   <li>List format: {@code slots: [10, "12-14", "20,22"]}</li>
     * </ul>
     * </p>
     */
    private static List<Integer> parseSlots(ConfigurationSection section) {
        List<Integer> slots = new ArrayList<>();
        
        // Try "slot" key first (single value)
        if (section.contains("slot")) {
            Object slotValue = section.get("slot");
            if (slotValue instanceof Integer) {
                slots.add((Integer) slotValue);
            } else if (slotValue instanceof String) {
                slots.addAll(parseSlotString((String) slotValue));
            }
        }
        
        // Try "slots" key (list)
        if (section.contains("slots")) {
            List<?> slotsList = section.getList("slots");
            if (slotsList != null) {
                for (Object obj : slotsList) {
                    if (obj instanceof Integer) {
                        slots.add((Integer) obj);
                    } else if (obj instanceof String) {
                        slots.addAll(parseSlotString((String) obj));
                    } else if (obj instanceof Number) {
                        slots.add(((Number) obj).intValue());
                    }
                }
            }
        }
        
        return slots;
    }
    
    /**
     * Parses a slot string that may be a range or comma-separated list.
     */
    private static List<Integer> parseSlotString(String input) {
        List<Integer> slots = new ArrayList<>();
        
        // Check for comma-separated values
        if (input.contains(",")) {
            String[] parts = input.split(",");
            for (String part : parts) {
                slots.addAll(parseSlotString(part.trim()));
            }
            return slots;
        }
        
        // Check for range notation
        if (input.contains("-")) {
            String[] parts = input.split("-");
            if (parts.length == 2) {
                try {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
                        slots.add(i);
                    }
                    return slots;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        // Try as single integer
        try {
            slots.add(Integer.parseInt(input.trim()));
        } catch (NumberFormatException ignored) {
        }
        
        return slots;
    }
    
    // ==================== Builder Class ====================
    
    /**
     * Builder for creating ItemTemplate instances programmatically.
     */
    public static final class Builder {
        private final Material material;
        private String name;
        private List<String> lore;
        private int amount = 1;
        private List<Integer> slots;
        private Map<Enchantment, Integer> enchantments;
        private Set<ItemFlag> flags;
        private Integer customModelData;
        private boolean glow;
        private boolean unbreakable;
        private String skullValue;
        
        private Builder(@NotNull Material material) {
            this.material = material;
        }
        
        @NotNull
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }
        
        @NotNull
        public Builder lore(@NotNull String... lines) {
            this.lore = new ArrayList<>(Arrays.asList(lines));
            return this;
        }
        
        @NotNull
        public Builder lore(@NotNull List<String> lines) {
            this.lore = new ArrayList<>(lines);
            return this;
        }
        
        @NotNull
        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }
        
        @NotNull
        public Builder slot(int slot) {
            if (this.slots == null) {
                this.slots = new ArrayList<>();
            }
            this.slots.add(slot);
            return this;
        }
        
        @NotNull
        public Builder slots(int... slots) {
            if (this.slots == null) {
                this.slots = new ArrayList<>();
            }
            for (int slot : slots) {
                this.slots.add(slot);
            }
            return this;
        }
        
        @NotNull
        public Builder enchant(@NotNull Enchantment enchant, int level) {
            if (this.enchantments == null) {
                this.enchantments = new HashMap<>();
            }
            this.enchantments.put(enchant, level);
            return this;
        }
        
        @NotNull
        public Builder flags(@NotNull ItemFlag... flags) {
            if (this.flags == null) {
                this.flags = new HashSet<>();
            }
            Collections.addAll(this.flags, flags);
            return this;
        }
        
        @NotNull
        public Builder customModelData(int data) {
            this.customModelData = data;
            return this;
        }
        
        @NotNull
        public Builder glow(boolean glow) {
            this.glow = glow;
            return this;
        }
        
        @NotNull
        public Builder glow() {
            return glow(true);
        }
        
        @NotNull
        public Builder unbreakable(boolean unbreakable) {
            this.unbreakable = unbreakable;
            return this;
        }
        
        @NotNull
        public Builder unbreakable() {
            return unbreakable(true);
        }
        
        @NotNull
        public Builder skull(@Nullable String value) {
            this.skullValue = value;
            return this;
        }
        
        @NotNull
        public ItemTemplate build() {
            return new ItemTemplate(this);
        }
    }
}
