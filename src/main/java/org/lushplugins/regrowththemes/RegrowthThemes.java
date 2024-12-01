package org.lushplugins.regrowththemes;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.plugin.SpigotPlugin;
import org.lushplugins.regrowththemes.command.ThemeCommand;
import org.lushplugins.regrowththemes.config.ConfigManager;
import org.lushplugins.regrowththemes.listener.EditorListener;
import org.lushplugins.regrowththemes.listener.PacketListener;
import org.lushplugins.regrowththemes.listener.ViewerListener;
import org.lushplugins.regrowththemes.schematic.SchematicManager;

import java.util.Objects;

public final class RegrowthThemes extends SpigotPlugin {
    private static RegrowthThemes plugin;

    private ConfigManager configManager;
    private SchematicManager schematicManager;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager();
        configManager.reloadConfig();
        schematicManager = new SchematicManager();

        registerListeners(
            new EditorListener(),
            new ViewerListener());

        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());

        registerCommand(new ThemeCommand());

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, (task) -> {
            SchematicManager schemManager = getSchematicManager();
            if (schemManager == null) {
                task.cancel();
                return;
            }

            Player[] players = schemManager.getSchematics().stream()
                .flatMap(schematic -> schematic.getUsers().stream())
                .distinct()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toArray(Player[]::new);

            ChatColorHandler.sendActionBarMessage(players, "&cYou are currently in edit mode");
        }, 20, 20);
    }

    @Override
    public void onDisable() {
        schematicManager = null;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public static RegrowthThemes getInstance() {
        return plugin;
    }
}
