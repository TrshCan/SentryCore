package com.test.sentry;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SentryEmergencyBuff {

    private final SentryManager sentryManager;
    // Map storing standard cooldown end times per player UUID
    private final Map<UUID, Long> regularCooldowns = new HashMap<>();
    // Map storing fatal cooldown end times per player UUID
    private final Map<UUID, Long> fatalCooldowns = new HashMap<>();

    public SentryEmergencyBuff(SentryManager sentryManager) {
        this.sentryManager = sentryManager;
    }

    /**
     * Attempts to trigger the emergency buff for a player taking damage when near an active Sentry Core.
     * @param player The player taking damage
     * @param finalHealth Expected health after damage
     * @return true if fatal damage was prevented
     */
    public boolean handleDamage(Player player, double finalHealth) {
        // Quick short-circuit: if health isn't dropping below the maximum possible trigger (12 HP / 6 hearts), ignore.
        if (finalHealth > 12.0) return false;

        Location pLoc = player.getLocation();
        SentryData bestCore = null;
        int maxTier = 0;

        // Find the best nearby active core within 15 blocks
        for (Location coreLoc : sentryManager.getActiveSentries()) {
            if (coreLoc.getWorld().equals(pLoc.getWorld()) && coreLoc.distanceSquared(pLoc) <= 225.0) { // 15^2
                SentryData data = sentryManager.getData(coreLoc);
                if (data != null && data.getBuffTier() > maxTier) {
                    maxTier = data.getBuffTier();
                    bestCore = data;
                }
            }
        }

        if (bestCore == null || maxTier == 0) return false;

        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // 1. Check for Tier 10 Fatal trigger
        if (maxTier >= 10 && finalHealth <= 0) {
            long fatalCdEnd = fatalCooldowns.getOrDefault(pid, 0L);
            if (now >= fatalCdEnd) {
                // Prevent death!
                player.setHealth(1.0);
                
                // Visual/Audio Totem Feedback 
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);

                // Apply massive effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0)); // Regen I 10s
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0)); // Instant Health I
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 4)); // Resistance V 5s
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 3)); // Absorption IV

                // Set 10 minute cooldown
                fatalCooldowns.put(pid, now + (10 * 60 * 1000));
                
                // Optionally message the player
                player.sendMessage(net.kyori.adventure.text.Component.text("Your Sentry Core has saved your life! (10m cooldown)")
                    .color(net.kyori.adventure.text.format.TextColor.fromHexString("#FFD700")));
                    
                return true; 
            }
        }

        // 2. Check for regular threshold triggers
        double threshold = getThresholdForTier(maxTier);
        if (finalHealth <= threshold) {
            long regularCdEnd = regularCooldowns.getOrDefault(pid, 0L);
            if (now >= regularCdEnd) {
                applyTierEffects(player, maxTier);
                long cooldownMs = getCooldownForTier(maxTier) * 1000L;
                regularCooldowns.put(pid, now + cooldownMs);
                
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.5f);
            }
        }

        return false;
    }

    private double getThresholdForTier(int tier) {
        return switch (tier) {
            case 1 -> 4.0;
            case 2, 3, 10 -> 5.0; // Tier 10 secondary trigger is 5.0 HP
            case 4 -> 6.0;
            case 5 -> 7.0;
            case 6 -> 8.0;
            case 7 -> 9.0;
            case 8 -> 10.0;
            case 9 -> 12.0;
            default -> 0.0;
        };
    }

    private int getCooldownForTier(int tier) {
        return switch (tier) {
            case 1, 2, 3, 4 -> 60;
            case 5, 6 -> 90;
            case 7 -> 120; // 2m
            case 8 -> 180; // 3m
            case 9 -> 300; // 5m
            case 10 -> 30; // 30s for the secondary trigger
            default -> 60;
        };
    }

    private void applyTierEffects(Player player, int tier) {
        switch (tier) {
            case 1 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0));
            }
            case 2 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
            }
            case 3 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 1)); // Regen II
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 0)); // Res I
            }
            case 4 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 8 * 20, 0));
            }
            case 5 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 12 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1)); // Absorp II (amplifier 1)
            }
            case 6 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 2)); // Regen III (amplifier 2)
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1)); // Speed II
            }
            case 7 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10 * 20, 1)); // Res II
            }
            case 8 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 2)); // Res III
            }
            case 9 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1)); // Instant Health II
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 3)); // Res IV
            }
            case 10 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 0)); // Res I
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1)); // Absorp II
            }
        }
    }

    public void cleanup() {
        regularCooldowns.clear();
        fatalCooldowns.clear();
    }
}
