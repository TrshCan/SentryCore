package com.test.sentry;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tracks all registered Sentry Core locations and their associated state data.
 */
public final class SentryManager {

    // Key: block location of the Conduit. Value: mutable sentry state.
    private final Map<Location, SentryData> sentries = new HashMap<>();

    public void addSentry(Location coreLoc, String ownerName) {
        Location loc = coreLoc.toBlockLocation();
        if (!sentries.containsKey(loc)) {
            SentryData data = new SentryData(ownerName);
            sentries.put(loc, data);
            // Sentry starts inactive, leaving the conduit as-is
            setSentryActiveState(loc, data, false);
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
        }
        sentries.remove(loc);
    }

    /** Returns the SentryData for the given location, or null if not a sentry. */
    public SentryData getData(Location coreLoc) {
        return sentries.get(coreLoc.toBlockLocation());
    }

    public boolean isSentry(Location loc) {
        return sentries.containsKey(loc.toBlockLocation());
    }

    /** Returns an unmodifiable view of all registered sentry locations. */
    public Set<Location> getActiveSentries() {
        return Collections.unmodifiableSet(sentries.keySet());
    }

    public void clear() {
        for (Map.Entry<Location, SentryData> entry : sentries.entrySet()) {
            setSentryActiveState(entry.getKey(), entry.getValue(), false);
        }
        sentries.clear();
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
    }
}
