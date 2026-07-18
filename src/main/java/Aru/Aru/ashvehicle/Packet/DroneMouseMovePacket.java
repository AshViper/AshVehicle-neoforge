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

public record DroneMouseMovePacket(double speedX, double speedY) implements CustomPacketPayload {
    public static final Type<DroneMouseMovePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "drone_mouse_move"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneMouseMovePacket> STREAM_CODEC =
            StreamCodec.ofMember(DroneMouseMovePacket::write, DroneMouseMovePacket::new);

    public DroneMouseMovePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readDouble(), buf.readDouble());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(this.speedX);
        buf.writeDouble(this.speedY);
    }

    public static void handle(DroneMouseMovePacket msg, IPayloadContext context) {
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
            if (drone != null) {
                drone.mouseInput(msg.speedX, msg.speedY);
            }
        });
    }

    @Override
    public Type<DroneMouseMovePacket> type() {
        return TYPE;
    }
}
