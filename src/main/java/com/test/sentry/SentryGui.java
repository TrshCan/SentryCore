package com.test.sentry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Builds and opens the Sentry control GUI panels.
 *
 * Main GUI (9×3):
 *   ┌─────────────────────────────────────────┐
 *   │ G  G  G  G  G  G  G  G  G              row 0
 *   │ G  ⬜ G  ◉  G  G  G  G  G              row 1  (⬜=toggle, ◉=mode)
 *   │ G  G  G  G  G  G  G  G  G              row 2
 *   └─────────────────────────────────────────┘
 *   G = Gray Glass Pane (filler)
 *   Slot 10 = ON/OFF toggle (Lime or Gray Dye)
 *   Slot 13 = Mode selector (Comparator)
 *
 * Mode GUI (9×1):
 *   ┌──────────────────────────────────────────┐
 *   │ G  G  ◈  G  ◈  G  ◈  G  G               │
 *   └──────────────────────────────────────────┘
 *   Slot 2 = Amethyst Shard
 *   Slot 4 = Prismarine Shard
 *   Slot 6 = Echo Shard
 */
public final class SentryGui {

    // Titles encoded with the core location so the click handler can look up the sentry
    public static final String MAIN_GUI_TITLE_PREFIX = "§5Sentry Control";
    public static final String MODE_GUI_TITLE_PREFIX = "§5Sentry Mode";

    private static final TextColor ACTIVE_COLOR   = TextColor.fromHexString("#55FF55"); // green
    private static final TextColor INACTIVE_COLOR = TextColor.fromHexString("#FF5555"); // red
    private static final TextColor GOLD_COLOR     = TextColor.fromHexString("#FFAA00");
    private static final TextColor GRAY_COLOR     = TextColor.fromHexString("#AAAAAA");


    // ─────────────────────── Main GUI ───────────────────────

    /**
     * Opens the main Sentry control GUI for a player.
     *
     * @param player  the player opening the GUI
     * @param coreLoc the sentry's conduit block location
     * @param data    the sentry's current state
     */
    public static void openMain(Player player, Location coreLoc, SentryData data) {
        // Title encodes the sentry location so the listener can identify which sentry was clicked
        String title = buildMainTitle(coreLoc);
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(title));

        // Fill with gray glass panes
        ItemStack filler = buildFiller();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Slot 10 — Toggle (ON = Lime Dye, OFF = Gray Dye)
        inv.setItem(10, buildToggleItem(data.isActive()));

        // Slot 13 — Mode selector (Comparator)
        inv.setItem(13, buildModeSelectorItem(data.getMode()));

        player.openInventory(inv);
    }

    // ─────────────────────── Mode Sub-GUI ───────────────────────

    /**
     * Opens the mode-selection sub-GUI for a player.
     */
    public static void openModeSelector(Player player, Location coreLoc, SentryData data) {
        String title = buildModeTitle(coreLoc);
        Inventory inv = Bukkit.createInventory(null, 9, Component.text(title));

        // Fill with filler
        ItemStack filler = buildFiller();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, filler);
        }

        // Slot 2 — Amethyst Mode
        inv.setItem(2, buildModeItem(SentryMode.AMETHYST, data.getMode()));
        // Slot 4 — Prismarine Mode
        inv.setItem(4, buildModeItem(SentryMode.PRISMARINE, data.getMode()));
        // Slot 6 — Echo Mode
        inv.setItem(6, buildModeItem(SentryMode.ECHO, data.getMode()));

        player.openInventory(inv);
    }

    // ─────────────────────── Item Builders ───────────────────────

    private static ItemStack buildToggleItem(boolean active) {
        ItemStack item = new ItemStack(active ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
            Component.text(active ? "⬛ Sentry Active" : "⬛ Sentry Inactive")
                .color(active ? ACTIVE_COLOR : INACTIVE_COLOR)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text(active ? "Click to Deactivate" : "Click to Activate")
                .color(GRAY_COLOR)
                .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildModeSelectorItem(SentryMode currentMode) {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
            Component.text("⚙ Change Mode")
                .color(GOLD_COLOR)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text("Current: " + currentMode.getDisplayName())
                .color(ACTIVE_COLOR)
                .decoration(TextDecoration.ITALIC, false),
            Component.text("Click to select a mode")
                .color(GRAY_COLOR)
                .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildModeItem(SentryMode mode, SentryMode current) {
        boolean isActive = mode == current;
        ItemStack item = new ItemStack(mode.getFuelMaterial());
        ItemMeta meta = item.getItemMeta();

        TextColor nameColor = isActive ? ACTIVE_COLOR : INACTIVE_COLOR;
        String statusLabel = isActive ? "✔ ACTIVE" : "✘ INACTIVE";

        meta.displayName(
            Component.text(mode.getDisplayName())
                .color(nameColor)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text(statusLabel)
                .color(nameColor)
                .decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            Component.text("Fuel: " + formatMaterial(mode.getFuelMaterial()))
                .color(GRAY_COLOR)
                .decoration(TextDecoration.ITALIC, false),
            Component.text("Fires every " + mode.getTickInterval() + " ticks")
                .color(GRAY_COLOR)
                .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildFiller() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    // ─────────────────────── Title Encoding ───────────────────────

    /**
     * Encodes the sentry block location into the GUI title so the listener
     * can safely look up the correct SentryData without using fragile metadata.
     * Format: "§5Sentry Control|world,x,y,z"
     */
    public static String buildMainTitle(Location loc) {
        return MAIN_GUI_TITLE_PREFIX + "|" + encodeLocation(loc);
    }

    public static String buildModeTitle(Location loc) {
        return MODE_GUI_TITLE_PREFIX + "|" + encodeLocation(loc);
    }

    public static Location decodeLocationFromTitle(String title) {
        try {
            int sep = title.indexOf('|');
            if (sep == -1) return null;
            String[] parts = title.substring(sep + 1).split(",");
            if (parts.length != 4) return null;
            return new Location(
                Bukkit.getWorld(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private static String formatMaterial(Material mat) {
        String name = mat.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }

    private SentryGui() {}
}
