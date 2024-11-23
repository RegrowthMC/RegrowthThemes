package org.lushplugins.regrowththemes.listener;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.theme.Theme;

public class ViewerListener implements Listener {

    @EventHandler
    public void onChunkLoad(PlayerChunkLoadEvent event) {
        Theme currentTheme = RegrowthThemes.getInstance().getConfigManager().getCurrentTheme();
        if (currentTheme != null) {
            currentTheme.schematic().sendPackets(event.getPlayer(), event.getChunk());
        }
    }
}
