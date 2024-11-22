package org.lushplugins.regrowththemes.listener;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.theme.Theme;

public class ViewerListener implements Listener {

    @EventHandler
    public void onChunkLoad(PlayerChunkLoadEvent event) {
        Theme currentTheme = RegrowthThemes.getInstance().getConfigManager().getCurrentTheme();
        if (currentTheme != null) {
            Player player = event.getPlayer();
            currentTheme.schematic().sendPackets(player, event.getChunk());
//            Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
//                currentTheme.schematic().sendPackets(player, event.getChunk());
//            }, 20);
        }
    }
}
