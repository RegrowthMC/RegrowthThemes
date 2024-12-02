package org.lushplugins.regrowththemes.schematic;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
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
import org.lushplugins.regrowththemes.utils.NBTAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Schematic {
    private static final BlockData BUKKIT_AIR = Material.AIR.createBlockData();
    private static final BlockState AIR = BlockState.get("minecraft:air");

    // TODO: Move file, clipboard and potentially users to separate modifiable schematic
    private final File file;
    private final Clipboard clipboard;
    private final List<UUID> users = new ArrayList<>();
    private final HashMultimap<Vector2i, Long> locationMap = HashMultimap.create();
    private final HashMap<Long, SchematicBlock> blockMap = new HashMap<>();

    protected Schematic(File file, Clipboard clipboard) {
        this.file = file;
        this.clipboard = clipboard;

        Bukkit.getScheduler().runTaskAsynchronously(RegrowthThemes.getInstance(), () -> {
            for (BlockVector3 position : clipboard) {
                BaseBlock blockData = clipboard.getFullBlock(position);

                if (blockData.getMaterial().isAir()) {
                    continue;
                }

                SchematicBlock.TileEntityData blockEntityData = null;
                LinCompoundTag tag = blockData.getNbt();
                if (tag != null) {
                    blockEntityData = new SchematicBlock.TileEntityData(
                        BlockEntityTypes.getByName(blockData.getNbtId()),
                        (NBTCompound) NBTAdapter.adapt(tag)
                    );
                }

                Vector2i chunkLocation = new Vector2i(position.x() >> 4, position.z() >> 4);
                Vector3i location = new Vector3i(position.x(), position.y(), position.z());
                long serializedPosition = location.getSerializedPosition();

                SchematicBlock schemBlock = new SchematicBlock(
                    serializedPosition,
                    WrappedBlockState.getByString(blockData.getAsString()).getGlobalId(),
                    blockEntityData
                );

                locationMap.put(chunkLocation, serializedPosition);
                blockMap.put(serializedPosition, schemBlock);
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

//        Chunk chunk = state.getChunk();
//        Vector2i chunkLocation = new Vector2i(chunk.getX(), chunk.getZ());
//        chunkMap.put(chunkLocation, state);
//        locationMap.put(chunkLocation, new Vector3i(state.getX(), state.getY(), state.getZ()).getSerializedPosition());

        Location location = state.getLocation();
        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
            users.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.sendBlockChange(location, blockData));
        }, 5);
    }

    public void breakBlock(Location location) {
        BlockVector3 wePosition = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Region region = clipboard.getRegion();
        if (!region.contains(wePosition)) {
            return;
        }

        clipboard.setBlock(
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            AIR);

        Chunk chunk = location.getChunk();
        Vector2i chunkCoord = new Vector2i(chunk.getX(), chunk.getZ());


        long serializedPosition = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()).getSerializedPosition();
        locationMap.get(chunkCoord).remove(serializedPosition);
        blockMap.remove(serializedPosition);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
            users.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.sendBlockChange(location, BUKKIT_AIR));
        }, 5);
    }

    public void sendPackets(Player player, Chunk chunk) {
        Vector2i chunkLocation = new Vector2i(chunk.getX(), chunk.getZ());
        if (!locationMap.containsKey(chunkLocation)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RegrowthThemes.getInstance(), () -> {
            HashMultimap<Vector3i, WrapperPlayServerMultiBlockChange.EncodedBlock> encodedBlocks = HashMultimap.create();
            List<WrapperPlayServerBlockEntityData> entityDataPackets = new ArrayList<>();

            locationMap.get(chunkLocation).stream()
                .map(blockMap::get)
                .forEach(schemBlock -> {
                    Vector3i position = schemBlock.getPosition();
                    Vector3i chunkPosition = new Vector3i(position.getX() >> 4, position.getY() >> 4, position.getZ() >> 4);
                    encodedBlocks.put(chunkPosition, schemBlock.getEncodedBlock());

                    schemBlock.prepareNBTPacket().ifPresent(entityDataPackets::add);
                });

            for (Vector3i chunkPosition : encodedBlocks.keySet()) {
                WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(
                    chunkPosition,
                    null,
                    encodedBlocks.get(chunkPosition).toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new)
                );

                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }

            for (WrapperPlayServerBlockEntityData packet : entityDataPackets) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
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
