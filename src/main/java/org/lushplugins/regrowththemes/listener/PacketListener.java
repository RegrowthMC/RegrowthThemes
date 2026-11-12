package org.lushplugins.regrowththemes.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.schematic.Schematic;
import org.lushplugins.regrowththemes.theme.Theme;

public class PacketListener extends PacketListenerAbstract {

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

            Schematic schematic = theme.cachedSchematic();
            if (schematic == null || schematic.isEditor(event.getUser().getUUID())) {
                return;
            }

            if (schematic.containsPosition(packet.getBlockPosition().getSerializedPosition())) {
                event.setCancelled(true);
            }
        }
    }
}
