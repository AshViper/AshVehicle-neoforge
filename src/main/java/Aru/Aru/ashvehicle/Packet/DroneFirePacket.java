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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

public record DroneFirePacket(@Nullable UUID targetEntityUUID, @Nullable Vector3f targetPos) implements CustomPacketPayload {
    public static final Type<DroneFirePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "drone_fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DroneFirePacket> STREAM_CODEC =
            StreamCodec.ofMember(DroneFirePacket::write, DroneFirePacket::new);

    public DroneFirePacket(@Nullable UUID targetEntityUUID) {
        this(targetEntityUUID, null);
    }

    public DroneFirePacket(RegistryFriendlyByteBuf buf) {
        this(
                buf.readBoolean() ? buf.readUUID() : null,
                buf.readBoolean() ? buf.readVector3f() : null
        );
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.targetEntityUUID != null);
        if (this.targetEntityUUID != null) {
            buf.writeUUID(this.targetEntityUUID);
        }

        buf.writeBoolean(this.targetPos != null);
        if (this.targetPos != null) {
            buf.writeVector3f(this.targetPos);
        }
    }

    public static void handle(DroneFirePacket msg, IPayloadContext context) {
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
                Vec3 targetVec = msg.targetPos != null ? new Vec3(msg.targetPos) : null;
                drone.vehicleShoot(player, msg.targetEntityUUID, targetVec);
            }
        });
    }

    @Override
    public Type<DroneFirePacket> type() {
        return TYPE;
    }
}
