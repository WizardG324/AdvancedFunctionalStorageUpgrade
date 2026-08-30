package com.wizardg.func_au;

public interface DrawerSlotAccess {

    void func_au$setSettings(SlotSettings settings);

    SlotSettings func_au$getSettings();

    // The limit the slot would have without our caps applied
    int func_au$naturalLimit(int slot);
}
