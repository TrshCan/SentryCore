package com.test.sentry;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class SentryCoreItem {

    private static NamespacedKey SENTRY_TYPE_KEY;

    public static void init(JavaPlugin plugin) {
        SENTRY_TYPE_KEY = new NamespacedKey(plugin, "sentry_type");
    }

    public static NamespacedKey getSentryTypeKey() {
        return SENTRY_TYPE_KEY;
    }

    /**
     * Builds the Sentry Core item: a Conduit with a hidden enchantment glimmer,
     * a coloured display name, and a PDC tag identifying it as a sentry core.
     */
    public static ItemStack buildItem() {
        ItemStack item = new ItemStack(Material.CONDUIT, 1);
        ItemMeta meta = item.getItemMeta();

        // Purple bold display name using Adventure API
        meta.displayName(
            Component.text("Sentry Core")
                .color(TextColor.fromHexString("#AA00FF"))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );

        // Enchantment glimmer (hidden) — LUCK_OF_THE_SEA gives the enchant shimmer
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // PDC tag: sentry_type = "core"
        meta.getPersistentDataContainer().set(SENTRY_TYPE_KEY, PersistentDataType.STRING, "core");

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns true if the given ItemStack is a Sentry Core (has the PDC tag).
     */
    public static boolean isSentryCore(ItemStack item) {
        if (item == null || item.getType() != Material.CONDUIT) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String value = meta.getPersistentDataContainer()
                .get(SENTRY_TYPE_KEY, PersistentDataType.STRING);
        return "core".equals(value);
    }

    private SentryCoreItem() {}
}
