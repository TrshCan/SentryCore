package com.test.sentry;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

        String rawTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());


        // ── Main Control GUI ──
        if (rawTitle.startsWith(SentryGui.MAIN_GUI_TITLE_PREFIX)) {
            event.setCancelled(true); // never let players take items from our GUI

            Location coreLoc = SentryGui.decodeLocationFromTitle(rawTitle);
            if (coreLoc == null) return;
            SentryData data = sentryManager.getData(coreLoc);
            if (data == null) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            int slot = event.getRawSlot();

            // Slot 10 — Toggle
            if (slot == 10) {
                data.setActive(!data.isActive());
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
            // Slot 31 — Pick Up Sentry
            else if (slot == 31) {
                // Remove the sentry cleanly from manager (this removes the crystal/conduit)
                sentryManager.removeSentry(coreLoc);
                player.closeInventory();
                
                // Drop the Sentry Core item on the ground
                coreLoc.getWorld().dropItemNaturally(coreLoc, SentryCoreItem.buildItem());
                player.sendMessage(
                    net.kyori.adventure.text.Component.text("Sentry picked up.")
                        .color(net.kyori.adventure.text.format.TextColor.fromHexString("#AAAAAA"))
                );
            }
        }

        // ── Mode Selector GUI ──
        else if (rawTitle.startsWith(SentryGui.MODE_GUI_TITLE_PREFIX)) {
            event.setCancelled(true);

            Location coreLoc = SentryGui.decodeLocationFromTitle(rawTitle);
            if (coreLoc == null) return;
            SentryData data = sentryManager.getData(coreLoc);
            if (data == null) return;

            int slot = event.getRawSlot();
            SentryMode newMode = switch (slot) {
                case 2 -> SentryMode.AMETHYST;
                case 4 -> SentryMode.PRISMARINE;
                case 6 -> SentryMode.ECHO;
                default -> null;
            };

            if (newMode == null) return;

            data.setMode(newMode);
            // Reopen mode selector so the green/red indicators refresh
            SentryGui.openModeSelector(player, coreLoc, data);
        }
    }
}
