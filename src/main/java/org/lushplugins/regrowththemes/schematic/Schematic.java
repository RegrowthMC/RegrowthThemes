package org.lushplugins.regrowththemes.schematic;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.joml.Vector2i;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Schematic {
    private final File file;
    private final Region region;
    private final Map<Vector2i, Map<BlockVector3, BlockState>> chunkMap;
    private final List<UUID> users = new ArrayList<>();

    protected Schematic(File file, Clipboard clipboard) {
        this.file = file;
        this.region = new Region(clipboard);
        this.chunkMap = generateChunkMap(clipboard);
    }

    protected Schematic(File file, Region region) {
        this.file = file;
        this.region = region;
        this.chunkMap = new HashMap<>();
    }

    public String getName() {
        return file.getName();
    }

    public void placeBlock(BlockVector3 position, BlockState state) {
        region.expandToFit(position);

        Vector2i chunk = new Vector2i(position.x() / 16, position.z() / 16);
        Map<BlockVector3, BlockState> states = chunkMap.computeIfAbsent(chunk, (i) -> new HashMap<>());
        states.put(position, state);
    }

    public void breakBlock(BlockVector3 position) {
        if (!region.contains(position)) {
            return;
        }

        Vector2i chunk = new Vector2i(position.x() / 16, position.z() / 16);
        Map<BlockVector3, BlockState> states = chunkMap.get(chunk);
        if (states == null) {
            return;
        }

        states.remove(position);
    }

    public void sendPackets(Player player, Vector2i chunk) {
        if (!chunkMap.containsKey(chunk)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RegrowthThemes.getInstance(), () -> {
            // TODO: Run different method based on number of block states
            List<org.bukkit.block.BlockState> states = chunkMap.get(chunk).entrySet().stream()
                .map(entry -> {
                    BlockData blockData = BukkitAdapter.adapt(entry.getValue());
                    BlockVector3 position = entry.getKey();
                    Location location = new Location(null, position.x(), position.y(), position.z());
                    return blockData.createBlockState().copy(location);
                })
                .toList();

            player.sendBlockChanges(states);
        });
    }

    public void save() {
        BlockArrayClipboard clipboard = region.asClipboard();
        chunkMap.values().forEach(states -> states.forEach(clipboard::setBlock));

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (IOException e) {
            RegrowthThemes.getInstance().getLogger().log(Level.WARNING, "Failed to save schematic '" + file.getName() + "': ", e);
        }
    }

    public List<UUID> getUsers() {
        return users;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public void addUser(UUID uuid) {
        users.add(uuid);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);

        if (!hasUsers()) {
            save();
        }
    }

    public static Schematic create(String path, Location location) {
        if (!path.endsWith(".schem")) {
            path += ".schem";
        }

        File file = FileUtils.getSafeFile(new File(RegrowthThemes.getInstance().getDataFolder(), "schematics"), path);
        BlockVector3 origin = BukkitAdapter.asBlockVector(location);
        return new Schematic(file, new Region(origin));
    }

    public static Schematic load(String path) {
        if (!path.endsWith(".schem")) {
            path += ".schem";
        }

        return load(FileUtils.getSafeFile(new File(RegrowthThemes.getInstance().getDataFolder(), "schematics"), path));
    }

    public static Schematic load(File file) {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Clipboard clipboard = reader.read();
            return new Schematic(file, clipboard);
        } catch (IOException e) {
            return null;
        }
    }

    protected static Map<Vector2i, Map<BlockVector3, BlockState>> generateChunkMap(Clipboard clipboard) {
        Map<Vector2i, Map<BlockVector3, BlockState>> chunkMap = new HashMap<>();
        for (BlockVector3 position : clipboard) {
            BlockState state = clipboard.getBlock(position);
            if (state.isAir()) {
                continue;
            }

            Vector2i chunk = new Vector2i(position.x() / 16, position.z() / 16);
            Map<BlockVector3, BlockState> states = chunkMap.computeIfAbsent(chunk, (i) -> new HashMap<>());
            states.put(position, state);
        }

        return chunkMap;
    }

    protected static class Region {
        private final BlockVector3 origin;
        private BlockVector3 minPoint;
        private BlockVector3 maxPoint;

        public Region(Clipboard clipboard) {
            this.origin = clipboard.getOrigin();
            this.minPoint = clipboard.getMinimumPoint();
            this.maxPoint = clipboard.getMaximumPoint();
        }

        public Region(BlockVector3 origin) {
            this.origin = origin;
            this.minPoint = origin.subtract(1, 1, 1);
            this.maxPoint = origin.add(1, 1, 1);
        }

        public BlockVector3 getOrigin() {
            return origin;
        }

        public BlockVector3 getMinPoint() {
            return minPoint;
        }

        public BlockVector3 getMaxPoint() {
            return maxPoint;
        }

        public boolean contains(BlockVector3 position) {
            return position.containedWithin(minPoint, maxPoint);
        }

        public void expandToFit(BlockVector3 position) {
            if (!position.containedWithin(minPoint, maxPoint)) {
                minPoint = position.getMinimum(minPoint);
                maxPoint = position.getMaximum(maxPoint);
            }
        }

        public CuboidRegion asCuboidRegion() {
            return new CuboidRegion(minPoint, maxPoint);
        }

        public BlockArrayClipboard asClipboard() {
            CuboidRegion region = asCuboidRegion();
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            clipboard.setOrigin(origin);
            return clipboard;
        }
    }
}
