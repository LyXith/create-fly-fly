package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.block.Blocks;

public class CrafterRenderer extends GuiBlockRenderer<CrafterRenderState> {
    @Override
    protected void renderToTexture(CrafterRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.rotate(Axis.XP.rotationDegrees(-15.5f));
        matrices.rotate(Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-0.5f, -0.16f, -0.5f);
        matrices.scale(1, -1, 1);
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(Axis.ZP.rotationDegrees(getCurrentAngle()));
        matrices.rotate(Axis.XP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.pushPose();
        matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        CachedBuffers.block(AllBlocks.MECHANICAL_CRAFTER.defaultBlockState()).submit(matrices, queue);
        matrices.popPose();
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Crafter";
    }

    @Override
    public Class<CrafterRenderState> getRenderStateClass() {
        return CrafterRenderState.class;
    }
}
