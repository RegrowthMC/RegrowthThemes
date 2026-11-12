package org.lushplugins.regrowththemes.theme;

import dev.wyck.keys.ResourceKey;
import dev.wyck.renderer.packet.PacketHandler;
import dev.wyck.renderer.packet.data.VirtualBiome;
import dev.wyck.renderer.updater.BiomeUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;

import java.util.concurrent.CompletableFuture;

public record Theme(String name, @Nullable String biome) {
    private static final PacketHandler PACKET_HANDLER = PacketHandler.of(RegrowthThemes.getInstance());

    public @Nullable Schematic cachedSchematic() {
        return RegrowthThemes.getInstance().getSchematicManager().getSchematic(name);
    }

    public CompletableFuture<Schematic> schematic() {
        return RegrowthThemes.getInstance().getSchematicManager().getOrLoadSchematic(name);
    }

    public void apply() {
        PACKET_HANDLER.clearBiomes();

        if (biome != null) {
            PACKET_HANDLER
                .appendBiome(VirtualBiome.builder()
                    .biome(ResourceKey.of(biome))
                    .build())
                .register();

            BiomeUpdater biomeUpdater = BiomeUpdater.of(RegrowthThemes.getInstance());
            for (Player player : Bukkit.getOnlinePlayers()) {
                biomeUpdater.updateChunksForPlayer(player);
            }
        }

        schematic().thenAccept(schematic -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                schematic.sendPackets(player);
            }
        });
    }
}
