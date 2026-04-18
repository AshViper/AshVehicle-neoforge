package Aru.Aru.ashvehicle.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class AfterburnerFlameParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final float initialScale;

    public AfterburnerFlameParticle(ClientLevel level, double x, double y, double z,
                                    double dx, double dy, double dz, SpriteSet sprite) {
        super(level, x, y, z, dx, dy, dz);
        this.spriteSet = sprite;
        this.lifetime = 15 + this.random.nextInt(8); // Р‘РѕР»РµРµ РєРѕСЂРѕС‚РєР°СЏ Р¶РёР·РЅСЊ РґР»СЏ РґРёРЅР°РјРёС‡РЅРѕСЃС‚Рё
        this.gravity = -0.02F; // Р›РµРіРєРёР№ РїРѕРґСЉРµРј РІРІРµСЂС…
        
        // Р”РѕР±Р°РІР»СЏРµРј СЃР»СѓС‡Р°Р№РЅРѕРµ РѕС‚РєР»РѕРЅРµРЅРёРµ РґР»СЏ Р±РѕР»РµРµ СЂРµР°Р»РёСЃС‚РёС‡РЅРѕРіРѕ СЌС„С„РµРєС‚Р°
        this.xd = dx + (this.random.nextDouble() - 0.5) * 0.05;
        this.yd = dy + (this.random.nextDouble() - 0.5) * 0.05;
        this.zd = dz + (this.random.nextDouble() - 0.5) * 0.05;
        
        this.alpha = 0.95F;
        this.initialScale = 1.8F + this.random.nextFloat() * 0.4F; // РЎР»СѓС‡Р°Р№РЅС‹Р№ РЅР°С‡Р°Р»СЊРЅС‹Р№ СЂР°Р·РјРµСЂ
        this.quadSize = this.initialScale;
        
        // Р¦РІРµС‚ РїР»Р°РјРµРЅРё: РѕС‚ СЏСЂРєРѕ-РѕСЂР°РЅР¶РµРІРѕРіРѕ Рє Р¶РµР»С‚РѕРјСѓ
        this.rCol = 1.0F;
        this.gCol = 0.7F + this.random.nextFloat() * 0.2F;
        this.bCol = 0.3F + this.random.nextFloat() * 0.2F;
        
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();

        float ageRatio = (float) this.age / (float) this.lifetime;

        // РџР»Р°РІРЅРѕРµ СѓРІРµР»РёС‡РµРЅРёРµ СЂР°Р·РјРµСЂР° РІ РЅР°С‡Р°Р»Рµ, Р·Р°С‚РµРј СѓРјРµРЅСЊС€РµРЅРёРµ
        if (ageRatio < 0.3F) {
            this.quadSize = this.initialScale * (1.0F + ageRatio * 0.5F);
        } else {
            this.quadSize = this.initialScale * (1.15F - (ageRatio - 0.3F) * 1.2F);
        }

        // РџР»Р°РІРЅРѕРµ Р·Р°С‚СѓС…Р°РЅРёРµ СЃ Р±РѕР»РµРµ Р±С‹СЃС‚СЂС‹Рј РёСЃС‡РµР·РЅРѕРІРµРЅРёРµРј РІ РєРѕРЅС†Рµ
        if (ageRatio < 0.7F) {
            this.alpha = 0.95F * (1.0F - ageRatio * 0.5F);
        } else {
            this.alpha = 0.95F * (1.0F - ageRatio) * 0.5F;
        }

        // РР·РјРµРЅРµРЅРёРµ С†РІРµС‚Р°: РѕС‚ РѕСЂР°РЅР¶РµРІРѕРіРѕ Рє РєСЂР°СЃРЅРѕРјСѓ
        this.gCol = (0.7F + this.random.nextFloat() * 0.2F) * (1.0F - ageRatio * 0.5F);
        this.bCol = (0.3F + this.random.nextFloat() * 0.2F) * (1.0F - ageRatio * 0.7F);

        // Р—Р°РјРµРґР»РµРЅРёРµ РґРІРёР¶РµРЅРёСЏ
        this.xd *= 0.95;
        this.yd *= 0.95;
        this.zd *= 0.95;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** вњЁ РњР°РєСЃРёРјР°Р»СЊРЅР°СЏ СЏСЂРєРѕСЃС‚СЊ РґР»СЏ СЌС„С„РµРєС‚Р° СЃРІРµС‡РµРЅРёСЏ */
    @Override
    public int getLightColor(float partialTick) {
        // Р”РёРЅР°РјРёС‡РµСЃРєР°СЏ СЏСЂРєРѕСЃС‚СЊ РІ Р·Р°РІРёСЃРёРјРѕСЃС‚Рё РѕС‚ РІРѕР·СЂР°СЃС‚Р° С‡Р°СЃС‚РёС†С‹
        float ageRatio = ((float) this.age + partialTick) / (float) this.lifetime;
        int brightness = (int) (15.0F * (1.0F - ageRatio * 0.5F));
        brightness = Math.max(brightness, 0);
        return brightness << 20 | brightness << 4;
    }
}
