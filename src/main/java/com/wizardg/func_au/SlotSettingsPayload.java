package com.wizardg.func_au;

import java.util.ArrayList;
import java.util.List;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SlotSettingsPayload(BlockPos pos, SlotSettings settings) implements CustomPacketPayload {

    public static final Type<SlotSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AdvancedFunctionStorageUpgrade.MODID, "slot_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SlotSettingsPayload::pos,
            SlotSettings.STREAM_CODEC, SlotSettingsPayload::settings,
            SlotSettingsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SlotSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            // The client picks these values, so treat them as untrusted: check reach, then clamp.
            if (!player.level().isLoaded(payload.pos()) || player.distanceToSqr(payload.pos().getCenter()) > 64) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof ControllableDrawerTile<?> tile)) {
                return;
            }
            if (!AdvancedUpgrades.isSupported(tile)) {
                return;
            }
            int slot = AdvancedUpgrades.findSlot(tile);
            if (slot < 0) {
                return;
            }
            ItemStack upgrade = tile.getUtilityUpgrades().getStackInSlot(slot);
            upgrade.set(AdvancedFunctionStorageUpgrade.SLOT_SETTINGS.get(),
                    clamp(payload.settings(), tile));
            tile.recalculateUpgrades();
            tile.markForUpdate();
        });
    }

    private static SlotSettings clamp(SlotSettings settings, ControllableDrawerTile<?> tile) {
        int slots = AdvancedUpgrades.storageSlots(tile);
        DrawerSlotAccess access = tile instanceof ItemControllableDrawerTile<?> item
                && item.getStorage() instanceof DrawerSlotAccess handler ? handler : null;

        List<Integer> caps = new ArrayList<>();
        List<Boolean> voided = new ArrayList<>();
        for (int i = 0; i < slots; i++) {
            int cap = i < settings.caps().size() ? settings.caps().get(i) : 0;
            if (cap > 0) {
                int ceiling = access == null ? 0 : access.func_au$naturalLimit(i);
                cap = ceiling > 0 ? Math.min(Math.max(1, cap), ceiling) : Math.max(1, cap);
            } else {
                cap = 0;
            }
            caps.add(cap);
            voided.add(i < settings.voided().size() && settings.voided().get(i));
        }
        return new SlotSettings(settings.capsEnabled(), settings.voidEnabled(), List.copyOf(caps), List.copyOf(voided));
    }
}
