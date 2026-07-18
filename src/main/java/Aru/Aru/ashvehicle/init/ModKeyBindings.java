package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AshVehicle.MODID, value = Dist.CLIENT)
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.ashvehicle";

    public static final KeyMapping TARGETING_CAMERA = new KeyMapping(
        "key.ashvehicle.targeting_camera",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_T,
        CATEGORY
    );

    public static final KeyMapping THERMAL_TOGGLE = new KeyMapping(
        "key.ashvehicle.thermal_toggle",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        CATEGORY
    );

    public static final KeyMapping LOCK_TARGET = new KeyMapping(
        "key.ashvehicle.lock_target",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        CATEGORY
    );

    public static final KeyMapping TOGGLE_GEAR = new KeyMapping(
        "key.ashvehicle.toggle_gear",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        CATEGORY
    );

    public static final KeyMapping EXIT_DRONE = new KeyMapping(
        "key.ashvehicle.exit_drone",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        CATEGORY
    );

    public static final KeyMapping ZOOM_IN = new KeyMapping(
        "key.ashvehicle.zoom_in",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_EQUAL,
        CATEGORY
    );

    public static final KeyMapping ZOOM_OUT = new KeyMapping(
        "key.ashvehicle.zoom_out",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_MINUS,
        CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TARGETING_CAMERA);
        event.register(THERMAL_TOGGLE);
        event.register(LOCK_TARGET);
        event.register(TOGGLE_GEAR);
        event.register(EXIT_DRONE);
        event.register(ZOOM_IN);
        event.register(ZOOM_OUT);
    }
}

