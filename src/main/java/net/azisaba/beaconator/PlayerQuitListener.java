package net.azisaba.beaconator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Beaconator plugin;

    public PlayerQuitListener(Beaconator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removePlayerData(event.getPlayer());
    }
}
