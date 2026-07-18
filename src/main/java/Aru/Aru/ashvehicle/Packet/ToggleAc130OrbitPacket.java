package Aru.Aru.ashvehicle.Packet;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.vehicle.Ac130uEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleAc130OrbitPacket(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleAc130OrbitPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "toggle_ac130_orbit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAc130OrbitPacket> STREAM_CODEC =
            StreamCodec.ofMember(ToggleAc130OrbitPacket::encode, ToggleAc130OrbitPacket::decode);

    public static void encode(ToggleAc130OrbitPacket msg, RegistryFriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static ToggleAc130OrbitPacket decode(RegistryFriendlyByteBuf buf) {
        return new ToggleAc130OrbitPacket(buf.readInt());
    }

    public static void handle(final ToggleAc130OrbitPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(msg.entityId);
                if (entity instanceof Ac130uEntity ac130) {
                    if (ac130.isOrbiting()) {
                        ac130.stopOrbit();
                    } else {
                        ac130.startOrbit(player.position(), 150);
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
