package dev.khanh.plugin.kplugin.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Fluent builder for creating ItemStacks.
 * <p>
 * Supports display names, lore, enchantments, flags, custom model data,
 * skull textures (player names, UUIDs, URLs, base64), and configuration loading.
 * </p>
 * <p>
 * <strong>Skull Support:</strong> The {@link #skull(String)} method accepts multiple formats:
 * </p>
 * <ul>
 *   <li>Player names (2-16 alphanumeric characters): {@code "Notch"}</li>
 *   <li>Player UUIDs: {@code "069a79f4-44e9-4726-a5be-fca90e38aaf5"}</li>
 *   <li>Texture URLs: {@code "http://textures.minecraft.net/texture/..."}</li>
 *   <li>Base64 texture data: Any other string is treated as base64</li>
 * </ul>
 */
public final class ItemBuilder {
    
    private static final Pattern PLAYER_NAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
    private static final Pattern UUID_REGEX = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    
    private Material material;
    private int amount = 1;
    private String displayName;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchantments;
    private final Set<ItemFlag> flags;
    private Integer customModelData;
    private boolean unbreakable;
    private String skullValue;
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
        
        // Material handling (supports both "material" and "skull" keys)
        Material material;
        String skullValue = section.getString("skull");
        if (skullValue != null && !skullValue.isEmpty()) {
            material = Material.PLAYER_HEAD;
        } else {
            String materialName = section.getString("material");
            if (materialName == null) {
                return null;
            }
            material = Material.matchMaterial(materialName);
            if (material == null) {
                return null;
            }
        }
        
        ItemBuilder builder = new ItemBuilder(material);
        
        // Amount
        builder.amount = section.getInt("amount", 1);
        
        // Display name (supports both "name" and "display-name")
        String name = section.getString("name");
        if (name == null) {
            name = section.getString("display-name");
        }
        if (name != null) {
            builder.displayName = name;
        }
        
        // Lore
        if (section.contains("lore")) {
            builder.lore.addAll(section.getStringList("lore"));
        }
        
        // Enchantments
        ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
        if (enchantSection != null) {
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
        
        // Glow effect
        if (section.getBoolean("glow", false)) {
            builder.enchantments.put(Enchantment.DURABILITY, 1);
            builder.flags.add(ItemFlag.HIDE_ENCHANTS);
        }
        
        // Unbreakable
        builder.unbreakable = section.getBoolean("unbreakable", false);
        
        // Skull handling (supports "skull", "skull-owner", "skull-texture")
        if (skullValue != null && !skullValue.isEmpty()) {
            builder.skullValue = skullValue;
        } else {
            String skullOwner = section.getString("skull-owner");
            if (skullOwner != null && !skullOwner.isEmpty()) {
                builder.skullValue = skullOwner;
            } else {
                String skullTexture = section.getString("skull-texture");
                if (skullTexture != null && !skullTexture.isEmpty()) {
                    builder.skullValue = skullTexture;
                }
            }
        }
        
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
        this.amount = amount;
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
     * Sets the skull texture or owner (for PLAYER_HEAD material).
     * <p>
     * Accepts multiple formats:
     * </p>
     * <ul>
     *   <li>Player name (e.g., "Notch")</li>
     *   <li>Player UUID (e.g., "069a79f4-44e9-4726-a5be-fca90e38aaf5")</li>
     *   <li>Texture URL (e.g., "http://textures.minecraft.net/texture/...")</li>
     *   <li>Base64 encoded texture data</li>
     * </ul>
     *
     * @param value the skull value (player name, UUID, URL, or base64)
     * @return this builder for chaining
     */
    @NotNull
    public ItemBuilder skull(@Nullable String value) {
        this.skullValue = value;
        return this;
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
    public ItemBuilder modify(@NotNull Consumer<ItemBuilder> consumer) {
        consumer.accept(this);
        return this;
    }
    
    // ==================== Build Method ====================
    
    /**
     * Builds the final ItemStack.
     *
     * @return a new ItemStack
     */
    @NotNull
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Apply placeholder replacer
            Function<String, String> replacer = placeholderReplacer != null ?
                    placeholderReplacer : Function.identity();
            
            // Display name
            if (displayName != null) {
                String processedName = ColorUtil.colorize(replacer.apply(displayName));
                meta.setDisplayName(processedName);
            }
            
            // Lore
            if (!lore.isEmpty()) {
                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    processedLore.add(ColorUtil.colorize(replacer.apply(line)));
                }
                meta.setLore(processedLore);
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
            
            // Skull handling
            if (skullValue != null && meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;
                applySkullValue(skullMeta, skullValue);
            }
            
            item.setItemMeta(meta);
        }
        
        // Enchantments (applied after meta to support unsafe enchants)
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
        
        return item;
    }
    
    // ==================== Skull Helper Methods ====================
    
    /**
     * Applies skull value to skull meta.
     * <p>
     * Automatically detects format:
     * <ul>
     *   <li>Player name pattern: Sets as owner</li>
     *   <li>UUID pattern: Fetches player and sets as owner</li>
     *   <li>URL pattern: Converts to base64 and applies texture</li>
     *   <li>Otherwise: Treats as base64 texture</li>
     * </ul>
     * </p>
     */
    private void applySkullValue(@NotNull SkullMeta skullMeta, @NotNull String value) {
        if (value.isEmpty()) {
            return;
        }
        
        // Check for player name
        if (PLAYER_NAME_REGEX.matcher(value).matches()) {
            setSkullByPlayerName(skullMeta, value);
            return;
        }
        
        // Check for UUID
        if (UUID_REGEX.matcher(value).matches()) {
            try {
                UUID uuid = UUID.fromString(value);
                setSkullByUUID(skullMeta, uuid);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        // Check for URL
        if (value.startsWith("http://") || value.startsWith("https://")) {
            setSkullByUrl(skullMeta, value);
            return;
        }
        
        // Treat as base64
        setSkullByBase64(skullMeta, value);
    }
    
    /**
     * Sets skull by player name.
     */
    private void setSkullByPlayerName(@NotNull SkullMeta skullMeta, @NotNull String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        skullMeta.setOwningPlayer(offlinePlayer);
    }
    
    /**
     * Sets skull by player UUID.
     */
    private void setSkullByUUID(@NotNull SkullMeta skullMeta, @NotNull UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        skullMeta.setOwningPlayer(offlinePlayer);
    }
    
    /**
     * Sets skull by texture URL.
     */
    private void setSkullByUrl(@NotNull SkullMeta skullMeta, @NotNull String url) {
        String base64 = urlToBase64(url);
        setSkullByBase64(skullMeta, base64);
    }
    
    /**
     * Converts texture URL to base64.
     */
    private String urlToBase64(@NotNull String url) {
        String json = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", url);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }
    
    /**
     * Sets skull by base64 texture.
     */
    private void setSkullByBase64(@NotNull SkullMeta skullMeta, @NotNull String base64) {
        try {
            UUID randomUuid = UUID.randomUUID();
            PlayerProfile profile = Bukkit.createProfile(randomUuid);
            ProfileProperty property = new ProfileProperty("textures", base64);
            profile.setProperty(property);
            skullMeta.setPlayerProfile(profile);
        } catch (Exception e) {
            // Fallback: Try legacy method if available
            try {
                UUID randomUuid = UUID.randomUUID();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(randomUuid));
            } catch (Exception ignored) {
            }
        }
    }
}
