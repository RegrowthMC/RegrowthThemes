package org.lushplugins.regrowththemes.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.theme.Theme;

import java.io.File;

public class ConfigManager {
    private Theme currentTheme;

    public ConfigManager() {
        RegrowthThemes.getInstance().saveDefaultConfig();
        new File(RegrowthThemes.getInstance().getDataFolder(), "schematics").mkdirs();
    }

    public void reloadConfig() {
        RegrowthThemes plugin = RegrowthThemes.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        String currentThemeName = config.getString("current-theme", "none");
        if (!currentThemeName.equalsIgnoreCase("none")) {
            currentTheme = new Theme(Schematic.load(currentThemeName));
        } else {
            currentTheme = null;
        }
    }

    public boolean isCurrentTheme(String themeName) {
        return currentTheme.getName().equalsIgnoreCase(themeName);
    }

    public @Nullable Theme getCurrentTheme() {
        return currentTheme;
    }
}
