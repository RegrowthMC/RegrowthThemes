package org.lushplugins.regrowththemes.utils;

import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import org.enginehub.linbus.tree.LinTagType;

import java.util.HashMap;

public class NBTTypeAdapter {
    private static final HashMap<LinTagType<?>, NBTType<?>> LIN_TO_NBT_TYPE = new HashMap<>();
    private static final HashMap<NBTType<?>, LinTagType<?>> NBT_TO_LIN_TYPE = new HashMap<>();

    static {
        registerMapping(LinTagType.endTag(), NBTType.END);
        registerMapping(LinTagType.byteTag(), NBTType.BYTE);
        registerMapping(LinTagType.shortTag(), NBTType.SHORT);
        registerMapping(LinTagType.intTag(), NBTType.INT);
        registerMapping(LinTagType.longTag(), NBTType.LONG);
        registerMapping(LinTagType.floatTag(), NBTType.FLOAT);
        registerMapping(LinTagType.doubleTag(), NBTType.DOUBLE);
        registerMapping(LinTagType.byteArrayTag(), NBTType.BYTE_ARRAY);
        registerMapping(LinTagType.stringTag(), NBTType.STRING);
        registerMapping(LinTagType.listTag(), NBTType.LIST);
        registerMapping(LinTagType.compoundTag(), NBTType.COMPOUND);
        registerMapping(LinTagType.intArrayTag(), NBTType.INT_ARRAY);
        registerMapping(LinTagType.longArrayTag(), NBTType.LONG_ARRAY);
    }

    public static NBTType<?> adapt(LinTagType<?> type) {
        return LIN_TO_NBT_TYPE.get(type);
    }

    public static LinTagType<?> adapt(NBTType<?> type) {
        return NBT_TO_LIN_TYPE.get(type);
    }

    private static void registerMapping(LinTagType<?> tagType, NBTType<?> nbtType) {
        LIN_TO_NBT_TYPE.put(tagType, nbtType);
        NBT_TO_LIN_TYPE.put(nbtType, tagType);
    }
}
