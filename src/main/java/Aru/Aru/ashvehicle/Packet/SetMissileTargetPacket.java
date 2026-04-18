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

public record SetMissileTargetPacket(int entityId, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SetMissileTargetPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "set_missile_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMissileTargetPacket> STREAM_CODEC =
            StreamCodec.ofMember(SetMissileTargetPacket::write, SetMissileTargetPacket::new);

    public SetMissileTargetPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    public static void handle(SetMissileTargetPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            Entity entity = level.getEntity(msg.entityId);
            if (entity instanceof CoordinateTargetVehicle targetVehicle) {
                targetVehicle.handleCoordinateTarget(player, new Vec3(msg.x, msg.y, msg.z));
            }
        });
    }

    @Override
    public Type<SetMissileTargetPacket> type() {
        return TYPE;
    }
}
