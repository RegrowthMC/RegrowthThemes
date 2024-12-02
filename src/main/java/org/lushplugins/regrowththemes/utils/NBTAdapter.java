package org.lushplugins.regrowththemes.utils;

import com.github.retrooper.packetevents.protocol.nbt.*;
import org.enginehub.linbus.tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class NBTAdapter {
    private static final HashMap<Class<? extends NBT>, ConversionFunction<? extends NBT, LinTag<?>>> NBT_TO_LIN_MAP = new HashMap<>();
    private static final HashMap<Class<? extends LinTag<?>>, ConversionFunction<? extends LinTag<?>, NBT>> LIN_TO_NBT_MAP = new HashMap<>();

    static {
        // Convert nbt to lin
        registerNbtToLin(NBTByte.class, LinByteTag.class, (nbt) -> LinByteTag.of(nbt.getAsByte()));
        registerNbtToLin(NBTByteArray.class, LinByteArrayTag.class, (nbt) -> LinByteArrayTag.of(nbt.getValue()));
        registerNbtToLin(NBTCompound.class, LinCompoundTag.class, (nbt) -> {
            LinCompoundTag.Builder tagBuilder = LinCompoundTag.builder();

            for (Map.Entry<String, NBT> entry : nbt.getTags().entrySet()) {
                tagBuilder.put(entry.getKey(), adapt(entry.getValue()));
            }

            return tagBuilder.build();
        });
        registerNbtToLin(NBTDouble.class, LinDoubleTag.class, (nbt) -> LinDoubleTag.of(nbt.getAsDouble()));
        registerNbtToLin(NBTEnd.class, LinEndTag.class, (nbt) -> LinEndTag.instance());
        registerNbtToLin(NBTFloat.class, LinFloatTag.class, (nbt) -> LinFloatTag.of(nbt.getAsFloat()));
        registerNbtToLin(NBTInt.class, LinIntTag.class, (nbt) -> LinIntTag.of(nbt.getAsInt()));
        registerNbtToLin(NBTIntArray.class, LinIntArrayTag.class, (nbt) -> LinIntArrayTag.of(nbt.getValue()));
        registerNbtToLin(NBTList.class, LinListTag.class, NBTAdapter::convertList);
        registerNbtToLin(NBTLong.class, LinLongTag.class, (nbt) -> LinLongTag.of(nbt.getAsLong()));
        registerNbtToLin(NBTLongArray.class, LinLongArrayTag.class, (nbt) -> LinLongArrayTag.of(nbt.getValue()));
        registerNbtToLin(NBTShort.class, LinShortTag.class, (nbt) -> LinShortTag.of(nbt.getAsShort()));
        registerNbtToLin(NBTString.class, LinStringTag.class, (nbt) -> LinStringTag.of(nbt.getValue()));

        // Convert lin to nbt
        registerLinToNbt(LinByteTag.class, NBTByte.class, (tag) -> new NBTByte(tag.value()));
        registerLinToNbt(LinByteArrayTag.class, NBTByteArray.class, (tag) -> new NBTByteArray(tag.value()));
        registerLinToNbt(LinCompoundTag.class, NBTCompound.class, (tag) -> {
            NBTCompound nbt = new NBTCompound();

            for (Map.Entry<String, LinTag<?>> entry : tag.value().entrySet()) {
                nbt.setTag(entry.getKey(), adapt(entry.getValue()));
            }

            return nbt;
        });
        registerLinToNbt(LinDoubleTag.class, NBTDouble.class, (tag) -> new NBTDouble(tag.value()));
        registerLinToNbt(LinEndTag.class, NBTEnd.class, (tag) -> NBTEnd.INSTANCE);
        registerLinToNbt(LinFloatTag.class, NBTFloat.class, (tag) -> new NBTFloat(tag.value()));
        registerLinToNbt(LinIntTag.class, NBTInt.class, (tag) -> new NBTInt(tag.value()));
        registerLinToNbt(LinIntArrayTag.class, NBTIntArray.class, (tag) -> new NBTIntArray(tag.value()));
        registerLinToNbt(LinListTag.class, NBTList.class, NBTAdapter::convertList);
        registerLinToNbt(LinLongTag.class, NBTLong.class, (tag) -> new NBTLong(tag.value()));
        registerLinToNbt(LinLongArrayTag.class, NBTLongArray.class, (tag) -> new NBTLongArray(tag.value()));
        registerLinToNbt(LinShortTag.class, NBTShort.class, (tag) -> new NBTShort(tag.value()));
        registerLinToNbt(LinStringTag.class, NBTString.class, (tag) -> new NBTString(tag.value()));
    }

    public static LinTag<?> adapt(NBT nbt) {
        if (NBT_TO_LIN_MAP.containsKey(nbt.getClass())) {
            return NBT_TO_LIN_MAP.get(nbt.getClass()).apply(nbt);
        }

        // TODO: Check whether this is needed
        for (Map.Entry<Class<? extends NBT>, ConversionFunction<? extends NBT, LinTag<?>>> entry : NBT_TO_LIN_MAP.entrySet()) {
            if (entry.getKey().isInstance(nbt)) {
                return entry.getValue().apply(nbt);
            }
        }

        throw new IllegalArgumentException("No available adapter fond for provided NBT");
    }

    public static NBT adapt(LinTag<?> tag) {
        if (LIN_TO_NBT_MAP.containsKey(tag.getClass())) {
            return LIN_TO_NBT_MAP.get(tag.getClass()).apply(tag);
        }

        // TODO: Check whether this is needed
        for (Map.Entry<Class<? extends LinTag<?>>, ConversionFunction<? extends LinTag<?>, NBT>> entry : LIN_TO_NBT_MAP.entrySet()) {
            if (entry.getKey().isInstance(tag)) {
                return entry.getValue().apply(tag);
            }
        }

        throw new IllegalArgumentException("No available adapter fond for provided LinTag");
    }

    private static <T extends NBT, R extends LinTag<?>> void registerNbtToLin(Class<T> nbtClass, Class<R> linClass, Function<T, LinTag<?>> function) {
        NBT_TO_LIN_MAP.put(nbtClass, new ConversionFunction<>(nbtClass, function));
    }

    private static <T extends LinTag<?>, R extends NBT> void registerLinToNbt(Class<T> linClass, Class<R> nbtClass, Function<T, NBT> function) {
        LIN_TO_NBT_MAP.put(linClass, new ConversionFunction<>(linClass, function));
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    private static <T extends LinTag<?>> LinListTag<T> convertList(NBTList<?> nbt) {
        LinListTag.Builder<T> tagBuilder = (LinListTag.Builder<T>) LinListTag.builder(NBTTypeAdapter.adapt(nbt.getType()));

        for (NBT itemRaw : nbt.getTags()) {
            tagBuilder.add((T) adapt(itemRaw));
        }

        return tagBuilder.build();
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends NBT> NBTList<T> convertList(LinListTag<?> tag) {
        NBTList<T> nbt = (NBTList<T>) new NBTList<>(NBTTypeAdapter.adapt(tag.elementType()));

        for (LinTag<?> itemRaw : tag.value()) {
            nbt.addTag((T) adapt(itemRaw));
        }

        return nbt;
    }

    private record ConversionFunction<T, R>(Class<T> inputClass, Function<T, R> function) {

        public R apply(Object obj) {
            return function.apply(inputClass.cast(obj));
        }
    }
}
