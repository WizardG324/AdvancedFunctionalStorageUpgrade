package com.wizardg.func_au.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.buuz135.functionalstorage.client.gui.FluidDrawerInfoGuiAddon;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.wizardg.func_au.FluidDrawerAccess;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@Mixin(FluidDrawerInfoGuiAddon.class)
public abstract class FluidDrawerInfoGuiAddonMixin {

    @Shadow
    @Final
    private Supplier<BigFluidHandler> fluidHandlerSupplier;

    @ModifyArg(
            method = "drawForegroundLayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip("
                            + "Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"),
            index = 1,
            require = 0)
    private List<Component> func_au$addSlotCap(List<Component> lines, @Local(index = 9) int tank) {
        BigFluidHandler handler = fluidHandlerSupplier.get();
        if (!(handler instanceof FluidDrawerAccess access)) {
            return lines;
        }
        int cap = access.func_au$capFor(tank);
        if (cap <= 0) {
            return lines;
        }
        List<Component> withCap = new ArrayList<>(lines);
        withCap.add(Component.translatable("func_au.gui.slot_cap").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(NumberUtils.getFormattedFluid(cap)).withStyle(ChatFormatting.WHITE)));
        return withCap;
    }
}
