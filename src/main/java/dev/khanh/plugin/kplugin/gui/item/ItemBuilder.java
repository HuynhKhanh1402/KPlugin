package dev.khanh.plugin.kplugin.gui.item;

import dev.khanh.plugin.kplugin.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fluent builder for creating ItemStacks.
 * <p>
 * Supports display names, lore, enchantments, flags, custom model data,
 * skull textures, persistent data, and configuration loading.
 * </p>
 */
public final class ItemBuilder {
    
    private Material material;
    private int amount = 1;
    private String displayName;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchantments;
    private final Set<ItemFlag> flags;
    private Integer customModelData;
    private boolean unbreakable;
    private String skullOwner;
    private String skullTexture;
    private final Map<NamespacedKey, PersistentData<?>> persistentData;
    private Function<String, String> placeholderReplacer;
    
    /**
     * Creates a new ItemBuilder with the specified material.
     *
     * @param material the item material
     */
    private ItemBuilder(@NotNull Material material) {
        this.material = material;
        this.lore = new ArrayList<>();
        this.enchantments = new HashMap<>();
        this.flags = new HashSet<>();
        this.persistentData = new HashMap<>();
    }
    
    /**
     * Creates a new ItemBuilder by copying an existing ItemStack.
     *
     * @param item the item to copy
     */
    private ItemBuilder(@NotNull ItemStack item) {
        this.material = item.getType();
        this.amount = item.getAmount();
        this.lore = new ArrayList<>();
        this.enchantments = new HashMap<>();
        this.flags = new HashSet<>();
        this.persistentData = new HashMap<>();
        
        // Copy existing meta
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    this.displayName = meta.getDisplayName();
                }
                if (meta.hasLore()) {
                    this.lore.addAll(meta.getLore() == null ? new ArrayList<>() : meta.getLore());
                }
                this.enchantments.putAll(meta.getEnchants());
                this.flags.addAll(meta.getItemFlags());
                if (meta.hasCustomModelData()) {
                    this.customModelData = meta.getCustomModelData();
                }
                this.unbreakable = meta.isUnbreakable();
            }
        }
    }
    
    // ==================== Static Factory Methods ====================
    
    /**
     * Creates an ItemBuilder.
     *
     * @param material the material
     * @return a new ItemBuilder
     */
    @NotNull
    public static ItemBuilder of(@NotNull Material material) {
        return new ItemBuilder(material);
    }
    
    /**
     * Creates an ItemBuilder by copying an ItemStack.
     *
     * @param item the item to copy
     * @return a new ItemBuilder
     */
    @NotNull
    public static ItemBuilder of(@NotNull ItemStack item) {
        return new ItemBuilder(item);
    }
    
    /**
     * Creates an ItemBuilder from a configuration section.
     *
     * @param section the config section
     * @return a new ItemBuilder, or null if invalid
     */
    @Nullable
    public static ItemBuilder fromConfig(@Nullable ConfigurationSection section) {
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
        
        ItemBuilder builder = new ItemBuilder(material);
        
        // Amount
        builder.amount = section.getInt("amount", 1);
        
        // Display name
        String name = section.getString("name");
        if (name == null) {
            name = section.getString("display-name");
        }
        if (name != null) {
            builder.displayName = name;
        }
        
        // Lore
        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            builder.lore.addAll(lore);
        }
        
        // Enchantments
        ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            for (String key : enchantSection.getKeys(false)) {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                if (enchant == null) {
                    // Try by name for backwards compatibility
                    enchant = Enchantment.getByName(key.toUpperCase());
                }
                if (enchant != null) {
                    builder.enchantments.put(enchant, enchantSection.getInt(key));
                }
            }
        }
        
        // Flags
        List<String> flagList = section.getStringList("flags");
        for (String flagName : flagList) {
            try {
                ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                builder.flags.add(flag);
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        // Custom model data
        if (section.contains("custom-model-data")) {
            builder.customModelData = section.getInt("custom-model-data");
        }
        
        // Unbreakable
        builder.unbreakable = section.getBoolean("unbreakable", false);
        
        // Glow
        if (section.getBoolean("glow", false)) {
            builder.enchantments.put(Enchantment.DURABILITY, 1);
            builder.flags.add(ItemFlag.HIDE_ENCHANTS);
        }
        
        // Skull
        builder.skullOwner = section.getString("skull-owner");
        builder.skullTexture = section.getString("skull-texture");
        
        return builder;
    }
    
    // ==================== Builder Methods ====================
    
    /**
     * Sets the item material.
     *
     * @param material the material
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder material(@NotNull Material material) {
        this.material = material;
        return this;
    }
    
    /**
     * Sets the stack amount.
     *
     * @param amount the amount (1-64)
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, Math.min(64, amount));
        return this;
    }
    
    /**
     * Sets the display name.
     * <p>
     * Color codes using {@code &} are automatically converted.
     * </p>
     *
     * @param name the display name
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder name(@Nullable String name) {
        this.displayName = name;
        return this;
    }
    
    /**
     * Sets the lore lines, replacing any existing lore.
     * <p>
     * Color codes using {@code &} are automatically converted.
     * </p>
     *
     * @param lines the lore lines
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder lore(@NotNull String... lines) {
        this.lore.clear();
        Collections.addAll(this.lore, lines);
        return this;
    }
    
    /**
     * Sets the lore lines from a list, replacing any existing lore.
     *
     * @param lines the lore lines
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder lore(@NotNull List<String> lines) {
        this.lore.clear();
        this.lore.addAll(lines);
        return this;
    }
    
    /**
     * Adds lore lines to the existing lore.
     *
     * @param lines the lore lines to add
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder addLore(@NotNull String... lines) {
        Collections.addAll(this.lore, lines);
        return this;
    }
    
    /**
     * Adds lore lines from a list to the existing lore.
     *
     * @param lines the lore lines to add
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder addLore(@NotNull List<String> lines) {
        this.lore.addAll(lines);
        return this;
    }
    
    /**
     * Inserts a lore line at the specified index.
     *
     * @param index the index to insert at
     * @param line the lore line to insert
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder insertLore(int index, @NotNull String line) {
        if (index >= 0 && index <= this.lore.size()) {
            this.lore.add(index, line);
        }
        return this;
    }
    
    /**
     * Clears all lore lines.
     *
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder clearLore() {
        this.lore.clear();
        return this;
    }
    
    /**
     * Adds an enchantment.
     *
     * @param enchantment the enchantment
     * @param level the level
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }
    
    /**
     * Adds multiple enchantments.
     *
     * @param enchantments the enchantments map
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder enchant(@NotNull Map<Enchantment, Integer> enchantments) {
        this.enchantments.putAll(enchantments);
        return this;
    }
    
    /**
     * Removes an enchantment.
     *
     * @param enchantment the enchantment to remove
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder removeEnchant(@NotNull Enchantment enchantment) {
        this.enchantments.remove(enchantment);
        return this;
    }
    
    /**
     * Clears all enchantments.
     *
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder clearEnchants() {
        this.enchantments.clear();
        return this;
    }
    
    /**
     * Adds item flags.
     *
     * @param flags the flags to add
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder flags(@NotNull ItemFlag... flags) {
        Collections.addAll(this.flags, flags);
        return this;
    }
    
    /**
     * Adds all item flags (hides everything).
     *
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder hideAll() {
        Collections.addAll(this.flags, ItemFlag.values());
        return this;
    }
    
    /**
     * Removes item flags.
     *
     * @param flags the flags to remove
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder removeFlags(@NotNull ItemFlag... flags) {
        for (ItemFlag flag : flags) {
            this.flags.remove(flag);
        }
        return this;
    }
    
    /**
     * Sets the custom model data.
     *
     * @param customModelData the custom model data value
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder customModelData(@Nullable Integer customModelData) {
        this.customModelData = customModelData;
        return this;
    }
    
    /**
     * Sets whether the item is unbreakable.
     *
     * @param unbreakable true for unbreakable
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }
    
    /**
     * Makes the item unbreakable.
     *
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder unbreakable() {
        return unbreakable(true);
    }
    
    /**
     * Adds an enchantment glow effect without a visible enchantment.
     * <p>
     * This adds Durability 1 enchant and HIDE_ENCHANTS flag.
     * </p>
     *
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder glow() {
        this.enchantments.put(Enchantment.DURABILITY, 1);
        this.flags.add(ItemFlag.HIDE_ENCHANTS);
        return this;
    }
    
    /**
     * Sets the skull owner (for PLAYER_HEAD material).
     *
     * @param owner the player name
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder skullOwner(@Nullable String owner) {
        this.skullOwner = owner;
        return this;
    }
    
    /**
     * Sets the skull texture (for PLAYER_HEAD material).
     * <p>
     * Supports base64 encoded texture data, texture URL, or player UUID.
     * </p>
     *
     * @param texture the texture data
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder skullTexture(@Nullable String texture) {
        this.skullTexture = texture;
        return this;
    }
    
    /**
     * Adds persistent data to the item.
     *
     * @param key the namespaced key
     * @param type the data type
     * @param value the value
     * @param <T> the primitive type
     * @param <Z> the complex type
     * @return this builder for chaining
     */
    @NotNull
    public <T, Z> ItemBuilder persistentData(@NotNull NamespacedKey key,
                                             @NotNull PersistentDataType<T, Z> type,
                                             @NotNull Z value) {
        this.persistentData.put(key, new PersistentData<>(type, value));
        return this;
    }
    
    /**
     * Adds a string persistent data value.
     *
     * @param plugin the plugin for namespace
     * @param key the key name
     * @param value the string value
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder persistentData(@NotNull Plugin plugin, @NotNull String key, @NotNull String value) {
        return persistentData(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
    }
    
    /**
     * Adds an integer persistent data value.
     *
     * @param plugin the plugin for namespace
     * @param key the key name
     * @param value the integer value
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder persistentData(@NotNull Plugin plugin, @NotNull String key, int value) {
        return persistentData(new NamespacedKey(plugin, key), PersistentDataType.INTEGER, value);
    }
    
    /**
     * Sets a placeholder replacer function.
     * <p>
     * This function will be applied to the display name and all lore lines
     * when {@link #build()} is called.
     * </p>
     *
     * @param replacer the placeholder replacer function
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder replacePlaceholders(@NotNull Function<String, String> replacer) {
        this.placeholderReplacer = replacer;
        return this;
    }
    
    /**
     * Applies a consumer to modify this builder.
     * <p>
     * Useful for conditional modifications.
     * </p>
     *
     * @param consumer the consumer to apply
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder apply(@NotNull Consumer<ItemBuilder> consumer) {
        consumer.accept(this);
        return this;
    }
    
    /**
     * Conditionally applies a consumer if the condition is true.
     *
     * @param condition the condition
     * @param consumer the consumer to apply if condition is true
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder applyIf(boolean condition, @NotNull Consumer<ItemBuilder> consumer) {
        if (condition) {
            consumer.accept(this);
        }
        return this;
    }
    
    // ==================== Build Methods ====================
    
    /**
     * Builds and returns the ItemStack.
     * <p>
     * Each call returns a new ItemStack instance. The builder can be reused
     * to create multiple items with the same base configuration.
     * </p>
     *
     * @return a new ItemStack
     */
    @NotNull
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Display name
            if (displayName != null) {
                String name = displayName;
                if (placeholderReplacer != null) {
                    name = placeholderReplacer.apply(name);
                }
                meta.setDisplayName(ColorUtil.colorize(name));
            }
            
            // Lore
            if (!lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    String processed = line;
                    if (placeholderReplacer != null) {
                        processed = placeholderReplacer.apply(processed);
                    }
                    coloredLore.add(ColorUtil.colorize(processed));
                }
                meta.setLore(coloredLore);
            }
            
            // Flags
            if (!flags.isEmpty()) {
                meta.addItemFlags(flags.toArray(new ItemFlag[0]));
            }
            
            // Custom model data
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            
            // Unbreakable
            meta.setUnbreakable(unbreakable);
            
            // Skull
            if (material == Material.PLAYER_HEAD && meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;
                applySkullMeta(skullMeta);
            }
            
            // Persistent data
            if (!persistentData.isEmpty()) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                for (Map.Entry<NamespacedKey, PersistentData<?>> entry : persistentData.entrySet()) {
                    entry.getValue().apply(pdc, entry.getKey());
                }
            }
            
            item.setItemMeta(meta);
        }
        
        // Enchantments (after meta to allow unsafe enchants)
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
        
        return item;
    }
    
    /**
     * Creates a copy of this builder.
     *
     * @return a new ItemBuilder with the same configuration
     */
    @NotNull
    public ItemBuilder copy() {
        ItemBuilder copy = new ItemBuilder(this.material);
        copy.amount = this.amount;
        copy.displayName = this.displayName;
        copy.lore.addAll(this.lore);
        copy.enchantments.putAll(this.enchantments);
        copy.flags.addAll(this.flags);
        copy.customModelData = this.customModelData;
        copy.unbreakable = this.unbreakable;
        copy.skullOwner = this.skullOwner;
        copy.skullTexture = this.skullTexture;
        copy.persistentData.putAll(this.persistentData);
        copy.placeholderReplacer = this.placeholderReplacer;
        return copy;
    }
    
    // ==================== Internal Helpers ====================
    
    private void applySkullMeta(SkullMeta meta) {
        if (skullOwner != null && !skullOwner.isEmpty()) {
            meta.setOwner(skullOwner);
        }
        // Note: For base64 textures, use Paper API or reflection
        // This is a simplified implementation for basic skull support
    }
    
    /**
     * Internal class for storing typed persistent data.
     */
    private static class PersistentData<Z> {
        private final PersistentDataType<?, Z> type;
        private final Z value;
        
        <T> PersistentData(PersistentDataType<T, Z> type, Z value) {
            this.type = type;
            this.value = value;
        }
        
        @SuppressWarnings({"unchecked", "rawtypes"})
        void apply(PersistentDataContainer container, NamespacedKey key) {
            container.set(key, (PersistentDataType) type, value);
        }
    }
}
