package com.wizardg.func_au;

public interface FluidDrawerAccess {

    void func_au$setSettings(SlotSettings settings);

    // The capacity Functional Storage last handed the handler
    int func_au$naturalCapacity();

    // Narrow each tank to its cap. Safe to call repeatedly
    void func_au$applyCaps();

    // The configured cap for one tank in mB, or 0 when the tank has none
    int func_au$capFor(int tank);
}
