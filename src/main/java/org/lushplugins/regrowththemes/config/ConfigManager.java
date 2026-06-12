package org.lushplugins.regrowththemes.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
        new File(RegrowthThemes.getInstance().getDataFolder(), "themes").mkdirs();
    }

    public void reloadConfig() {
        RegrowthThemes plugin = RegrowthThemes.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        String currentThemeName = config.getString("current-theme", "none");
        if (!currentThemeName.equalsIgnoreCase("none")) {
            File themeFile = RegrowthThemes.getInstance().getDataPath()
                .resolve("themes")
                .resolve(currentThemeName + ".yml")
                .toFile();

            if (themeFile.exists()) {
                FileConfiguration themeConfig = YamlConfiguration.loadConfiguration(themeFile);
                String biome = themeConfig.getString("biome");

//                if (biome != null) {
//                    // TODO: Implement packet based biome displaying
//                    AbstractBiome biome = BiomeRegistry.registry().getBiome(ResourceKey.of(currentTheme.biome()));
//                    PacketHandler.of(plugin)
//                        .clearBiomes()
//                        .appendBiome(biome)
//                        .register();
//                }
            }

            currentTheme = new Theme(Schematic.load(currentThemeName));
        } else {
            currentTheme = null;
        }
    }

    public boolean isCurrentTheme(String themeName) {
        return currentTheme.name().equalsIgnoreCase(themeName);
    }

    public @Nullable Theme getCurrentTheme() {
        return currentTheme;
    }
}
