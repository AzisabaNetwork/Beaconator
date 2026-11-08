package net.azisaba.beaconator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class GuiListener implements Listener {

    private final Beaconator plugin;

    public GuiListener(Beaconator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BeaconGui) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BeaconGui gui)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        ConfigurationSection effectsSection = plugin.getConfig().getConfigurationSection("effects");
        if (effectsSection == null) return;

        for (String key : effectsSection.getKeys(false)) {
            ConfigurationSection effectConfig = effectsSection.getConfigurationSection(key);
            if (effectConfig == null || !effectConfig.getBoolean("enabled", true)) continue;
            if (effectConfig.getInt("slot", -1) != event.getSlot()) continue;

            int availableLevel = plugin.getAvailableEffectLevel(player, key);
            if (availableLevel < 0) {
                player.sendMessage(Component.text("この効果を使用するにはランクアップが必要です。", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                return;
            }
            PotionEffectType effectType = EffectResolver.resolve(key);
            if (effectType == null) {
                player.sendMessage(Component.text("この効果は現在利用できません。", NamedTextColor.RED));
                plugin.getLogger().warning("不明なポーション効果です: " + key);
                return;
            }
            Set<String> activeBuffs = plugin.getActiveBuffs(player);
            if (activeBuffs.contains(key)) {
                plugin.toggleBuff(player, key);
                player.removePotionEffect(effectType);
            } else {
                int limit = plugin.getMaxBuffs(player);
                if (activeBuffs.size() >= limit) {
                    String message = plugin.getConfig().getString("limit-reached-message", "&cあなたのランクでは最大{limit}個までです。");
                    message = message.replace("{limit}", String.valueOf(limit));
                    player.sendMessage(Texts.legacy(message));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                plugin.toggleBuff(player, key);
            }
            gui.updateItems(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }
    }
}
