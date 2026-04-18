package Aru.Aru.ashvehicle.Packet;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.init.event.ClientRenderEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NukeSkyPacket(double x, double y, double z, float radius, int durationTicks) implements CustomPacketPayload {
    public static final Type<NukeSkyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "nuke_sky"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NukeSkyPacket> STREAM_CODEC =
            StreamCodec.ofMember(NukeSkyPacket::write, NukeSkyPacket::new);

    public NukeSkyPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.radius);
        buf.writeVarInt(this.durationTicks);
    }

    public static void handle(NukeSkyPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> ClientRenderEvents.activateNukeSky(
                new Vec3(msg.x, msg.y, msg.z),
                msg.radius,
                msg.durationTicks
        ));
    }

    @Override
    public Type<NukeSkyPacket> type() {
        return TYPE;
    }
}
