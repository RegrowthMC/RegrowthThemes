package org.lushplugins.regrowththemes.schematic;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.joml.Vector2i;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class BukkitSchematic extends Schematic {
    private static final BlockData BUKKIT_AIR = Material.AIR.createBlockData();

    protected BukkitSchematic(File file, Clipboard clipboard) {
        super(file, clipboard);
    }

    protected BukkitSchematic(File file, Region region) {
        super(file, region);
    }

    public void placeBlock(BlockState state) {
        Location location = state.getLocation();
        BlockData blockData = state.getBlockData();

        super.placeBlock(BukkitAdapter.asBlockVector(location), BukkitAdapter.adapt(blockData));

        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> this.getUsers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(player -> player.sendBlockChange(location, blockData)), 5);
    }

    public void breakBlock(Location location) {
        super.breakBlock(BukkitAdapter.asBlockVector(location));

        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> this.getUsers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(player -> player.sendBlockChange(location, BUKKIT_AIR)), 5);
    }

    public void sendPackets(Player player, Chunk chunk) {
        super.sendPackets(player, new Vector2i(chunk.getX(), chunk.getZ()));
    }

    public static BukkitSchematic create(String path, Location location) {
        if (!path.endsWith(".schem")) {
            path += ".schem";
        }

        File file = FileUtils.getSafeFile(new File(RegrowthThemes.getInstance().getDataFolder(), "schematics"), path);
        BlockVector3 origin = BukkitAdapter.asBlockVector(location);
        return new BukkitSchematic(file, new Region(origin));
    }

    public static BukkitSchematic load(String path) {
        if (!path.endsWith(".schem")) {
            path += ".schem";
        }

        return load(FileUtils.getSafeFile(new File(RegrowthThemes.getInstance().getDataFolder(), "schematics"), path));
    }

    public static BukkitSchematic load(File file) {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            return new BukkitSchematic(file, clipboard);
        } catch (IOException e) {
            return null;
        }
    }
}
