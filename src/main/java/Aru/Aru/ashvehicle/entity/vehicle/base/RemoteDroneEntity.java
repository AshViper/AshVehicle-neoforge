package Aru.Aru.ashvehicle.entity.vehicle.base;

import Aru.Aru.ashvehicle.util.ItemStackDataUtil;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.atsuishio.superbwarfare.tools.VectorTool;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class RemoteDroneEntity extends GeoVehicleEntity {

    public static final EntityDataAccessor<Boolean> LINKED = SynchedEntityData.defineId(RemoteDroneEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> CONTROLLER = SynchedEntityData.defineId(RemoteDroneEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> GEAR_DOWN = SynchedEntityData.defineId(RemoteDroneEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> OPERATOR_X = SynchedEntityData.defineId(RemoteDroneEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> OPERATOR_Y = SynchedEntityData.defineId(RemoteDroneEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> OPERATOR_Z = SynchedEntityData.defineId(RemoteDroneEntity.class, EntityDataSerializers.FLOAT);

    public double lastTickSpeed;
    private boolean allowDismount = false;

    public RemoteDroneEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LINKED, false);
        builder.define(CONTROLLER, "");
        builder.define(GEAR_DOWN, true);
        builder.define(OPERATOR_X, 0f);
        builder.define(OPERATOR_Y, 0f);
        builder.define(OPERATOR_Z, 0f);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Linked", this.entityData.get(LINKED));
        compound.putString("Controller", this.entityData.get(CONTROLLER));
        compound.putBoolean("GearDown", this.entityData.get(GEAR_DOWN));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Linked")) this.entityData.set(LINKED, compound.getBoolean("Linked"));
        if (compound.contains("Controller")) this.entityData.set(CONTROLLER, compound.getString("Controller"));
        if (compound.contains("GearDown")) this.entityData.set(GEAR_DOWN, compound.getBoolean("GearDown"));
    }

    @Override
    public void mouseInput(double x, double y) {
    }

    @Override
    public void baseTick() {
        super.baseTick();
        lastTickSpeed = this.getDeltaMovement().length();

        if (!this.level().isClientSide && this.entityData.get(LINKED)) {
            Player controller = getController();
            if (controller != null) {
                ItemStack stack = controller.getMainHandItem();
                boolean isUsing = stack.is(ModItems.MONITOR.get()) 
                    && ItemStackDataUtil.getBoolean(stack, "Using")
                    && ItemStackDataUtil.getBoolean(stack, "Linked")
                    && ItemStackDataUtil.getString(stack, MonitorItem.LINKED_DRONE).equals(this.getStringUUID());
                
                if (isUsing && !this.hasPassenger(controller)) {
                    this.entityData.set(OPERATOR_X, (float) controller.getX());
                    this.entityData.set(OPERATOR_Y, (float) controller.getY());
                    this.entityData.set(OPERATOR_Z, (float) controller.getZ());
                    controller.startRiding(this, true);
                }
                
                if (!isUsing && this.hasPassenger(controller)) {
                    ejectController(controller);
                }
            }
        }
        
        this.refreshDimensions();
    }

    public void ejectController(Player controller) {
        allowDismount = true;
        controller.stopRiding();
        allowDismount = false;
        double x = this.entityData.get(OPERATOR_X);
        double y = this.entityData.get(OPERATOR_Y);
        double z = this.entityData.get(OPERATOR_Z);
        if (x != 0 || y != 0 || z != 0) {
            controller.teleportTo(x, y, z);
        }
        ItemStack stack = controller.getMainHandItem();
        if (stack.is(ModItems.MONITOR.get())) {
            ItemStackDataUtil.putBoolean(stack, "Using", false);
        }
    }

    public Player getController() {
        String uuid = this.entityData.get(CONTROLLER);
        if (uuid == null || uuid.isEmpty()) return null;
        return EntityFindUtil.findPlayer(this.level(), uuid);
    }

    public boolean isLinked() {
        return this.entityData.get(LINKED);
    }
    
    public boolean isGearDown() {
        return this.entityData.get(GEAR_DOWN);
    }
    
    public void toggleGear() {
        this.entityData.set(GEAR_DOWN, !this.entityData.get(GEAR_DOWN));
    }

    public Vec3 getOperatorPosition() {
        return new Vec3(
            this.entityData.get(OPERATOR_X),
            this.entityData.get(OPERATOR_Y),
            this.entityData.get(OPERATOR_Z)
        );
    }


    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        
        ItemStack stack = player.getMainHandItem();
        
        if (stack.is(ModItems.MONITOR.get())) {
            if (!player.isCrouching()) {
                return linkDrone(player, stack);
            } else {
                return unlinkDrone(player, stack);
            }
        }
        
        return InteractionResult.PASS;
    }

    private InteractionResult linkDrone(Player player, ItemStack stack) {
        if (this.entityData.get(LINKED)) {
            player.displayClientMessage(Component.translatable("tips.superbwarfare.drone.already_linked")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        
        if (ItemStackDataUtil.getBoolean(stack, "Linked")) {
            player.displayClientMessage(Component.translatable("tips.superbwarfare.monitor.already_linked")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        this.entityData.set(LINKED, true);
        this.entityData.set(CONTROLLER, player.getStringUUID());
        ItemStackDataUtil.updateTag(stack, tag -> MonitorItem.link(tag, this.getStringUUID()));
        
        player.displayClientMessage(Component.translatable("tips.superbwarfare.monitor.linked")
            .withStyle(ChatFormatting.GREEN), true);

        if (player instanceof ServerPlayer sp) {
            sp.level().playSound(null, sp.getOnPos(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.5F, 1);
        }
        
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    private InteractionResult unlinkDrone(Player player, ItemStack stack) {
        if (!this.entityData.get(LINKED)) {
            player.displayClientMessage(Component.translatable("tips.superbwarfare.drone.not_linked")
                .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        
        String controllerUUID = this.entityData.get(CONTROLLER);
        if (!controllerUUID.equals(player.getStringUUID())) {
            player.displayClientMessage(Component.translatable("tips.superbwarfare.drone.not_your_drone")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        Player controller = getController();
        if (controller != null && this.hasPassenger(controller)) {
            ejectController(controller);
        }
        
        this.entityData.set(CONTROLLER, "");
        this.entityData.set(LINKED, false);
        ItemStackDataUtil.updateTag(stack, tag -> MonitorItem.disLink(tag, player));
        
        player.displayClientMessage(Component.translatable("tips.superbwarfare.monitor.unlinked")
            .withStyle(ChatFormatting.GREEN), true);

        if (player instanceof ServerPlayer sp) {
            sp.level().playSound(null, sp.getOnPos(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.5F, 1);
        }
        
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }


    @Override
    public void travel() {
        if (!isLinked()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.95, 0.98, 0.95));
            return;
        }
        handleAircraftEngine();
    }

    protected void handleAircraftEngine() {
        float drag = (float) Mth.clamp(
            Math.max((this.onGround() ? 0.819 : 0.82) - 0.005 * this.getDeltaMovement().length(), 0.5),
            0.01, 0.99
        );

        boolean movingForward = this.getDeltaMovement().dot(this.getViewVector(1.0f)) > 0.0;
        this.setDeltaMovement(this.getDeltaMovement().add(
            this.getViewVector(1.0f).scale((movingForward ? 0.227 : 0.1) * this.getDeltaMovement().dot(this.getViewVector(1.0f)))
        ));
        this.setDeltaMovement(this.getDeltaMovement().multiply(drag, drag, drag));

        Player controller = getController();
        boolean isControlling = false;
        if (controller != null) {
            ItemStack stack = controller.getMainHandItem();
            isControlling = stack.is(ModItems.MONITOR.get()) 
                && ItemStackDataUtil.getBoolean(stack, "Using")
                && ItemStackDataUtil.getBoolean(stack, "Linked");
        }

        if (this.getHealth() > 0.1f * this.getMaxHealth()) {
            if (isControlling) {
                if (this.forwardInputDown()) {
                    this.entityData.set(POWER, (float) Mth.clamp(this.entityData.get(POWER) + 0.006f, 0, 1.0));
                }
                if (this.backInputDown()) {
                    float minPower = this.onGround() ? 0.0f : 0.4f;
                    this.entityData.set(POWER, Math.max(this.entityData.get(POWER) - 0.008f, minPower));
                }

                if (!this.onGround()) {
                    if (this.rightInputDown()) {
                        this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) - 0.8f);
                    } else if (this.leftInputDown()) {
                        this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) + 0.8f);
                    }
                }

                if (this.upInputDown()) {
                    if (this.onGround()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.1, 0));
                        this.entityData.set(POWER, Math.max(this.entityData.get(POWER), 0.7f));
                    } else {
                        this.setXRot(Mth.clamp(this.getXRot() - 0.6f, -89.0f, 89.0f));
                    }
                }

                if (this.downInputDown()) {
                    if (this.onGround()) {
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.95, 1.0, 0.95));
                    } else {
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.992, 1.0, 0.992));
                        this.setXRot(Mth.clamp(this.getXRot() + 0.4f, -89.0f, 89.0f));
                    }
                }
            } else {
                if (!this.onGround()) {
                    this.setXRot(Mth.clamp(this.getXRot() + 0.05f, -89.0f, 89.0f));
                }
            }

            float rotSpeed = 1.5f + 1.2f * Mth.abs(VectorTool.calculateY(this.getRoll()));
            float addY = Mth.clamp(Math.max((this.onGround() ? 0.6f : 0.25f) * (float) this.getDeltaMovement().length(), 0.0f) * this.getMouseMoveSpeedX(), -rotSpeed, rotSpeed);
            float addX = Mth.clamp(Math.min((float) Math.max(this.getDeltaMovement().dot(this.getViewVector(1.0f)) - 0.24, 0.15), 0.4f) * this.getMouseMoveSpeedY(), -3.5f, 3.5f);
            float addZ = this.entityData.get(DELTA_ROT) - (this.onGround() ? 0.0f : 0.004f) * this.getMouseMoveSpeedX() * (float) this.getDeltaMovement().dot(this.getViewVector(1.0f));

            this.setYRot(this.getYRot() + addY);
            if (!this.onGround()) {
                this.setXRot(this.getXRot() + addX);
                this.setZRot(this.getRoll() - addZ);
                
                float xSpeed = 1.0f + 20.0f * Mth.abs(this.getXRot() / 180.0f);
                float speed = Mth.clamp(Mth.abs(this.getRoll()) / (90.0f / xSpeed), 0.0f, 1.0f);
                if (this.getRoll() > 0) this.setZRot(this.getRoll() - Math.min(speed, this.getRoll()));
                else if (this.getRoll() < 0) this.setZRot(this.getRoll() + Math.min(speed, -this.getRoll()));
            }

            float power = this.entityData.get(POWER);
            this.setDeltaMovement(this.getDeltaMovement().add(this.getViewVector(1.0f).scale(power * 0.13)));

            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
            }
        }

        this.entityData.set(DELTA_ROT, this.entityData.get(DELTA_ROT) * 0.8f);
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        if (passenger instanceof Player player) {
            return player.getStringUUID().equals(this.entityData.get(CONTROLLER));
        }
        return false;
    }

    @Override
    public boolean canRiderInteract() {
        return false;
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        if (!allowDismount && passenger instanceof Player player
                && player.getStringUUID().equals(this.entityData.get(CONTROLLER))) {
            return;
        }
        super.removePassenger(passenger);
    }

    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        if (this.hasPassenger(passenger)) {
            callback.accept(passenger, this.getX(), this.getY() + 0.5, this.getZ());
        }
    }

    @Override
    public int getSeatIndex(Entity entity) {
        if (entity instanceof Player player && player.getStringUUID().equals(this.entityData.get(CONTROLLER))) {
            return 0;
        }
        return super.getSeatIndex(entity);
    }


    @Override
    public void destroy() {
        Player controller = getController();
        if (controller != null) {
            double opX = this.entityData.get(OPERATOR_X);
            double opY = this.entityData.get(OPERATOR_Y);
            double opZ = this.entityData.get(OPERATOR_Z);
            
            if (this.hasPassenger(controller)) {
                allowDismount = true;
                controller.stopRiding();
                allowDismount = false;
            }
            
            if (opX != 0 || opY != 0 || opZ != 0) {
                controller.teleportTo(opX, opY, opZ);
            }
            
            ItemStack stack = controller.getMainHandItem();
            if (stack.is(ModItems.MONITOR.get())) {
                ItemStackDataUtil.putBoolean(stack, "Using", false);
                ItemStackDataUtil.updateTag(stack, tag -> MonitorItem.disLink(tag, controller));
            }
        }
        super.destroy();
    }
    
    @Override
    public void vehicleShoot(net.minecraft.world.entity.LivingEntity living, java.util.UUID uuid, Vec3 targetPos) {
        modifyGunData("Missile", data -> {
            if (!data.canShoot(this.getAmmoSupplier())) return;
            
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                data.shoot(new com.atsuishio.superbwarfare.data.gun.ShootParameters(
                    this.getAmmoSupplier(),
                    living,
                    serverLevel,
                    getShootPos("Missile", 1f),
                    getShootVec("Missile", 1f),
                    data,
                    data.compute().getSpread(),
                    true,
                    uuid,
                    targetPos
                ));
            }
        });
        
        var gunData = getGunData("Missile");
        if (gunData != null && living != null) {
            afterShoot(gunData, getShootVec("Missile", 1f));
            playShootSound3p(living, "Missile");
        }
    }
}
