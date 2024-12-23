package org.lushplugins.regrowththemes.command.subcommands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.schematic.SchematicManager;

public class ViewCommand extends SubCommand {

    public ViewCommand() {
        super("view");
        addRequiredPermission("regrowththemes.view");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command cannot be ran by console");
            return true;
        }

        if (args.length < 1) {
            ChatColorHandler.sendMessage(sender, "&cIncorrect arguments, try: /themes view <schematic>");
            return true;
        }

        SchematicManager schematicManager = RegrowthThemes.getInstance().getSchematicManager();
        String schematicName = args[0];
        Schematic schematic = schematicManager.getOrLoadSchematic(schematicName);
        if (schematic == null) {
            ChatColorHandler.sendMessage(sender, "&cInvalid schematic defined");
            return true;
        }

        for (Chunk sentChunk : player.getSentChunks()) {
            schematic.sendPackets(player, sentChunk);
        }

        return true;
    }
}
