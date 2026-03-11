package com.test.sentry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class SentryConfig {

    private final int maxTier;
    private final FileConfiguration config;

    public SentryConfig(FileConfiguration config) {
        this.config = config;
        // Cap max-tier to 40
        int loadedMax = config.getInt("max-tier", 10);
        this.maxTier = Math.min(loadedMax, 40);
    }

    public int getMaxTier() {
        return maxTier;
    }

    public int getRangeBonus(int tier) {
        return getUpgradeValue("range", tier);
    }

    public int getRechargeReduction(int tier) {
        return getUpgradeValue("recharge", tier);
    }

    public int getDamageBonus(int tier) {
        return getUpgradeValue("damage", tier);
    }

    public int getBuffAmplifier(int tier) {
        return getUpgradeValue("buff", tier);
    }

    public int getTargetsBonus(int tier) {
        return getUpgradeValue("targets", tier);
    }

    private int getUpgradeValue(String statPath, int tier) {
        if (tier <= 0) return 0;
        ConfigurationSection section = config.getConfigurationSection("upgrades." + statPath);
        if (section != null && section.contains(String.valueOf(tier))) {
            return section.getInt(String.valueOf(tier));
        }
        return 0; // Default if not defined in config
    }
}
