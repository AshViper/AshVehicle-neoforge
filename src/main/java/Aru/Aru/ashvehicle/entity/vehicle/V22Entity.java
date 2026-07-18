package Aru.Aru.ashvehicle.entity.vehicle;

import com.atsuishio.superbwarfare.data.DataLoader;
import com.atsuishio.superbwarfare.data.vehicle.DefaultVehicleData;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.tools.VectorTool;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Math;

public class V22Entity extends GeoVehicleEntity {
    public V22Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static boolean vtolMode = true;
    private static final EntityDataAccessor<Float> VTOL_ROT = SynchedEntityData.defineId(V22Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WEAPON_BAY_ROT = SynchedEntityData.defineId(V22Entity.class, EntityDataSerializers.FLOAT);
    public float vtolRotO = 0f;
    public float weaponBayRotO = 0f;

    @OnlyIn(Dist.CLIENT)
    private V22Entity.V22EngineSound engineSound;

    DefaultVehicleData computed = this.computed();
    JsonObject engineInfo = computed.getEngineInfo();
    EngineInfo.Aircraft aircraft = DataLoader.GSON.fromJson(engineInfo, EngineInfo.Aircraft.class);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VTOL_ROT, 0.0F);
        builder.define(WEAPON_BAY_ROT, 0.0F);
    }

    public void setPodRot(float value) {
        this.entityData.set(VTOL_ROT, value);
    }

    public float getPodRot() {
        return this.entityData.get(VTOL_ROT);
    }

    public void setWeaponBayRot(float value) {
        this.entityData.set(WEAPON_BAY_ROT, value);
    }

    public float getWeaponBayRot() {
        return this.entityData.get(WEAPON_BAY_ROT);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // г‚Їгѓ©г‚¤г‚ўгѓігѓ€еЃґгЃ§г‚Ёгѓіг‚ёгѓійџіг‚’з®Ўзђ†
        if (this.level().isClientSide) {
            handleEngineSound();
        }

        vtolRotO = getPodRot();
        float target = this.vtolMode ? 0.0F : 85.0F;
        float current = getPodRot();
        float diff = target - current;
        float newRot = current + diff * 0.05f;
        setPodRot(newRot);

        int driverWeapon = this.getWeaponIndex(0);
        boolean driverNeedsBay = driverWeapon >= 1;
        weaponBayRotO = getWeaponBayRot();
        float target1 = driverNeedsBay ? 90.0F : 0.0F;
        float current1 = getWeaponBayRot();
        float diff1 = target1 - current1;
        float newRot1 = current1 + diff1 * 0.1f;
        setWeaponBayRot(newRot1);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleEngineSound() {
        boolean shouldPlay = this.engineRunning() && !this.isRemoved();

        if (shouldPlay) {
            if (this.engineSound == null || !Minecraft.getInstance().getSoundManager().isActive(this.engineSound)) {
                this.engineSound = new V22Entity.V22EngineSound(this);
                Minecraft.getInstance().getSoundManager().play(this.engineSound);
            }
        } else {
            if (this.engineSound != null) {
                this.engineSound = null;
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.level().isClientSide && this.engineSound != null) {
            this.engineSound = null;
        }
    }

    // Vг‚­гѓјз”Ёпј€Packet гЃ‹г‚‰е‘јгЃ°г‚Њг‚‹пј‰
    public void toggleVtolMode() {
        vtolMode = !vtolMode;
    }

    // г‚Ёгѓіг‚ёгѓійџіг‚Їгѓ©г‚№
    @OnlyIn(Dist.CLIENT)
    public static class V22EngineSound extends AbstractTickableSoundInstance {
        private final V22Entity vehicle;

        public V22EngineSound(V22Entity vehicle) {
            super(vehicle.getEngineSound(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
            this.vehicle = vehicle;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.0F;
            this.pitch = 1.0F;  // е›єе®љгѓ”гѓѓгѓЃ
            this.x = vehicle.getX();
            this.y = vehicle.getY();
            this.z = vehicle.getZ();
        }

        @Override
        public void tick() {
            if (this.vehicle.isRemoved() || !this.vehicle.engineRunning()) {
                this.stop();
                return;
            }

            // дЅЌзЅ®г‚’ж›ґж–°
            this.x = this.vehicle.getX();
            this.y = this.vehicle.getY();
            this.z = this.vehicle.getZ();

            // йџій‡Џг‚’иЁ€з®—пј€POWERгЃ«еџєгЃҐгЃЏпј‰
            float power = org.joml.Math.abs(this.vehicle.getPower());
            float targetVolume = Mth.clamp(power * 2.0F, 0.0F, 3.0F);

            // г‚№гѓ гѓјг‚єгЃ«йџій‡Џг‚’е¤‰еЊ–
            this.volume = Mth.lerp(0.1F, this.volume, targetVolume);

            // г‚ўгѓ•г‚їгѓјгѓђгѓјгѓЉгѓјпј€г‚№гѓ—гѓЄгѓігѓ€пј‰ж™‚гЃЇйџій‡Џг‚’е°‘гЃ—дёЉгЃ’г‚‹
            if (this.vehicle.sprintInputDown()) {
                this.volume = org.joml.Math.min(this.volume * 1.2F, 4.0F);
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
