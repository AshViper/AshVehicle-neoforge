// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package Aru.Aru.ashvehicle.client.model.vehicle;

import Aru.Aru.ashvehicle.client.model.VehicleModel;
import Aru.Aru.ashvehicle.entity.vehicle.T90Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class T90Model extends VehicleModel<T90Entity> {
   public T90Model() {
   }

   public VehicleModel.@Nullable TransformContext<T90Entity> collectTransform(String boneName) {
      switch (boneName) {
         case "turret":
            return (bone, vehicle, state) -> bone.setRotY(this.turretYRot * ((float)Math.PI / 180F));
         case "barrel":
         case "burel":
            return (bone, vehicle, state) -> {
               float a = this.turretYaw;
               float r = (Mth.abs(a) - 90.0F) / 90.0F;
               float r2;
               if (Mth.abs(a) <= 90.0F) {
                  r2 = a / 90.0F;
               } else if (a < 0.0F) {
                  r2 = -(180.0F + a) / 90.0F;
               } else {
                  r2 = (180.0F - a) / 90.0F;
               }

               bone.setRotX(Mth.clamp(-this.turretXRot - r * this.pitch - r2 * this.roll, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * ((float)Math.PI / 180F));
            };
         case "wheel1":
         case "wheel2":
         case "wheel3":
         case "wheel4":
         case "wheel5":
         case "wheel6":
         case "wheel7":
         case "wheel8":
            return (bone, vehicle, state) -> bone.setRotX(1.5F * this.leftWheelRot);
         case "wheel9":
         case "wheel10":
         case "wheel11":
         case "wheel12":
         case "wheel13":
         case "wheel14":
         case "wheel15":
         case "wheel16":
            return (bone, vehicle, state) -> bone.setRotX(1.5F * this.rightWheelRot);
         default:
            return super.collectTransform(boneName);
      }
   }

   public ResourceLocation getModelResource(T90Entity object) {
      return ResourceLocation.fromNamespaceAndPath("ashvehicle", "geo/t_90.geo.json");
   }

   public ResourceLocation getTextureResource(T90Entity object) {
      return ResourceLocation.fromNamespaceAndPath("ashvehicle", "textures/entity/t_90.png");
   }

   public ResourceLocation getAnimationResource(T90Entity animatable) {
      return ResourceLocation.fromNamespaceAndPath("ashvehicle", "animations/t_90.animation.json");
   }
}
