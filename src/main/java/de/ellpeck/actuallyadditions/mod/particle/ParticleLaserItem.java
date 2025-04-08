/*
 * This file ("ParticleLaserItem.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//TODO: Figure out how to make items render ghostly (translucent)
public class ParticleLaserItem extends Particle {
    public static final ParticleRenderType ITEM_RENDER = new ParticleRenderType() {

        @Nullable
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            return tesselator.begin(Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "actuallyadditions:item_particle";
        }
    };

    private final double otherX;
    private final double otherY;
    private final double otherZ;

    private final ItemStack stack;

    private ParticleLaserItem(ClientLevel world, double posX, double posY, double posZ, ItemStack stack, double motionY) {
        this(world, posX, posY, posZ, stack, motionY, 0, 0, 0);
    }

    public ParticleLaserItem(ClientLevel world, double posX, double posY, double posZ, ItemStack stack, double motionY, double otherX, double otherY, double otherZ) {
        super(world, posX + (world.random.nextDouble() - 0.5) / 8, posY, posZ + (world.random.nextDouble() - 0.5) / 8);
        this.stack = stack;
        this.otherX = otherX;
        this.otherY = otherY;
        this.otherZ = otherZ;

        this.xd = 0;
        this.yd = motionY;
        this.zd = 0;

        this.lifetime = 10;
        this.hasPhysics = false;
    }

    @Override
    public void remove() {
        super.remove();

        if (this.otherX != 0 || this.otherY != 0 || this.otherZ != 0) {
            this.level.addParticle(Factory.createData(this.stack, 0, 0, 0),
                    this.otherX, this.otherY, this.otherZ, 0, -0.025, 0);
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera renderInfo, float partialTicks) {
        Vec3 cam = renderInfo.getPosition();

        PoseStack matrices = new PoseStack();
        matrices.pushPose();

        matrices.translate(x - cam.x, y - cam.y, z - cam.z);
        matrices.scale(0.3F, 0.3F, 0.3F);

        double boop = Util.getMillis() / 600D;
        matrices.mulPose(Axis.YP.rotationDegrees((float) (boop * 40D % 360)));

        int blockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(x, y, z));
        int skyLight = level.getBrightness(LightLayer.SKY, BlockPos.containing(x, y, z));
        AssetUtil.renderItemWithoutScrewingWithColors(this.stack, matrices, LightTexture.pack(blockLight, skyLight), OverlayTexture.NO_OVERLAY);

        matrices.popPose();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ITEM_RENDER;
    }

    public static class Factory implements ParticleProvider<LaserItemParticleData> {
        public Factory(SpriteSet sprite) {

        }

        @Override
        public Particle createParticle(LaserItemParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ParticleLaserItem(worldIn, x, y, z, data.stack(), ySpeed, data.outputX(), data.outputY(), data.outputZ());
        }

        public static ParticleOptions createData(ItemStack stack, double outputX, double outputY, double outputZ) {
            return new LaserItemParticleData(stack, outputX, outputY, outputZ);
        }
    }
}
