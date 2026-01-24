package dev.khanh.plugin.kplugin.gui.placeholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Chainable placeholder resolver for GUI text.
 * <p>
 * This resolver supports registering custom placeholder handlers and
 * provides built-in replacements for common placeholders like player
 * name, page numbers, and custom data.
 * </p>
 * 
 * <p>
 * The resolver processes placeholders in the format <code>{key}</code>
 * and supports context-aware replacements with player and custom data.
 * </p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * PlaceholderResolver resolver = new PlaceholderResolver();
 * 
 * // Add built-in placeholders
 * resolver.addPlayerPlaceholders();
 * 
 * // Add custom resolver
 * resolver.addResolver("balance", (player, context) -> 
 *     String.valueOf(economy.getBalance(player))
 * );
 * 
 * // Apply placeholders
 * String result = resolver.apply(player, "&eHello {player}! Balance: ${balance}", context);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class PlaceholderResolver {
    
    private final Map<String, BiFunction<Player, PlaceholderContext, String>> resolvers;
    
    /**
     * Creates a new placeholder resolver with default resolvers.
     */
    public PlaceholderResolver() {
        this.resolvers = new HashMap<>();
        
        // Initialize default resolvers
        addPlayerPlaceholders();
    }
    
    /**
     * Adds a placeholder resolver.
     *
     * @param placeholder the placeholder key (without braces)
     * @param resolver the resolver function (player, context) → replacement
     * @return this resolver for chaining
     */
    @NotNull
    public PlaceholderResolver addResolver(@NotNull String placeholder, @NotNull BiFunction<Player, PlaceholderContext, String> resolver) {
        resolvers.put(placeholder.toLowerCase(), resolver);
        return this;
    }
    
    /**
     * Adds a simple placeholder resolver that doesn't use context.
     *
     * @param placeholder the placeholder key
     * @param resolver the resolver function (player) → replacement
     * @return this resolver for chaining
     */
    @NotNull
    public PlaceholderResolver addResolver(@NotNull String placeholder, @NotNull Function<Player, String> resolver) {
        resolvers.put(placeholder.toLowerCase(), (player, context) -> resolver.apply(player));
        return this;
    }
    
    /**
     * Adds built-in player placeholders.
     * <p>
     * Registers: {player}, {player_name}, {player_uuid}, {player_displayname}
     * </p>
     *
     * @return this resolver for chaining
     */
    @NotNull
    public PlaceholderResolver addPlayerPlaceholders() {
        addResolver("player", Player::getName);
        addResolver("player_name", Player::getName);
        addResolver("player_uuid", player -> player.getUniqueId().toString());
        addResolver("player_displayname", Player::getDisplayName);
        return this;
    }
    
    /**
     * Applies placeholders to text.
     *
     * @param player the player for placeholder context
     * @param text the text to process
     * @return the text with placeholders replaced
     */
    @NotNull
    public String apply(@Nullable Player player, @NotNull String text) {
        return apply(player, text, null);
    }
    
    /**
     * Applies placeholders to text with custom context.
     *
     * @param player the player for placeholder context
     * @param text the text to process
     * @param context custom context data
     * @return the text with placeholders replaced
     */
    @NotNull
    public String apply(@Nullable Player player, @NotNull String text, @Nullable PlaceholderContext context) {
        String result = text;
        
        // Apply internal resolvers
        for (Map.Entry<String, BiFunction<Player, PlaceholderContext, String>> entry : resolvers.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                String replacement = entry.getValue().apply(player, context);
                if (replacement != null) {
                    result = result.replace(placeholder, replacement);
                }
            }
        }
        
        // Apply context placeholders
        if (context != null) {
            result = context.apply(result);
        }
        
        return result;
    }
    
    /**
     * Creates a function for applying placeholders.
     * <p>
     * Useful for passing to ConfigItem.build() and similar methods.
     * </p>
     *
     * @param player the player
     * @param context the context
     * @return a function that applies placeholders
     */
    @NotNull
    public Function<String, String> asFunction(@Nullable Player player, @Nullable PlaceholderContext context) {
        return text -> apply(player, text, context);
    }
    
    /**
     * Creates a function for applying placeholders without context.
     *
     * @param player the player
     * @return a function that applies placeholders
     */
    @NotNull
    public Function<String, String> asFunction(@Nullable Player player) {
        return text -> apply(player, text, null);
    }
}
