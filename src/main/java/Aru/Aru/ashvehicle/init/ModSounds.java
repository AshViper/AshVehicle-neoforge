package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(Registries.SOUND_EVENT, AshVehicle.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> M1A1_ABRAMS_FIRE = register("m1a1_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> M1A1_ABRAMS_ENGINE = register("m1a1_engine");
    public static final DeferredHolder<SoundEvent, SoundEvent> M1A1_ABRAMS_RELOAD = register("m1a1_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> SU57_ENGINE = register("su57_engine");
    public static final DeferredHolder<SoundEvent, SoundEvent> J20_ENGINE = register("j-20_engine");
    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER_ENGINE = register("reaper-engine");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHEEL_STEP = register("wheel_step");
    public static final DeferredHolder<SoundEvent, SoundEvent> YX_100_VERY_FAR = register("yx_100_veryfar");
    public static final DeferredHolder<SoundEvent, SoundEvent> YX_100_RELOAD = register("yx_100_reload");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return REGISTRY.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AshVehicle.MODID, name)));
    }

    private ModSounds() {
    }
}
