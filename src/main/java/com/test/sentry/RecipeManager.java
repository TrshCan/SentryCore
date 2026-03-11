package com.test.sentry;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public final class RecipeManager {

    /**
     * Registers the shaped Sentry Core recipe.
     *
     * Pattern:
     *   O O O
     *   O N O   (N = Nether Star, O = Obsidian)
     *   O O O
     *
     * Result: SentryCoreItem (Conduit with PDC tag + glimmer)
     */
    public static void register(JavaPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "sentry_core");
        ShapedRecipe recipe = new ShapedRecipe(key, SentryCoreItem.buildItem(null));

        recipe.shape("OOO", "ONO", "OOO");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('N', Material.NETHER_STAR);

        plugin.getServer().addRecipe(recipe);
        plugin.getLogger().info("Registered Sentry Core crafting recipe.");
    }

    private RecipeManager() {}
}
