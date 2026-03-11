package com.test.sentry;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class SentryGuiListener implements Listener {

    private final SentryManager sentryManager;

    public SentryGuiListener(SentryManager sentryManager) {
        this.sentryManager = sentryManager;
    }

    // ─────────────── Open Main GUI on Shift + Right-Click ───────────────

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only care about right-click on a block, main hand, while sneaking
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CONDUIT) return;

        Location coreLoc = block.getLocation();
        if (!sentryManager.isSentry(coreLoc)) return;

        // It's a registered sentry — open the control GUI and cancel block interaction
        event.setCancelled(true);
        SentryData data = sentryManager.getData(coreLoc);
        SentryGui.openMain(player, coreLoc, data);
    }

    // ─────────────── Open GUI on Shift + Right-Click End Crystal ───────────────

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        Entity entity = event.getRightClicked();
        if (!(entity instanceof EnderCrystal crystal)) return;

        // Check if this crystal belongs to any active sentry
        for (Location coreLoc : sentryManager.getActiveSentries()) {
            SentryData data = sentryManager.getData(coreLoc);
            if (data != null && crystal.getUniqueId().equals(data.getCrystalUuid())) {
                event.setCancelled(true);
                SentryGui.openMain(player, coreLoc, data);
                return;
            }
        }
    }

    // ─────────────── Protect End Crystal from Damage ───────────────

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal crystal)) return;

        for (Location coreLoc : sentryManager.getActiveSentries()) {
            SentryData data = sentryManager.getData(coreLoc);
            if (data != null && crystal.getUniqueId().equals(data.getCrystalUuid())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // ─────────────── Handle Clicks Inside GUIs ───────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        org.bukkit.inventory.InventoryHolder invHolder = event.getInventory().getHolder();
        if (!(invHolder instanceof SentryGuiHolder holder)) return;

        event.setCancelled(true); // never let players take items from our GUI

        Location coreLoc = holder.getSentryLocation();
        SentryData data = sentryManager.getData(coreLoc);
        if (data == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int slot = event.getRawSlot();

        // ── Main Control GUI ──
        if (holder.getType() == SentryGuiHolder.GuiType.MAIN) {

            // Slot 10 — Toggle
            if (slot == 10) {
                sentryManager.setSentryActiveState(coreLoc, data, !data.isActive());
                // Refresh the GUI
                SentryGui.openMain(player, coreLoc, data);
            }
            // Slot 13 — Open mode selector
            else if (slot == 13) {
                SentryGui.openModeSelector(player, coreLoc, data);
            }
            // Slot 16 — Access Fuel Storage
            else if (slot == 16) {
                Block barrelBlock = coreLoc.clone().subtract(0, 2, 0).getBlock();
                if (barrelBlock.getState() instanceof org.bukkit.block.Container container) {
                    player.openInventory(container.getInventory());
                } else {
                    player.sendMessage(
                        net.kyori.adventure.text.Component.text("✘ Could not find the Barrel.")
                            .color(net.kyori.adventure.text.format.TextColor.fromHexString("#FF4444"))
                    );
                    player.closeInventory();
                }
            }
            // Slot 22 — Upgrade Menu
            else if (slot == 22) {
                SentryGui.openUpgradeMenu(player, coreLoc, data, sentryManager.getConfig());
            }
            // Slot 24 — Target List
            else if (slot == 24) {
                SentryGui.openTargetMenu(player, coreLoc, data);
            }
            // Slot 31 — Pick Up Sentry
            else if (slot == 31) {
                // Remove the sentry cleanly from manager (this removes the crystal/conduit)
                int r = data.getRangeTier();
                int rc = data.getRechargeTier();
                int d = data.getDamageTier();
                int b = data.getBuffTier();
                int t = data.getTargetsTier();
                java.util.Set<org.bukkit.entity.EntityType> targets = data.getAllowedTargets();
                sentryManager.removeSentry(coreLoc);
                player.closeInventory();
                
                // Drop the Sentry Core item on the ground
                coreLoc.getWorld().dropItemNaturally(coreLoc, SentryCoreItem.buildItem(data.getOwnerName(), r, rc, d, b, t, targets));
                player.sendMessage(
                    net.kyori.adventure.text.Component.text("Sentry picked up.")
                        .color(net.kyori.adventure.text.format.TextColor.fromHexString("#AAAAAA"))
                );
            }
        }

        // ── Mode Selector GUI ──
        else if (holder.getType() == SentryGuiHolder.GuiType.MODE) {


            SentryMode newMode = switch (slot) {
                case 2 -> SentryMode.AMETHYST;
                case 4 -> SentryMode.PRISMARINE;
                case 6 -> SentryMode.ECHO;
                default -> null;
            };

            if (newMode == null) return;

            data.setMode(newMode);
            sentryManager.saveToPDC(coreLoc, data);
            // Reopen mode selector so the green/red indicators refresh
            SentryGui.openModeSelector(player, coreLoc, data);
        }

        // ── Upgrade GUI ──
        else if (holder.getType() == SentryGuiHolder.GuiType.UPGRADE) {
            SentryConfig config = sentryManager.getConfig();
            if (data.getTotalTier() >= config.getMaxTier()) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Max tier reached!").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FF4444")));
                return;
            }

            boolean upgraded = false;
            if (slot == 0) {
                data.setRangeTier(data.getRangeTier() + 1);
                upgraded = true;
            } else if (slot == 2) {
                data.setRechargeTier(data.getRechargeTier() + 1);
                upgraded = true;
            } else if (slot == 4) {
                data.setDamageTier(data.getDamageTier() + 1);
                upgraded = true;
            } else if (slot == 6) {
                data.setBuffTier(data.getBuffTier() + 1);
                upgraded = true;
            } else if (slot == 8) {
                data.setTargetsTier(data.getTargetsTier() + 1);
                upgraded = true;
            }

            if (upgraded) {
                // Save immediately to Block PDC
                sentryManager.saveToPDC(coreLoc, data);
                // Refresh the Upgrade GUI
                SentryGui.openUpgradeMenu(player, coreLoc, data, config);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            }
        }

        // ── Target List GUI ──
        else if (holder.getType() == SentryGuiHolder.GuiType.TARGET_LIST) {
            org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == org.bukkit.Material.AIR) return;

            org.bukkit.Material mat = clickedItem.getType();
            // Find EntityType based on material egg clicked
            org.bukkit.entity.EntityType hitType = null;
            for (java.util.Map.Entry<org.bukkit.entity.EntityType, org.bukkit.Material> entry : SentryTargets.HOSTILE_MOBS.entrySet()) {
                if (entry.getValue() == mat) {
                    hitType = entry.getKey();
                    break;
                }
            }

            if (hitType != null) {
                java.util.Set<org.bukkit.entity.EntityType> allowed = data.getAllowedTargets();
                if (allowed.contains(hitType)) {
                    allowed.remove(hitType);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
                } else {
                    allowed.add(hitType);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                }
                sentryManager.saveToPDC(coreLoc, data);
                SentryGui.openTargetMenu(player, coreLoc, data);
            }
        }
    }
}
