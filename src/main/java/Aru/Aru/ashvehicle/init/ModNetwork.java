package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.Packet.DroneExitPacket;
import Aru.Aru.ashvehicle.Packet.DroneFirePacket;
import Aru.Aru.ashvehicle.Packet.DroneGearPacket;
import Aru.Aru.ashvehicle.Packet.DroneInputPacket;
import Aru.Aru.ashvehicle.Packet.DroneMouseMovePacket;
import Aru.Aru.ashvehicle.Packet.NukeSkyPacket;
import Aru.Aru.ashvehicle.Packet.PreviewCoordinateTargetPacket;
import Aru.Aru.ashvehicle.Packet.SetMissileTargetPacket;
import Aru.Aru.ashvehicle.Packet.TogglePodPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    public static final ModNetwork INSTANCE = new ModNetwork();
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(SetMissileTargetPacket.TYPE, SetMissileTargetPacket.STREAM_CODEC, SetMissileTargetPacket::handle);
        registrar.playToServer(PreviewCoordinateTargetPacket.TYPE, PreviewCoordinateTargetPacket.STREAM_CODEC, PreviewCoordinateTargetPacket::handle);
        registrar.playToServer(TogglePodPacket.TYPE, TogglePodPacket.STREAM_CODEC, TogglePodPacket::handle);
        registrar.playToClient(NukeSkyPacket.TYPE, NukeSkyPacket.STREAM_CODEC, NukeSkyPacket::handle);
        registrar.playToServer(DroneMouseMovePacket.TYPE, DroneMouseMovePacket.STREAM_CODEC, DroneMouseMovePacket::handle);
        registrar.playToServer(DroneInputPacket.TYPE, DroneInputPacket.STREAM_CODEC, DroneInputPacket::handle);
        registrar.playToServer(DroneFirePacket.TYPE, DroneFirePacket.STREAM_CODEC, DroneFirePacket::handle);
        registrar.playToServer(DroneGearPacket.TYPE, DroneGearPacket.STREAM_CODEC, DroneGearPacket::handle);
        registrar.playToServer(DroneExitPacket.TYPE, DroneExitPacket.STREAM_CODEC, DroneExitPacket::handle);
    }

    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public void sendToPlayersNear(ServerLevel level, double x, double y, double z, double radius, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, payload);
    }
}
