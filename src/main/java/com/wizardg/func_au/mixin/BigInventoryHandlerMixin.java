package com.wizardg.func_au.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.wizardg.func_au.Config;
import com.wizardg.func_au.DrawerSlotAccess;
import com.wizardg.func_au.SlotSettings;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Mixin(BigInventoryHandler.class)
public abstract class BigInventoryHandlerMixin implements DrawerSlotAccess {

    @Unique
    private SlotSettings func_au$settings = SlotSettings.EMPTY;

    @Unique
    private boolean func_au$bypass;

    @Override
    public void func_au$setSettings(SlotSettings settings) {
        this.func_au$settings = settings == null ? SlotSettings.EMPTY : settings;
    }

    @Override
    public SlotSettings func_au$getSettings() {
        return this.func_au$settings;
    }

    @Override
    public int func_au$naturalLimit(int slot) {
        this.func_au$bypass = true;
        try {
            return ((BigInventoryHandler) (Object) this).getSlotLimit(slot);
        } finally {
            this.func_au$bypass = false;
        }
    }

    @ModifyReturnValue(method = "getSlotLimit(I)I", at = @At("RETURN"))
    private int func_au$capSlot(int original, int slot) {
        if (func_au$bypass) {
            return original;
        }
        int cap = func_au$settings.cap(slot);
        return cap > 0 ? Math.min(original, cap) : original;
    }

    @ModifyExpressionValue(
            method = "extractItem",
            at = @At(value = "INVOKE", target = "Lcom/buuz135/functionalstorage/inventory/BigInventoryHandler;getSlotLimit(I)I"))
    private int func_au$doNotThrottleExtraction(int original, int slot, int amount, boolean simulate) {
        return func_au$naturalLimit(slot);
    }

    @ModifyReturnValue(method = "getSlotLimit(ILnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"))
    private int func_au$capSlotFor(int original, int slot, ItemStack stack) {
        return func_au$applyCap(original, slot);
    }

    @Unique
    private int func_au$applyCap(int original, int slot) {
        if (func_au$bypass) {
            return original;
        }
        int cap = func_au$settings.cap(slot);
        if (cap <= 0) {
            return original;
        }
        return Math.min(original, Math.max(cap, func_au$storedAmount(slot)));
    }

    @Unique
    private int func_au$storedAmount(int slot) {
        var stored = ((BigInventoryHandler) (Object) this).getStoredStacks();
        return slot >= 0 && slot < stored.size() ? stored.get(slot).getAmount() : 0;
    }

    @Inject(method = "serializeNBT(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("HEAD"))
    private void func_au$repairOversizedStacks(HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
        for (BigInventoryHandler.BigStack big : ((BigInventoryHandler) (Object) this).getStoredStacks()) {
            ItemStack stack = big.getStack();
            if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
                big.setStack(stack.copyWithCount(stack.getMaxStackSize()));
            }
        }
    }

    @ModifyExpressionValue(
            method = "insertItem",
            at = @At(value = "INVOKE", target = "Lcom/buuz135/functionalstorage/inventory/BigInventoryHandler;isVoid()Z", ordinal = 1))
    private boolean func_au$voidOnlyChosenSlots(boolean original, int slot, ItemStack stack, boolean simulate) {
        if (!func_au$settings.voidEnabled()) {
            return original;
        }

        boolean allowed = !Config.requireVoidUpgrade() || original;
        return allowed && func_au$settings.voids(slot);
    }
}
