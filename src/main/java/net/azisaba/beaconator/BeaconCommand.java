package net.azisaba.beaconator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BeaconCommand implements CommandExecutor {

    private final Beaconator plugin;

    public BeaconCommand(Beaconator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ使用できます");
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.isBeaconEnabled()) {
            player.sendMessage("ビーコンは無効化されています");
            return true;
        }
        if (args.length == 0) {
            plugin.getBuffGUI().openBeaconGui(player);
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("beaconator.reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage("Configをリロードしました");
                } else {
                    sender.sendMessage(ChatColor.RED + "権限がありません");
                }
                return  true;
            }
        }
        return false;
    }
}
