package com.test.sentry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class StructureChecker {

    /**
     * Validates that a placed Conduit block forms a valid Sentry structure.
     *
     * Requirements:
     *  1. The block directly below the core must be a CHEST.
     *  2. The 8 blocks surrounding the chest (same Y level) must all be OBSIDIAN.
     *
     *   O O O
     *   O C O   ← chest level (C = chest, O = obsidian)
     *   O O O
     *       ↑ core is on top of C
     */
    public static boolean isValidStructure(Block coreBlock) {
        Block chestBlock = coreBlock.getRelative(BlockFace.DOWN);
        if (chestBlock.getType() != Material.CHEST) return false;

        return isObsidianFrame(chestBlock);
    }

    /**
     * Checks if the 8 surrounding blocks of the given block are all OBSIDIAN.
     */
    public static boolean isObsidianFrame(Block center) {
        int[][] offsets = {
            {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
            {-1, 0,  0},             {1, 0,  0},
            {-1, 0,  1}, {0, 0,  1}, {1, 0,  1}
        };
        for (int[] offset : offsets) {
            Block relative = center.getRelative(offset[0], offset[1], offset[2]);
            if (relative.getType() != Material.OBSIDIAN) return false;
        }
        return true;
    }

    /**
     * Returns the chest block beneath the given core block, or null if absent.
     */
    public static Block getChestBlock(Block coreBlock) {
        Block below = coreBlock.getRelative(BlockFace.DOWN);
        return below.getType() == Material.CHEST ? below : null;
    }

    /**
     * Checks whether the given location is one of the 8 obsidian frame blocks
     * relative to a chest at chestLoc.
     */
    public static boolean isFrameBlock(Location frameLoc, Location chestLoc) {
        int dx = frameLoc.getBlockX() - chestLoc.getBlockX();
        int dy = frameLoc.getBlockY() - chestLoc.getBlockY();
        int dz = frameLoc.getBlockZ() - chestLoc.getBlockZ();
        return dy == 0
                && Math.abs(dx) <= 1
                && Math.abs(dz) <= 1
                && !(dx == 0 && dz == 0); // not the center
    }

    private StructureChecker() {}
}
