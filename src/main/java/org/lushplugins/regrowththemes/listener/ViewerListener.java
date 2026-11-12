package org.lushplugins.regrowththemes.listener;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;

public class ViewerListener implements Listener {

    @EventHandler
    public void onChunkLoad(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        Schematic schematic = RegrowthThemes.getInstance().getSchematicManager().getViewing(player.getUniqueId());
        if (schematic != null) {
            schematic.sendPackets(player, event.getChunk());
        }
    }
}
