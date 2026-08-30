package com.wizardg.func_au.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.wizardg.func_au.client.DrawerScreenHook;

@Mixin({DrawerTile.class, FluidDrawerTile.class})
public abstract class DrawerTileMixin {

    @Inject(method = "initClient", at = @At("TAIL"))
    private void func_au$addConfigButton(CallbackInfo ci) {
        DrawerScreenHook.addTo((ControllableDrawerTile<?>) (Object) this);
    }
}
