package dev.khanh.plugin.kplugin.placeholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Simple and flexible placeholder system using direct string replacement.
 * <p>
 * This system works like simple string replacement - the key you set is exactly
 * what gets replaced. No forced format like {@code {key}} or {@code %key%}.
 * You define the full placeholder string yourself.
 * </p>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Free-style placeholders (you define the exact string to replace)</li>
 *   <li>Thread-safe operations</li>
 *   <li>Simple and efficient API</li>
 *   <li>Support for static values and dynamic resolvers</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Usage</h3>
 * <pre>{@code
 * Placeholders ph = Placeholders.create()
 *     .set("{player}", player.getName())
 *     .set("%balance%", "1000")
 *     .set("$level", "10");
 * 
 * String result = ph.apply("Hello {player}! Balance: %balance%, Level: $level");
 * // Result: "Hello Steve! Balance: 1000, Level: 10"
 * }</pre>
 * 
 * <h3>Any Format You Want</h3>
 * <pre>{@code
 * // PlaceholderAPI style
 * Placeholders ph = Placeholders.create()
 *     .set("%player_name%", player.getName())
 *     .set("%player_health%", "20");
 * 
 * // Mustache style
 * Placeholders ph2 = Placeholders.create()
 *     .set("{{name}}", "World")
 *     .set("{{count}}", "5");
 * 
 * // Simple prefix style
 * Placeholders ph3 = Placeholders.create()
 *     .set("$var", "value")
 *     .set("@user", "admin");
 * 
 * // Even no special chars
 * Placeholders ph4 = Placeholders.create()
 *     .set("PLAYER_NAME", player.getName());
 * }</pre>
 * 
 * <h3>Dynamic Resolvers</h3>
 * <pre>{@code
 * Placeholders ph = Placeholders.create()
 *     .resolver("{time}", () -> LocalTime.now().toString())
 *     .resolver("{random}", () -> String.valueOf(new Random().nextInt(100)));
 * 
 * String result = ph.apply("Time: {time}, Random: {random}");
 * }</pre>
 * 
 * <h3>Player-Aware Resolvers</h3>
 * <pre>{@code
 * Placeholders ph = Placeholders.create()
 *     .playerResolver("%health%", p -> String.valueOf((int) p.getHealth()))
 *     .playerResolver("%level%", p -> String.valueOf(p.getLevel()));
 * 
 * String result = ph.apply("HP: %health%, Level: %level%", player);
 * }</pre>
 * 
 * <h3>Combining with PlaceholderAPI</h3>
 * <pre>{@code
 * Placeholders ph = Placeholders.create()
 *     .set("{custom}", "value");
 * 
 * // First apply internal placeholders, then external
 * Function<String, String> resolver = ph.toFunction()
 *     .andThen(str -> PlaceholderAPI.setPlaceholders(player, str));
 * 
 * String result = resolver.apply("{custom} and %player_name%");
 * }</pre>
 *
 * @since 4.0.0
 * @author KhanhHuynh
 */
public final class Placeholders {
    
    private final Map<String, String> staticValues;
    private final Map<String, Function<Player, String>> playerResolvers;
    
    private Placeholders() {
        this.staticValues = new ConcurrentHashMap<>();
        this.playerResolvers = new ConcurrentHashMap<>();
    }
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates a new empty Placeholders instance.
     *
     * @return new Placeholders instance
     */
    @NotNull
    public static Placeholders create() {
        return new Placeholders();
    }
    
    /**
     * Creates a new Placeholders instance from a map of values.
     * <p>
     * The map keys are the exact strings to be replaced.
     * </p>
     *
     * @param values the initial placeholder values (key = string to replace, value = replacement)
     * @return new Placeholders instance
     */
    @NotNull
    public static Placeholders of(@NotNull Map<String, String> values) {
        Placeholders ph = create();
        ph.staticValues.putAll(values);
        return ph;
    }
    
    // ==================== Configuration ====================
    
    /**
     * Sets a static placeholder value.
     * <p>
     * The key is the exact string that will be replaced in the text.
     * </p>
     *
     * @param key the exact string to replace (e.g., "{player}", "%name%", "$var")
     * @param value the replacement value
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders set(@NotNull String key, @NotNull String value) {
        staticValues.put(key, value);
        return this;
    }
    
    /**
     * Sets a static placeholder value from any object.
     *
     * @param key the exact string to replace
     * @param value the value (converted via toString())
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders set(@NotNull String key, @NotNull Object value) {
        staticValues.put(key, String.valueOf(value));
        return this;
    }
    
    /**
     * Sets multiple placeholder values from a map.
     *
     * @param values the values to set (key = string to replace, value = replacement)
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders setAll(@NotNull Map<String, ?> values) {
        values.forEach((key, value) -> staticValues.put(key, String.valueOf(value)));
        return this;
    }
    
    /**
     * Adds a dynamic resolver (evaluated on each apply).
     *
     * @param key the exact string to replace
     * @param resolver function that returns the value
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders resolver(@NotNull String key, @NotNull java.util.function.Supplier<String> resolver) {
        playerResolvers.put(key, player -> resolver.get());
        return this;
    }
    
    /**
     * Adds a player-aware resolver.
     *
     * @param key the exact string to replace
     * @param resolver function that takes a player and returns the value
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders playerResolver(@NotNull String key, @NotNull Function<Player, String> resolver) {
        playerResolvers.put(key, resolver);
        return this;
    }
    
    /**
     * Adds common player placeholders with {@code {key}} format.
     * <p>
     * Registers: {player}, {player_name}, {player_uuid}, {player_displayname},
     * {player_health}, {player_level}, {player_world}
     * </p>
     *
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders withPlayerPlaceholders() {
        return withPlayerPlaceholders("{", "}");
    }
    
    /**
     * Adds common player placeholders with custom format.
     * <p>
     * Example: {@code withPlayerPlaceholders("%", "%")} registers %player%, %player_name%, etc.
     * </p>
     *
     * @param prefix the prefix for placeholder keys
     * @param suffix the suffix for placeholder keys
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders withPlayerPlaceholders(@NotNull String prefix, @NotNull String suffix) {
        playerResolver(prefix + "player" + suffix, Player::getName);
        playerResolver(prefix + "player_name" + suffix, Player::getName);
        playerResolver(prefix + "player_uuid" + suffix, p -> p.getUniqueId().toString());
        playerResolver(prefix + "player_displayname" + suffix, Player::getDisplayName);
        playerResolver(prefix + "player_health" + suffix, p -> String.valueOf((int) p.getHealth()));
        playerResolver(prefix + "player_level" + suffix, p -> String.valueOf(p.getLevel()));
        playerResolver(prefix + "player_world" + suffix, p -> p.getWorld().getName());
        return this;
    }
    
    /**
     * Removes a placeholder.
     *
     * @param key the placeholder key
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders remove(@NotNull String key) {
        staticValues.remove(key);
        playerResolvers.remove(key);
        return this;
    }
    
    /**
     * Clears all placeholders.
     *
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders clear() {
        staticValues.clear();
        playerResolvers.clear();
        return this;
    }
    
    // ==================== Application ====================
    
    /**
     * Applies placeholders to text.
     *
     * @param text the text to process
     * @return the processed text
     */
    @NotNull
    public String apply(@NotNull String text) {
        return apply(text, null);
    }
    
    /**
     * Applies placeholders to text with player context.
     *
     * @param text the text to process
     * @param player the player for player-aware resolvers (can be null)
     * @return the processed text
     */
    @NotNull
    public String apply(@NotNull String text, @Nullable Player player) {
        if (text.isEmpty()) {
            return text;
        }
        
        String result = text;
        
        // Apply static values (direct string replacement)
        for (Map.Entry<String, String> entry : staticValues.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        
        // Apply dynamic resolvers (direct string replacement)
        for (Map.Entry<String, Function<Player, String>> entry : playerResolvers.entrySet()) {
            if (result.contains(entry.getKey())) {
                String value = entry.getValue().apply(player);
                if (value != null) {
                    result = result.replace(entry.getKey(), value);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Creates a copy of this Placeholders with the same configuration.
     *
     * @return a new Placeholders instance with copied values
     */
    @NotNull
    public Placeholders copy() {
        Placeholders copy = new Placeholders();
        copy.staticValues.putAll(this.staticValues);
        copy.playerResolvers.putAll(this.playerResolvers);
        return copy;
    }
    
    /**
     * Merges another Placeholders into this one.
     * <p>
     * Values from other will override existing values with the same key.
     * </p>
     *
     * @param other the Placeholders to merge
     * @return this instance for chaining
     */
    @NotNull
    public Placeholders merge(@NotNull Placeholders other) {
        this.staticValues.putAll(other.staticValues);
        this.playerResolvers.putAll(other.playerResolvers);
        return this;
    }
    
    // ==================== Conversion ====================
    
    /**
     * Converts to a Function for use with other APIs.
     *
     * @return a function that applies placeholders
     */
    @NotNull
    public Function<String, String> toFunction() {
        return this::apply;
    }
    
    /**
     * Converts to a Function with player context.
     *
     * @param player the player
     * @return a function that applies placeholders
     */
    @NotNull
    public Function<String, String> toFunction(@Nullable Player player) {
        return text -> apply(text, player);
    }
    
    // ==================== Accessors ====================
    
    /**
     * Checks if a key is registered.
     *
     * @param key the key to check
     * @return true if the key has a value or resolver
     */
    public boolean has(@NotNull String key) {
        return staticValues.containsKey(key) || playerResolvers.containsKey(key);
    }
    
    /**
     * Gets the static value for a key.
     *
     * @param key the key
     * @return the value or null
     */
    @Nullable
    public String get(@NotNull String key) {
        return staticValues.get(key);
    }
    
    /**
     * Gets the number of registered placeholders.
     *
     * @return the count
     */
    public int size() {
        return staticValues.size() + playerResolvers.size();
    }
    
    /**
     * Checks if there are no registered placeholders.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return staticValues.isEmpty() && playerResolvers.isEmpty();
    }
}
