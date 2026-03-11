package com.test.sentry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class SentryListener implements Listener {

    private final SentryManager sentryManager;

    public SentryListener(SentryManager sentryManager) {
        this.sentryManager = sentryManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlock();
        if (placed.getType() != Material.CONDUIT) return;

        ItemStack itemInHand = event.getItemInHand();
        if (!SentryCoreItem.isSentryCore(itemInHand)) return;

        if (StructureChecker.isValidStructure(placed)) {
            String ownerName = SentryCoreItem.getOwner(itemInHand);
            int rangeTier = SentryCoreItem.getRangeTier(itemInHand);
            int rechargeTier = SentryCoreItem.getRechargeTier(itemInHand);
            int damageTier = SentryCoreItem.getDamageTier(itemInHand);
            int buffTier = SentryCoreItem.getBuffTier(itemInHand);
            int targetsTier = SentryCoreItem.getTargetsTier(itemInHand);
            java.util.Set<org.bukkit.entity.EntityType> allowedTargets = SentryCoreItem.getAllowedTargets(itemInHand);
            
            // Try to load existing data from obsidian block first (in case of server crash)
            SentryData existingData = sentryManager.loadFromPDC(placed.getLocation());
            if (existingData != null) {
                 sentryManager.addSentry(placed.getLocation(), existingData.getOwnerName(),
                    existingData.getRangeTier(), existingData.getRechargeTier(),
                    existingData.getDamageTier(), existingData.getBuffTier(), existingData.getTargetsTier(), existingData.getAllowedTargets());
            } else {
                 sentryManager.addSentry(placed.getLocation(), ownerName, rangeTier, rechargeTier, damageTier, buffTier, targetsTier, allowedTargets);
            }
            
            // Force disable waterlogging
            if (placed.getBlockData() instanceof org.bukkit.block.data.Waterlogged wl) {
                wl.setWaterlogged(false);
                placed.setBlockData(wl);
            }

            event.getPlayer().sendMessage(
                Component.text("✔ Sentry Core placed! Shift+Right-Click to activate.")
                    .color(TextColor.fromHexString("#AA00FF"))
            );
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                Component.text("✘ Invalid structure! Place on Obsidian above a Barrel surrounded by Obsidian.")
                    .color(TextColor.fromHexString("#FF4444"))
            );
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && SentryCoreItem.isSentryCore(currentItem)) {
            String ownerName = event.getWhoClicked().getName();
            // Freshly crafted cores are always Tier 0, and default targets
            event.setCurrentItem(SentryCoreItem.buildItem(ownerName, 0, 0, 0, 0, 0, SentryTargets.getDefaultTargets()));
        }
    }

    private void dropCoreAndRemove(Location coreLoc) {
        SentryData data = sentryManager.getData(coreLoc);
        String ownerName = data != null ? data.getOwnerName() : null;
        int r = data != null ? data.getRangeTier() : 0;
        int rc = data != null ? data.getRechargeTier() : 0;
        int d = data != null ? data.getDamageTier() : 0;
        int b = data != null ? data.getBuffTier() : 0;
        int t = data != null ? data.getTargetsTier() : 0;
        java.util.Set<org.bukkit.entity.EntityType> targets = data != null ? data.getAllowedTargets() : SentryTargets.getDefaultTargets();
        
        sentryManager.removeSentry(coreLoc);
        coreLoc.getWorld().dropItemNaturally(coreLoc, SentryCoreItem.buildItem(ownerName, r, rc, d, b, t, targets));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();

        // 1. If the core itself is broken directly — prevent it
        if (broken.getType() == Material.CONDUIT && sentryManager.isSentry(broken.getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                Component.text("You cannot break the core directly. Pick it up from the GUI or break the structure.")
                    .color(TextColor.fromHexString("#FF4444"))
            );
            return;
        }

        // 2. If the barrel 2 blocks beneath a sentry is broken
        if (broken.getType() == Material.BARREL) {
            Block coreAbove = broken.getRelative(0, 2, 0);
            if (sentryManager.isSentry(coreAbove.getLocation())) {
                dropCoreAndRemove(coreAbove.getLocation());
                return;
            }
        }

        // 3. If Obsidian is broken (could be the block under the core, or the frame around the barrel)
        if (broken.getType() == Material.OBSIDIAN) {
            // Case A: Is it the obsidian block directly beneath the core?
            Block coreAbove = broken.getRelative(BlockFace.UP);
            if (sentryManager.isSentry(coreAbove.getLocation())) {
                dropCoreAndRemove(coreAbove.getLocation());
                return;
            }

            // Case B: Is it one of the 8 frame blocks surrounding a barrel?
            // The barrel would be adjacent to this broken block at the same Y level.
            Set<Block> candidateBarrels = getNeighboringContainers(broken, Material.BARREL);
            for (Block barrel : candidateBarrels) {
                Block potentialCore = barrel.getRelative(0, 2, 0);
                if (sentryManager.isSentry(potentialCore.getLocation())) {
                    // Frame is now broken
                    if (!StructureChecker.isObsidianFrame(barrel)) {
                        dropCoreAndRemove(potentialCore.getLocation());
                    }
                }
            }
        }
    }

    /**
     * Returns all blocks of the specified material that are adjacent (within 1 block on the same Y) to the given block.
     */
    private Set<Block> getNeighboringContainers(Block block, Material containerType) {
        Set<Block> result = new HashSet<>();
        int[][] offsets = {
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0,  0},             {1, 0,  0},
            {-1, 0,  1}, {0, 0,  1}, {1, 0,  1}
        };
        for (int[] off : offsets) {
            Block candidate = block.getRelative(off[0], off[1], off[2]);
            if (candidate.getType() == containerType) {
                result.add(candidate);
            }
        }
        return result;
    }
}
