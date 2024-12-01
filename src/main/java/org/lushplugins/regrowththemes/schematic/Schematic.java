package org.lushplugins.regrowththemes.schematic;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.util.Vector3i;
import com.google.common.collect.HashMultimap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.enginehub.linbus.tree.*;
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
    private static final BlockData BUKKIT_AIR = Material.AIR.createBlockData();
    private static final BlockState AIR = BlockState.get("minecraft:air");

    private final File file;
    private final Clipboard clipboard;
    private final List<UUID> users = new ArrayList<>();
    private final HashMultimap<Vector2i, org.bukkit.block.BlockState> chunkMap = HashMultimap.create();
    private final HashMultimap<Vector2i, Long> locationMap = HashMultimap.create();

    protected Schematic(File file, Clipboard clipboard) {
        this.file = file;
        this.clipboard = clipboard;

        World world = Bukkit.getWorld("world");
        Bukkit.getScheduler().runTaskAsynchronously(RegrowthThemes.getInstance(), () -> {
            for (BlockVector3 position : clipboard) {
                BlockData blockData = BukkitAdapter.adapt(clipboard.getBlock(position));
                BaseBlock temp = clipboard.getBlock(position).toBaseBlock();

                if (blockData.getMaterial().isAir()) {
                    continue;
                }

                Location location = new Location(world, position.x(), position.y(), position.z());
                Chunk chunk = location.getChunk();
                org.bukkit.block.BlockState state = blockData.createBlockState().copy(location);

                Vector2i chunkLocation = new Vector2i(chunk.getX(), chunk.getZ());
                chunkMap.put(chunkLocation, state);
                locationMap.put(chunkLocation, new Vector3i(position.x(), position.y(), position.z()).getSerializedPosition());
            }
        });
    }

    public String getName() {
        return file.getName();
    }

    public boolean containsPosition(long position) {
        return locationMap.containsValue(position);
    }

    public void placeBlock(org.bukkit.block.BlockState state) {
        BlockData blockData = state.getBlockData();
        BlockState blockState = BukkitAdapter.adapt(blockData);

        LazyReference<LinCompoundTag> nbtData = null;
        if (state instanceof Skull skull) {
            PlayerProfile playerProfile = skull.getPlayerProfile();
            if (playerProfile != null) {
                ProfileProperty property = playerProfile.getProperties().stream().findFirst().orElse(null);
                if (property != null) {
                    LinCompoundTag.Builder propertyTagBuilder = LinCompoundTag.builder()
                        .put("name", LinStringTag.of(property.getName()))
                        .put("value", LinStringTag.of(property.getValue()));

                    if (property.getSignature() != null) {
                        propertyTagBuilder.put("signature", LinStringTag.of(property.getSignature()));
                    }

                    nbtData = LazyReference.from(() -> LinCompoundTag.builder()
                        .put("id", LinStringTag.of("minecraft:skull"))
                        .put("x", LinIntTag.of(skull.getX() - clipboard.getMinimumPoint().x()))
                        .put("y", LinIntTag.of(skull.getY() - clipboard.getMinimumPoint().y()))
                        .put("z", LinIntTag.of(skull.getZ() - clipboard.getMinimumPoint().z()))
                        .put("profile", LinCompoundTag.builder()
                            .put("properties", LinListTag.builder(LinTagType.compoundTag())
                                .add(propertyTagBuilder.build())
                                .build())
                            .build())
                        .build());
                }
            }
        }

        BaseBlock builtState = blockState.toBaseBlock(nbtData);
        clipboard.setBlock(
            state.getX(),
            state.getY(),
            state.getZ(),
            builtState);

        Chunk chunk = state.getChunk();
        Vector2i chunkLocation = new Vector2i(chunk.getX(), chunk.getZ());
        chunkMap.put(chunkLocation, state);
        locationMap.put(chunkLocation, new Vector3i(state.getX(), state.getY(), state.getZ()).getSerializedPosition());

        Location location = state.getLocation();
        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
            users.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.sendBlockChange(location, blockData));
        }, 5);
    }

    public void breakBlock(Location location) {
        BlockVector3 position = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Region region = clipboard.getRegion();
        if (!region.contains(position)) {
            return;
        }

        clipboard.setBlock(
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            AIR);

        Chunk chunk = location.getChunk();
        Vector2i chunkCoord = new Vector2i(chunk.getX(), chunk.getZ());
        chunkMap.get(chunkCoord).stream()
            .filter(state -> state.getLocation() == location)
            .findFirst()
            .ifPresent(state -> chunkMap.remove(chunkCoord, state));

        locationMap.get(chunkCoord).remove(new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
            users.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.sendBlockChange(location, BUKKIT_AIR));
        }, 5);
    }

    public void sendPackets(Player player, Chunk chunk) {
        Vector2i chunkCoordinate = new Vector2i(chunk.getX(), chunk.getZ());
        if (!chunkMap.containsKey(chunkCoordinate)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RegrowthThemes.getInstance(), () -> {
            // TODO: Run different method based on number of block states
            player.sendBlockChanges(chunkMap.get(chunkCoordinate));
        });
    }

    public void save() {
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
        CuboidRegion region = new CuboidRegion(
            BlockVector3.at(location.getBlockX() - 150, location.getWorld().getMaxHeight(), location.getBlockZ() - 150),
            BlockVector3.at(location.getBlockX() + 150, location.getWorld().getMinHeight(), location.getBlockZ() + 150));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        return new Schematic(file, clipboard);
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
            return new Schematic(file, reader.read());
        } catch (IOException e) {
            return null;
        }
    }
}
