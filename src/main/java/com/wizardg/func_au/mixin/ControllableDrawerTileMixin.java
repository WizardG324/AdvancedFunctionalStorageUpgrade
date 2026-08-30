package com.wizardg.func_au.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.wizardg.func_au.AdvancedUpgrades;
import com.wizardg.func_au.DrawerSlotAccess;
import com.wizardg.func_au.FluidDrawerAccess;
import com.wizardg.func_au.SlotSettings;

@Mixin(ControllableDrawerTile.class)
public abstract class ControllableDrawerTileMixin {

    @Shadow
    private boolean isCreative;

    @Inject(method = "recalculateUpgrades", at = @At("TAIL"))
    private void func_au$pushSettings(CallbackInfo ci) {
        ControllableDrawerTile<?> self = (ControllableDrawerTile<?>) (Object) this;
        if (self instanceof FluidDrawerTile fluid
                && fluid.getFluidHandler() instanceof FluidDrawerAccess fluidAccess) {
            fluidAccess.func_au$setSettings(func_au$settingsFor(self));
            fluidAccess.func_au$applyCaps();
            return;
        }
        if (self instanceof ItemControllableDrawerTile<?> tile
                && tile.getStorage() instanceof DrawerSlotAccess access) {

            access.func_au$setSettings(func_au$settingsFor(self));
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void func_au$recalculateAfterLoad(CallbackInfo ci) {
        ((ControllableDrawerTile<?>) (Object) this).recalculateUpgrades();
    }

    @Unique
    private SlotSettings func_au$settingsFor(ControllableDrawerTile<?> tile) {
        return this.isCreative || !AdvancedUpgrades.isSupported(tile)
                ? SlotSettings.EMPTY
                : AdvancedUpgrades.settingsIn(tile);
    }
}
