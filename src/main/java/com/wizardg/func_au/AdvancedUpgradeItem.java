package com.wizardg.func_au;

import com.buuz135.functionalstorage.item.UpgradeItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// Extends Functional Storage's own upgrade item so the drawer's utility slots accept it
public class AdvancedUpgradeItem extends UpgradeItem {

    public AdvancedUpgradeItem() {
        super(new Item.Properties().stacksTo(1), UpgradeItem.Type.UTILITY);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("func_au.tooltip.advanced_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("func_au.tooltip.no_ender").withStyle(ChatFormatting.DARK_GRAY));
        SlotSettings settings = stack.getOrDefault(AdvancedFunctionStorageUpgrade.SLOT_SETTINGS.get(), SlotSettings.EMPTY);
        if (settings.capsEnabled()) {
            tooltip.add(Component.translatable("func_au.tooltip.caps_on").withStyle(ChatFormatting.DARK_AQUA));
        }
        if (settings.voidEnabled()) {
            tooltip.add(Component.translatable("func_au.tooltip.void_on").withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
