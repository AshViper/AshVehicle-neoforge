package Aru.Aru.ashvehicle.init;

import com.atsuishio.superbwarfare.item.common.container.ContainerBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import Aru.Aru.ashvehicle.AshVehicle;

@SuppressWarnings("unused")
public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AshVehicle.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKTANK_TAB = TABS.register("ash-tank",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.ashvehicle.ash-tank"))
                    .icon(() -> new ItemStack(ModItem.ASHVEHICLE_TANK_ICON.get()))
                    .displayItems((param, output) -> {
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.T_90.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.GEPARD_1A2.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.M3A3_BRADLEY.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.TOS.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.SAPSAN_GRIM2.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.PANTSIR_S1.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.KV_2.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.M1A1_ABRAMS.get()));
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKSHIP_TAB = TABS.register("ash-ship",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.ashvehicle.ash-ship"))
                    .icon(() -> new ItemStack(ModItem.ASHVEHICLE_SHIP_ICON.get()))
                    .displayItems((param, output) -> {
                        output.accept(ContainerBlockItem.createInstance(ModEntities.RUBBER_BOAT.get()));
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKAIR_TAB = TABS.register("ash-air",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.ashvehicle.ash-air"))
                    .icon(() -> new ItemStack(ModItem.ASHVEHICLE_AIR_ICON.get()))
                    .displayItems((param, output) -> {
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.UH_60.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.MH_60M.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.MIG_29.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_4.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_14.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_16.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_15.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.SU_33.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.SU_25.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_39E.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.SU_34.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_35B.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_35A.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.B_2.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_22.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_18.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_117.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.SU_57.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.V_22.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.F_2.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.SU_27.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.J_20.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.EuroFighter.get()));
                        //output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.REAPER.get())); need to fix
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.YF_23.get()));
                        //output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.X_47B.get())); need to fix
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.RAH_66.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.AH_64.get()));
                        output.accept(ContainerBlockItem.createInstance((EntityType) ModEntities.ZELENSKY.get()));
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB = TABS.register("ash-item",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.ashvehicle.ash-item"))
                    .icon(() -> new ItemStack(ModItem.ASHVEHICLE_ITEM_ICON.get()))
                    .displayItems((params, output) -> {
                        ModItem.ITEMS.getEntries().stream()
                                .filter(e -> !e.getId().getPath().contains("ashvehicle"))
                                .forEach(e -> output.accept(e.get()));
                    })
                    .build()
    );
}
