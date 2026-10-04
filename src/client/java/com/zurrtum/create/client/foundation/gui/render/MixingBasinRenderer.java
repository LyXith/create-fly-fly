package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class MixingBasinRenderer extends GuiBlockRenderer<MixingBasinRenderState> {
    @Override
    protected void renderToTexture(MixingBasinRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.rotate(Axis.XP.rotationDegrees(-15.5f));
        matrices.rotate(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.8f, -0.5f);
        matrices.scale(1, -1, 1);

        float time = AnimationTickHolder.getRenderTime();
        float angle = getCurrentAngle(time);
        CachedBuffers.block(AllBlocks.MECHANICAL_MIXER.defaultBlockState()).submit(matrices, queue);

        matrices.pushPose();
        matrices.rotateAround(Axis.YP.rotationDegrees(angle * 2), 0.5f, 0.5f, 0.5f);
        CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.rotateAround(Axis.YP.rotationDegrees(angle * 4), 0.5f, 0.5f, 0.5f);
        CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_HEAD, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0, -1.65f, 0);
        CachedBuffers.block(AllBlocks.BASIN.defaultBlockState()).submit(matrices, queue);
    }

    private static float getCurrentAngle(float time) {
        return time * 4.0f % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        return -((Mth.sin(time / 32.0f) + 1) / 5 + 0.5f);
    }

    @Override
    protected String getTextureLabel() {
        return "Mixing Basin";
    }

    @Override
    public Class<MixingBasinRenderState> getRenderStateClass() {
        return MixingBasinRenderState.class;
    }
}