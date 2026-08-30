package com.wizardg.func_au.client;

import com.wizardg.func_au.AdvancedFunctionStorageUpgrade;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = AdvancedFunctionStorageUpgrade.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    static void onScreenClosing(ScreenEvent.Closing event) {
        SlotConfigGuiAddon.commitActive();
    }

    @SubscribeEvent
    static void onTooltip(RenderTooltipEvent.Pre event) {
        if (SlotConfigGuiAddon.isSwallowingTooltips()) {
            event.setCanceled(true);
        }
    }
}
