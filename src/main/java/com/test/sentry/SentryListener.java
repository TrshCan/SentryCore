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
            sentryManager.addSentry(placed.getLocation(), ownerName);
            event.getPlayer().sendMessage(
                Component.text("✔ Sentry Core placed! Shift+Right-Click to activate.")
                    .color(TextColor.fromHexString("#AA00FF"))
            );
        } else {
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
            event.setCurrentItem(SentryCoreItem.buildItem(ownerName));
        }
    }

    private void dropCoreAndRemove(Location coreLoc) {
        SentryData data = sentryManager.getData(coreLoc);
        String ownerName = data != null ? data.getOwnerName() : null;
        sentryManager.removeSentry(coreLoc);
        coreLoc.getWorld().dropItemNaturally(coreLoc, SentryCoreItem.buildItem(ownerName));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();

        // 1. If the core itself is broken — remove it directly
        if (broken.getType() == Material.CONDUIT && sentryManager.isSentry(broken.getLocation())) {
            event.setDropItems(false); // prevent normal conduit drop
            dropCoreAndRemove(broken.getLocation());
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
