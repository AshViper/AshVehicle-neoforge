package Aru.Aru.ashvehicle;

import Aru.Aru.ashvehicle.init.ModEntities;
import Aru.Aru.ashvehicle.init.ModItem;
import Aru.Aru.ashvehicle.init.ModNetwork;
import Aru.Aru.ashvehicle.init.ModParticleTypes;
import Aru.Aru.ashvehicle.init.ModSounds;
import Aru.Aru.ashvehicle.init.ModTabs;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(AshVehicle.MODID)
public class AshVehicle {
    public static final String MODID = "ashvehicle";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AshVehicle(IEventBus bus) {
        ModEntities.REGISTRY.register(bus);
        ModTabs.TABS.register(bus);
        ModSounds.REGISTRY.register(bus);
        ModItem.ITEMS.register(bus);
        ModParticleTypes.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(ModNetwork::register);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("common setup");
    }
}
