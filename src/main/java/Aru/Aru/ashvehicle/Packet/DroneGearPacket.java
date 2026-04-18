package Aru.Aru.ashvehicle.Packet;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.vehicle.base.RemoteDroneEntity;
import Aru.Aru.ashvehicle.tools.DroneFindUtil;
import Aru.Aru.ashvehicle.util.ItemStackDataUtil;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.Monitor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DroneGearPacket() implements CustomPacketPayload {
    public static final Type<DroneGearPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "drone_gear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneGearPacket> STREAM_CODEC =
            StreamCodec.ofMember(DroneGearPacket::write, DroneGearPacket::new);

    public DroneGearPacket(RegistryFriendlyByteBuf buf) {
        this();
    }

    private void write(RegistryFriendlyByteBuf buf) {
    }

    public static void handle(DroneGearPacket msg, IPayloadContext context) {
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

            String droneUUID = ItemStackDataUtil.getString(stack, Monitor.LINKED_DRONE);
            RemoteDroneEntity drone = DroneFindUtil.findRemoteDrone(player.level(), droneUUID);
            if (drone != null) {
                drone.toggleGear();
            }
        });
    }

    @Override
    public Type<DroneGearPacket> type() {
        return TYPE;
    }
}
