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
import org.bukkit.inventory.ItemStack;

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
            sentryManager.addSentry(placed.getLocation());
            event.getPlayer().sendMessage(
                Component.text("✔ Sentry Core activated!")
                    .color(TextColor.fromHexString("#AA00FF"))
            );
        } else {
            event.getPlayer().sendMessage(
                Component.text("✘ Invalid structure! Place the Sentry Core on a Chest surrounded by Obsidian.")
                    .color(TextColor.fromHexString("#FF4444"))
            );
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();

        // 1. If the core itself is broken — remove it directly
        if (broken.getType() == Material.CONDUIT && sentryManager.isSentry(broken.getLocation())) {
            sentryManager.removeSentry(broken.getLocation());
            return;
        }

        // 2. If the chest beneath a sentry is broken, invalidate the sentry above
        if (broken.getType() == Material.CHEST) {
            Block above = broken.getRelative(BlockFace.UP);
            if (above.getType() == Material.CONDUIT && sentryManager.isSentry(above.getLocation())) {
                sentryManager.removeSentry(above.getLocation());
                return;
            }
        }

        // 3. If an Obsidian frame block is broken — check surrounding sentries
        if (broken.getType() == Material.OBSIDIAN) {
            // The chest, if part of a sentry, would be at broken.relative(0,0,0) neighbors.
            // We check all 8 possible "center chest" positions around this obsidian block.
            Set<Block> candidateChests = getNeighboringChests(broken);
            for (Block chest : candidateChests) {
                Block potentialCore = chest.getRelative(BlockFace.UP);
                if (potentialCore.getType() == Material.CONDUIT
                        && sentryManager.isSentry(potentialCore.getLocation())) {
                    // Verify the frame is still intact (it won't be, since we're breaking one)
                    if (!StructureChecker.isObsidianFrame(chest)) {
                        sentryManager.removeSentry(potentialCore.getLocation());
                    }
                }
            }
        }
    }

    /**
     * Returns all CHEST blocks that are adjacent (within 1 block on the same Y) to the given block.
     */
    private Set<Block> getNeighboringChests(Block block) {
        Set<Block> result = new HashSet<>();
        int[][] offsets = {
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0,  0},             {1, 0,  0},
            {-1, 0,  1}, {0, 0,  1}, {1, 0,  1}
        };
        for (int[] off : offsets) {
            Block candidate = block.getRelative(off[0], off[1], off[2]);
            if (candidate.getType() == Material.CHEST) {
                result.add(candidate);
            }
        }
        return result;
    }
}
