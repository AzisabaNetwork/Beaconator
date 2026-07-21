package net.azisaba.beaconator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public final class Beaconator extends JavaPlugin {

    private Map<UUID, Set<PotionEffectType>> activeBuffs;
    private BeaconGui beaconGui;
    private BuffTask buffTask;
    private boolean beaconEnabled = false;
    private File playerDataFolder;
    private Gson gson;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        
        this.playerDataFolder = new File(getDataFolder(), "playerdata");
        if (!this.playerDataFolder.exists()) {
            this.playerDataFolder.mkdirs();
        }
        this.gson = new Gson();

        this.activeBuffs = new HashMap<>();
        this.beaconGui = new BeaconGui(this);

        this.getCommand("buff").setExecutor(new BeaconCommand(this));
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        long interval = this.getConfig().getLong("update-interval", 80L);
        this.buffTask = new BuffTask(this);
        this.buffTask.runTaskTimer(this, 0L, interval);

        String value = this.getConfig().getString("enabled", "true");
        if (value.equalsIgnoreCase("true")) {
            beaconEnabled = true;
        }

        this.getLogger().info("Beaconator has been enabled.");
    }

    @Override
    public void onDisable() {
        if (this.buffTask != null) {
            this.buffTask.cancel();
        }
        this.buffTask.clearAllEffects();
        this.getLogger().info("Beaconator has been disabled.");
    }

    public Set<PotionEffectType> getActiveBuffs(Player player) {
        return activeBuffs.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public void removePlayerData(Player player) {
        activeBuffs.remove(player.getUniqueId());
    }

    public void toggleBuff(Player player, PotionEffectType effectType) {
        Set<PotionEffectType> playerBuffs = getActiveBuffs(player);
        if (playerBuffs.contains(effectType)) {
            playerBuffs.remove(effectType);
        } else {
            playerBuffs.add(effectType);
        }
        activeBuffs.put(player.getUniqueId(), playerBuffs);
        savePlayerBuffs(player.getUniqueId(), playerBuffs);
    }

    public int getMaxBuffs(Player player) {
        if (player.hasPermission("beaconator.level.expert") || player.hasPermission("beaconator.level.master")) return 3;
        if (player.hasPermission("beaconator.level.apprentice") || player.hasPermission("beaconator.level.adept")) return 2;
        return 0;
    }

    public int getAvailableEffectLevel(Player player, String effectKey) {
        ConfigurationSection ranks = getConfig().getConfigurationSection("effects." + effectKey + ".ranks");
        if (ranks == null) return -1;
        if (player.hasPermission("beaconator.level.master") && ranks.contains("master")) {
            return ranks.getInt("master") - 1;
        }
        if (player.hasPermission("beaconator.level.expert") && ranks.contains("expert")) {
            return ranks.getInt("expert") - 1;
        }
        if (player.hasPermission("beaconator.level.adept") && ranks.contains("adept")) {
            return ranks.getInt("adept") - 1;
        }
        if (player.hasPermission("beaconator.level.apprentice") && ranks.contains("apprentice")) {
            return ranks.getInt("apprentice") - 1;
        }
        return -1;
    }

    public BeaconGui getBuffGUI() {
        return beaconGui;
    }

    public boolean isBeaconEnabled() {
        return beaconEnabled;
    }

    public void loadPlayerBuffs(UUID uuid) {
        File file = new File(playerDataFolder, uuid.toString() + ".json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> effectNames = gson.fromJson(reader, listType);

            if (effectNames != null && !effectNames.isEmpty()) {
                Set<PotionEffectType> buffs = new HashSet<>();
                for (String name : effectNames) {
                    PotionEffectType type = PotionEffectType.getByName(name);
                    if (type != null) {
                        buffs.add(type);
                    }
                }
                activeBuffs.put(uuid, buffs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePlayerBuffs(UUID uuid, Set<PotionEffectType> buffs) {
        List<String> effectNames = new ArrayList<>();
        for (PotionEffectType type : buffs) {
            effectNames.add(type.getName());
        }

        File file = new File(playerDataFolder, uuid.toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(effectNames, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}