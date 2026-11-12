package org.lushplugins.regrowththemes.listener;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;

import java.util.UUID;

public class EditorListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Schematic schematic = RegrowthThemes.getInstance().getSchematicManager().getViewing(uuid);
        if (schematic != null && schematic.isEditor(uuid)) {
            event.setCancelled(true);
            BlockState blockState = event.getBlock().getState();
            schematic.setBlock(blockState);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Schematic schematic = RegrowthThemes.getInstance().getSchematicManager().getViewing(uuid);
        if (schematic != null && schematic.isEditor(uuid)) {
            event.setCancelled(true);
            schematic.removeBlock(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        RegrowthThemes.getInstance().getSchematicManager().invalidateUser(event.getPlayer().getUniqueId());
    }
}
