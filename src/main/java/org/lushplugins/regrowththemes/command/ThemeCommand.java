package org.lushplugins.regrowththemes.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.regrowththemes.command.subcommands.CreateCommand;
import org.lushplugins.regrowththemes.command.subcommands.EditCommand;
import org.lushplugins.regrowththemes.command.subcommands.ReloadCommand;

public class ThemeCommand extends Command {

    public ThemeCommand() {
        super("theme");
        addSubCommand(new CreateCommand());
        addSubCommand(new EditCommand());
        addSubCommand(new ReloadCommand());
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings, @NotNull String[] strings1) {
        return true;
    }
}
