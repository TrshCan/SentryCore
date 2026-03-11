package com.test;

import com.test.sentry.RecipeManager;
import com.test.sentry.SentryCoreItem;
import com.test.sentry.SentryGuiListener;
import com.test.sentry.SentryListener;
import com.test.sentry.SentryManager;
import com.test.sentry.SentryTask;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    private SentryManager sentryManager;
    private SentryTask sentryTask;

    @Override
    public void onEnable() {
        // Initialise the PDC key (must happen before recipe or item creation)
        SentryCoreItem.init(this);

        // Register custom recipe
        RecipeManager.register(this);

        // Set up sentry tracking
        sentryManager = new SentryManager();

        // Register block place/break listener
        getServer().getPluginManager().registerEvents(new SentryListener(sentryManager), this);

        // Register GUI interaction listener (shift+right-click + inventory clicks)
        getServer().getPluginManager().registerEvents(new SentryGuiListener(sentryManager), this);

        // Start the repeating sentry task (runs every 10 ticks)
        sentryTask = new SentryTask(sentryManager);
        sentryTask.runTaskTimer(this, 10L, 10L);

        getLogger().info("SentryCore enabled — Sentry task running.");
    }

    @Override
    public void onDisable() {
        if (sentryTask != null && !sentryTask.isCancelled()) {
            sentryTask.cancel();
        }
        if (sentryManager != null) {
            sentryManager.clear();
        }
        getLogger().info("SentryCore disabled.");
    }
}
