package Aru.Aru.ashvehicle.mixin;

import Aru.Aru.ashvehicle.entity.vehicle.base.RemoteDroneEntity;
import Aru.Aru.ashvehicle.tools.DroneFindUtil;
import Aru.Aru.ashvehicle.util.ItemStackDataUtil;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MonitorItem.class)
public class MonitorMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        String linkedDroneUUID = ItemStackDataUtil.getString(itemstack, MonitorItem.LINKED_DRONE);
        if (linkedDroneUUID != null && !linkedDroneUUID.isEmpty() && !linkedDroneUUID.equals("none")) {
            RemoteDroneEntity remoteDrone = DroneFindUtil.findRemoteDrone(entity.level(), linkedDroneUUID);
            if (remoteDrone != null) {
                ci.cancel();
            }
        }
    }
}
