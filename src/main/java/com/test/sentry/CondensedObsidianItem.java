package com.test.sentry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CondensedObsidianItem {

    private static NamespacedKey CONDENSED_OBSIDIAN_KEY;

    public static void init(JavaPlugin plugin) {
        CONDENSED_OBSIDIAN_KEY = new NamespacedKey(plugin, "condensed_obsidian");
    }

    /**
     * Builds the Condensed Obsidian item: Obsidian block with a hidden enchantment glimmer
     * and a PDC tag identifying it.
     */
    public static ItemStack buildItem() {
        ItemStack item = new ItemStack(Material.OBSIDIAN, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Condensed Obsidian")
                    .color(TextColor.fromHexString("#FFAA00")) // Gold color
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));
            
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("A dense, magical block of obsidian,")
                    .color(TextColor.fromHexString("#AAAAAA")) // Gray color
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("required to power and upgrade Sentry Cores.")
                    .color(TextColor.fromHexString("#AAAAAA"))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            if (CONDENSED_OBSIDIAN_KEY != null) {
                meta.getPersistentDataContainer().set(CONDENSED_OBSIDIAN_KEY, PersistentDataType.BYTE, (byte) 1);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Returns true if the given ItemStack is Condensed Obsidian (has the PDC tag).
     */
    public static boolean isCondensedObsidian(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        if (CONDENSED_OBSIDIAN_KEY == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(CONDENSED_OBSIDIAN_KEY, PersistentDataType.BYTE);
    }
}
