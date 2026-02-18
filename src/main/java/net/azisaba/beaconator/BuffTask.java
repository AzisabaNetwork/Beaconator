package net.azisaba.beaconator;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class BuffTask extends BukkitRunnable {

    private final Beaconator plugin;

    public BuffTask(Beaconator plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long duration = plugin.getConfig().getLong("update-interval", 80L) + 240L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<PotionEffectType> activeEffects = plugin.getActiveBuffs(player);
            if (activeEffects.isEmpty()) continue;

            for (PotionEffectType effectType : activeEffects) {
                int level = plugin.getAvailableEffectLevel(player, effectType.getName());
                if (level >= 0) {
                    player.addPotionEffect(new PotionEffect(effectType, (int) duration, level, true, true));
                }
            }
        }
    }

    public void clearAllEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<PotionEffectType> activeEffects = plugin.getActiveBuffs(player);
            for (PotionEffectType effectType : activeEffects) {
                player.removePotionEffect(effectType);
            }
        }
    }
}