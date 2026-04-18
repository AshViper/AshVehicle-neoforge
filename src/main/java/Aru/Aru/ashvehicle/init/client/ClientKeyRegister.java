package Aru.Aru.ashvehicle.init.client;

import Aru.Aru.ashvehicle.AshVehicle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = AshVehicle.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientKeyRegister {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ClientKeyMappings.register(event);
    }
}

