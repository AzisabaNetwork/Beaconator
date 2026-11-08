package net.azisaba.beaconator;

import org.bukkit.Bukkit;
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
        if (!plugin.isBeaconEnabled()) return;
        int duration = (int) (plugin.getConfig().getLong("update-interval", 80L) + 240L);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<String> activeEffects = plugin.getActiveBuffs(player);
            if (activeEffects.isEmpty()) continue;

            for (String key : activeEffects) {
                PotionEffectType effectType = EffectResolver.resolve(key);
                if (effectType == null) continue;
                int level = plugin.getAvailableEffectLevel(player, key);
                if (level < 0) continue;

                PotionEffect current = player.getPotionEffect(effectType);
                if (current != null && current.getAmplifier() > level && current.getDuration() <= duration) {
                    player.removePotionEffect(effectType);
                }
                player.addPotionEffect(new PotionEffect(effectType, duration, level, true, true));
            }
        }
    }

    public void clearAllEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String key : plugin.getActiveBuffs(player)) {
                PotionEffectType effectType = EffectResolver.resolve(key);
                if (effectType != null) {
                    player.removePotionEffect(effectType);
                }
            }
        }
    }
}
