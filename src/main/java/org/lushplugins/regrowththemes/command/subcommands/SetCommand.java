package org.lushplugins.regrowththemes.command.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.regrowththemes.RegrowthThemes;

import java.io.IOException;
import java.util.List;

public class SetCommand extends SubCommand {

    public SetCommand() {
        super("set");
        addRequiredPermission("regrowththemes.set");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (args.length < 1) {
            ChatColorHandler.sendMessage(sender, "&cIncorrect arguments, try: /themes set <theme>");
            return true;
        }

        String theme = args[0];
        RegrowthThemes plugin = RegrowthThemes.getInstance();
        plugin.getConfigManager().setCurrentTheme(theme);

        try {
            FileConfiguration config = plugin.getConfig();
            config.set("current-theme", theme);
            config.save(plugin.getDataPath().resolve("config.yml").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args, @NonNull @NotNull String[] fullArgs) {
        return args.length == 1 ? RegrowthThemes.getInstance().getConfigManager().getThemes() : null;
    }
}
