package Aru.Aru.ashvehicle.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThermalBufferSource implements MultiBufferSource {

    private final MultiBufferSource delegate;
   public ThermalBufferSource(MultiBufferSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        VertexConsumer original = delegate.getBuffer(renderType);
        return new WhiteVertexConsumer(original);
    }

    private static class WhiteVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        WhiteVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return delegate.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return delegate.setColor(255, 255, 255, a);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return delegate.setUv1(0, 10);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return delegate.setUv2(240, 240);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return delegate.setNormal(x, y, z);
        }

    }
}
