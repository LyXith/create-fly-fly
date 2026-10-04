package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.block.Blocks;

public class MillstoneRenderer extends GuiBlockRenderer<MillstoneRenderState> {
    @Override
    protected void renderToTexture(MillstoneRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.translate(-0.5f, -0.21f, -0.5f);
        matrices.scale(1, -1, -1);
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(Axis.XP.rotationDegrees(22.5f));
        matrices.rotate(Axis.YP.rotationDegrees(getCurrentAngle()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.MILLSTONE_COG, Blocks.AIR.defaultBlockState()).submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(Axis.XP.rotationDegrees(22.5f));
        matrices.rotate(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.block(AllBlocks.MILLSTONE.defaultBlockState()).submit(matrices, queue);
    }

    private static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360 * 2;
    }

    @Override
    protected String getTextureLabel() {
        return "Millstone";
    }

    @Override
    public Class<MillstoneRenderState> getRenderStateClass() {
        return MillstoneRenderState.class;
    }
}
