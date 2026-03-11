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
    private static NamespacedKey SENTRY_OWNER_KEY;
    private static NamespacedKey SENTRY_RANGE_KEY;
    private static NamespacedKey SENTRY_RECHARGE_KEY;
    private static NamespacedKey SENTRY_DAMAGE_KEY;
    private static NamespacedKey SENTRY_BUFF_KEY;
    private static NamespacedKey SENTRY_TARGETS_KEY;
    private static NamespacedKey SENTRY_ALLOWED_TARGETS_KEY;

    public static void init(JavaPlugin plugin) {
        SENTRY_TYPE_KEY = new NamespacedKey(plugin, "sentry_type");
        SENTRY_OWNER_KEY = new NamespacedKey(plugin, "sentry_owner");
        SENTRY_RANGE_KEY = new NamespacedKey(plugin, "sentry_range_tier");
        SENTRY_RECHARGE_KEY = new NamespacedKey(plugin, "sentry_recharge_tier");
        SENTRY_DAMAGE_KEY = new NamespacedKey(plugin, "sentry_damage_tier");
        SENTRY_BUFF_KEY = new NamespacedKey(plugin, "sentry_buff_tier");
        SENTRY_TARGETS_KEY = new NamespacedKey(plugin, "sentry_targets_tier");
        SENTRY_ALLOWED_TARGETS_KEY = new NamespacedKey(plugin, "sentry_allowed_targets");
    }

    public static NamespacedKey getSentryTypeKey() {
        return SENTRY_TYPE_KEY;
    }

    /**
     * Builds the Sentry Core item: a Conduit with a hidden enchantment glimmer,
     * a coloured display name, and a PDC tag identifying it as a sentry core.
     * Optionally assigns an owner, persists upgrade tier levels, and allowed targets.
     */
    public static ItemStack buildItem(String ownerName, int rangeTier, int rechargeTier, int damageTier, int buffTier, int targetsTier, java.util.Set<org.bukkit.entity.EntityType> allowedTargets) {
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

        // PDC tags
        meta.getPersistentDataContainer().set(SENTRY_TYPE_KEY, PersistentDataType.STRING, "core");
        meta.getPersistentDataContainer().set(SENTRY_RANGE_KEY, PersistentDataType.INTEGER, rangeTier);
        meta.getPersistentDataContainer().set(SENTRY_RECHARGE_KEY, PersistentDataType.INTEGER, rechargeTier);
        meta.getPersistentDataContainer().set(SENTRY_DAMAGE_KEY, PersistentDataType.INTEGER, damageTier);
        meta.getPersistentDataContainer().set(SENTRY_BUFF_KEY, PersistentDataType.INTEGER, buffTier);
        meta.getPersistentDataContainer().set(SENTRY_TARGETS_KEY, PersistentDataType.INTEGER, targetsTier);

        if (allowedTargets != null) {
            String targetsStr = allowedTargets.stream().map(Enum::name).collect(java.util.stream.Collectors.joining(","));
            meta.getPersistentDataContainer().set(SENTRY_ALLOWED_TARGETS_KEY, PersistentDataType.STRING, targetsStr);
        }

        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Range Tier: " + rangeTier).color(TextColor.fromHexString("#AAAAAA")).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Recharge Speed Tier: " + rechargeTier).color(TextColor.fromHexString("#AAAAAA")).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Damage Tier: " + damageTier).color(TextColor.fromHexString("#AAAAAA")).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Passive Buff Tier: " + buffTier).color(TextColor.fromHexString("#AAAAAA")).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Multi-Target Tier: " + targetsTier).color(TextColor.fromHexString("#AAAAAA")).decoration(TextDecoration.ITALIC, false));

        if (ownerName != null) {
            meta.getPersistentDataContainer().set(SENTRY_OWNER_KEY, PersistentDataType.STRING, ownerName);
            lore.add(Component.empty());
            lore.add(Component.text("Owner: " + ownerName)
                .color(TextColor.fromHexString("#FFFF55"))
                .decoration(TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Extracts the owner's name from a Sentry Core item, or null if not found.
     */
    public static String getOwner(ItemStack item) {
        if (!isSentryCore(item)) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(SENTRY_OWNER_KEY, PersistentDataType.STRING);
    }

    public static int getRangeTier(ItemStack item) {
        if (!isSentryCore(item) || item.getItemMeta() == null) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SENTRY_RANGE_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getRechargeTier(ItemStack item) {
        if (!isSentryCore(item) || item.getItemMeta() == null) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SENTRY_RECHARGE_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getDamageTier(ItemStack item) {
        if (!isSentryCore(item) || item.getItemMeta() == null) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SENTRY_DAMAGE_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getBuffTier(ItemStack item) {
        if (!isSentryCore(item) || item.getItemMeta() == null) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SENTRY_BUFF_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getTargetsTier(ItemStack item) {
        if (!isSentryCore(item) || item.getItemMeta() == null) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SENTRY_TARGETS_KEY, PersistentDataType.INTEGER, 0);
    }

    public static java.util.Set<org.bukkit.entity.EntityType> getAllowedTargets(ItemStack item) {
        if (!isSentryCore(item) || item.getItemMeta() == null) return SentryTargets.getDefaultTargets();
        String targetsStr = item.getItemMeta().getPersistentDataContainer().get(SENTRY_ALLOWED_TARGETS_KEY, PersistentDataType.STRING);
        if (targetsStr == null || targetsStr.isEmpty()) return SentryTargets.getDefaultTargets();
        
        java.util.Set<org.bukkit.entity.EntityType> targets = new java.util.HashSet<>();
        for (String s : targetsStr.split(",")) {
            try {
                targets.add(org.bukkit.entity.EntityType.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return targets.isEmpty() ? SentryTargets.getDefaultTargets() : targets;
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
