package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

public class BlazeBurnerElementRenderer extends GuiBlockRenderer<BlazeBurnerRenderState> {
    @Override
    protected void renderToTexture(BlazeBurnerRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.rotate(Axis.XP.rotationDegrees(-22.5f));
        matrices.rotate(Axis.YP.rotationDegrees(-45));
        matrices.scale(1, -1, 1);
        float horizontalAngle = AngleHelper.rad(270);
        boolean canDrawFlame = state.heatLevel().isAtLeast(HeatLevel.FADING);
        PartialModel drawHat = AllPartialModels.LOGISTICS_HAT;

        CachedBuffers.partial(AllPartialModels.BLAZE_CAGE, state.block())
            .rotateCentered(horizontalAngle + Mth.PI, Direction.UP).light(LightCoordsUtil.FULL_BRIGHT)
            .submit(matrices, queue);
        BlazeBurnerRenderer.getBlazeBurnerRenderData(
            null,
            state.block(),
            state.heatLevel(),
            state.animation(),
            horizontalAngle,
            canDrawFlame,
            state.drawGoggles(),
            drawHat,
            state.hash()
        ).submit(matrices, queue);
    }

    @Override
    protected float getTranslateY(int height, int windowScaleFactor) {
        return height / 1.6f;
    }

    @Override
    protected String getTextureLabel() {
        return "Blaze Burner";
    }

    @Override
    public Class<BlazeBurnerRenderState> getRenderStateClass() {
        return BlazeBurnerRenderState.class;
    }
}
