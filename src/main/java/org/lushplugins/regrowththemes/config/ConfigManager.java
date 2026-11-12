package org.lushplugins.regrowththemes.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.theme.Theme;

import java.util.Objects;

public class ConfigManager {
    private String currentTheme;

    public ConfigManager() {
        RegrowthThemes.getInstance().saveDefaultConfig();
        RegrowthThemes.getInstance().getSchematicsPath().toFile().mkdirs();
        RegrowthThemes.getInstance().getThemesPath().toFile().mkdirs();
    }

    public void reload() {
        RegrowthThemes plugin = RegrowthThemes.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        String currentThemeName = config.getString("current-theme", "none");
        if (!isCurrentTheme(currentThemeName)) {
            currentTheme = currentThemeName.equalsIgnoreCase("none") ? null : currentThemeName;

            Theme theme = getCurrentTheme();
            if (theme != null) {
                theme.apply();
            }
        }
    }

    public boolean isCurrentTheme(String themeName) {
        return themeName.equalsIgnoreCase(currentTheme);
    }

    public @Nullable String getCurrentThemeName() {
        return currentTheme;
    }

    public @Nullable Theme getCurrentTheme() {
        return RegrowthThemes.getInstance().getThemeManager().getTheme(currentTheme);
    }
}
