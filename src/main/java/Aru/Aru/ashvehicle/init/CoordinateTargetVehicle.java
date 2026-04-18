package Aru.Aru.ashvehicle.init;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface CoordinateTargetVehicle {
    int getId();

    void handleCoordinateTarget(Player player, Vec3 targetPos);

    default void previewCoordinateTarget(Player player, Vec3 targetPos) {
    }

    default void clearCoordinatePreview(Player player) {
    }

    default double getCoordinateTargetMaxRange() {
        return 12000.0;
    }
}
