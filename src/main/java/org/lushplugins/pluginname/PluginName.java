package org.lushplugins.pluginname;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginName extends JavaPlugin {
    private static PluginName plugin;

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
    
    public static PluginName getInstance() {
        return plugin;
    }
}
