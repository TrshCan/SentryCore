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
 * Main GUI (9×4):
 *   ┌─────────────────────────────────────────┐
 *   │ G  G  G  G  G  G  G  G  G              row 0
 *   │ G  ⬜ G  ◉  G  G  ⬇  G  G              row 1  (⬜=toggle, ◉=mode, ⬇=fuel)
 *   │ G  G  G  G  G  G  G  G  G              row 2
 *   │ G  G  G  G  ❌ G  G  G  G              row 3  (❌=pick up)
 *   └─────────────────────────────────────────┘
 *   G = Gray Glass Pane (filler)
 *   Slot 10 = ON/OFF toggle (Lime or Gray Dye)
 *   Slot 13 = Mode selector (Comparator)
 *   Slot 16 = Access Fuel (Hopper)
 *   Slot 31 = Pick Up (Barrier)
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
    public static final String UPGRADE_GUI_TITLE_PREFIX = "§5Sentry Upgrade";

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
        SentryGuiHolder holder = new SentryGuiHolder(coreLoc, SentryGuiHolder.GuiType.MAIN);
        String ownerName = data.getOwnerName() != null ? data.getOwnerName() : "Unknown";
        Inventory inv = Bukkit.createInventory(holder, 36, Component.text("§5" + ownerName + "'s Sentry"));
        holder.setInventory(inv);

        // Fill with gray glass panes
        ItemStack filler = buildFiller();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, filler);
        }

        // Slot 10 — Toggle (ON = Lime Dye, OFF = Gray Dye)
        inv.setItem(10, buildToggleItem(data.isActive()));

        // Slot 13 — Mode selector (Comparator)
        inv.setItem(13, buildModeSelectorItem(data.getMode()));

        // Slot 16 — Access Fuel (Hopper)
        inv.setItem(16, buildAccessFuelItem());

        // Slot 22 — Upgrade Menu (Nether Star)
        inv.setItem(22, buildUpgradeMenuItem(data));

        // Slot 24 — Target List (Zombie Head)
        inv.setItem(24, buildTargetListItem());

        // Slot 31 — Pick Up Sentry (Barrier)
        inv.setItem(31, buildPickUpItem());

        player.openInventory(inv);
    }

    // ─────────────────────── Mode Sub-GUI ───────────────────────

    /**
     * Opens the mode-selection sub-GUI for a player.
     */
    public static void openModeSelector(Player player, Location coreLoc, SentryData data) {
        SentryGuiHolder holder = new SentryGuiHolder(coreLoc, SentryGuiHolder.GuiType.MODE);
        String ownerName = data.getOwnerName() != null ? data.getOwnerName() : "Unknown";
        Inventory inv = Bukkit.createInventory(holder, 9, Component.text("§5" + ownerName + "'s Sentry Mode"));
        holder.setInventory(inv);

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

        // Return to main menu
        inv.setItem(8, buildReturnMenuItem());

        player.openInventory(inv);
    }

    // ─────────────────────── Upgrade Sub-GUI ───────────────────────

    public static void openUpgradeMenu(Player player, Location coreLoc, SentryData data, SentryConfig config) {
        SentryGuiHolder holder = new SentryGuiHolder(coreLoc, SentryGuiHolder.GuiType.UPGRADE);
        String ownerName = data.getOwnerName() != null ? data.getOwnerName() : "Unknown";
        Inventory inv = Bukkit.createInventory(holder, 18, Component.text("§5" + ownerName + "'s Upgrades"));
        holder.setInventory(inv);

        ItemStack filler = buildFiller();
        for (int i = 0; i < 18; i++) inv.setItem(i, filler);

        // Slot 0 — Range (Bow)
        inv.setItem(0, buildUpgradeStatItem("Range", Material.BOW, data.getRangeTier(),
            config.getRangeBonus(data.getRangeTier()), config.getRangeBonus(data.getRangeTier() + 1), "Blocks", data, config));

        // Slot 2 — Recharge Speed (Clock)
        inv.setItem(2, buildUpgradeStatItem("Recharge Speed", Material.CLOCK, data.getRechargeTier(),
            config.getRechargeReduction(data.getRechargeTier()), config.getRechargeReduction(data.getRechargeTier() + 1), "% Faster", data, config));

        // Slot 4 — Damage (Diamond Sword)
        inv.setItem(4, buildUpgradeStatItem("Damage", Material.DIAMOND_SWORD, data.getDamageTier(),
            config.getDamageBonus(data.getDamageTier()), config.getDamageBonus(data.getDamageTier() + 1), "% Bonus", data, config));

        // Slot 6 — Passive Buff (Beacon)
        inv.setItem(6, buildUpgradeStatItem("Passive Buff", Material.BEACON, data.getBuffTier(),
            config.getBuffAmplifier(data.getBuffTier()), config.getBuffAmplifier(data.getBuffTier() + 1), "Amplifier", data, config));

        // Slot 8 — Targets (Crossbow)
        inv.setItem(8, buildUpgradeStatItem("Multi-Target", Material.CROSSBOW, data.getTargetsTier(),
            config.getTargetsBonus(data.getTargetsTier()), config.getTargetsBonus(data.getTargetsTier() + 1), "Targets", data, config));

        // Return to main menu
        inv.setItem(17, buildReturnMenuItem());

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

    private static ItemStack buildAccessFuelItem() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
            Component.text("⬇ Access Fuel Storage")
                .color(GOLD_COLOR)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text("Click to open the Barrel's inventory"),
            Component.text("to add or remove fuel.")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildUpgradeMenuItem(SentryData data) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
            Component.text("⭐ Upgrades")
                .color(GOLD_COLOR)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text("Click to View/Purchase Upgrades").color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false),
            Component.text("Total Tier: " + data.getTotalTier() + "/40").color(ACTIVE_COLOR).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildUpgradeStatItem(String statName, Material mat, int currentTier, int currentVal, int nextVal, String unit, SentryData data, SentryConfig config) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        boolean canUpgrade = data.getTotalTier() < config.getMaxTier();

        meta.displayName(
            Component.text(statName + " Upgrade").color(GOLD_COLOR)
                .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true)
        );

        List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text("Tier: " + currentTier).color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Current: +" + currentVal + " " + unit).color(ACTIVE_COLOR).decoration(TextDecoration.ITALIC, false));
        
        if (canUpgrade) {
            int cost = config.getUpgradeCost(currentTier + 1);
            lore.add(Component.text("Next Tier: +" + nextVal + " " + unit).color(GOLD_COLOR).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Cost: " + cost + " Condensed Obsidian").color(TextColor.fromHexString("#D455FF")).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("► Click to Upgrade").color(ACTIVE_COLOR).decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.empty());
            lore.add(Component.text("Max Total Tier Reached (40)").color(INACTIVE_COLOR).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildPickUpItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
            Component.text("❌ Pick Up Sentry")
                .color(INACTIVE_COLOR)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text("Safely deactivates and removes the sentry,"),
            Component.text("dropping the Sentry Core on the ground.")
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

    private static ItemStack buildTargetListItem() {
        ItemStack item = new ItemStack(Material.ZOMBIE_HEAD, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Target List").color(ACTIVE_COLOR).decoration(TextDecoration.ITALIC, false));
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("Click to configure which").color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("mobs this sentry attacks.").color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack buildReturnMenuItem() {
        ItemStack item = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("← Return").color(GOLD_COLOR).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("Click to return to Main Menu").color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Opens the Target List configuration GUI for a player.
     */
    public static void openTargetMenu(Player player, Location coreLoc, SentryData data) {
        SentryGuiHolder holder = new SentryGuiHolder(coreLoc, SentryGuiHolder.GuiType.TARGET_LIST);
        String ownerName = data.getOwnerName() != null ? data.getOwnerName() : "Unknown";
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("§5" + ownerName + "'s Target List"));
        holder.setInventory(inv);

        java.util.Set<org.bukkit.entity.EntityType> allowed = data.getAllowedTargets();
        int slot = 0;

        for (java.util.Map.Entry<org.bukkit.entity.EntityType, Material> entry : SentryTargets.HOSTILE_MOBS.entrySet()) {
            if (slot >= 54) break; // sanity check
            org.bukkit.entity.EntityType type = entry.getKey();
            boolean isTargeted = allowed.contains(type);

            ItemStack item = new ItemStack(entry.getValue(), 1);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Name beautifully
                String prettyName = type.name().replace("_", " ").toLowerCase();
                String[] words = prettyName.split(" ");
                StringBuilder formattedName = new StringBuilder();
                for (String w : words) {
                    formattedName.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                }
                
                meta.displayName(Component.text(formattedName.toString().trim()).color(TextColor.fromHexString("#FFAA00")).decoration(TextDecoration.ITALIC, false));

                java.util.List<Component> lore = new java.util.ArrayList<>();
                lore.add(Component.empty());
                if (isTargeted) {
                    lore.add(Component.text("► TARGETED").color(ACTIVE_COLOR).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
                    lore.add(Component.text("Click to Ignore").color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false));
                } else {
                    lore.add(Component.text("► IGNORED").color(INACTIVE_COLOR).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
                    lore.add(Component.text("Click to Target").color(GRAY_COLOR).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        inv.setItem(53, buildReturnMenuItem());

        player.openInventory(inv);
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
