package net.azisaba.beaconator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

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
        Component title = Texts.legacy(plugin.getConfig().getString("gui-title", "&1Player Buffs"));
        int rows = Math.max(1, Math.min(6, plugin.getConfig().getInt("gui-rows", 3)));
        BeaconGui holder = new BeaconGui(plugin);
        holder.inv = Bukkit.createInventory(holder, rows * 9, title);
        holder.updateItems(player);
        player.openInventory(holder.inv);
    }

    public void updateItems(Player player) {
        inv.clear();
        ConfigurationSection effects = plugin.getConfig().getConfigurationSection("effects");
        if (effects == null) return;
        Set<String> activeSet = plugin.getActiveBuffs(player);
        for (String key : effects.getKeys(false)) {
            ConfigurationSection config = effects.getConfigurationSection(key);
            if (config == null || !config.getBoolean("enabled", true)) continue;
            int slot = config.getInt("slot", -1);
            if (slot < 0 || slot >= inv.getSize()) continue;

            int level = plugin.getAvailableEffectLevel(player, key);
            boolean isLocked = (level < 0);
            boolean isActive = !isLocked && activeSet.contains(key);

            Material mat = isLocked ? Material.BARRIER : Material.matchMaterial(config.getString("icon", "STONE"));
            if (mat == null) mat = Material.STONE;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            String status = isLocked ? "&c[未解放]" : (isActive ? "&a[有効]" : "&7[無効]");
            String lvlStr = isLocked ? "???" : String.valueOf(level + 1);
            Component name = Texts.item(applyPlaceholders(config.getString("display-name", key), status, lvlStr));
            if (isLocked) {
                name = Texts.noItalic(Component.text(Texts.plain(name), NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH)
                        .append(Component.text(" [未解放]", NamedTextColor.RED).decoration(TextDecoration.STRIKETHROUGH, false)));
            }
            meta.displayName(name);

            List<Component> lore = new ArrayList<>();
            for (String line : config.getStringList("lore")) {
                lore.add(Texts.item(applyPlaceholders(line, status, lvlStr)));
            }
            if (isLocked) {
                lore.add(Texts.noItalic(Component.text("※この効果を使用するにはランクアップが必要です", NamedTextColor.RED)));
            }
            meta.lore(lore);

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            meta.setEnchantmentGlintOverride(isActive);

            item.setItemMeta(meta);
            inv.setItem(slot, item);
        }
    }

    private static String applyPlaceholders(String text, String status, String level) {
        if (text == null) return "";
        return text.replace("{status}", status).replace("{level}", level);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
