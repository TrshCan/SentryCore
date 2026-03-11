package com.test.sentry;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SentryGuiHolder implements InventoryHolder {

    public enum GuiType {
        MAIN, MODE, UPGRADE, TARGET_LIST
    }

    private final Location sentryLocation;
    private final GuiType type;
    private Inventory inventory;

    public SentryGuiHolder(Location sentryLocation, GuiType type) {
        this.sentryLocation = sentryLocation;
        this.type = type;
    }

    public Location getSentryLocation() {
        return sentryLocation;
    }

    public GuiType getType() {
        return type;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory != null ? inventory : org.bukkit.Bukkit.createInventory(null, 9); // Fallback to avoid null
    }
}
