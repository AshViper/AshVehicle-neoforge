package Aru.Aru.ashvehicle.entity.vehicle;

import Aru.Aru.ashvehicle.init.CoordinateTargetVehicle;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.RangeTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TosEntity extends GeoVehicleEntity implements CoordinateTargetVehicle {
    private static final String ROCKET_WEAPON = "Rocket";
    private static final int MAX_BARRAGE_SHOTS = 24;
    private static final double MAX_TARGET_SPREAD = 200.0;
    private static final double MAX_COORDINATE_RANGE = 6000.0;
    private static final double MAX_ALIGNMENT_ANGLE = 5.0;
    private static final EntityDataAccessor<Boolean> COORDINATE_AIM_LOCKED =
            SynchedEntityData.defineId(TosEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID previewOwner;
    private Vec3 previewTarget;

    private UUID coordinateStrikeOwner;
    private Vec3 coordinateStrikeCenter;
    private Vec3 queuedImpactPoint;
    private int queuedShots;

    public TosEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COORDINATE_AIM_LOCKED, false);
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public double getCoordinateTargetMaxRange() {
        return MAX_COORDINATE_RANGE;
    }

    @Override
    public void previewCoordinateTarget(Player player, Vec3 targetPos) {
        if (this.level().isClientSide() || !this.canControlCoordinateTarget(player) || this.isStrikeActive()) {
            return;
        }

        Vec3 groundTarget = this.snapToSurface(targetPos);
        if (!this.isWithinCoordinateRange(groundTarget)) {
            this.clearCoordinatePreview(player);
            return;
        }

        this.previewOwner = player.getUUID();
        this.previewTarget = groundTarget;
        this.syncCoordinateLockState();
    }

    @Override
    public void clearCoordinatePreview(Player player) {
        if (player != null && this.previewOwner != null && !this.previewOwner.equals(player.getUUID())) {
            return;
        }

        this.previewOwner = null;
        this.previewTarget = null;
        this.syncCoordinateLockState();
    }

    @Override
    public void handleCoordinateTarget(Player player, Vec3 targetPos) {
        if (this.level().isClientSide() || !this.canControlCoordinateTarget(player)) {
            return;
        }

        GunData gunData = this.getGunData(ROCKET_WEAPON);
        if (gunData == null) {
            return;
        }

        Vec3 groundTarget = this.snapToSurface(targetPos);
        if (!this.isWithinCoordinateRange(groundTarget)) {
            return;
        }

        int availableShots = Math.min(MAX_BARRAGE_SHOTS, gunData.currentAvailableShots(this.getAmmoSupplier()));
        if (availableShots <= 0) {
            return;
        }

        this.clearCoordinatePreview(player);
        this.coordinateStrikeOwner = player.getUUID();
        this.coordinateStrikeCenter = groundTarget;
        this.queuedImpactPoint = null;
        this.queuedShots = availableShots;
        this.syncCoordinateLockState();
    }

    @Override
    public void mouseInput(double x, double y) {
        if (this.isCoordinateAimLocked()) {
            this.getEntityData().set(VehicleEntity.MOUSE_SPEED_X, 0.0F);
            this.getEntityData().set(VehicleEntity.MOUSE_SPEED_Y, 0.0F);
            return;
        }

        super.mouseInput(x, y);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (this.isCoordinateAimLocked()) {
            this.setFireInputDown(false);
            this.getEntityData().set(VehicleEntity.MOUSE_SPEED_X, 0.0F);
            this.getEntityData().set(VehicleEntity.MOUSE_SPEED_Y, 0.0F);
        }

        if (!this.level().isClientSide()) {
            this.tickCoordinatePreview();
            this.tickCoordinateStrike();
            this.syncCoordinateLockState();
        }
    }

    private void tickCoordinatePreview() {
        if (this.previewTarget == null || this.isStrikeActive()) {
            return;
        }

        Player player = this.getPreviewOwner();
        if (player == null || !this.canControlCoordinateTarget(player)) {
            this.clearCoordinatePreview(null);
            return;
        }

        this.aimTurretAtTarget(this.previewTarget);
    }

    private void tickCoordinateStrike() {
        if (!this.isStrikeActive()) {
            return;
        }

        Player shooter = this.getCoordinateStrikeOwner();
        GunData gunData = this.getGunData(ROCKET_WEAPON);
        if (shooter == null || gunData == null) {
            this.clearCoordinateStrike();
            return;
        }

        if (this.queuedImpactPoint == null) {
            this.queuedImpactPoint = this.createRandomImpactPoint();
        }

        Vec3 launchVector = this.calculateLaunchVector(this.queuedImpactPoint);
        if (launchVector == null) {
            this.clearCoordinateStrike();
            return;
        }

        this.turretAutoAimFromVector(launchVector);
        if (!this.isTurretAligned(launchVector)) {
            return;
        }

        if (!this.fireCoordinateRocket(shooter, gunData, launchVector, this.queuedImpactPoint)) {
            this.clearCoordinateStrike();
            return;
        }

        this.queuedShots--;
        this.queuedImpactPoint = null;

        if (this.queuedShots <= 0) {
            this.clearCoordinateStrike();
        }
    }

    private boolean fireCoordinateRocket(Player shooter, GunData gunData, Vec3 launchVector, Vec3 impactPoint) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        Vec3 launchDirection = launchVector.normalize();
        boolean canShoot = gunData.canShoot(this.getAmmoSupplier());
        if (!canShoot) {
            return false;
        }

        this.modifyGunData(ROCKET_WEAPON, data -> {
            Vec3 shootPos = this.getShootPos(ROCKET_WEAPON, 1.0F);
            data.shoot(new ShootParameters(
                    this.getAmmoSupplier(),
                    shooter,
                    serverLevel,
                    shootPos,
                    launchDirection,
                    data,
                    0.0D,
                    true,
                    null,
                    impactPoint
            ));
        });

        this.afterShoot(gunData, launchDirection);
        this.playShootSound3p(shooter, ROCKET_WEAPON);
        return true;
    }

    private void aimTurretAtTarget(Vec3 targetPos) {
        Vec3 launchVector = this.calculateLaunchVector(targetPos);
        if (launchVector != null) {
            this.turretAutoAimFromVector(launchVector);
        }
    }

    private Vec3 calculateLaunchVector(Vec3 targetPos) {
        Vec3 shootPos = this.getShootPos(ROCKET_WEAPON, 1.0F);
        return RangeTool.calculateLaunchVector(
                shootPos,
                targetPos,
                this.getProjectileVelocity(ROCKET_WEAPON),
                this.getProjectileGravity(ROCKET_WEAPON),
                false
        );
    }

    private Player getPreviewOwner() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.previewOwner == null) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.previewOwner);
    }

    private Player getCoordinateStrikeOwner() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.coordinateStrikeOwner == null) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.coordinateStrikeOwner);
    }

    private Vec3 createRandomImpactPoint() {
        double angle = this.random.nextDouble() * (Math.PI * 2.0);
        double radius = Math.sqrt(this.random.nextDouble()) * MAX_TARGET_SPREAD;
        double x = this.coordinateStrikeCenter.x + Math.cos(angle) * radius;
        double z = this.coordinateStrikeCenter.z + Math.sin(angle) * radius;
        return this.snapToSurface(new Vec3(x, this.coordinateStrikeCenter.y, z));
    }

    private Vec3 snapToSurface(Vec3 targetPos) {
        BlockPos surfacePos = this.level().getHeightmapPos(
                Heightmap.Types.WORLD_SURFACE,
                BlockPos.containing(targetPos.x, 0.0, targetPos.z)
        );
        return new Vec3(targetPos.x, surfacePos.getY(), targetPos.z);
    }

    private boolean canControlCoordinateTarget(Player player) {
        return player != null && player.getVehicle() == this;
    }

    private boolean isStrikeActive() {
        return this.coordinateStrikeCenter != null && this.queuedShots > 0;
    }

    private boolean isCoordinateAimLocked() {
        return this.getEntityData().get(COORDINATE_AIM_LOCKED);
    }

    private boolean isWithinCoordinateRange(Vec3 targetPos) {
        double dx = targetPos.x - this.getX();
        double dz = targetPos.z - this.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= MAX_COORDINATE_RANGE;
    }

    private boolean isTurretAligned(Vec3 launchVector) {
        Vec3 barrelVector = this.getBarrelVector(1.0F).normalize();
        Vec3 desiredVector = launchVector.normalize();
        double minDot = Math.cos(Math.toRadians(MAX_ALIGNMENT_ANGLE));
        return barrelVector.dot(desiredVector) >= minDot;
    }

    private void clearCoordinateStrike() {
        this.coordinateStrikeOwner = null;
        this.coordinateStrikeCenter = null;
        this.queuedImpactPoint = null;
        this.queuedShots = 0;
        this.syncCoordinateLockState();
    }

    private void syncCoordinateLockState() {
        this.getEntityData().set(COORDINATE_AIM_LOCKED, this.previewTarget != null || this.isStrikeActive());
    }
}
