package Aru.Aru.ashvehicle.entity.projectile;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.network.message.receive.ClientIndicatorMessage;
import com.atsuishio.superbwarfare.tools.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Agm158Entity extends MissileProjectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private enum Phase { DROP, CRUISE, HOMING }
    private Phase phase = Phase.DROP;

    private int dropTicks = 20; // 0.5Р В·Р’В§РІР‚в„ў

    public Agm158Entity(EntityType<? extends Agm158Entity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.setDamageValue(1100.0F);
        this.setExplosionDamageValue(180.0F);
        this.setExplosionRadiusValue(12.0F);
        this.setDistracted(false);
        this.setDurability(25);
    }

    protected @NotNull Item getDefaultItem() {
        return (Item)ModItems.LARGE_ANTI_GROUND_MISSILE.get();
    }

    public void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity != this.getOwner() && (this.getOwner() == null || entity != this.getOwner().getVehicle())) {
            if (this.level() instanceof ServerLevel) {
                Entity player = this.getOwner();
                if (player instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity)player;
                    if (!living.level().isClientSide() && living instanceof ServerPlayer) {
                        ServerPlayer player1 = (ServerPlayer)living;
                        living.level().playSound((Player)null, living.blockPosition(), (SoundEvent)ModSounds.INDICATION.get(), SoundSource.VOICE, 1.0F, 1.0F);
                        PacketDistributor.sendToPlayer(player1, new ClientIndicatorMessage(0, 5));
                    }
                }

                DamageHandler.doDamage(entity, ModDamageTypes.causeProjectileHitDamage(this.level().registryAccess(), this, this.getOwner()), this.getDamageValue());
                if (entity instanceof LivingEntity) {
                    entity.invulnerableTime = 0;
                }

                this.causeExplode(result.getLocation());
                this.discard();
            }

        }
    }

    public void onHitBlock(@NotNull BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level() instanceof ServerLevel) {
            BlockPos resultPos = blockHitResult.getBlockPos();
            float hardness = this.level().getBlockState(resultPos).getBlock().defaultDestroyTime();
            if (hardness != -1.0F) {
                if ((Boolean)ExplosionConfig.EXPLOSION_DESTROY.get()) {
                    if (this.getFirstHit()) {
                        this.causeExplode(blockHitResult.getLocation());
                        this.setFirstHit(false);
                        Mod.queueServerWork(3, this::discard);
                    }

                    if ((Boolean)ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()) {
                        this.level().destroyBlock(resultPos, true);
                    }
                }
            } else {
                this.causeExplode(blockHitResult.getLocation());
                this.discard();
            }

            if (!(Boolean)ExplosionConfig.EXPLOSION_DESTROY.get()) {
                this.causeExplode(blockHitResult.getLocation());
                this.discard();
            }
        }

    }

    public void tick() {
        super.tick();
        this.mediumTrail();
        Entity entity = EntityFindUtil.findEntity(this.level(), (String)this.entityData.get(TARGET_UUID));

        // Р С–РЎвЂњРІР‚РЋР С–РІР‚С™РЎвЂ“Р С–РІР‚С™Р’В¤Р ВµРІР‚РЋР’В¦Р В·РЎвЂ™РІР‚В Р С—РЎВРІвЂљВ¬Р ВµРІР‚В¦РЎвЂњР С–РІР‚С™РЎвЂ“Р С–РЎвЂњРЎВР С–РЎвЂњРІР‚В°Р С–Р С“РЎСљР С–Р С“Р’В®Р С–Р С“РЎвЂўР С–Р С“РЎвЂўР С—РЎВРІР‚В°
        for(Entity e : SeekTool.seekLivingEntities(this, 32.0F, 90.0F)) {
            if (e.getType().is(ModTags.EntityTypes.DECOY) && !this.isDistracted()) {
                this.entityData.set(TARGET_UUID, e.getStringUUID());
                this.setDistracted(true);
                break;
            }
        }

        //===========================
        //     Р Р†РІР‚вЂќРІР‚В  Р С–РЎвЂњРІР‚СћР С–РІР‚С™Р’В§Р С–РЎвЂњРЎВР С–РІР‚С™РЎвЂќР ВµРІвЂљВ¬Р’В¶Р ВµРЎвЂўР Р‹ Р Р†РІР‚вЂќРІР‚В 
        //===========================
        if (entity != null && !this.entityData.get(TARGET_UUID).equals("none")) {

            double dist = this.distanceTo(entity);

            switch (phase) {

                // ----------------------------------
                // Р Р†РІР‚ВР’В  0.5Р В·Р’В§РІР‚в„ўР С–Р С“Р’В Р С–Р С“РІР‚ВР С‘РЎвЂ™Р вЂ¦Р Т‘РЎвЂРІР‚в„–
                // ----------------------------------
                case DROP -> {
                    this.setDeltaMovement(0, -1.1, 0); // Р С‘РЎвЂ™Р вЂ¦Р Т‘РЎвЂРІР‚в„–Р в„–Р вЂљРЎСџР ВµРЎвЂќР’В¦

                    dropTicks--;
                    if (dropTicks <= 0) {
                        phase = Phase.CRUISE;
                    }
                }

                // ----------------------------------
                // Р Р†РІР‚ВР Р‹ Р В¶Р’В°РўвЂР ВµРІвЂћвЂ“РЎвЂ“Р ВµР’В·Р Р‹Р С‘РІвЂљВ¬Р вЂћ
                // ----------------------------------
                case CRUISE -> {

                    // 50mР Т‘Р’В»РўС’Р ВµРІР‚В РІР‚В¦Р С–Р С“Р вЂћР С–РІР‚С™РІР‚В°Р С‘Р вЂћР’ВР ВµР’В°Р вЂ№Р С–РЎвЂњРІР‚СћР С–РІР‚С™Р’В§Р С–РЎвЂњРЎВР С–РІР‚С™РЎвЂќР С–Р С“РЎвЂ
                    if (dist < 200) {
                        phase = Phase.HOMING;
                        break;
                    }

                    // Р С–РІР‚С™РЎвЂ”Р С–РЎвЂњРЎВР С–РІР‚С™Р вЂ Р С–РЎвЂњРЎвЂњР С–РЎвЂњРІвЂљВ¬Р В¶РІР‚вЂњРІвЂћвЂ“Р ВµРЎвЂ™РІР‚ВР С–Р С“РЎвЂР В¶Р’В°РўвЂР ВµРІвЂћвЂ“РЎвЂ“Р В·Р’В§Р’В»Р ВµРІР‚в„–РІР‚СћР С—РЎВРІвЂљВ¬Р в„–Р’В«Р’ВР ВµРЎвЂќР’В¦Р ВµРІР‚С”РЎвЂќР ВµР’В®РЎв„ўР С—РЎВРІР‚В°
                    Vec3 horizontalTarget = new Vec3(
                            entity.getX(),
                            this.getY(),     // Р в„–Р’В«Р’ВР ВµРЎвЂќР’В¦Р ВµРІР‚С”РЎвЂќР ВµР’В®РЎв„ўР С—РЎВРЎСљР В¶Р’В°РўвЂР ВµРІвЂћвЂ“РЎвЂ“Р в„–Р в‚¬РІР‚С”Р С‘Р Р‹Р Р‰
                            entity.getZ()
                    );

                    Vec3 toVec = horizontalTarget.subtract(this.position()).normalize();

                    this.turn(toVec, 6.0F);             // Р В·Р’В·Р’В©Р С–РІР‚С™РІР‚С›Р С–Р С“РІР‚в„–Р С–Р С“Р вЂћР В¶РІР‚вЂќРІР‚в„–Р ВµРІР‚С”РЎвЂє
                    this.setDeltaMovement(
                            this.getDeltaMovement().scale(0.05)
                                    .add(this.getLookAngle().scale(8.0F))
                    ); // Р ВµР’В·Р Р‹Р С‘РІвЂљВ¬Р вЂћР в„–Р вЂљРЎСџР ВµРЎвЂќР’В¦
                }

                // ----------------------------------
                // Р Р†РІР‚ВРЎС› Р ВµРІР‚В¦РЎвЂњР С–Р С“Р’В®Р С‘Р вЂћР’ВР ВµР’В°Р вЂ№Р В¶РІР‚вЂњРІвЂћвЂ“Р ВµРЎВР РЏР С—РЎВРІвЂљВ¬HOMINGР С—РЎВРІР‚В°
                // ----------------------------------
                case HOMING -> {

                    if ((!entity.getPassengers().isEmpty() || entity instanceof VehicleEntity)
                            && entity.tickCount % (int)Math.max(0.04 * dist, 2.0F) == 0) {
                        entity.level().playSound(null, entity.getOnPos(),
                                entity instanceof Pig ? SoundEvents.PIG_HURT : ModSounds.MISSILE_WARNING.get(),
                                SoundSource.PLAYERS, 2.0F, 1.0F);
                    }

                    Vec3 targetPos = new Vec3(
                            entity.getX(),
                            entity.getY() + (0.5F * entity.getBbHeight()) + (entity instanceof EnderDragon ? -3 : 0),
                            entity.getZ()
                    );

                    Vec3 toVec = RangeTool.calculateFiringSolution(
                            this.position(),
                            targetPos,
                            entity.getDeltaMovement(),
                            this.getDeltaMovement().length(),
                            0.0F
                    );

                    if (this.tickCount > 1) {
                        this.setLostTarget(VectorTool.calculateAngle(this.getDeltaMovement(), toVec) > 120.0 && !this.isLostTarget());

                        if (!this.isLostTarget()) {
                            this.turn(toVec, Mth.clamp((float)(this.tickCount - 1) * 0.5F, 0.0F, 15.0F));
                            this.setDeltaMovement(
                                    this.getDeltaMovement().scale(0.05)
                                            .add(this.getLookAngle().scale(8.0F))
                            );
                        }

                        if (this.isLostTarget()) {
                            this.entityData.set(TARGET_UUID, "none");
                        }
                    }
                }
            }
        }

        // Р ВµРІР‚В¦РЎвЂњР С–Р С“Р’В®Р ВµР вЂЎРЎвЂ”Р ВµРІР‚ВР вЂ¦Р С–РЎвЂњР’В»Р В¶Р’В°РўвЂР В¶Р вЂ Р Р‹Р ВµРІР‚РЋР’В¦Р В·РЎвЂ™РІР‚В 
        if (this.tickCount > 200 || this.isInWater()) {
            if (this.level() instanceof ServerLevel) {
                ProjectileTool.causeCustomExplode(
                        this,
                        ModDamageTypes.causeProjectileExplosionDamage(this.level().registryAccess(), this, this.getOwner()),
                        this,
                        this.getExplosionDamageValue(),
                        this.getExplosionRadiusValue()
                );
            }

            this.discard();
        }

        if (!this.level().isClientSide) {
            BlockHitResult $$hit = this.level().clip(new ClipContext(
                this.position(),
                this.position().add(this.getDeltaMovement().scale(2)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
            ));
            if ($$hit.getType() != BlockHitResult.Type.MISS) {
                this.destroyBlock($$hit);
            }
        }
    }

    public double getDefaultGravity() {
        return this.tickCount < 8 ? 0.15D : super.getDefaultGravity();
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar data) {}

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public @NotNull SoundEvent getSound() {
        return (SoundEvent)ModSounds.ROCKET_FLY.get();
    }

    public float getVolume() {
        return 0.7F;
    }

    public float getMaxHealth() {
        return 70.0F;
    }
}
