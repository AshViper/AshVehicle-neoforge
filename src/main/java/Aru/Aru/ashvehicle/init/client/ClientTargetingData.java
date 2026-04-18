package Aru.Aru.ashvehicle.init.client;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientTargetingData {
    private static final List<Entity> lockedTargets = new ArrayList<>();

    /** и¤‡ж•°г‚їгѓјг‚Ігѓѓгѓ€г‚’гѓ­гѓѓг‚Ї */
    public static void setLockedTargets(List<Entity> entities) {
        lockedTargets.clear();
        if (entities != null) {
            lockedTargets.addAll(entities);
        }
    }

    /** еЌдёЂг‚їгѓјг‚Ігѓѓгѓ€г‚’гѓ­гѓѓг‚Їпј€и¤‡ж•°еЇѕеїњгЃЁзµ±дёЂпј‰ */
    public static void setLockedTarget(@Nullable Entity entity) {
        if (entity != null) {
            setLockedTargets(List.of(entity));
        } else {
            clearLockedTargets();
        }
    }

    /** гѓ­гѓѓг‚Їи§Јй™¤ */
    public static void clearLockedTargets() {
        lockedTargets.clear();
    }

    /** еЌдёЂгѓ­гѓѓг‚ЇеЏ–еѕ—пјљгѓЄг‚№гѓ€гЃ®жњЂе€ќг‚’иї”гЃ™ */
    @Nullable
    public static Entity getLockedTarget() {
        return lockedTargets.isEmpty() ? null : lockedTargets.get(0);
    }

    /** и¤‡ж•°гѓ­гѓѓг‚ЇеЏ–еѕ—пј€иЄ­гЃїеЏ–г‚Ље°‚з”Ёпј‰ */
    public static List<Entity> getLockedTargets() {
        return Collections.unmodifiableList(lockedTargets);
    }

    /** еЌдёЂгѓ­гѓѓг‚Їи§Јй™¤пј€гѓЎг‚Ѕгѓѓгѓ‰дє’жЏ›пј‰ */
    public static void clearLockedTarget() {
        clearLockedTargets();
    }
}