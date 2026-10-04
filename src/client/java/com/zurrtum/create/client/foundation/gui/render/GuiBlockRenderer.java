package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zurrtum.create.client.catnip.gui.render.FeaturePass;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.level.CardinalLighting;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public abstract class GuiBlockRenderer<T extends PictureInPictureRenderState> extends PictureInPictureRenderer<T> {
    private static final Vector3fc DIFFUSE_LIGHT_0 = new Vector3f(0, 0, 1).rotate(Axis.YP.rotationDegrees(12.5f))
        .rotate(Axis.XP.rotationDegrees(45.0f));
    private static final Vector3fc DIFFUSE_LIGHT_1 = new Vector3f(0, 0, 1).rotate(Axis.YP.rotationDegrees(-20.0f))
        .rotate(Axis.XP.rotationDegrees(50.0f));

    @Override
    public void prepare(
        T renderState,
        GuiRenderState guiRenderState,
        FeatureRenderDispatcher featureRenderDispatcher,
        int guiScale
    ) {
        int width = (renderState.x1() - renderState.x0()) * guiScale;
        int height = (renderState.y1() - renderState.y0()) * guiScale;
        boolean needsAResize = texture == null || texture.getWidth(0) != width || texture.getHeight(0) != height;
        if (!needsAResize && textureIsReadyToBlit(renderState)) {
            blitTexture(renderState, guiRenderState);
        } else {
            prepareTexturesAndProjection(needsAResize, width, height);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            PoseStack poseStack = new PoseStack();
            poseStack.translate(width / 2.0F, getTranslateY(height, guiScale), 0.0F);
            float scale = guiScale * renderState.scale();
            poseStack.scale(scale, scale, -scale);
            renderToTexture(renderState, poseStack, submitNodeStorage);
            renderAllFeatures(featureRenderDispatcher, textureView, depthTextureView);
            modelViewStack.popMatrix();
            blitTexture(renderState, guiRenderState);
        }
    }

    protected void renderAllFeatures(
        FeatureRenderDispatcher featureRenderDispatcher,
        GpuTextureView color,
        GpuTextureView depth
    ) {
        Minecraft mc = Minecraft.getInstance();
        Lighting lighting = mc.gameRenderer.lighting();
        lighting.updateBuffer(Lighting.Entry.LEVEL, getLight0(), getLight1());
        lighting.setupFor(Lighting.Entry.LEVEL);
        FeaturePass.renderAllFeatures(featureRenderDispatcher, submitNodeStorage, color, depth);
        if (mc.level != null) {
            lighting.updateLevel(mc.level.dimensionType().cardinalLightType());
        } else {
            lighting.updateLevel(CardinalLighting.Type.DEFAULT);
        }
    }

    protected Vector3fc getLight0() {
        return DIFFUSE_LIGHT_0;
    }

    protected Vector3fc getLight1() {
        return DIFFUSE_LIGHT_1;
    }
}
