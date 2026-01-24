package dev.khanh.plugin.kplugin.gui.placeholder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Context object for storing custom placeholder values.
 * <p>
 * This class allows you to pass custom data for placeholder replacement
 * without creating multiple specialized resolver functions.
 * </p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * PlaceholderContext context = new PlaceholderContext()
 *     .set("page", "1")
 *     .set("total_pages", "5")
 *     .set("item_name", "Diamond Sword");
 * 
 * String text = "{player} is viewing {item_name} on page {page}/{total_pages}";
 * String result = resolver.apply(player, text, context);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class PlaceholderContext {
    
    private final Map<String, String> values;
    
    /**
     * Creates a new empty placeholder context.
     */
    public PlaceholderContext() {
        this.values = new HashMap<>();
    }
    
    /**
     * Creates a new placeholder context with initial values.
     *
     * @param values the initial placeholder values
     */
    public PlaceholderContext(@NotNull Map<String, String> values) {
        this.values = new HashMap<>(values);
    }
    
    /**
     * Sets a placeholder value.
     *
     * @param key the placeholder key (without braces)
     * @param value the replacement value
     * @return this context for chaining
     */
    @NotNull
    public PlaceholderContext set(@NotNull String key, @Nullable String value) {
        if (value != null) {
            values.put(key.toLowerCase(), value);
        } else {
            values.remove(key.toLowerCase());
        }
        return this;
    }
    
    /**
     * Sets a placeholder value from an object.
     *
     * @param key the placeholder key
     * @param value the value (converted to string via toString())
     * @return this context for chaining
     */
    @NotNull
    public PlaceholderContext set(@NotNull String key, @Nullable Object value) {
        return set(key, value != null ? value.toString() : null);
    }
    
    /**
     * Gets a placeholder value.
     *
     * @param key the placeholder key
     * @return the value, or null if not set
     */
    @Nullable
    public String get(@NotNull String key) {
        return values.get(key.toLowerCase());
    }
    
    /**
     * Gets a placeholder value with a default.
     *
     * @param key the placeholder key
     * @param defaultValue the default value if not set
     * @return the value or default
     */
    @NotNull
    public String getOrDefault(@NotNull String key, @NotNull String defaultValue) {
        return values.getOrDefault(key.toLowerCase(), defaultValue);
    }
    
    /**
     * Checks if a placeholder is set.
     *
     * @param key the placeholder key
     * @return true if the key has a value
     */
    public boolean has(@NotNull String key) {
        return values.containsKey(key.toLowerCase());
    }
    
    /**
     * Removes a placeholder value.
     *
     * @param key the placeholder key
     * @return this context for chaining
     */
    @NotNull
    public PlaceholderContext remove(@NotNull String key) {
        values.remove(key.toLowerCase());
        return this;
    }
    
    /**
     * Clears all placeholder values.
     *
     * @return this context for chaining
     */
    @NotNull
    public PlaceholderContext clear() {
        values.clear();
        return this;
    }
    
    /**
     * Gets all placeholder values.
     *
     * @return a copy of the values map
     */
    @NotNull
    public Map<String, String> getAll() {
        return new HashMap<>(values);
    }
    
    /**
     * Applies this context's placeholders to text.
     *
     * @param text the text to process
     * @return the text with placeholders replaced
     */
    @NotNull
    public String apply(@NotNull String text) {
        String result = text;
        
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Creates a copy of this context.
     *
     * @return a new context with the same values
     */
    @NotNull
    public PlaceholderContext copy() {
        return new PlaceholderContext(values);
    }
    
    /**
     * Merges another context into this one.
     * <p>
     * Values from the other context will override existing values.
     * </p>
     *
     * @param other the context to merge
     * @return this context for chaining
     */
    @NotNull
    public PlaceholderContext merge(@NotNull PlaceholderContext other) {
        values.putAll(other.values);
        return this;
    }
}
