package Aru.Aru.ashvehicle.client.event;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.client.screen.TargetingCameraScreen;
import Aru.Aru.ashvehicle.entity.vehicle.base.RemoteDroneEntity;
import com.atsuishio.superbwarfare.client.MouseMovementHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AshVehicle.MODID, value = Dist.CLIENT)
public class DroneMouseHandler {

    private static Vec2 posO = new Vec2(0, 0);
    private static Vec2 posN = new Vec2(0, 0);

    private static boolean notInGame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return true;
        if (mc.getOverlay() != null) return true;
        if (mc.screen != null) return true;
        if (!mc.mouseHandler.isMouseGrabbed()) return true;
        return !mc.isWindowActive();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        posO = posN;
        posN = MouseMovementHandler.INSTANCE.getMousePos();

        RemoteDroneEntity drone = DroneControlHandler.getControlledDrone(player);
        if (drone == null) return;

        if (notInGame()) return;

        float zoom = TargetingCameraScreen.isActive() ? TargetingCameraScreen.getCurrentZoom() : 1.0f;
        double sensitivity = 0.15;
        double speedX = (sensitivity / zoom) * (posN.x - posO.x);
        double speedY = (sensitivity / zoom) * (posN.y - posO.y);

        if (TargetingCameraScreen.isActive()) {
            TargetingCameraScreen.handleRawMouseInput(speedX * 15.0, speedY * 15.0);
        }
    }
}

