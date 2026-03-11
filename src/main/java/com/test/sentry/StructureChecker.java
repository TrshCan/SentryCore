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
     *  1. The block directly below the core must be OBSIDIAN.
     *  2. The block below that must be a BARREL.
     *  3. The 8 blocks surrounding the BARREL (same Y level) must all be OBSIDIAN.
     *
     *   O O O
     *   O B O   ← level Y-2 (B = barrel, O = obsidian)
     *   O O O
     *     O     ← level Y-1 (obsidian)
     *     C     ← level Y (conduit/core)
     */
    public static boolean isValidStructure(Block coreBlock) {
        Block below = coreBlock.getRelative(BlockFace.DOWN);
        if (below.getType() != Material.OBSIDIAN) return false;

        Block barrelBlock = below.getRelative(BlockFace.DOWN);
        if (barrelBlock.getType() != Material.BARREL) return false;

        return isObsidianFrame(barrelBlock);
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
     * Returns the barrel block beneath the given core block, or null if absent.
     * The barrel should be 2 blocks below the core.
     */
    public static Block getFuelContainer(Block coreBlock) {
        Block belowTwo = coreBlock.getRelative(0, -2, 0);
        return belowTwo.getType() == Material.BARREL ? belowTwo : null;
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
