package net.azisaba.beaconator;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class EffectResolver {

    private static final Map<String, String> LEGACY_NAMES = new HashMap<>();

    static {
        LEGACY_NAMES.put("SLOW", "slowness");
        LEGACY_NAMES.put("FAST_DIGGING", "haste");
        LEGACY_NAMES.put("SLOW_DIGGING", "mining_fatigue");
        LEGACY_NAMES.put("INCREASE_DAMAGE", "strength");
        LEGACY_NAMES.put("HEAL", "instant_health");
        LEGACY_NAMES.put("HARM", "instant_damage");
        LEGACY_NAMES.put("JUMP", "jump_boost");
        LEGACY_NAMES.put("CONFUSION", "nausea");
        LEGACY_NAMES.put("DAMAGE_RESISTANCE", "resistance");
    }

    private EffectResolver() {
    }

    @Nullable
    public static PotionEffectType resolve(String configKey) {
        if (configKey == null || configKey.isEmpty()) return null;
        String name = LEGACY_NAMES.getOrDefault(configKey.toUpperCase(Locale.ROOT), configKey);
        NamespacedKey key = name.contains(":")
                ? NamespacedKey.fromString(name.toLowerCase(Locale.ROOT))
                : NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT));
        if (key == null) return null;
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(key);
    }
}
