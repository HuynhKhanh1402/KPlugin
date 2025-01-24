package dev.khanh.plugin.kplugin.item;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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

import java.lang.reflect.Field;
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
     * Sets the skull texture for the item using either a player name or a base64 string.
     * <p>
     * If the value matches a valid player name, the skull will be set to that player's head.
     * Otherwise, the value is treated as a base64 texture string.
     * </p>
     *
     * @param value The player name or base64 string for the skull's texture.
     * @return This {@link ItemStackWrapper} instance for chaining.
     * @throws IllegalArgumentException If the item meta is not a {@link SkullMeta}.
     */
    public ItemStackWrapper setSkull(String value) {
        setItemMeta(meta -> {
            Preconditions.checkArgument(meta instanceof SkullMeta,
                    "Item meta must be an instance of SkullMeta");
            if (PLAYER_NAME_REGEX.matcher(value).matches()) {
                setSkullMetaByPlayerName(meta, value);
            } else {
                setSkullMetaByBase64(meta, value);
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
     * @throws IllegalArgumentException If the configuration is invalid.
     */
    @SuppressWarnings("DataFlowIssue")
    public static ItemStack fromConfigurationSection(@NotNull ConfigurationSection section, @NotNull Function<String, String> translator) throws IllegalArgumentException {
        String materialName = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + materialName);
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
            enchantSection.getKeys(false).forEach(key -> {
                //noinspection deprecation
                Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                if (enchantment != null) {
                    wrapper.addEnchant(enchantment, enchantSection.getInt(key, 1));
                } else {
                    throw new IllegalArgumentException("Invalid enchantment: " + key);
                }
            });
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
            wrapper.setSkull(translator.apply(section.getString("skull", "")));
        }

        return wrapper.getItemStack();
    }
    /**
     * Sets the skull meta using a player's name.
     *
     * @param itemMeta The item meta to modify.
     * @param playerName The player name to set as the skull owner.
     */
    private static void setSkullMetaByPlayerName(ItemMeta itemMeta, String playerName) {
        Preconditions.checkArgument(itemMeta instanceof SkullMeta,
                "ItemMeta must be an instance of SkullMeta.");
        SkullMeta skullMeta = (SkullMeta) itemMeta;

        Optional<OfflinePlayer> cachedOfflinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> playerName.equals(player.getName()))
                .findFirst();

        OfflinePlayer offlinePlayer = cachedOfflinePlayer.orElseGet(() -> CACHED_OFFLINE_PLAYERS.getIfPresent(playerName));
        if (offlinePlayer == null) {
            try {
                //noinspection deprecation
                offlinePlayer = CACHED_OFFLINE_PLAYERS.get(playerName, () -> Bukkit.getOfflinePlayer(playerName));
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        skullMeta.setOwningPlayer(offlinePlayer);
    }

    /**
     * Sets the skull meta using a base64 string.
     *
     * @param itemMeta The item meta to modify.
     * @param base64 The base64 string to use for the skull texture.
     */
    private static void setSkullMetaByBase64(ItemMeta itemMeta, String base64) {
        Preconditions.checkArgument(itemMeta instanceof SkullMeta,
                "ItemMeta must be an instance of SkullMeta.");
        SkullMeta skullMeta = (SkullMeta) itemMeta;

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64));
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
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
