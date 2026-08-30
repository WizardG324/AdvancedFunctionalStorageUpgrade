package com.wizardg.func_au.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.wizardg.func_au.Config;
import com.wizardg.func_au.FluidTankAccess;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@Mixin(BigFluidHandler.CustomFluidTank.class)
public abstract class CustomFluidTankMixin implements FluidTankAccess {

    @Unique
    private boolean func_au$narrowed;

    @Unique
    private boolean func_au$voids;

    @Unique
    private boolean func_au$drawerVoids;

    @Override
    public void func_au$setVoiding(boolean narrowed, boolean voids, boolean drawerVoids) {
        this.func_au$narrowed = narrowed;
        this.func_au$voids = voids;
        this.func_au$drawerVoids = drawerVoids;
    }

    @Unique
    private boolean func_au$voidsHere() {
        if (!func_au$narrowed) {
            return func_au$drawerVoids;
        }
        boolean allowed = !Config.requireVoidUpgrade() || func_au$drawerVoids;
        return allowed && func_au$voids;
    }

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void func_au$refuseWhenOverCapacity(FluidStack resource, IFluidHandler.FluidAction action,
                                                CallbackInfoReturnable<Integer> cir) {
        BigFluidHandler.CustomFluidTank self = (BigFluidHandler.CustomFluidTank) (Object) this;

        if (self.getFluidAmount() > self.getCapacity()) {
            cir.setReturnValue(func_au$voidsHere() ? resource.getAmount() : 0);
        }
    }

    @ModifyExpressionValue(
            method = "fill",
            at = @At(value = "INVOKE",
                    target = "Lcom/buuz135/functionalstorage/fluid/BigFluidHandler;isDrawerVoid()Z"))
    private boolean func_au$voidOnlyChosenTanks(boolean original) {
        if (!func_au$narrowed) {
            return original;
        }

        boolean allowed = !Config.requireVoidUpgrade() || original;
        return allowed && func_au$voids;
    }
}
