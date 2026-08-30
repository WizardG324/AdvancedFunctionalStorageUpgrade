package com.wizardg.func_au;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AdvancedFunctionStorageUpgrade.MODID)
public class AdvancedFunctionStorageUpgrade {

    public static final String MODID = "func_au";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    // Extending FS's UpgradeItem puts this in their creative tab already
    public static final DeferredItem<Item> ADVANCED_UPGRADE = ITEMS.register("advanced_upgrade", AdvancedUpgradeItem::new);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SlotSettings>> SLOT_SETTINGS =
            COMPONENTS.register("slot_settings", () -> DataComponentType.<SlotSettings>builder()
                    .persistent(SlotSettings.CODEC)
                    .networkSynchronized(SlotSettings.STREAM_CODEC)
                    .build());

    public AdvancedFunctionStorageUpgrade(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SlotSettingsPayload.TYPE, SlotSettingsPayload.STREAM_CODEC,
                SlotSettingsPayload::handle);
    }
}
