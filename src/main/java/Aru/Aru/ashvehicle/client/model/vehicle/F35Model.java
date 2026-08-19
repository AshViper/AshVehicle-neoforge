package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.client.model.VehicleModel;
import Aru.Aru.ashvehicle.entity.vehicle.F35Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class F35Model extends VehicleModel<F35Entity> {
    public F35Model() {
    }

    @Nullable
    public VehicleModel.TransformContext<F35Entity> collectTransform(String boneName) {
        TransformContext var1000;
        switch (boneName){
            case "l_wheels2":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(vehicle.getGearRot() * (-(float)Math.PI / 180F));
                break;
            case "r_wheels2":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(vehicle.getGearRot() * ((float)Math.PI / 180F));
                break;
            case "LGDRearLeft", "gearFL":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(vehicle.getGearRot() * (-(float)Math.PI / 180F) + 1.5f);
                break;
            case "LGDRearRight", "gearFR":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(vehicle.getGearRot() * ((float)Math.PI / 180F) - 1.5f);
                break;
            case "fr_wheels":
                var1000 = (bone, vehicle, state) -> bone.setRotX(vehicle.getGearRot() * ((float)Math.PI / 180F));
                break;
            case "LFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * ((float)Math.PI / 180F));
                break;
            case "LRlap":
                var1000 = (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * ((float)Math.PI / 180F));
                break;
            case "BRFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * ((float)Math.PI / 180F));
                break;
            case "BLFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * ((float)Math.PI / 180F));
                break;
            case "VBRFlap", "VBLFlap":
                var1000 = (bone, vehicle, state) -> bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20.0F, 20.0F) * ((float)Math.PI / 180F));
                break;
            case "engine2":
                var1000 = (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
                break;
            default :
                var1000 = null;
                break;
        }
        return var1000;
    }

    @Override
    public ResourceLocation getModelResource(F35Entity object) {
        return ResourceLocation.fromNamespaceAndPath("ashvehicle", "geo/f-35b.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(F35Entity object) {
        return ResourceLocation.fromNamespaceAndPath("ashvehicle", "textures/entity/f-35b.png");
    }

    @Override
    public ResourceLocation getAnimationResource(F35Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath("ashvehicle", "animations/f-35.animation.json");
    }
}
