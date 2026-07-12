package org.lushplugins.regrowththemes.config;

import dev.wyck.keys.ResourceKey;
import dev.wyck.renderer.packet.PacketHandler;
import dev.wyck.renderer.packet.data.VirtualBiome;
import dev.wyck.renderer.updater.BiomeUpdater;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.theme.Theme;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private static final PacketHandler PACKET_HANDLER = PacketHandler.of(RegrowthThemes.getInstance());

    private Theme currentTheme;
    private List<String> themes;

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
            setCurrentTheme(currentThemeName);
        } else {
            currentTheme = null;
        }

        themes = Arrays.stream(new File(RegrowthThemes.getInstance().getDataFolder(), "themes").listFiles())
            .filter(file -> file.getName().endsWith(".yml"))
            .map(file -> file.getName().replace(".yml", ""))
            .toList();
    }

    public boolean isCurrentTheme(String themeName) {
        return currentTheme.name().equalsIgnoreCase(themeName);
    }

    public @Nullable Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String theme) {
        PACKET_HANDLER.clearBiomes();

        File themeFile = RegrowthThemes.getInstance().getDataPath()
            .resolve("themes")
            .resolve(theme + ".yml")
            .toFile();

        String biome;
        if (themeFile.exists()) {
            FileConfiguration themeConfig = YamlConfiguration.loadConfiguration(themeFile);
            biome = themeConfig.getString("biome");

            if (biome != null) {
                PACKET_HANDLER
                    .appendBiome(VirtualBiome.builder()
                        .biome(ResourceKey.of(biome))
                        .build())
                    .register();
            }
        } else {
            biome = null;
        }

        if (biome != null || (currentTheme != null && currentTheme.biome() != null)) {
            BiomeUpdater biomeUpdater = BiomeUpdater.of(RegrowthThemes.getInstance());
            for (Player player : Bukkit.getOnlinePlayers()) {
                biomeUpdater.updateChunksForPlayer(player);
            }
        }

        currentTheme = new Theme(theme, Schematic.load(theme), biome);
    }

    public List<String> getThemes() {
        return themes;
    }
}
