package Aru.Aru.ashvehicle.tools;

import Aru.Aru.ashvehicle.entity.vehicle.base.RemoteDroneEntity;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import net.minecraft.world.level.Level;

public class DroneFindUtil {

    public static RemoteDroneEntity findRemoteDrone(Level level, String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) return null;
        var entity = EntityFindUtil.findEntity(level, uuidString);
        return entity instanceof RemoteDroneEntity drone ? drone : null;
    }
}
