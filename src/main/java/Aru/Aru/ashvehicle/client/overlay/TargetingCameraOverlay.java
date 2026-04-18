package Aru.Aru.ashvehicle.client.overlay;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.client.screen.TargetingCameraScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TargetingCameraOverlay implements LayeredDraw.Layer {
    
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, "targeting_camera");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!TargetingCameraScreen.isActive()) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        TargetingCameraScreen.renderHUD(guiGraphics, screenWidth, screenHeight);
    }
}
