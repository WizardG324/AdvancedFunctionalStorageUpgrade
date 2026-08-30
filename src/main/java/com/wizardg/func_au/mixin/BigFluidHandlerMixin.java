package com.wizardg.func_au.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.wizardg.func_au.FluidDrawerAccess;
import com.wizardg.func_au.FluidTankAccess;
import com.wizardg.func_au.SlotSettings;

@Mixin(BigFluidHandler.class)
public abstract class BigFluidHandlerMixin implements FluidDrawerAccess {

    @Shadow
    private int capacity;

    @Shadow
    public abstract BigFluidHandler.CustomFluidTank[] getTankList();

    @Shadow
    public abstract boolean isDrawerVoid();

    @Shadow
    public abstract net.neoforged.neoforge.fluids.FluidStack getFluidInTank(int tank);

    @Unique
    private SlotSettings func_au$settings = SlotSettings.EMPTY;

    @Unique
    private boolean func_au$applying;

    @Override
    public void func_au$setSettings(SlotSettings settings) {
        this.func_au$settings = settings == null ? SlotSettings.EMPTY : settings;
    }

    @Override
    public int func_au$naturalCapacity() {
        return capacity;
    }

    @Override
    public int func_au$capFor(int tank) {
        return func_au$settings.cap(tank);
    }

    @Override
    public void func_au$applyCaps() {
        if (func_au$applying) {
            return;
        }
        func_au$applying = true;
        try {
            BigFluidHandler.CustomFluidTank[] tanks = getTankList();
            for (int i = 0; i < tanks.length; i++) {
                int cap = func_au$settings.cap(i);

                tanks[i].setCapacity(cap > 0 ? Math.min(capacity, cap) : capacity);

                ((FluidTankAccess) tanks[i]).func_au$setVoiding(
                        func_au$settings.voidEnabled(), func_au$settings.voids(i), isDrawerVoid());
            }
        } finally {
            func_au$applying = false;
        }
    }

    @ModifyReturnValue(method = "getTankCapacity", at = @At("RETURN"))
    private int func_au$reportAtLeastContents(int original, int tank) {
        return Math.max(original, getFluidInTank(tank).getAmount());
    }

    @Inject(method = "setCapacity", at = @At("TAIL"))
    private void func_au$reapplyCaps(int newCapacity, CallbackInfo ci) {
        func_au$applyCaps();
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"))
    private void func_au$reapplyAfterLoad(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo ci) {
        func_au$applyCaps();
    }
}
