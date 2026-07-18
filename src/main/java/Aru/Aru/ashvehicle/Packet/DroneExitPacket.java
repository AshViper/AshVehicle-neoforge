package Aru.Aru.ashvehicle.Packet;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.vehicle.base.RemoteDroneEntity;
import Aru.Aru.ashvehicle.tools.DroneFindUtil;
import Aru.Aru.ashvehicle.util.ItemStackDataUtil;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DroneExitPacket() implements CustomPacketPayload {
    public static final Type<DroneExitPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "drone_exit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneExitPacket> STREAM_CODEC =
            StreamCodec.ofMember(DroneExitPacket::write, DroneExitPacket::new);

    public DroneExitPacket(RegistryFriendlyByteBuf buf) {
        this();
    }

    private void write(RegistryFriendlyByteBuf buf) {
    }

    public static void handle(DroneExitPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())
                    || !ItemStackDataUtil.getBoolean(stack, "Using")
                    || !ItemStackDataUtil.getBoolean(stack, "Linked")) {
                return;
            }

            String droneUUID = ItemStackDataUtil.getString(stack, MonitorItem.LINKED_DRONE);
            RemoteDroneEntity drone = DroneFindUtil.findRemoteDrone(player.level(), droneUUID);
            ItemStackDataUtil.putBoolean(stack, "Using", false);

            if (drone != null && drone.hasPassenger(player)) {
                drone.ejectController(player);
            }
        });
    }

    @Override
    public Type<DroneExitPacket> type() {
        return TYPE;
    }
}
