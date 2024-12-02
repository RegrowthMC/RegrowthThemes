package org.lushplugins.regrowththemes.schematic;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SchematicBlock {
    private final long position;
    private final int stateId;
    private final TileEntityData tileEntityData;

    public SchematicBlock(long position, int stateId, @Nullable TileEntityData tileEntityData) {
        this.position = position;
        this.stateId = stateId;
        this.tileEntityData = tileEntityData;
    }

    public Vector3i getPosition() {
        return new Vector3i(position);
    }

    public long getSerializedPosition() {
        return position;
    }

    public WrappedBlockState getBlockState() {
        return WrappedBlockState.getByGlobalId(stateId);
    }

    public int getStateId() {
        return stateId;
    }

    public WrapperPlayServerMultiBlockChange.EncodedBlock getEncodedBlock() {
        Vector3i position = this.getPosition();
        return new WrapperPlayServerMultiBlockChange.EncodedBlock(getBlockState(), position.getX(), position.getY(), position.getZ());
    }

    public boolean hasTileEntityData() {
        return tileEntityData != null;
    }

    public TileEntityData getTileEntityData() {
        return tileEntityData;
    }

    public Optional<WrapperPlayServerBlockEntityData> prepareNBTPacket() {
        if (tileEntityData != null) {
            return Optional.of(new WrapperPlayServerBlockEntityData(this.getPosition(), tileEntityData.getBlockEntityType(), tileEntityData.getNBT()));
        } else {
            return Optional.empty();
        }
    }

    public static class TileEntityData {
        private final BlockEntityType blockEntityType;
        private final NBTCompound nbt;

        public TileEntityData(BlockEntityType blockEntityType, NBTCompound nbt) {
            this.blockEntityType = blockEntityType;
            this.nbt = nbt;
        }

        public BlockEntityType getBlockEntityType() {
            return blockEntityType;
        }

        public NBTCompound getNBT() {
            return nbt;
        }
    }
}
