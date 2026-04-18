package Aru.Aru.ashvehicle.client.layer;

import Aru.Aru.ashvehicle.entity.vehicle.J20Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class J20GlowLayer extends GeoRenderLayer<J20Entity> {
    private static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("ashvehicle", "textures/entity/j-20_glow.png");

    public J20GlowLayer(GeoRenderer<J20Entity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, J20Entity animatable, BakedGeoModel bakedModel,
                       RenderType baseRenderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        // рџ”ё з™єе…‰з”Ёгѓ¬гѓігѓЂгѓјг‚їг‚¤гѓ—пј€еёёгЃ«жњЂе¤§е…‰й‡ЏгЃ§жЏЏз”»пј‰
        RenderType glowRenderType = RenderType.eyes(GLOW_TEXTURE);

        // рџ”ё VertexConsumer г‚’еЏ–еѕ—
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        // рџ”ё гѓўгѓ‡гѓ«жЏЏз”»пј€GeckoLib гЃ®ж­ЈгЃ—гЃ„е‘јгЃіе‡єгЃ—ж–№жі•пј‰
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                0xF000F0,         // packedLight
                packedOverlay,
                0xFFFFFFFF // ARGB
        );
    }
}
