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

public record DroneInputPacket(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down)
        implements CustomPacketPayload {
    public static final Type<DroneInputPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "drone_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneInputPacket> STREAM_CODEC =
            StreamCodec.ofMember(DroneInputPacket::write, DroneInputPacket::new);

    public DroneInputPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.forward);
        buf.writeBoolean(this.backward);
        buf.writeBoolean(this.left);
        buf.writeBoolean(this.right);
        buf.writeBoolean(this.up);
        buf.writeBoolean(this.down);
    }

    public static void handle(DroneInputPacket msg, IPayloadContext context) {
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
            if (drone == null) {
                return;
            }

            drone.setForwardInputDown(msg.forward);
            drone.setBackInputDown(msg.backward);
            drone.setLeftInputDown(msg.left);
            drone.setRightInputDown(msg.right);
            drone.setUpInputDown(msg.up);
            drone.setDownInputDown(msg.down);
        });
    }

    @Override
    public Type<DroneInputPacket> type() {
        return TYPE;
    }
}
