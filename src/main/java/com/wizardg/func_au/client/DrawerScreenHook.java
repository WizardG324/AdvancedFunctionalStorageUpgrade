package com.wizardg.func_au.client;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;

public final class DrawerScreenHook {

    private DrawerScreenHook() {
    }

    public static void addTo(ControllableDrawerTile<?> tile) {
        tile.addGuiAddonFactory(() -> new SlotConfigGuiAddon(tile));
    }
}
