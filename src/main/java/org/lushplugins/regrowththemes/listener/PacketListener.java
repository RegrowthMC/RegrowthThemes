package org.lushplugins.regrowththemes.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.schematic.SchematicManager;
import org.lushplugins.regrowththemes.schematic.SchematicUser;
import org.lushplugins.regrowththemes.theme.Theme;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketListener extends PacketListenerAbstract {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public PacketListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        //In this listener we aim to process packets on another thread.
        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(event);
            Theme theme = RegrowthThemes.getInstance().getConfigManager().getCurrentTheme();
            if (theme == null) {
                return;
            }

            Schematic schematic = theme.schematic();
            if (schematic == null) {
                return;
            }

            if (schematic.containsPosition(packet.getBlockPosition().getSerializedPosition())) {
                event.setCancelled(true);
            }
        }
    }

    private @Nullable Schematic getCurrentSchematic(UUID uuid) {
        SchematicManager schematicManager = RegrowthThemes.getInstance().getSchematicManager();
        SchematicUser user = schematicManager.getUser(uuid);
        if (user == null) {
            return null;
        }

        return user.getCurrentSchematic();
    }
}
