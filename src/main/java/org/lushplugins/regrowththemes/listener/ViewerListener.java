package org.lushplugins.regrowththemes.listener;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.theme.Theme;

public class ViewerListener implements Listener {

    @EventHandler
    public void onChunkLoad(PlayerChunkLoadEvent event) {
        Theme currentTheme = RegrowthThemes.getInstance().getConfigManager().getCurrentTheme();
        if (currentTheme != null) {
            Player player = event.getPlayer();
            Schematic schematic = currentTheme.schematic();
            if (schematic != null) {
                schematic.sendPackets(player, event.getChunk());
            }
        }
    }
}
