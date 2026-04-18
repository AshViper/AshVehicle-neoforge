package Aru.Aru.ashvehicle.Packet;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.init.CoordinateTargetVehicle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PreviewCoordinateTargetPacket(int entityId, boolean clear, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<PreviewCoordinateTargetPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "preview_coordinate_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PreviewCoordinateTargetPacket> STREAM_CODEC =
            StreamCodec.ofMember(PreviewCoordinateTargetPacket::write, PreviewCoordinateTargetPacket::new);

    public PreviewCoordinateTargetPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.clear);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    public static void handle(PreviewCoordinateTargetPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            Entity entity = level.getEntity(msg.entityId);
            if (!(entity instanceof CoordinateTargetVehicle targetVehicle)) {
                return;
            }

            if (msg.clear) {
                targetVehicle.clearCoordinatePreview(player);
            } else {
                targetVehicle.previewCoordinateTarget(player, new Vec3(msg.x, msg.y, msg.z));
            }
        });
    }

    @Override
    public Type<PreviewCoordinateTargetPacket> type() {
        return TYPE;
    }
}
