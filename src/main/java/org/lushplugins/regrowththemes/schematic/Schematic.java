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
import com.google.common.collect.Multimap;
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
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Schematic {
    private static final BlockData BUKKIT_AIR = Material.AIR.createBlockData();
    private static final BlockState AIR = BlockState.get("minecraft:air");

    private final Clipboard clipboard;
    private final File file;
    private Multimap<Vector2i, Long> locationMap = HashMultimap.create();
    private Map<Long, SchematicBlock> blockMap = new HashMap<>();
    private final Set<UUID> editors = new HashSet<>();
    // TODO: Fully implement
    private Callable<Collection<Player>> viewerEngine = Collections::emptySet;

    protected Schematic(Clipboard clipboard, File file) {
        this.clipboard = clipboard;
        this.file = file;
    }

    public CompletableFuture<Void> readClipboard() {
        return CompletableFuture.runAsync(() -> {
            Multimap<Vector2i, Long> locationMap = HashMultimap.create();
            Map<Long, SchematicBlock> blockMap = new HashMap<>();

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

            List<Long> removedLocations = new ArrayList<>(this.locationMap.values());
            removedLocations.removeAll(locationMap.values());

            this.locationMap = locationMap;
            this.blockMap = blockMap;

            for (Player viewer : viewers()) {
                sendPackets(viewer);

                for (Long removedLocation : removedLocations) {
                    Vector3i position = new Vector3i(removedLocation);
                    World world = viewer.getWorld();
                    Location location = new Location(world, position.getX(), position.getY(), position.getZ());
                    viewer.sendBlockChange(location, world.getBlockData(location));
                }
            }
        });
    }

    public boolean containsPosition(long position) {
        return locationMap.containsValue(position);
    }

    public void setBlock(Location location, BlockState state) {
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

        BaseBlock builtState = state.toBaseBlock(nbtData);
        clipboard.setBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ(), builtState);

//        Chunk chunk = state.getChunk();
//        Vector2i chunkLocation = new Vector2i(chunk.getX(), chunk.getZ());
//        chunkMap.put(chunkLocation, state);
//        locationMap.put(chunkLocation, new Vector3i(state.getX(), state.getY(), state.getZ()).getSerializedPosition());
    }

    public void setBlock(org.bukkit.block.BlockState state) {
        BlockData blockData = state.getBlockData();
        BlockState blockState = BukkitAdapter.adapt(blockData);

        Location location = state.getLocation();
        setBlock(location, blockState);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
            editors.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.sendBlockChange(location, blockData));
        }, 5);
    }

    public void removeBlock(Location location) {
        BlockVector3 wePosition = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Region region = clipboard.getRegion();
        if (region.contains(wePosition)) {
            clipboard.setBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ(), AIR);

            Bukkit.getScheduler().runTaskLaterAsynchronously(RegrowthThemes.getInstance(), () -> {
                editors.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(player -> player.sendBlockChange(location, BUKKIT_AIR));
            }, 5);
        }
    }

    public void save(File file) {
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (IOException e) {
            RegrowthThemes.getInstance().getLogger().log(Level.WARNING, "Failed to save schematic '" + file.getName() + "': ", e);
        }
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

    public void sendPackets(Player player) {
        Bukkit.getScheduler().runTask(RegrowthThemes.getInstance(), () -> {
            for (Chunk sentChunk : player.getSentChunks()) {
                sendPackets(player, sentChunk);
            }
        });
    }

    public boolean hasEditors() {
        return !editors.isEmpty();
    }

    public boolean isEditor(UUID uuid) {
        return editors.contains(uuid);
    }

    public Set<UUID> getEditors() {
        return editors;
    }

    public void addEditor(UUID uuid) {
        editors.add(uuid);
    }

    public void removeEditor(UUID uuid) {
        if (editors.remove(uuid) && editors.isEmpty()) {
            readClipboard();
            save(file);
        }
    }

    public Collection<Player> viewers() {
        try {
            return viewerEngine.call();
        } catch (Exception e) {
            RegrowthThemes.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void setViewerEngine(Callable<Collection<Player>> viewerEngine) {
        this.viewerEngine = viewerEngine;

        for (Player viewer : viewers()) {
            sendPackets(viewer);
        }
    }

    public void clearViewerEngine() {
        this.viewerEngine = Collections::emptySet;
    }

    public static Schematic create(String path, Location location) {
        if (!path.endsWith(".schem")) {
            path += ".schem";
        }

        File file = FileUtils.getSafeFile(RegrowthThemes.getInstance().getDataPath().resolve("schematics"), path);
        CuboidRegion region = new CuboidRegion(
            BlockVector3.at(location.getBlockX() - 150, location.getWorld().getMaxHeight(), location.getBlockZ() - 150),
            BlockVector3.at(location.getBlockX() + 150, location.getWorld().getMinHeight(), location.getBlockZ() + 150));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        return new Schematic(clipboard, file);
    }

    public static CompletableFuture<Schematic> load(File file) {
        if (!file.exists()) {
            return CompletableFuture.completedFuture(null);
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            return CompletableFuture.completedFuture(null);
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            Schematic schematic = new Schematic(reader.read(), file);
            String schematicName = file.getName().replace(".schem", "");
            schematic.setViewerEngine(() -> RegrowthThemes.getInstance().getSchematicManager().getViewing(schematicName).stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList());
            return schematic.readClipboard().thenApply((_) -> schematic);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    public static CompletableFuture<Schematic> load(String path) {
        path = StringUtils.appendIfMissing(path, ".schem");
        return load(FileUtils.getSafeFile(RegrowthThemes.getInstance().getSchematicsPath(), path));
    }
}
