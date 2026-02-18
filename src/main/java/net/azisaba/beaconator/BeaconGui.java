package net.azisaba.beaconator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BeaconGui implements InventoryHolder {

    private final Beaconator plugin;
    private Inventory inv;

    public BeaconGui(Beaconator plugin) {
        this.plugin = plugin;
    }

    public void openBeaconGui(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui-title", "&1Player Buffs"));
        int rows = plugin.getConfig().getInt("gui-rows", 3);
        inv = Bukkit.createInventory(this, rows * 9, title);

        updateItems(player);
        player.openInventory(inv);
    }

    public void updateItems(Player player) {
        inv.clear();
        ConfigurationSection effects = plugin.getConfig().getConfigurationSection("effects");
        if (effects == null) return;
        Set<PotionEffectType> activeSet = plugin.getActiveBuffs(player);
        for (String key : effects.getKeys(false)) {
            ConfigurationSection config = effects.getConfigurationSection(key);
            int level = plugin.getAvailableEffectLevel(player, key);
            boolean isLocked = (level < 0);
            PotionEffectType type = PotionEffectType.getByName(key);
            Material mat = isLocked ? Material.BARRIER : Material.valueOf(config.getString("icon", "STONE"));
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            String name = ChatColor.translateAlternateColorCodes('&', config.getString("display-name", key));
            meta.setDisplayName(isLocked ? "§8§m" + ChatColor.stripColor(name) + " §c[未解放]" : name);
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("lore")) {
                String status = isLocked ? "§c[未解放]" : (activeSet.contains(type) ? "§a[有効]" : "§7[無効]");
                String lvlStr = isLocked ? "???" : String.valueOf(level + 1);
                lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("{status}", status).replace("{level}", lvlStr)));
            }
            if (isLocked) lore.add("§c※この効果を使用するにはランクアップが必要です");

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            if (!isLocked && activeSet.contains(type)) meta.addEnchant(Enchantment.DURABILITY, 1, true);

            item.setItemMeta(meta);
            inv.setItem(config.getInt("slot"), item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}