package com.test.sentry;

import java.util.UUID;

/**
 * Holds the mutable runtime state for a single placed Sentry.
 */
public class SentryData {

    private boolean active;
    private SentryMode mode;
    private UUID crystalUuid;

    public SentryData() {
        this.active = true;                  // starts active on placement
        this.mode = SentryMode.AMETHYST;     // default mode
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public SentryMode getMode() { return mode; }
    public void setMode(SentryMode mode) { this.mode = mode; }

    public UUID getCrystalUuid() { return crystalUuid; }
    public void setCrystalUuid(UUID crystalUuid) { this.crystalUuid = crystalUuid; }
}
