package net.azisaba.beaconator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class GuiListener implements Listener {

    private final Beaconator plugin;

    public GuiListener(Beaconator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BeaconGui)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ConfigurationSection effectsSection = plugin.getConfig().getConfigurationSection("effects");
        if (effectsSection == null) return;

        for (String key : effectsSection.getKeys(false)) {
            ConfigurationSection effectConfig = effectsSection.getConfigurationSection(key);
            if (effectConfig != null && effectConfig.getInt("slot") == event.getSlot()) {
                int availableLevel = plugin.getAvailableEffectLevel(player, key);
                if (availableLevel < 0) {
                    player.sendMessage(ChatColor.RED + "この効果を使用するにはランクアップが必要です。");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    return;
                }
                PotionEffectType effectType = PotionEffectType.getByName(key);
                if (effectType == null) return;
                Set<PotionEffectType> activeBuffs = plugin.getActiveBuffs(player);
                boolean isCurrentlyActive = activeBuffs.contains(effectType);
                if (isCurrentlyActive) {
                    plugin.toggleBuff(player, effectType);
                    player.removePotionEffect(effectType);
                } else {
                    int limit = plugin.getMaxBuffs(player);
                    if (activeBuffs.size() >= limit) {
                        String message = plugin.getConfig().getString("limit-reached-message", "&cあなたのランクでは最大{limit}個までです。");
                        message = message.replace("{limit}", String.valueOf(limit));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    } else {
                        plugin.toggleBuff(player, effectType);
                    }
                }
                ((BeaconGui) event.getInventory().getHolder()).updateItems(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                return;
            }
        }
    }
}