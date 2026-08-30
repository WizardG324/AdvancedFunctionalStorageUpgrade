package com.wizardg.func_au;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AdvancedUpgrades {

    private AdvancedUpgrades() {
    }

    public static int findSlot(ControllableDrawerTile<?> tile) {
        InventoryComponent<?> utility = tile.getUtilityUpgrades();
        for (int i = 0; i < utility.getSlots(); i++) {
            if (utility.getStackInSlot(i).getItem() instanceof AdvancedUpgradeItem) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack find(ControllableDrawerTile<?> tile) {
        int slot = findSlot(tile);
        return slot < 0 ? ItemStack.EMPTY : tile.getUtilityUpgrades().getStackInSlot(slot);
    }

    public static boolean hasVoidUpgrade(ControllableDrawerTile<?> tile) {
        if (!Config.requireVoidUpgrade()) {
            return true;
        }
        InventoryComponent<?> utility = tile.getUtilityUpgrades();
        for (int i = 0; i < utility.getSlots(); i++) {
            if (utility.getStackInSlot(i).is(voidUpgrade())) {
                return true;
            }
        }
        return false;
    }

    // Ender Drawers? haha... no
    public static boolean isSupported(ControllableDrawerTile<?> tile) {
        return !(tile instanceof EnderDrawerTile);
    }

    // Number of item slots a drawer has.
    public static int storageSlots(ControllableDrawerTile<?> tile) {
        if (tile instanceof ItemControllableDrawerTile<?> item
                && item.getStorage() instanceof BigInventoryHandler handler) {
            return handler.getStoredStacks().size();
        }
        if (tile instanceof FluidDrawerTile fluid) {
            return fluid.getFluidHandler().getTanks();
        }
        return 0;
    }

    private static Item voidUpgrade() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("functionalstorage", "void_upgrade"));
    }

    public static SlotSettings settingsOf(ItemStack stack) {
        return stack.isEmpty()
                ? SlotSettings.EMPTY
                : stack.getOrDefault(AdvancedFunctionStorageUpgrade.SLOT_SETTINGS.get(), SlotSettings.EMPTY);
    }

    public static SlotSettings settingsIn(ControllableDrawerTile<?> tile) {
        return settingsOf(find(tile));
    }
}
