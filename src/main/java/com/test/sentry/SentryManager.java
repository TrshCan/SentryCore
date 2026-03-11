package com.test.sentry;

import org.bukkit.Location;

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

    public void addSentry(Location coreLoc) {
        sentries.putIfAbsent(coreLoc.toBlockLocation(), new SentryData());
    }

    public void removeSentry(Location coreLoc) {
        sentries.remove(coreLoc.toBlockLocation());
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
        sentries.clear();
    }
}
