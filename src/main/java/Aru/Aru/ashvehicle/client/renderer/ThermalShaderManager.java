package Aru.Aru.ashvehicle.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility wrapper for the F18 thermal toggle path.
 */
@OnlyIn(Dist.CLIENT)
public final class ThermalShaderManager {
    private static boolean enabled = false;

    private ThermalShaderManager() {
    }

    public static void ensureInitialized() {
        if (enabled) {
            ThermalEffectRenderer.ensureInitialized();
        }
    }

    @Nullable
    public static RenderTarget getEntityMaskTarget() {
        return enabled ? ThermalEffectRenderer.getEntityMaskTarget() : null;
    }

    public static void beginFrame() {
        // LevelRendererMixin drives the thermal pass directly.
    }

    public static void applyThermalEffect() {
        // LevelRendererMixin drives the thermal pass directly.
    }

    public static void cleanup() {
        ThermalEffectRenderer.cleanup();
    }

    public static void onResize(int width, int height) {
        ThermalEffectRenderer.onResize(width, height);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
        cleanup();
    }
}
