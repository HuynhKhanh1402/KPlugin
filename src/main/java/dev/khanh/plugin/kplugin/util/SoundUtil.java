package dev.khanh.plugin.kplugin.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for playing sounds with multi-version compatibility.
 * <p>
 * This utility handles the differences between Minecraft versions by:
 * </p>
 * <ul>
 *   <li>Attempting to use {@link Sound} enum values (pre-1.9 format)</li>
 *   <li>Falling back to namespaced key strings (1.9+ format)</li>
 *   <li>Providing version-specific sound mappings</li>
 * </ul>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Modern format (1.9+)
 * SoundUtil.play(player, "entity.player.levelup", 1.0f, 1.0f);
 * 
 * // Legacy format (will auto-convert)
 * SoundUtil.play(player, "LEVEL_UP", 1.0f, 1.0f);
 * 
 * // GUI sounds
 * SoundUtil.playClickSound(player);
 * SoundUtil.playOpenSound(player);
 * SoundUtil.playNavigateSound(player);
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class SoundUtil {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SoundUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Map<String, String> LEGACY_TO_MODERN = new HashMap<>();
    
    static {
        // Common GUI sounds - legacy to modern mapping
        LEGACY_TO_MODERN.put("CLICK", "ui.button.click");
        LEGACY_TO_MODERN.put("UI_BUTTON_CLICK", "ui.button.click");
        LEGACY_TO_MODERN.put("LEVEL_UP", "entity.player.levelup");
        LEGACY_TO_MODERN.put("ENTITY_PLAYER_LEVELUP", "entity.player.levelup");
        LEGACY_TO_MODERN.put("CHEST_OPEN", "block.chest.open");
        LEGACY_TO_MODERN.put("BLOCK_CHEST_OPEN", "block.chest.open");
        LEGACY_TO_MODERN.put("CHEST_CLOSE", "block.chest.close");
        LEGACY_TO_MODERN.put("BLOCK_CHEST_CLOSE", "block.chest.close");
        LEGACY_TO_MODERN.put("WOOD_CLICK", "block.wood.hit");
        LEGACY_TO_MODERN.put("BLOCK_WOOD_HIT", "block.wood.hit");
        LEGACY_TO_MODERN.put("ANVIL_USE", "block.anvil.use");
        LEGACY_TO_MODERN.put("BLOCK_ANVIL_USE", "block.anvil.use");
        LEGACY_TO_MODERN.put("NOTE_PLING", "block.note_block.pling");
        LEGACY_TO_MODERN.put("BLOCK_NOTE_BLOCK_PLING", "block.note_block.pling");
        LEGACY_TO_MODERN.put("ORB_PICKUP", "entity.experience_orb.pickup");
        LEGACY_TO_MODERN.put("ENTITY_EXPERIENCE_ORB_PICKUP", "entity.experience_orb.pickup");
        LEGACY_TO_MODERN.put("VILLAGER_NO", "entity.villager.no");
        LEGACY_TO_MODERN.put("ENTITY_VILLAGER_NO", "entity.villager.no");
        LEGACY_TO_MODERN.put("VILLAGER_YES", "entity.villager.yes");
        LEGACY_TO_MODERN.put("ENTITY_VILLAGER_YES", "entity.villager.yes");
        LEGACY_TO_MODERN.put("BAT_TAKEOFF", "entity.bat.takeoff");
        LEGACY_TO_MODERN.put("ENTITY_BAT_TAKEOFF", "entity.bat.takeoff");
        LEGACY_TO_MODERN.put("ENDERDRAGON_WINGS", "entity.ender_dragon.flap");
        LEGACY_TO_MODERN.put("ENTITY_ENDER_DRAGON_FLAP", "entity.ender_dragon.flap");
    }
    
    /**
     * Plays a sound for a player.
     * <p>
     * This method handles both legacy Sound enum names and modern
     * namespaced key strings, providing compatibility across versions.
     * </p>
     *
     * @param player the player to play the sound for
     * @param soundName the sound name (enum name or namespaced key)
     * @param volume the volume (0.0 to 1.0+)
     * @param pitch the pitch (0.5 to 2.0, 1.0 is normal)
     */
    public static void play(@NotNull Player player, @NotNull String soundName, float volume, float pitch) {
        play(player, player.getLocation(), soundName, volume, pitch);
    }
    
    /**
     * Plays a sound at a specific location.
     *
     * @param player the player to play the sound for
     * @param location the location to play the sound at
     * @param soundName the sound name
     * @param volume the volume
     * @param pitch the pitch
     */
    public static void play(@NotNull Player player, @NotNull Location location, @NotNull String soundName, float volume, float pitch) {
        String processedName = normalizeSoundName(soundName);
        
        // Try Sound enum first
        Sound sound = getSoundEnum(processedName);
        if (sound != null) {
            player.playSound(location, sound, volume, pitch);
            return;
        }
        
        // Try as namespaced key string (1.9+)
        try {
            // Modern API (1.9+) - use string directly
            player.playSound(location, processedName, volume, pitch);
        } catch (Exception e) {
            LoggerUtil.warning("Failed to play sound '" + soundName + "': " + e.getMessage());
        }
    }
    
    /**
     * Plays a sound for a player without catching exceptions.
     * <p>
     * Use this when you want to handle errors yourself.
     * </p>
     *
     * @param player the player
     * @param soundName the sound name
     * @param volume the volume
     * @param pitch the pitch
     * @throws IllegalArgumentException if the sound is invalid
     */
    public static void playUnsafe(@NotNull Player player, @NotNull String soundName, float volume, float pitch) throws IllegalArgumentException {
        String processedName = normalizeSoundName(soundName);
        Sound sound = getSoundEnum(processedName);
        
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } else {
            player.playSound(player.getLocation(), processedName, volume, pitch);
        }
    }
    
    /**
     * Normalizes a sound name to the modern format.
     * <p>
     * Converts legacy enum names to modern namespaced keys.
     * </p>
     *
     * @param soundName the input sound name
     * @return the normalized sound name
     */
    @NotNull
    private static String normalizeSoundName(@NotNull String soundName) {
        String upper = soundName.toUpperCase();
        
        // Check legacy mapping
        if (LEGACY_TO_MODERN.containsKey(upper)) {
            return LEGACY_TO_MODERN.get(upper);
        }
        
        // If it contains dots, assume it's already modern format
        if (soundName.contains(".")) {
            return soundName.toLowerCase();
        }
        
        // Try to convert enum-style to modern format
        // E.g., "ENTITY_PLAYER_LEVELUP" → "entity.player.levelup"
        return soundName.toLowerCase().replace("_", ".");
    }
    
    /**
     * Attempts to get a Sound enum value from a string.
     *
     * @param soundName the sound name
     * @return the Sound enum, or null if not found
     */
    @Nullable
    private static Sound getSoundEnum(@NotNull String soundName) {
        try {
            // Try direct enum lookup
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try with underscores
            try {
                String withUnderscores = soundName.toUpperCase().replace(".", "_");
                return Sound.valueOf(withUnderscores);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
    
    /**
     * Checks if a sound name is valid.
     *
     * @param soundName the sound name to check
     * @return true if the sound exists
     */
    public static boolean isValidSound(@NotNull String soundName) {
        String normalized = normalizeSoundName(soundName);
        return getSoundEnum(normalized) != null || normalized.contains(".");
    }
    
    // ==================== Predefined GUI Sounds ====================
    
    /**
     * Plays the default GUI click sound.
     *
     * @param player the player
     */
    public static void playClickSound(@NotNull Player player) {
        play(player, "ui.button.click", 1.0f, 1.0f);
    }
    
    /**
     * Plays the default GUI open sound.
     *
     * @param player the player
     */
    public static void playOpenSound(@NotNull Player player) {
        play(player, "block.chest.open", 1.0f, 1.0f);
    }
    
    /**
     * Plays the default GUI close sound.
     *
     * @param player the player
     */
    public static void playCloseSound(@NotNull Player player) {
        play(player, "block.chest.close", 1.0f, 1.0f);
    }
    
    /**
     * Plays the default page navigation sound.
     *
     * @param player the player
     */
    public static void playNavigateSound(@NotNull Player player) {
        play(player, "entity.experience_orb.pickup", 0.5f, 1.2f);
    }
    
    /**
     * Plays an error sound.
     *
     * @param player the player
     */
    public static void playErrorSound(@NotNull Player player) {
        play(player, "entity.villager.no", 1.0f, 1.0f);
    }
    
    /**
     * Plays a success sound.
     *
     * @param player the player
     */
    public static void playSuccessSound(@NotNull Player player) {
        play(player, "entity.player.levelup", 1.0f, 1.0f);
    }
}
