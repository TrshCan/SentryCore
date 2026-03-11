package com.test.sentry;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tracks all registered Sentry Core locations and their associated state data.
 */
public final class SentryManager {

    private final JavaPlugin plugin;
    private final SentryConfig config;
    private final NamespacedKey ownerKey, modeKey, rangeKey, rechargeKey, damageKey, buffKey, targetsKey, allowedTargetsKey;
    private final Color baseColor; // Added baseColor field

    // Tracking all active Sentry Cores by Location
    private final Map<Location, SentryData> activeSentries = new HashMap<>();

    // Tracks cooldowns and effects for the new reactive Emergency Buff system
    private final SentryEmergencyBuff emergencyBuffManager;

    public SentryManager(JavaPlugin plugin, SentryConfig config) { // Changed MyPlugin to JavaPlugin to match original
        this.plugin = plugin;
        this.config = config;
        this.baseColor = Color.fromRGB(23, 23, 23);  // Example core color
        this.ownerKey = new NamespacedKey(plugin, "sentry_owner");
        this.modeKey = new NamespacedKey(plugin, "sentry_mode");
        this.rangeKey = new NamespacedKey(plugin, "sentry_range");
        this.rechargeKey = new NamespacedKey(plugin, "sentry_recharge");
        this.damageKey = new NamespacedKey(plugin, "sentry_damage");
        this.buffKey = new NamespacedKey(plugin, "sentry_buff");
        this.targetsKey = new NamespacedKey(plugin, "sentry_targets");
        this.allowedTargetsKey = new NamespacedKey(plugin, "sentry_allowed_targets");
        this.emergencyBuffManager = new SentryEmergencyBuff(this);
    }

    public SentryEmergencyBuff getEmergencyBuffManager() {
        return emergencyBuffManager;
    }

    public SentryConfig getConfig() {
        return config;
    }

    public void addSentry(Location coreLoc, String ownerName, int rangeTier, int rechargeTier, int damageTier, int buffTier, int targetsTier, java.util.Set<org.bukkit.entity.EntityType> allowedTargets) {
        Location loc = coreLoc.toBlockLocation();
        if (!activeSentries.containsKey(loc)) {
            SentryData data = new SentryData(ownerName);
            data.setRangeTier(rangeTier);
            data.setRechargeTier(rechargeTier);
            data.setDamageTier(damageTier);
            data.setBuffTier(buffTier);
            data.setTargetsTier(targetsTier);
            if (allowedTargets != null) {
                data.setAllowedTargets(new java.util.HashSet<>(allowedTargets));
            }
            
            activeSentries.put(loc, data);
            // Sentry starts inactive, leaving the conduit as-is
            setSentryActiveState(loc, data, false);
            saveToPDC(loc, data);
        }
    }

    public void removeSentry(Location coreLoc) {
        Location loc = coreLoc.toBlockLocation();
        SentryData data = this.getData(loc);
        if (data != null) {
            // Clean up crystal if it was active
            setSentryActiveState(loc, data, false);
            // Also ensure the conduit block is removed if the whole thing is being destroyed
            loc.getBlock().setType(Material.AIR);
            
            // Clear PDC data from the underlying obsidian block
            Location obsiLoc = loc.clone().subtract(0, 1, 0);
            if (obsiLoc.getBlock().getState() instanceof TileState tile) {
                PersistentDataContainer pdc = tile.getPersistentDataContainer();
                pdc.remove(ownerKey);
                pdc.remove(modeKey);
                pdc.remove(rangeKey);
                pdc.remove(rechargeKey);
                pdc.remove(damageKey);
                pdc.remove(buffKey);
                pdc.remove(targetsKey);
                pdc.remove(allowedTargetsKey);
                tile.update();
            }
        }
        activeSentries.remove(loc);
    }

    /** Returns the SentryData for the given location, or null if not a sentry. */
    public SentryData getData(Location coreLoc) {
        return activeSentries.get(coreLoc.toBlockLocation());
    }

    public boolean isSentry(Location loc) {
        return activeSentries.containsKey(loc.toBlockLocation());
    }

    /** Returns an unmodifiable view of all registered sentry locations. */
    public Set<Location> getActiveSentries() {
        return Collections.unmodifiableSet(activeSentries.keySet());
    }

    public void clear() {
        for (Map.Entry<Location, SentryData> entry : activeSentries.entrySet()) {
            setSentryActiveState(entry.getKey(), entry.getValue(), false);
        }
        activeSentries.clear();
        emergencyBuffManager.cleanup();
    }

    /**
     * Toggles the physical manifestation of the sentry (Conduit vs End Crystal).
     * Called when the active state changes or when the sentry is created/destroyed.
     */
    public void setSentryActiveState(Location coreLoc, SentryData data, boolean active) {
        data.setActive(active);
        
        if (active) {
            // Remove conduit block and spawn crystal
            coreLoc.getBlock().setType(Material.AIR);
            
            // Spawn crystal centered in the block
            Location spawnLoc = coreLoc.clone().add(0.5, 0.0, 0.5);
            EnderCrystal crystal = (EnderCrystal) coreLoc.getWorld().spawnEntity(spawnLoc, EntityType.END_CRYSTAL);
            crystal.setInvulnerable(true);
            crystal.setShowingBottom(false); // remove the bedrock base
            
            // Store the entity UUID so we can remove it later
            data.setCrystalUuid(crystal.getUniqueId());
        } else {
            // Remove crystal
            if (data.getCrystalUuid() != null) {
                org.bukkit.entity.Entity entity = Bukkit.getEntity(data.getCrystalUuid());
                if (entity != null) {
                    entity.remove();
                }
                data.setCrystalUuid(null);
            }
            // Restore conduit block
            coreLoc.getBlock().setType(Material.CONDUIT);
        }
        saveToPDC(coreLoc, data);
    }

    /**
     * Saves SentryData to the PersistentDataContainer of the Obsidian block below the core.
     */
    public void saveToPDC(Location coreLoc, SentryData data) {
        Location obsiLoc = coreLoc.clone().subtract(0, 1, 0);
        if (obsiLoc.getBlock().getState() instanceof TileState tile) {
            PersistentDataContainer pdc = tile.getPersistentDataContainer();
            if (data.getOwnerName() != null) pdc.set(ownerKey, PersistentDataType.STRING, data.getOwnerName());
            pdc.set(modeKey, PersistentDataType.STRING, data.getMode().name());
            pdc.set(rangeKey, PersistentDataType.INTEGER, data.getRangeTier());
            pdc.set(rechargeKey, PersistentDataType.INTEGER, data.getRechargeTier());
            pdc.set(damageKey, PersistentDataType.INTEGER, data.getDamageTier());
            pdc.set(buffKey, PersistentDataType.INTEGER, data.getBuffTier());
            pdc.set(targetsKey, PersistentDataType.INTEGER, data.getTargetsTier());

            if (data.getAllowedTargets() != null) {
                String targetsStr = data.getAllowedTargets().stream().map(Enum::name).collect(java.util.stream.Collectors.joining(","));
                pdc.set(allowedTargetsKey, PersistentDataType.STRING, targetsStr);
            }

            tile.update();
        }
    }

    /**
     * Loads SentryData from the PersistentDataContainer of the Obsidian block below the core, if any.
     */
    public SentryData loadFromPDC(Location coreLoc) {
        Location obsiLoc = coreLoc.clone().subtract(0, 1, 0);
        if (obsiLoc.getBlock().getState() instanceof TileState tile) {
            PersistentDataContainer pdc = tile.getPersistentDataContainer();
            if (pdc.has(ownerKey, PersistentDataType.STRING)) {
                String ownerName = pdc.get(ownerKey, PersistentDataType.STRING);
                SentryData data = new SentryData(ownerName);
                
                if (pdc.has(modeKey, PersistentDataType.STRING)) {
                    try {
                        data.setMode(SentryMode.valueOf(pdc.get(modeKey, PersistentDataType.STRING)));
                    } catch (IllegalArgumentException ignored) {}
                }
                
                data.setRangeTier(pdc.getOrDefault(rangeKey, PersistentDataType.INTEGER, 0));
                data.setRechargeTier(pdc.getOrDefault(rechargeKey, PersistentDataType.INTEGER, 0));
                data.setDamageTier(pdc.getOrDefault(damageKey, PersistentDataType.INTEGER, 0));
                data.setBuffTier(pdc.getOrDefault(buffKey, PersistentDataType.INTEGER, 0));
                data.setTargetsTier(pdc.getOrDefault(targetsKey, PersistentDataType.INTEGER, 0));
                
                String targetsStr = pdc.get(allowedTargetsKey, PersistentDataType.STRING);
                if (targetsStr != null && !targetsStr.isEmpty()) {
                    java.util.Set<org.bukkit.entity.EntityType> targets = new java.util.HashSet<>();
                    for (String s : targetsStr.split(",")) {
                        try {
                            targets.add(org.bukkit.entity.EntityType.valueOf(s));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    if (!targets.isEmpty()) {
                        data.setAllowedTargets(targets);
                    }
                }

                return data;
            }
        }
        return null;
    }
}
