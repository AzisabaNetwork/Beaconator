package net.azisaba.beaconator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class Beaconator extends JavaPlugin {

    private static final String[] RANKS = {"apprentice", "adept", "expert", "master"};

    private Map<UUID, Set<String>> activeBuffs;
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

        this.getCommand("beacon").setExecutor(new BeaconCommand(this));
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        long interval = this.getConfig().getLong("update-interval", 80L);
        this.buffTask = new BuffTask(this);
        this.buffTask.runTaskTimer(this, 0L, interval);

        this.beaconEnabled = this.getConfig().getBoolean("enabled", true);

        validateEffects();

        for (Player player : getServer().getOnlinePlayers()) {
            loadPlayerBuffs(player.getUniqueId());
        }

        this.getLogger().info("Beaconator has been enabled.");
    }

    @Override
    public void onDisable() {
        if (this.buffTask != null) {
            this.buffTask.cancel();
            this.buffTask.clearAllEffects();
        }
        this.getLogger().info("Beaconator has been disabled.");
    }

    public void reload() {
        reloadConfig();
        this.beaconEnabled = getConfig().getBoolean("enabled", true);
        validateEffects();
    }

    public void validateEffects() {
        ConfigurationSection effects = getConfig().getConfigurationSection("effects");
        if (effects == null) return;
        for (String key : effects.getKeys(false)) {
            if (EffectResolver.resolve(key) == null) {
                getLogger().warning("不明なポーション効果です: " + key);
            }
        }
    }

    public Set<String> getActiveBuffs(Player player) {
        return activeBuffs.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public void removePlayerData(Player player) {
        activeBuffs.remove(player.getUniqueId());
    }

    public void toggleBuff(Player player, String effectKey) {
        Set<String> playerBuffs = getActiveBuffs(player);
        if (!playerBuffs.remove(effectKey)) {
            playerBuffs.add(effectKey);
        }
        activeBuffs.put(player.getUniqueId(), playerBuffs);
        savePlayerBuffs(player.getUniqueId(), playerBuffs);
    }

    public int getRankTier(Player player) {
        for (int i = RANKS.length - 1; i >= 0; i--) {
            if (player.hasPermission("beaconator.level." + RANKS[i])) return i;
        }
        return -1;
    }

    public int getMaxBuffs(Player player) {
        int tier = getRankTier(player);
        if (tier >= 2) return 3; // expert, master
        if (tier >= 0) return 2; // apprentice, adept
        return 0;
    }

    public int getAvailableEffectLevel(Player player, String effectKey) {
        ConfigurationSection effect = getConfig().getConfigurationSection("effects." + effectKey);
        if (effect == null || !effect.getBoolean("enabled", true)) return -1;
        ConfigurationSection ranks = effect.getConfigurationSection("ranks");
        if (ranks == null) return -1;
        int tier = getRankTier(player);
        int level = 0;
        for (int i = 0; i <= tier; i++) {
            if (ranks.contains(RANKS[i])) {
                level = Math.max(level, ranks.getInt(RANKS[i]));
            }
        }
        return level - 1;
    }

    public BeaconGui getBuffGUI() {
        return beaconGui;
    }

    public boolean isBeaconEnabled() {
        return beaconEnabled;
    }

    public void loadPlayerBuffs(UUID uuid) {
        File file = new File(playerDataFolder, uuid + ".json");
        if (!file.exists()) return;

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> effectKeys = gson.fromJson(reader, listType);
            if (effectKeys == null || effectKeys.isEmpty()) return;

            ConfigurationSection effects = getConfig().getConfigurationSection("effects");
            Set<String> buffs = new HashSet<>();
            for (String key : effectKeys) {
                if (effects != null && effects.isConfigurationSection(key)) {
                    buffs.add(key);
                }
            }
            activeBuffs.put(uuid, buffs);
        } catch (IOException | RuntimeException e) {
            getLogger().warning("プレイヤーデータの読み込みに失敗しました: " + file.getName() + " (" + e.getMessage() + ")");
        }
    }

    private void savePlayerBuffs(UUID uuid, Set<String> buffs) {
        File file = new File(playerDataFolder, uuid + ".json");
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            gson.toJson(new ArrayList<>(buffs), writer);
        } catch (IOException e) {
            getLogger().warning("プレイヤーデータの保存に失敗しました: " + file.getName() + " (" + e.getMessage() + ")");
        }
    }
}
