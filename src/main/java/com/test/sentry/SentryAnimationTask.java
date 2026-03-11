package com.test.sentry;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Handles the animated "locking-on" Guardian beam effect for Prismarine mode.
 * Runs every tick for 20 ticks, drawing the beam toward the mob's *current* position,
 * then deals damage on the final tick.
 */
public class SentryAnimationTask extends BukkitRunnable {

    private final Location origin;
    private final Monster target;
    private int ticksLeft = 20;

    public SentryAnimationTask(Location origin, Monster target) {
        this.origin = origin;
        this.target = target;
    }

    @Override
    public void run() {
        if (target.isDead() || !target.isValid()) {
            this.cancel();
            return;
        }

        Location targetEye = target.getEyeLocation();
        drawParticleLine(origin, targetEye, Particle.BUBBLE, 0.4);

        ticksLeft--;

        // On the final tick, deal damage and apply effects
        if (ticksLeft <= 0) {
            target.damage(10.0);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false));
            origin.getWorld().playSound(origin, Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.0f);
            this.cancel();
        } else {
            // Play a ticking sound while charging up
            origin.getWorld().playSound(origin, Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 2.0f);
        }
    }

    private void drawParticleLine(Location start, Location end, Particle particle, double stepSize) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        if (distance == 0) return;
        
        direction.normalize();

        for (double d = 0; d < distance; d += stepSize) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Starts the Prismarine laser animation task.
     */
    public static void startPrismarineLaser(JavaPlugin plugin, Location coreLoc, Monster target) {
        Location shootFrom = coreLoc.clone().add(0.5, 0.5, 0.5);
        new SentryAnimationTask(shootFrom, target).runTaskTimer(plugin, 0L, 1L);
    }
}
