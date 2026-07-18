package Aru.Aru.ashvehicle.item;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JerryCanItem extends Item {
    public static final int MAX_FUEL = 1000000;

    public JerryCanItem(Properties pProperties) {
        super(pProperties.durability(MAX_FUEL).setNoRepair());
    }

    @EventBusSubscriber(modid = "ashvehicle")
    public static class EventHandler {
        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof JerryCanItem) {
                if (event.getTarget() instanceof VehicleEntity vehicle) {
                    Player player = event.getEntity();

                    if (player.isShiftKeyDown()) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);

                        if (!player.level().isClientSide) {
                            double currentEnergy = vehicle.getEnergy();
                            double maxEnergy = vehicle.getMaxEnergy();

                            if (currentEnergy < maxEnergy) {
                                int fuelInCan = stack.getMaxDamage() - stack.getDamageValue();
                                double needed = maxEnergy - currentEnergy;
                                int toTransfer = (int) Math.min(fuelInCan, needed);

                                if (toTransfer > 0) {
                                    try {
                                        ((Aru.Aru.ashvehicle.mixin.VehicleEntityAccessor) vehicle).superbwarfare$invokeSetEnergy((int)(currentEnergy + toTransfer));

                                        stack.setDamageValue(stack.getDamageValue() + toTransfer);

                                        player.displayClientMessage(Component.translatable("message.ashvehicle.refilled", toTransfer).withStyle(ChatFormatting.GREEN), true);
                                    } catch (Exception e) {
                                        player.displayClientMessage(Component.literal("燃料の補充に失敗しました。").withStyle(ChatFormatting.RED), false);
                                    }
                                }
                            } else {
                                player.displayClientMessage(Component.translatable("message.ashvehicle.already_full").withStyle(ChatFormatting.RED), true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        int fuel = pStack.getMaxDamage() - pStack.getDamageValue();
        pTooltipComponents.add(Component.translatable("tooltip.ashvehicle.jerry_can.fuel", fuel, MAX_FUEL).withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.ashvehicle.jerry_can.usage").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pIsAdvanced);
    }
}
