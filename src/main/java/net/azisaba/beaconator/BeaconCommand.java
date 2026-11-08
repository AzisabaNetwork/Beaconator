package net.azisaba.beaconator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("beaconator.reload")) {
                plugin.reload();
                sender.sendMessage(Component.text("Configをリロードしました"));
            } else {
                sender.sendMessage(Component.text("権限がありません", NamedTextColor.RED));
            }
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ使用できます", NamedTextColor.RED));
            return true;
        }
        if (!plugin.isBeaconEnabled()) {
            player.sendMessage(Component.text("ビーコンは無効化されています"));
            return true;
        }
        if (args.length == 0) {
            plugin.getBuffGUI().openBeaconGui(player);
            return true;
        }
        return false;
    }
}
