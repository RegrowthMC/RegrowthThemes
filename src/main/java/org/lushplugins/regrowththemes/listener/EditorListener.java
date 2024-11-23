package org.lushplugins.regrowththemes.listener;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.BukkitSchematic;
import org.lushplugins.regrowththemes.schematic.SchematicManager;
import org.lushplugins.regrowththemes.schematic.SchematicUser;

public class EditorListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        BukkitSchematic schematic = getCurrentSchematic(player);
        if (schematic == null) {
            return;
        }

        event.setCancelled(true);
        BlockState blockState = event.getBlock().getState();
        schematic.placeBlock(blockState);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        BukkitSchematic schematic = getCurrentSchematic(player);
        if (schematic == null) {
            return;
        }

        event.setCancelled(true);
        schematic.breakBlock(event.getBlock().getLocation());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        RegrowthThemes.getInstance().getSchematicManager().removeUser(player.getUniqueId());
    }

    private @Nullable BukkitSchematic getCurrentSchematic(Player player) {
        SchematicManager schematicManager = RegrowthThemes.getInstance().getSchematicManager();
        SchematicUser user = schematicManager.getUser(player.getUniqueId());
        if (user == null) {
            return null;
        }

        return user.getCurrentSchematic();
    }
}
