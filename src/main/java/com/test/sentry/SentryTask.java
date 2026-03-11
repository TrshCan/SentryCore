package com.test.sentry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import org.bukkit.block.Container;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class SentryTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final SentryManager sentryManager;
    private long tickCount = 0;

    public SentryTask(JavaPlugin plugin, SentryManager sentryManager) {
        this.plugin = plugin;
        this.sentryManager = sentryManager;
    }

    @Override
    public void run() {
        tickCount++;

        // Copy to avoid ConcurrentModificationException if sentries are removed during iteration
        Set<Location> sentries = new HashSet<>(sentryManager.getActiveSentries());

        for (Location coreLoc : sentries) {
            processSentry(coreLoc);
        }
    }

    private void processSentry(Location coreLoc) {
        // Look up state — skip if inactive
        SentryData data = sentryManager.getData(coreLoc);
        if (data == null || !data.isActive()) return;

        Block coreBlock = coreLoc.getBlock();

        // When a sentry is active, the CONDUIT block is replaced by AIR (and an EnderCrystal entity)
        // If a player somehow places a block here, the sentry breaks.
        if (coreBlock.getType() != Material.AIR) {
            sentryManager.removeSentry(coreLoc);
            return;
        }

        // Validate barrel is still present and accessible (2 blocks down)
        Block barrelBlock = coreBlock.getRelative(0, -2, 0);
        if (!(barrelBlock.getState() instanceof Container container)) {
            sentryManager.removeSentry(coreLoc);
            return;
        }

        SentryMode mode = data.getMode();
        SentryConfig config = sentryManager.getConfig();

        // 1. Recharge Speed modifier from Upgrade Tier
        int rechargeReductionPercent = config.getRechargeReduction(data.getRechargeTier());
        double reductionFactor = 1.0 - (rechargeReductionPercent / 100.0);
        int realTickInterval = Math.max(1, (int) Math.round(mode.getTickInterval() * reductionFactor));

        // Only fire when this mode's modified tick interval aligns
        if (tickCount % realTickInterval != 0) return;

        // Passive beacon functionality has been removed in favor of Reacting Buffs on Damage.

        // Scan the full chest inventory for the required fuel material
        Inventory inventory = container.getInventory();
        int fuelSlot = findFuelSlot(inventory, mode.getFuelMaterial());
        if (fuelSlot == -1) return; // no fuel of this type

        // 2. Range modifier from Upgrade Tier
        double baseRange = 20.0;
        double realRange = baseRange + config.getRangeBonus(data.getRangeTier());

        // 4. Targets modifier from Upgrade Tier
        int maxTargets = 1 + config.getTargetsBonus(data.getTargetsTier());

        java.util.List<Mob> targets = findNearestMonsters(coreLoc, realRange, maxTargets, data.getAllowedTargets());
        if (targets.isEmpty()) return;

        // 3. Damage modifier from Upgrade Tier
        int damageBonusPercent = config.getDamageBonus(data.getDamageTier());
        double damageMultiplier = 1.0 + (damageBonusPercent / 100.0);

        for (Mob target : targets) {
            switch (mode) {
                case AMETHYST  -> fireAmethyst(coreLoc, target, damageMultiplier);
                case PRISMARINE -> firePrismarine(coreLoc, target, damageMultiplier);
                case ECHO      -> fireEcho(coreLoc, target, damageMultiplier);
            }
        }
        removeOneFuel(inventory, fuelSlot);
    }

    /**
     * Scans the inventory for the first slot containing the specified material.
     * Returns the slot index, or -1 if not found.
     */
    private int findFuelSlot(Inventory inventory, Material material) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                return i;
            }
        }
        return -1;
    }

    // ─────────────────── Mode A — Amethyst Shard ───────────────────

    private void fireAmethyst(Location coreLoc, Mob target, double damageMult) {
        Location shootFrom = coreLoc.clone().add(0.5, 0.5, 0.5);
        Location targetEye = target.getEyeLocation();

        Vector direction = targetEye.toVector()
                .subtract(shootFrom.toVector())
                .normalize();

        Arrow arrow = coreLoc.getWorld().spawn(shootFrom, Arrow.class);
        arrow.setVelocity(direction.multiply(1.5));
        arrow.setInvisible(true);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        // Arrow damage handles knockback, but explicit damage is dealt immediately
        arrow.setDamage(0.0);
        arrow.setSilent(true);

        // Explicit base 3.0 damage multiplied by Upgrade Tier and Glowing (3 seconds = 60 ticks)
        target.damage(3.0 * damageMult);
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));

        // Sound & particle at core
        coreLoc.getWorld().playSound(coreLoc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.2f);
        coreLoc.getWorld().spawnParticle(Particle.GLOW, shootFrom, 8, 0.1, 0.1, 0.1, 0.05);
    }

    // ─────────────────── Mode B — Prismarine Shard ───────────────────

    private void firePrismarine(Location coreLoc, Mob target, double damageMult) {
        // Delegate to the 20-tick tracking animation task
        SentryAnimationTask.startPrismarineLaser(plugin, coreLoc, target, damageMult);
    }

    // ─────────────────── Mode C — Echo Shard ───────────────────

    private void fireEcho(Location coreLoc, Mob target, double damageMult) {
        Location shootFrom = coreLoc.clone().add(0.5, 0.5, 0.5);
        Location targetEye = target.getEyeLocation();

        // Sonic boom particle line + burst at mob
        drawParticleLine(shootFrom, targetEye, Particle.SONIC_BOOM, 1.0);
        target.getWorld().spawnParticle(Particle.SONIC_BOOM, targetEye, 1, 0, 0, 0, 0);

        target.damage(25.0 * damageMult);

        coreLoc.getWorld().playSound(coreLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
    }

    // ─────────────────── Helpers ───────────────────

    /**
     * Finds up to {@code maxTargets} Mob entities within {@code radius} blocks of {@code origin}, sorted by distance.
     * Filtering using the specific allowedTargets set.
     */
    private java.util.List<Mob> findNearestMonsters(Location origin, double radius, int maxTargets, java.util.Set<org.bukkit.entity.EntityType> allowedTargets) {
        Collection<Mob> nearby = origin.getWorld().getNearbyEntitiesByType(
                Mob.class, origin, radius);
        return nearby.stream()
                .filter(m -> allowedTargets.contains(m.getType()))
                .filter(m -> !m.isDead() && m.isValid())
                .sorted(Comparator.comparingDouble(m -> m.getLocation().distanceSquared(origin)))
                .limit(maxTargets)
                .toList();
    }

    /**
     * Draws a line of particles from {@code start} to {@code end} with a step of {@code stepSize} blocks.
     */
    private void drawParticleLine(Location start, Location end, Particle particle, double stepSize) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        for (double d = 0; d < distance; d += stepSize) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Removes one item from the specified inventory slot, clearing the slot if the stack hits 0.
     */
    private void removeOneFuel(Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item == null) return;
        if (item.getAmount() <= 1) {
            inventory.setItem(slot, null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
