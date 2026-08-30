package com.wizardg.func_au;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SlotSettings(boolean capsEnabled, boolean voidEnabled, List<Integer> caps, List<Boolean> voided) {

    public static final SlotSettings EMPTY = new SlotSettings(false, false, List.of(), List.of());

    public static final Codec<SlotSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("caps_enabled").forGetter(SlotSettings::capsEnabled),
            Codec.BOOL.fieldOf("void_enabled").forGetter(SlotSettings::voidEnabled),
            Codec.INT.listOf().fieldOf("caps").forGetter(SlotSettings::caps),
            Codec.BOOL.listOf().fieldOf("voided").forGetter(SlotSettings::voided)
    ).apply(i, SlotSettings::new));

    private static final int MAX_SLOTS = 64;

    public static final StreamCodec<ByteBuf, SlotSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SlotSettings::capsEnabled,
            ByteBufCodecs.BOOL, SlotSettings::voidEnabled,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list(MAX_SLOTS)), SlotSettings::caps,
            ByteBufCodecs.BOOL.apply(ByteBufCodecs.list(MAX_SLOTS)), SlotSettings::voided,
            SlotSettings::new);

    // 0 when this slot has no cap set, or when caps are switched off entirely.
    public int cap(int slot) {
        return capsEnabled && slot >= 0 && slot < caps.size() ? Math.max(0, caps.get(slot)) : 0;
    }

    public boolean voids(int slot) {
        return voidEnabled && slot >= 0 && slot < voided.size() && voided.get(slot);
    }

    public SlotSettings withCap(int slot, int value, int slotCount) {
        List<Integer> next = new ArrayList<>(padded(caps, slotCount, 0));
        next.set(slot, Math.max(0, value));
        return new SlotSettings(capsEnabled, voidEnabled, List.copyOf(next), voided);
    }

    public SlotSettings withVoid(int slot, boolean value, int slotCount) {
        List<Boolean> next = new ArrayList<>(padded(voided, slotCount, false));
        next.set(slot, value);
        return new SlotSettings(capsEnabled, voidEnabled, caps, List.copyOf(next));
    }

    public SlotSettings withCapsEnabled(boolean value) {
        return new SlotSettings(value, voidEnabled, caps, voided);
    }

    public SlotSettings withVoidEnabled(boolean value) {
        return new SlotSettings(capsEnabled, value, caps, voided);
    }

    private static <T> List<T> padded(List<T> source, int size, T filler) {
        List<T> out = new ArrayList<>(source);
        while (out.size() < size) {
            out.add(filler);
        }
        return out;
    }
}
