package com.test.sentry;

import java.util.UUID;

/**
 * Holds the mutable runtime state for a single placed Sentry.
 */
public class SentryData {

    private boolean active;
    private SentryMode mode;
    private UUID crystalUuid;
    private String ownerName;
    private int rangeTier = 0;
    private int rechargeTier = 0;
    private int damageTier = 0;
    private int buffTier = 0;
    private int targetsTier = 0;

    public SentryData(String ownerName) {
        this.active = false;                 // starts INACTIVE on placement
        this.mode = SentryMode.AMETHYST;     // default mode
        this.ownerName = ownerName;
    }

    public int getTotalTier() {
        return rangeTier + rechargeTier + damageTier + buffTier + targetsTier;
    }

    public int getRangeTier() { return rangeTier; }
    public void setRangeTier(int rangeTier) { this.rangeTier = rangeTier; }

    public int getRechargeTier() { return rechargeTier; }
    public void setRechargeTier(int rechargeTier) { this.rechargeTier = rechargeTier; }

    public int getDamageTier() { return damageTier; }
    public void setDamageTier(int damageTier) { this.damageTier = damageTier; }

    public int getBuffTier() { return buffTier; }
    public void setBuffTier(int buffTier) { this.buffTier = buffTier; }

    public int getTargetsTier() { return targetsTier; }
    public void setTargetsTier(int targetsTier) { this.targetsTier = targetsTier; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public SentryMode getMode() { return mode; }
    public void setMode(SentryMode mode) { this.mode = mode; }

    public UUID getCrystalUuid() { return crystalUuid; }
    public void setCrystalUuid(UUID crystalUuid) { this.crystalUuid = crystalUuid; }
}
