package org.lushplugins.regrowththemes.command.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.regrowththemes.RegrowthThemes;

public class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload");
        addRequiredPermission("regrowththemes.reload");
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings, @NotNull String[] strings1) {
        RegrowthThemes.getInstance().getConfigManager().reloadConfig();
        ChatColorHandler.sendMessage(commandSender, "&aSuccessfully reloaded");
        return true;
    }
}
