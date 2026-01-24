package dev.khanh.plugin.kplugin.gui.animation;

import dev.khanh.plugin.kplugin.gui.GUI;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Pulse animation that toggles enchantment glow on items.
 * <p>
 * This animation creates a pulsing glow effect by adding and removing
 * a fake enchantment with the HIDE_ENCHANTS flag. The pulse effect
 * cycles between glowing and non-glowing states.
 * </p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * PulseAnimation pulse = new PulseAnimation(gui, 10); // Pulse every 10 ticks
 * pulse.addSlot(4); // Make slot 4 pulse
 * pulse.addSlot(13);
 * pulse.start();
 * }</pre>
 *
 * @since 3.1.0
 * @author KhanhHuynh
 */
public class PulseAnimation extends AbstractAnimation {
    
    private final GUI gui;
    private final Set<Integer> slots;
    private final Enchantment glowEnchantment;
    private boolean glowing;
    
    /**
     * Creates a new pulse animation for the specified GUI.
     *
     * @param gui the GUI to animate
     * @param updateInterval the ticks between pulses
     */
    public PulseAnimation(@NotNull GUI gui, long updateInterval) {
        super(updateInterval);
        this.gui = gui;
        this.slots = new HashSet<>();
        this.glowEnchantment = Enchantment.DURABILITY; // Use a subtle enchantment for glow
        this.glowing = false;
    }
    
    /**
     * Adds a slot to pulse.
     *
     * @param slot the slot index
     * @return this animation for chaining
     */
    @NotNull
    public PulseAnimation addSlot(int slot) {
        slots.add(slot);
        return this;
    }
    
    /**
     * Removes a slot from pulsing.
     *
     * @param slot the slot index
     * @return this animation for chaining
     */
    @NotNull
    public PulseAnimation removeSlot(int slot) {
        slots.remove(slot);
        return this;
    }
    
    /**
     * Clears all pulsing slots.
     *
     * @return this animation for chaining
     */
    @NotNull
    public PulseAnimation clearSlots() {
        slots.clear();
        return this;
    }
    
    /**
     * Gets the pulsing slots.
     *
     * @return the slot set
     */
    @NotNull
    public Set<Integer> getSlots() {
        return new HashSet<>(slots);
    }
    
    @Override
    protected boolean update(int frame) {
        glowing = !glowing;
        
        for (int slot : slots) {
            ItemStack item = gui.getInventory().getItem(slot);
            if (item == null || !item.hasItemMeta()) {
                continue;
            }
            
            ItemStack updated = item.clone();
            ItemMeta meta = updated.getItemMeta();
            
            if (glowing) {
                // Add glow
                meta.addEnchant(glowEnchantment, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                // Remove glow
                meta.removeEnchant(glowEnchantment);
            }
            
            updated.setItemMeta(meta);
            gui.setItem(slot, updated);
        }
        
        // Pulse animations run indefinitely
        return true;
    }
    
    @Override
    protected void onStop() {
        // Remove glow from all slots when stopped
        for (int slot : slots) {
            ItemStack item = gui.getInventory().getItem(slot);
            if (item == null || !item.hasItemMeta()) {
                continue;
            }
            
            ItemStack updated = item.clone();
            ItemMeta meta = updated.getItemMeta();
            meta.removeEnchant(glowEnchantment);
            updated.setItemMeta(meta);
            gui.setItem(slot, updated);
        }
        
        glowing = false;
    }
}
