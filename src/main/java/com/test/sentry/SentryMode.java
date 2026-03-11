package com.test.sentry;

import org.bukkit.Material;

/**
 * The three firing modes of a Sentry, each driven by a specific fuel item.
 */
public enum SentryMode {

    AMETHYST("Amethyst Mode", Material.AMETHYST_SHARD, 10),
    PRISMARINE("Prismarine Mode", Material.PRISMARINE_SHARD, 20),
    ECHO("Echo Mode", Material.ECHO_SHARD, 40);

    private final String displayName;
    private final Material fuelMaterial;
    private final int tickInterval; // how often this mode fires

    SentryMode(String displayName, Material fuelMaterial, int tickInterval) {
        this.displayName = displayName;
        this.fuelMaterial = fuelMaterial;
        this.tickInterval = tickInterval;
    }

    public String getDisplayName() { return displayName; }
    public Material getFuelMaterial() { return fuelMaterial; }
    public int getTickInterval() { return tickInterval; }
}
