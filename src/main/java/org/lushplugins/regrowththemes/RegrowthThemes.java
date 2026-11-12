package org.lushplugins.regrowththemes;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.chatcolorhandler.paper.PaperColor;
import org.lushplugins.lushlib.utils.plugin.SpigotPlugin;
import org.lushplugins.regrowththemes.command.ThemeCommand;
import org.lushplugins.regrowththemes.config.ConfigManager;
import org.lushplugins.regrowththemes.listener.EditorListener;
import org.lushplugins.regrowththemes.listener.PacketListener;
import org.lushplugins.regrowththemes.listener.ViewerListener;
import org.lushplugins.regrowththemes.schematic.SchematicManager;
import org.lushplugins.regrowththemes.theme.Theme;
import org.lushplugins.regrowththemes.theme.ThemeManager;
import revxrsal.commands.bukkit.BukkitLamp;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class RegrowthThemes extends SpigotPlugin {
    private static RegrowthThemes plugin;

    private SchematicManager schematicManager;
    private ThemeManager themeManager;
    private ConfigManager configManager;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        schematicManager = new SchematicManager();

        themeManager = new ThemeManager();
        themeManager.reload();

        configManager = new ConfigManager();
        configManager.reload();

        registerListeners(
            new EditorListener(),
            new ViewerListener()
        );

        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());

        BukkitLamp.builder(this)
            .parameterTypes(types -> types
                .addParameterType(Theme.class, (input, _) -> {
                    return RegrowthThemes.getInstance().getThemeManager().getTheme(input.readString());
                }))
            .suggestionProviders(providers -> providers
                .addProvider(Theme.class, (_) -> {
                    return RegrowthThemes.getInstance().getThemeManager().getThemeNames();
                }))
            .build()
            .register(new ThemeCommand());

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, (task) -> {
            SchematicManager schemManager = getSchematicManager();
            if (schemManager == null) {
                task.cancel();
                return;
            }

            List<Player> players = schemManager.getCachedSchematics().stream()
                .filter(Objects::nonNull)
                .flatMap(schematic -> schematic.getEditors().stream())
                .distinct()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();

            PaperColor.handler().sendActionBarMessage(players, "<#ff6969>You are currently in edit mode");
        }, 20, 20);
    }

    @Override
    public void onDisable() {
        schematicManager = null;
    }

    public Path getThemesPath() {
        return getDataPath().resolve("themes");
    }

    public Path getSchematicsPath() {
        return getDataPath().resolve("schematics");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public static RegrowthThemes getInstance() {
        return plugin;
    }
}
