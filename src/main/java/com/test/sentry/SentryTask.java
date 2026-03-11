package com.test.sentry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SentryTask extends BukkitRunnable {

    private final SentryManager sentryManager;
    private long tickCount = 0;

    public SentryTask(SentryManager sentryManager) {
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

        // Validate core is still a Conduit
        if (coreBlock.getType() != Material.CONDUIT) {
            sentryManager.removeSentry(coreLoc);
            return;
        }

        // Validate chest is still present and accessible
        Block chestBlock = coreBlock.getRelative(BlockFace.DOWN);
        if (!(chestBlock.getState() instanceof Container container)) {
            sentryManager.removeSentry(coreLoc);
            return;
        }

        SentryMode mode = data.getMode();

        // Only fire when this mode's tick interval aligns
        if (tickCount % mode.getTickInterval() != 0) return;

        // Scan the full chest inventory for the required fuel material
        Inventory inventory = container.getInventory();
        int fuelSlot = findFuelSlot(inventory, mode.getFuelMaterial());
        if (fuelSlot == -1) return; // no fuel of this type

        Monster target = findNearestMonster(coreLoc, 20);
        if (target == null) return;

        switch (mode) {
            case AMETHYST  -> fireAmethyst(coreLoc, target);
            case PRISMARINE -> firePrismarine(coreLoc, target);
            case ECHO      -> fireEcho(coreLoc, target);
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

    private void fireAmethyst(Location coreLoc, Monster target) {
        Location shootFrom = coreLoc.clone().add(0.5, 0.5, 0.5);
        Location targetEye = target.getEyeLocation();

        Vector direction = targetEye.toVector()
                .subtract(shootFrom.toVector())
                .normalize();

        Arrow arrow = coreLoc.getWorld().spawn(shootFrom, Arrow.class);
        arrow.setVelocity(direction.multiply(1.5));
        arrow.setInvisible(true);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        arrow.setDamage(3.0);
        arrow.setSilent(true);

        // Glowing effect on mob (3 seconds = 60 ticks)
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));

        // Sound & particle at core
        coreLoc.getWorld().playSound(coreLoc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.2f);
        coreLoc.getWorld().spawnParticle(Particle.GLOW, shootFrom, 8, 0.1, 0.1, 0.1, 0.05);
    }

    // ─────────────────── Mode B — Prismarine Shard ───────────────────

    private void firePrismarine(Location coreLoc, Monster target) {
        Location shootFrom = coreLoc.clone().add(0.5, 0.5, 0.5);
        Location targetEye = target.getEyeLocation();

        // Draw Guardian-laser-style particle line from core to mob
        drawParticleLine(shootFrom, targetEye, Particle.BUBBLE, 0.4);

        // Damage and Slowness II (5 seconds = 100 ticks)
        target.damage(10.0);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false));

        coreLoc.getWorld().playSound(coreLoc, Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.0f);
    }

    // ─────────────────── Mode C — Echo Shard ───────────────────

    private void fireEcho(Location coreLoc, Monster target) {
        Location shootFrom = coreLoc.clone().add(0.5, 0.5, 0.5);
        Location targetEye = target.getEyeLocation();

        // Soul particle line from core to mob + sonic boom at mob
        drawParticleLine(shootFrom, targetEye, Particle.SOUL, 0.5);
        target.getWorld().spawnParticle(Particle.SONIC_BOOM, targetEye, 1, 0, 0, 0, 0);

        target.damage(25.0);

        coreLoc.getWorld().playSound(coreLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
    }

    // ─────────────────── Helpers ───────────────────

    /**
     * Finds the nearest Monster entity within {@code radius} blocks of {@code origin}.
     */
    private Monster findNearestMonster(Location origin, double radius) {
        Collection<Monster> nearby = origin.getWorld().getNearbyEntitiesByType(
                Monster.class, origin, radius);
        Optional<Monster> nearest = nearby.stream()
                .min(Comparator.comparingDouble(m -> m.getLocation().distanceSquared(origin)));
        return nearest.orElse(null);
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
