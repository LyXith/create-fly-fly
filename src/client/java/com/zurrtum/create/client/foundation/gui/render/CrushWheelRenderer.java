package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.block.Blocks;

public class CrushWheelRenderer extends GuiBlockRenderer<CrushWheelRenderState> {
    @Override
    protected void renderToTexture(CrushWheelRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.rotate(com.mojang.math.Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-1.5f, -0.6f, -0.5f);
        matrices.scale(1, -1, 1);
        SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.CRUSHING_WHEEL, Blocks.AIR.defaultBlockState());

        float angle = getCurrentAngle();
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(com.mojang.math.Axis.XP.rotationDegrees(-90));
        matrices.rotate(com.mojang.math.Axis.YP.rotationDegrees(angle));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        model.submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.translate(2, 0, 0);
        matrices.rotate(com.mojang.math.Axis.XP.rotationDegrees(-90));
        matrices.rotate(com.mojang.math.Axis.YP.rotationDegrees(-angle));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        model.submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    public Class<CrushWheelRenderState> getRenderStateClass() {
        return CrushWheelRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Crush Wheel";
    }
}
