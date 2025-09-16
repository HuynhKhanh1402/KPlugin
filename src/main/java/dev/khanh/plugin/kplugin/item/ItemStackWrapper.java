package dev.khanh.plugin.kplugin.item;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.khanh.plugin.kplugin.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;


public class ItemStackWrapper {
    private final ItemStack itemStack;

    private static final Cache<String, OfflinePlayer> CACHED_OFFLINE_PLAYERS = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private static final Pattern PLAYER_NAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    /**
     * Constructor to create an ItemStackWrapper from a Material type.
     *
     * @param material The Material type to wrap in an ItemStack.
     */
    public ItemStackWrapper(Material material) {
        this.itemStack = new ItemStack(material);
    }

    /**
     * Constructor to create an ItemStackWrapper from an existing ItemStack.
     *
     * @param itemStack The ItemStack to wrap.
     */
    public ItemStackWrapper(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    /**
     * Sets the display name of the item.
     *
     * @param name The display name to set.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper setDisplayName(String name) {
        setItemMeta(meta -> meta.setDisplayName(name));
        return this;
    }

    /**
     * Sets the lore of the item, replacing any existing lore.
     *
     * @param lines The lore lines to set.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper setLore(String... lines) {
        setItemMeta(meta -> meta.setLore(Arrays.asList(lines)));
        return this;
    }

    /**
     * Adds lore to the item.
     *
     * @param lines The lore lines to add.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper addLore(String... lines) {
        setItemMeta(meta -> {
            List<String> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.getLore())) : new ArrayList<>();
            lore.addAll(Arrays.asList(lines));
            meta.setLore(lore);
        });
        return this;
    }

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment The enchantment to add.
     * @param level The level of the enchantment.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper addEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Removes an enchantment from the item.
     *
     * @param enchantment The enchantment to remove.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper removeEnchant(Enchantment enchantment) {
        itemStack.removeEnchantment(enchantment);
        return this;
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param data The custom model data to set.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper setCustomModelData(Integer data) {
        setItemMeta(meta -> meta.setCustomModelData(data));
        return this;
    }

    /**
     * Sets the amount of the item.
     *
     * @param amount The amount of the item.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Adds an ItemFlag to the ItemStack.
     *
     * @param flags The ItemFlags to add.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper addItemFlags(ItemFlag... flags) {
        setItemMeta(meta -> meta.addItemFlags(flags));
        return this;
    }

    /**
     * Removes an ItemFlag from the ItemStack.
     *
     * @param flags The ItemFlags to remove.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper removeItemFlags(ItemFlag... flags) {
        setItemMeta(meta -> meta.removeItemFlags(flags));
        return this;
    }

    /**
     * Checks if the ItemStack has a specific ItemFlag.
     *
     * @param flag The ItemFlag to check.
     * @return True if the ItemStack has the flag, false otherwise.
     */
    public boolean hasItemFlag(ItemFlag flag) {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasItemFlag(flag);
    }

    /**
     * Sets the skull texture for the item using either a player name, UUID, URL, or base64 string.
     * <p>
     * If the value matches a valid player name pattern, the skull will be set to that player's head.
     * If the value looks like a UUID, it will be parsed and used to fetch the corresponding player.
     * If the value starts with "http://" or "https://", it will be treated as a texture URL.
     * Otherwise, the value is treated as a base64 texture string.
     * </p>
     *
     * @param value The player name, UUID, URL, or base64 string for the skull's texture.
     * @return This {@link ItemStackWrapper} instance for chaining.
     * @throws IllegalArgumentException If the item is not a player head.
     */
    public ItemStackWrapper setSkull(String value) {
        if (value == null || value.isEmpty()) {
            return this;
        }

        // Check if the item is a player head
        if (itemStack.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("Cannot set skull texture on non-skull item: " + itemStack.getType());
        }

        setItemMeta(meta -> {
            SkullMeta skullMeta = (SkullMeta) meta;
            
            // Check if input is a valid player name
            if (PLAYER_NAME_REGEX.matcher(value).matches()) {
                setSkullByPlayerName(skullMeta, value);
            } 
            // Check if input is a valid UUID
            else if (value.length() == 36 && value.contains("-")) {
                try {
                    UUID uuid = UUID.fromString(value);
                    setSkullByUUID(skullMeta, uuid);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, treat as base64
                    setSkullByBase64(skullMeta, value);
                }
            }
            // Check if input is a URL
            else if (value.startsWith("http://") || value.startsWith("https://")) {
                setSkullByUrl(skullMeta, value);
            }
            // Treat as base64 texture
            else {
                setSkullByBase64(skullMeta, value);
            }
        });
        return this;
    }

    /**
     * Applies a translator function to the display name and lore of the item.
     *
     * @param translator A function to translate strings.
     * @return This ItemStackWrapper for chaining.
     */
    public ItemStackWrapper applyTranslator(Function<String, String> translator) {
        setItemMeta(meta -> {
            if (meta.hasDisplayName()) {
                meta.setDisplayName(translator.apply(meta.getDisplayName()));
            }

            if (meta.hasLore()) {
                List<String> translatedLore = new ArrayList<>();
                for (String line : Objects.requireNonNull(meta.getLore())) {
                    translatedLore.add(translator.apply(line));
                }
                meta.setLore(translatedLore);
            }
        });
        return this;
    }


    /**
     * Returns the original wrapped {@link ItemStack}.
     * Modifications to the returned {@link ItemStack} affect the wrapper directly.
     *
     * @return The original wrapped {@link ItemStack}.
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Returns a cloned copy of the wrapped {@link ItemStack}.
     * Modifications to the returned copy do not affect the wrapper.
     *
     * @return A cloned copy of the wrapped {@link ItemStack}.
     */
    @NotNull
    public ItemStack toItemStack() {
        return itemStack.clone();
    }

    /**
     * Builds and returns a cloned copy of the wrapped {@link ItemStack},
     * representing its final state.
     *
     * @return A cloned copy of the wrapped {@link ItemStack}.
     */
    @NotNull
    public ItemStack build() {
        return itemStack.clone();
    }


    /**
     * Creates an ItemStack from a configuration section.
     *
     * @param section The configuration section containing item data.
     * @param translator A function to translate strings, used for localization.
     * @return The constructed ItemStack.
     * @throws IllegalArgumentException If the configuration contains invalid data
     *                                  (e.g., invalid material, enchantment, or flag).
     */
    public static ItemStack fromConfigurationSection(@NotNull ConfigurationSection section, @NotNull Function<String, String> translator) throws IllegalArgumentException {
        Material material;
        if (section.contains("skull") && !section.getString("skull", "").isEmpty()) {
            material = Material.PLAYER_HEAD;
        } else {
            String materialName = section.getString("material", "STONE");
            material = Material.matchMaterial(materialName);
            if (material == null) {
                throw new IllegalArgumentException("Invalid material: " + materialName);
            }
        }

        ItemStackWrapper wrapper = new ItemStackWrapper(material)
                .setAmount(section.getInt("amount", 1));

        Optional.ofNullable(section.getString("display-name"))
                .ifPresent(displayName -> wrapper.setDisplayName(ColorUtil.colorize(translator.apply(displayName))));

        if (section.contains("lore")) {
            wrapper.addLore(section.getStringList("lore")
                    .stream()
                    .map(line -> ColorUtil.colorize(translator.apply(line)))
                    .toArray(String[]::new)
            );
        }

        if (section.contains("enchantments")) {
            ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
            if (enchantSection != null) {
                enchantSection.getKeys(false).forEach(key -> {
                    //noinspection deprecation
                    Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                    if (enchantment != null) {
                        wrapper.addEnchant(enchantment, enchantSection.getInt(key, 1));
                    } else {
                        // Try to get enchantment by key
                        try {
                            enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                            if (enchantment != null) {
                                wrapper.addEnchant(enchantment, enchantSection.getInt(key, 1));
                                return;
                            }
                        } catch (Exception ignored) {
                            // Fall through to exception
                        }
                        throw new IllegalArgumentException("Invalid enchantment: " + key);
                    }
                });
            }
        }

        if (section.contains("custom-model-data")) {
            wrapper.setCustomModelData(section.getInt("custom-model-data"));
        }

        if (section.contains("flags")) {
            List<String> flagNames = section.getStringList("flags");
            flagNames.forEach(flagName -> {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                    wrapper.addItemFlags(flag);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid ItemFlag: " + flagName);
                }
            });
        }

        if (section.contains("skull")) {
            String skullValue = section.getString("skull", "");
            if (!skullValue.isEmpty()) {
                try {
                    // Apply translation
                    String translatedValue = translator.apply(skullValue);
                    // Set skull texture
                    wrapper.setSkull(translatedValue);
                } catch (Exception e) {
                    // Log error but continue with default skull
                    System.err.println("Error setting skull texture: " + skullValue);
                    System.err.println("Cause: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return wrapper.getItemStack();
    }

    /**
     * Creates an {@link ItemStack} from a configuration section without string translation.
     * <p>
     * This is a shorthand for {@link #fromConfigurationSection(ConfigurationSection, Function)}
     * with {@link Function#identity()} as the translator.
     * </p>
     *
     * @param section The {@link ConfigurationSection} containing item data.
     * @return The constructed {@link ItemStack}.
     * @throws IllegalArgumentException If the configuration contains invalid data
     *                                  (e.g., invalid material, enchantment, or flag).
     */
    public static ItemStack fromConfigurationSection(@NotNull ConfigurationSection section) throws IllegalArgumentException {
        return  fromConfigurationSection(section, Function.identity());
    }


    /**
     * Sets the skull texture using a player name.
     *
     * @param skullMeta The skull meta to modify.
     * @param playerName The player name to set as the skull owner.
     */
    private static void setSkullByPlayerName(SkullMeta skullMeta, String playerName) {
        // Try to get from cache first
        OfflinePlayer offlinePlayer = CACHED_OFFLINE_PLAYERS.getIfPresent(playerName);

        if (offlinePlayer == null) {
            // Check if player is online first (faster than checking offline players)
            offlinePlayer = Bukkit.getPlayer(playerName);

            if (offlinePlayer == null) {
                // Check if getOfflinePlayerIfCached method is available (Paper API)
                try {
                    offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerName);
                } catch (NoSuchMethodError ignored) {
                    // Method not available, will use getOfflinePlayer below
                }
            }

            // If still not found, fetch using getOfflinePlayer and cache
            if (offlinePlayer == null) {
                try {
                    offlinePlayer = CACHED_OFFLINE_PLAYERS.get(playerName, () -> Bukkit.getOfflinePlayer(playerName));
                } catch (ExecutionException e) {
                    throw new RuntimeException("Failed to fetch offline player: " + playerName, e);
                }
            }
        }

        skullMeta.setOwningPlayer(offlinePlayer);
    }

    /**
     * Sets the skull texture using a UUID.
     *
     * @param skullMeta The skull meta to modify.
     * @param uuid The UUID to set as the skull owner.
     */
    private static void setSkullByUUID(SkullMeta skullMeta, UUID uuid) {
        // Simply use Bukkit's built-in method to get the player by UUID
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        skullMeta.setOwningPlayer(offlinePlayer);
    }
    
    /**
     * Sets the skull texture using a Mojang URL.
     *
     * @param skullMeta The skull meta to modify.
     * @param url The URL to the skin texture.
     */
    private static void setSkullByUrl(SkullMeta skullMeta, String url) {
        // Convert URL to Base64 and use that method
        setSkullByBase64(skullMeta, urlToBase64(url));
    }
    
    /**
     * Converts a texture URL to a Base64 string.
     *
     * @param url The texture URL.
     * @return A Base64 string representing the URL texture data.
     */
    private static String urlToBase64(String url) {
        try {
            // Make sure the URL is valid
            java.net.URI actualUrl = new java.net.URI(url);
            String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
            return Base64.getEncoder().encodeToString(toEncode.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }

    /**
     * Sets the skull texture using a base64 string.
     *
     * @param skullMeta The skull meta to modify.
     * @param base64 The base64 string for the skull's texture.
     */
    private static void setSkullByBase64(SkullMeta skullMeta, String base64) {
        if (base64 == null || base64.isEmpty()) {
            return;
        }

        try {
            // Generate a stable UUID based on the texture data
            UUID id = new UUID(
                    base64.substring(base64.length() - Math.min(20, base64.length())).hashCode(),
                    base64.substring(base64.length() - Math.min(10, base64.length())).hashCode()
            );
            
            // Use Paper API directly - the safe approach for 1.16+
            com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(id, "");
            profile.setProperty(
                    new ProfileProperty("textures", base64)
            );
            skullMeta.setPlayerProfile(profile);
        } catch (Exception e) {
            System.err.println("Failed to set skull texture using Paper API: " + e.getMessage());
            
            // Fallback to older Paper approach if the first method fails
            try {
                com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "");
                profile.setProperty(
                        new ProfileProperty("textures", base64)
                );
                skullMeta.setPlayerProfile(profile);
            } catch (Exception fallbackError) {
                throw new RuntimeException("Failed to set skull texture using all available methods", fallbackError);
            }
        }
    }

    /**
     * A helper method to modify the ItemMeta of the current ItemStack.
     *
     * @param modifier The function to modify the ItemMeta.
     */
    private void setItemMeta(ItemMetaModifier modifier) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            modifier.modify(meta);
            itemStack.setItemMeta(meta);
        }
    }

    /**
     * Functional interface to modify ItemMeta.
     */
    @FunctionalInterface
    private interface ItemMetaModifier {
        void modify(ItemMeta meta);
    }
}
