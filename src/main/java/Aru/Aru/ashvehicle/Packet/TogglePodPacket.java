package Aru.Aru.ashvehicle.Packet;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.entity.vehicle.SapsanEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TogglePodPacket(int entityId) implements CustomPacketPayload {
    public static final Type<TogglePodPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "toggle_pod"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TogglePodPacket> STREAM_CODEC =
            StreamCodec.ofMember(TogglePodPacket::write, TogglePodPacket::new);

    public TogglePodPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
    }

    public static void handle(TogglePodPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Entity entity = player.level().getEntity(msg.entityId);
            if (entity instanceof SapsanEntity sapsan && player.getVehicle() == sapsan) {
                sapsan.togglePod();
            }
        });
    }

    @Override
    public Type<TogglePodPacket> type() {
        return TYPE;
    }
}
