package org.lushplugins.regrowththemes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.schematic.SchematicManager;
import org.lushplugins.regrowththemes.utils.FileUtils;

import java.io.File;

public class CreateCommand extends SubCommand {

    public CreateCommand() {
        super("create");
        addRequiredPermission("regrowththemes.edit");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command cannot be ran by console");
            return true;
        }

        if (args.length < 1) {
            ChatColorHandler.sendMessage(sender, "&cIncorrect arguments, try: /themes create <theme>");
            return true;
        }

        String schematicName = args[0];
        if (!schematicName.endsWith(".schem")) {
            schematicName += ".schem";
        }

        if (FileUtils.getSafeFile(new File(RegrowthThemes.getInstance().getDataFolder(), "schematics"), schematicName).exists()) {
            ChatColorHandler.sendMessage(sender, "&cThat theme already exists");
            return true;
        }

        SchematicManager schematicManager = RegrowthThemes.getInstance().getSchematicManager();
        Schematic schematic = Schematic.create(schematicName, player.getLocation());
        schematicManager.addSchematic(schematic);
        schematicManager.updateUser(player.getUniqueId(), schematicName);
        return true;
    }
}
