package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawRenderer extends GuiBlockRenderer<SawRenderState> {
    @Override
    protected void renderToTexture(SawRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.rotate(Axis.XP.rotationDegrees(-15.5f));
        matrices.rotate(Axis.YP.rotationDegrees(112.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1, -1, 1);

        matrices.pushPose();
        matrices.rotateAround(Axis.XP.rotationDegrees(getCurrentAngle()), 0.5f, 0.5f, 0.5f);
        CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X))
            .submit(matrices, queue);
        matrices.popPose();
        CachedBuffers.block(AllBlocks.MECHANICAL_SAW.defaultBlockState().setValue(SawBlock.FACING, Direction.UP))
            .submit(matrices, queue);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(Axis.ZP.rotationDegrees(-90));
        matrices.rotate(Axis.YP.rotationDegrees(-90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return -(AnimationTickHolder.getRenderTime() * 4.0f) % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Saw";
    }

    @Override
    public Class<SawRenderState> getRenderStateClass() {
        return SawRenderState.class;
    }

}
