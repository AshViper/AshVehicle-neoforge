package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.client.overlay.TargetingCameraOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Client event registration for AshVehicle
 */
@EventBusSubscriber(modid = AshVehicle.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        // Register Targeting Camera overlay (above UCAV HUD)
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, TargetingCameraOverlay.ID, new TargetingCameraOverlay());
    }
}

