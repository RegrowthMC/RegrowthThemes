package org.lushplugins.regrowththemes;

import org.bukkit.plugin.java.JavaPlugin;

public final class RegrowthThemes extends JavaPlugin {
    private static RegrowthThemes plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Enable implementation
    }

    @Override
    public void onDisable() {
        // Disable implementation
    }

    public static RegrowthThemes getInstance() {
        return plugin;
    }
}
