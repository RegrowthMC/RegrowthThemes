package org.lushplugins.regrowththemes.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.schematic.SchematicManager;
import org.lushplugins.regrowththemes.theme.Theme;
import org.lushplugins.regrowththemes.utils.FileUtils;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.IOException;

@SuppressWarnings("unused")
@Command("theme")
public class ThemeCommand {

    @Subcommand("set")
    @CommandPermission("regrowththemes.set")
    public void set(Theme theme) {
        try {
            FileConfiguration config = RegrowthThemes.getInstance().getConfig();
            config.set("current-theme", theme.name());
            config.save(RegrowthThemes.getInstance().getDataPath().resolve("config.yml").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RegrowthThemes.getInstance().getConfigManager().reload();

        theme.apply();
    }

    @Subcommand("view")
    @CommandPermission("regrowththemes.view")
    public void view(BukkitCommandActor actor, Theme theme) {
        Player player = actor.requirePlayer();

        theme.schematic().thenAccept((schematic) -> {
            if (schematic == null) {
                actor.error("That theme does not have a schematic");
                return;
            }

            RegrowthThemes.getInstance().getSchematicManager().setViewing(player.getUniqueId(), theme.name());
            schematic.sendPackets(player);
        });
    }

    @Subcommand("create")
    @CommandPermission("regrowththemes.create")
    public void create(BukkitCommandActor actor, String theme) {
        Player player = actor.requirePlayer();

        String schematicName = StringUtils.appendIfMissing(theme, ".schem");
        if (FileUtils.getSafeFile(RegrowthThemes.getInstance().getSchematicsPath(), schematicName).exists()) {
            actor.error("That theme already exists");
            return;
        }

        SchematicManager schematicManager = RegrowthThemes.getInstance().getSchematicManager();
        Schematic schematic = Schematic.create(schematicName, player.getLocation());
        schematicManager.addSchematic(theme, schematic);
        schematicManager.setEditing(player.getUniqueId(), schematicName);
    }

    @Subcommand("edit")
    @CommandPermission("regrowththemes.edit")
    public void edit(BukkitCommandActor actor, Theme theme) {
        Player player = actor.requirePlayer();

        theme.schematic().thenAccept(schematic -> {
            if (schematic == null) {
                actor.error("That theme does not have a schematic");
                return;
            }

            RegrowthThemes.getInstance().getSchematicManager().setEditing(player.getUniqueId(), theme.name());

            schematic.sendPackets(player);
        });
    }

    @Subcommand("reload")
    @CommandPermission("regrowththemes.reload")
    public void reload(BukkitCommandActor actor) {
        RegrowthThemes.getInstance().getConfigManager().reload();
        RegrowthThemes.getInstance().getThemeManager().reload();
        actor.reply("<green>Successfully reloaded");
    }
}
