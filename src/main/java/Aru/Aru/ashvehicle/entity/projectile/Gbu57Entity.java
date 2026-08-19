package Aru.Aru.ashvehicle.entity.projectile;

import com.atsuishio.superbwarfare.entity.projectile.DestroyableProjectile;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.tools.ProjectileTool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Gbu57Entity extends DestroyableProjectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean penetrating = false;
    private int penetrateDistance = 0;
    private static final int MAX_PENETRATE = 60;

    public Gbu57Entity(EntityType<? extends Gbu57Entity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.setExplosionRadiusValue(22.0F);
        this.setExplosionDamageValue(650.0F);
    }

    public boolean hurt(@NotNull DamageSource source, float amount) {
        Entity entity = source.getDirectEntity();
        if (entity instanceof Gbu57Entity mk82Entity) {
            if (mk82Entity.getOwner() == this.getOwner()) {
                return false;
            }
        }

        return super.hurt(source, amount);
    }

    protected @NotNull Item getDefaultItem() {
        return (Item)ModItems.MEDIUM_AERIAL_BOMB.get();
    }

    protected void onHitBlock(@NotNull BlockHitResult hit) {
        super.onHitBlock(hit);

        if (this.level().isClientSide) return;

        this.penetrating = true;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.3));
    }

    public void tick() {
        super.tick();

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (!penetrating) {
            if (this.tickCount > 600) {
                ProjectileTool.causeCustomExplode(
                        this,
                        this.getExplosionDamageValue(),
                        this.getExplosionRadiusValue(),
                        1.2F
                );
                this.discard();
            }
            return;
        }

        /* ===== GBU-57 penetration phase (batched in one tick) ===== */
        Vec3 motion = this.getDeltaMovement();

        while (penetrateDistance < MAX_PENETRATE && motion.length() >= 0.1) {
            Vec3 direction = motion.normalize();
            Vec3 nextPos = this.position().add(direction);
            this.setPos(nextPos.x, nextPos.y, nextPos.z);

            BlockPos pos = this.blockPosition();

            if (!serverLevel.isEmptyBlock(pos)) {
                float hardness = serverLevel.getBlockState(pos)
                        .getDestroySpeed(serverLevel, pos);

                if (hardness < 0) {
                    explodeAndDiscard();
                    return;
                }

                // removeBlock instead of destroyBlock - avoids break particles and neighbor updates
                serverLevel.removeBlock(pos, false);

                double slow = Math.max(0.5, 1.0 - hardness * 0.08);
                motion = motion.scale(slow);
            }

            penetrateDistance++;
        }

        this.setDeltaMovement(motion);

        ProjectileTool.causeCustomExplode(
                this,
                this.getExplosionDamageValue() * 2.0F,
                this.getExplosionRadiusValue() * 1.5F,
                1.2F
        );

        this.discard();
    }

    private void explodeAndDiscard() {
        ProjectileTool.causeCustomExplode(
                this,
                this.getExplosionDamageValue() * 2.0F,
                this.getExplosionRadiusValue() * 1.5F,
                1.2F
        );
        this.discard();
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar data) {}

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public @NotNull SoundEvent getSound() {
        return (SoundEvent)ModSounds.SHELL_FLY.get();
    }

    public float getVolume() {
        return 0.7F;
    }

    public boolean shouldSyncMotion() {
        return true;
    }
}
