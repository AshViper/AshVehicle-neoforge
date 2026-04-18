package Aru.Aru.ashvehicle.client.event;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.Packet.DroneExitPacket;
import Aru.Aru.ashvehicle.Packet.DroneFirePacket;
import Aru.Aru.ashvehicle.Packet.DroneGearPacket;
import Aru.Aru.ashvehicle.Packet.DroneInputPacket;
import Aru.Aru.ashvehicle.client.screen.TargetingCameraScreen;
import Aru.Aru.ashvehicle.entity.vehicle.base.RemoteDroneEntity;
import Aru.Aru.ashvehicle.init.ModKeyBindings;
import Aru.Aru.ashvehicle.init.ModNetwork;
import Aru.Aru.ashvehicle.tools.DroneFindUtil;
import Aru.Aru.ashvehicle.util.ItemStackDataUtil;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.Monitor;
import com.atsuishio.superbwarfare.tools.TraceTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AshVehicle.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class DroneControlHandler {

    private static boolean lastForward = false;
    private static boolean lastBackward = false;
    private static boolean lastLeft = false;
    private static boolean lastRight = false;
    private static boolean lastUp = false;
    private static boolean lastDown = false;
    
    private static int fireTickCounter = 0;
    
    private static boolean lastTargetingKey = false;
    private static boolean lastThermalKey = false;
    private static boolean lastLockKey = false;
    private static boolean lastGearKey = false;
    private static boolean lastExitKey = false;
    private static boolean lastZoomInKey = false;
    private static boolean lastZoomOutKey = false;

    private static boolean notInGame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return true;
        if (mc.getOverlay() != null) return true;
        if (mc.screen != null) return true;
        if (!mc.mouseHandler.isMouseGrabbed()) return true;
        return !mc.isWindowActive();
    }

    public static RemoteDroneEntity getControlledDrone(LocalPlayer player) {
        if (player == null) return null;
        
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) return null;
        if (!ItemStackDataUtil.getBoolean(stack, "Using")) return null;
        if (!ItemStackDataUtil.getBoolean(stack, "Linked")) return null;
        
        String droneUUID = ItemStackDataUtil.getString(stack, Monitor.LINKED_DRONE);
        return DroneFindUtil.findRemoteDrone(player.level(), droneUUID);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null) return;
        
        RemoteDroneEntity drone = getControlledDrone(player);
        if (drone == null) {
            if (lastForward || lastBackward || lastLeft || lastRight || lastUp || lastDown) {
                sendInputPacket(false, false, false, false, false, false);
                resetInputState();
            }
            if (TargetingCameraScreen.isActive()) {
                TargetingCameraScreen.close();
            }
            return;
        }

        ensureTargetingCamera(drone);

        if (notInGame()) {
            sendInputPacket(false, false, false, false, false, false);
            resetInputState();
            return;
        }

        handleCameraKeys(drone);
        handleGearKey();
        handleExitKey();
        
        TargetingCameraScreen.tick();

        boolean forward = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean up = mc.options.keyJump.isDown();
        boolean down = mc.options.keyShift.isDown();

        sendInputPacket(forward, backward, left, right, up, down);
        
        lastForward = forward;
        lastBackward = backward;
        lastLeft = left;
        lastRight = right;
        lastUp = up;
        lastDown = down;

        handleFiring(mc, player, drone);
    }

    private static void ensureTargetingCamera(RemoteDroneEntity drone) {
        if (!TargetingCameraScreen.isActive() || TargetingCameraScreen.getDrone() != drone) {
            TargetingCameraScreen.open(drone);
        }
    }

    private static void handleFiring(Minecraft mc, LocalPlayer player, RemoteDroneEntity drone) {
        boolean attackKeyDown = mc.options.keyAttack.isDown();
        
        if (attackKeyDown) {
            fireTickCounter++;
            
            if (fireTickCounter >= 2) {
                fireTickCounter = 0;
                
                Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 lookVec = drone.getViewVector(1.0f);
                
                java.util.UUID lockedTargetUUID = TargetingCameraScreen.getLockedTargetUUID();
                
                Entity targetEntity = null;
                if (lockedTargetUUID == null) {
                    targetEntity = TraceTool.droneFindLookingEntity(drone, cameraPos, 512, 1.0f);
                }
                
                BlockHitResult blockResult = drone.level().clip(new ClipContext(
                    cameraPos,
                    cameraPos.add(lookVec.scale(512)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    drone
                ));
                
                Vector3f targetPos = null;
                if (blockResult != null) {
                    Vec3 hitPos = blockResult.getLocation();
                    targetPos = new Vector3f((float) hitPos.x, (float) hitPos.y, (float) hitPos.z);
                }
                
                java.util.UUID finalTargetUUID = lockedTargetUUID != null ? lockedTargetUUID : 
                    (targetEntity != null ? targetEntity.getUUID() : null);
                
                ModNetwork.INSTANCE.sendToServer(new DroneFirePacket(finalTargetUUID, targetPos));
            }
        } else {
            fireTickCounter = 0;
        }
    }

    private static void sendInputPacket(boolean forward, boolean backward, 
                                        boolean left, boolean right, 
                                        boolean up, boolean down) {
        ModNetwork.INSTANCE.sendToServer(
            new DroneInputPacket(forward, backward, left, right, up, down)
        );
    }

    private static void resetInputState() {
        lastForward = false;
        lastBackward = false;
        lastLeft = false;
        lastRight = false;
        lastUp = false;
        lastDown = false;
    }

    private static void handleCameraKeys(RemoteDroneEntity drone) {
        boolean targetingKey = ModKeyBindings.TARGETING_CAMERA.isDown();
        if (targetingKey && !lastTargetingKey) {
            TargetingCameraScreen.toggle(drone);
        }
        lastTargetingKey = targetingKey;

        if (TargetingCameraScreen.isActive()) {
            boolean thermalKey = ModKeyBindings.THERMAL_TOGGLE.isDown();
            if (thermalKey && !lastThermalKey) {
                TargetingCameraScreen.toggleThermal();
            }
            lastThermalKey = thermalKey;

            boolean lockKey = ModKeyBindings.LOCK_TARGET.isDown();
            if (lockKey && !lastLockKey) {
                TargetingCameraScreen.toggleLock();
            }
            lastLockKey = lockKey;

            boolean zoomInKey = ModKeyBindings.ZOOM_IN.isDown();
            if (zoomInKey && !lastZoomInKey) {
                TargetingCameraScreen.zoomIn();
            }
            lastZoomInKey = zoomInKey;

            boolean zoomOutKey = ModKeyBindings.ZOOM_OUT.isDown();
            if (zoomOutKey && !lastZoomOutKey) {
                TargetingCameraScreen.zoomOut();
            }
            lastZoomOutKey = zoomOutKey;
        } else {
            lastThermalKey = false;
            lastLockKey = false;
            lastZoomInKey = false;
            lastZoomOutKey = false;
        }
    }

    private static void handleGearKey() {
        boolean gearKey = ModKeyBindings.TOGGLE_GEAR.isDown();
        if (gearKey && !lastGearKey) {
            ModNetwork.INSTANCE.sendToServer(new DroneGearPacket());
        }
        lastGearKey = gearKey;
    }

    private static void handleExitKey() {
        boolean exitKey = ModKeyBindings.EXIT_DRONE.isDown();
        if (exitKey && !lastExitKey) {
            ModNetwork.INSTANCE.sendToServer(new DroneExitPacket());
            TargetingCameraScreen.close();
        }
        lastExitKey = exitKey;
    }
}

