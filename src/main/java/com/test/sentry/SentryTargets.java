package com.test.sentry;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class SentryTargets {

    // A map of EntityType to its corresponding spawn egg Material
    public static final Map<EntityType, Material> HOSTILE_MOBS = new LinkedHashMap<>();

    static {
        add(EntityType.ZOMBIE, Material.ZOMBIE_SPAWN_EGG);
        add(EntityType.SKELETON, Material.SKELETON_SPAWN_EGG);
        add(EntityType.CREEPER, Material.CREEPER_SPAWN_EGG);
        add(EntityType.SPIDER, Material.SPIDER_SPAWN_EGG);
        add(EntityType.CAVE_SPIDER, Material.CAVE_SPIDER_SPAWN_EGG);
        add(EntityType.ENDERMAN, Material.ENDERMAN_SPAWN_EGG);
        add(EntityType.PHANTOM, Material.PHANTOM_SPAWN_EGG);
        add(EntityType.SLIME, Material.SLIME_SPAWN_EGG);
        add(EntityType.MAGMA_CUBE, Material.MAGMA_CUBE_SPAWN_EGG);
        add(EntityType.BOGGED, Material.BOGGED_SPAWN_EGG);
        add(EntityType.STRAY, Material.STRAY_SPAWN_EGG);
        add(EntityType.HUSK, Material.HUSK_SPAWN_EGG);
        add(EntityType.DROWNED, Material.DROWNED_SPAWN_EGG);
        add(EntityType.WITCH, Material.WITCH_SPAWN_EGG);
        add(EntityType.PILLAGER, Material.PILLAGER_SPAWN_EGG);
        add(EntityType.VINDICATOR, Material.VINDICATOR_SPAWN_EGG);
        add(EntityType.EVOKER, Material.EVOKER_SPAWN_EGG);
        add(EntityType.RAVAGER, Material.RAVAGER_SPAWN_EGG);
        add(EntityType.WARDEN, Material.WARDEN_SPAWN_EGG);
        add(EntityType.PIGLIN, Material.PIGLIN_SPAWN_EGG);
        add(EntityType.PIGLIN_BRUTE, Material.PIGLIN_BRUTE_SPAWN_EGG);
        add(EntityType.ZOMBIFIED_PIGLIN, Material.ZOMBIFIED_PIGLIN_SPAWN_EGG);
        add(EntityType.GHAST, Material.GHAST_SPAWN_EGG);
        add(EntityType.BLAZE, Material.BLAZE_SPAWN_EGG);
        add(EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SPAWN_EGG);
        add(EntityType.SHULKER, Material.SHULKER_SPAWN_EGG);
        add(EntityType.SILVERFISH, Material.SILVERFISH_SPAWN_EGG);
        add(EntityType.ENDERMITE, Material.ENDERMITE_SPAWN_EGG);
        add(EntityType.GUARDIAN, Material.GUARDIAN_SPAWN_EGG);
        add(EntityType.ELDER_GUARDIAN, Material.ELDER_GUARDIAN_SPAWN_EGG);
        // Added BREEZE if 1.21 is available, but sticking to standard ones.
    }

    private static void add(EntityType type, Material egg) {
        HOSTILE_MOBS.put(type, egg);
    }

    /**
     * Gets the default set of targets for a new Sentry.
     * Currently returns all configured hostile mobs.
     */
    public static Set<EntityType> getDefaultTargets() {
        return new HashSet<>(HOSTILE_MOBS.keySet());
    }
}
