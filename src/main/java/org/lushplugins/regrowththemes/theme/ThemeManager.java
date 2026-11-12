package org.lushplugins.regrowththemes.theme;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ThemeManager {
    private Map<String, Theme> themes = Collections.emptyMap();

    public void reload() {
        themes = Arrays.stream(RegrowthThemes.getInstance().getThemesPath().toFile().listFiles())
            .filter(file -> file.getName().endsWith(".yml"))
            .collect(Collectors.toMap(
                file -> file.getName().replace(".yml", ""),
                file -> {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String name = file.getName().replace(".yml", "");
                    String biome = config.getString("biome");

                    return new Theme(name, biome);
                }
            ));
    }

    public Set<String> getThemeNames() {
        return themes.keySet();
    }

    public @Nullable Theme getTheme(String name) {
        return themes.get(name);
    }
}
