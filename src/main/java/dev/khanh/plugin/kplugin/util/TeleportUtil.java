package dev.khanh.plugin.kplugin.util;

import dev.khanh.plugin.kplugin.KPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for asynchronous entity teleportation using FoliaLib.
 * <p>
 * <b>Platform behavior:</b>
 * <ul>
 *   <li><b>Folia:</b> Teleports the entity asynchronously in the correct region.</li>
 *   <li><b>Paper (with async teleport support):</b> Teleports asynchronously.</li>
 *   <li><b>Spigot:</b> Falls back to teleporting the entity on the next tick (not instant).</li>
 * </ul>
 * <b>Note:</b> On Spigot, immediate teleport is not performed to avoid thread safety issues and to expose bugs from expecting instant teleports.
 * <br>Always use the returned {@code CompletableFuture<Boolean>} to handle post-teleport logic.
 * <br>Success state is indicated by the Boolean result.
 */
public class TeleportUtil {
    /**
     * Teleports an entity asynchronously to the specified location.
     * <p>
     * Platform-specific behavior:
     * <ul>
     *   <li>Folia: Teleports in the entity's region asynchronously.</li>
     *   <li>Paper: Teleports asynchronously if supported.</li>
     *   <li>Spigot: Teleports on the next tick.</li>
     * </ul>
     * @param entity   The entity to teleport
     * @param location The target location
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return KPlugin.getFoliaLib().getScheduler().teleportAsync(entity, location);
    }

    /**
     * Teleports an entity asynchronously to the specified location with a teleport cause.
     * <p>
     * Platform-specific behavior:
     * <ul>
     *   <li>Folia: Teleports in the entity's region asynchronously.</li>
     *   <li>Paper: Teleports asynchronously if supported.</li>
     *   <li>Spigot: Teleports on the next tick.</li>
     * </ul>
     * @param entity   The entity to teleport
     * @param location The target location
     * @param cause    The teleport cause
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        return KPlugin.getFoliaLib().getScheduler().teleportAsync(entity, location, cause);
    }
}
