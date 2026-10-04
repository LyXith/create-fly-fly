package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FanRenderer extends GuiBlockRenderer<FanRenderState> {
    @Override
    protected void renderToTexture(FanRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.rotate(Axis.XP.rotationDegrees(-15.5f));
        matrices.rotate(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.92f, -0.75f, -0.5f);
        matrices.scale(1, -1, 1);
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.rotate(Axis.ZP.rotationDegrees(getCurrentAngle() * 16));
        matrices.rotate(Axis.XP.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.ENCASED_FAN_INNER, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.pushPose();
        matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        CachedBuffers.block(AllBlocks.ENCASED_FAN.defaultBlockState()).submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0, 0, 2);
        BlockState blockState = state.target();
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) {
            Fluid fluid = fluidState.getType();
            FluidStateModelSet fluidStateModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
            FluidRenderHelper.extractFluidRenderState(
                null,
                null,
                fluidStateModelSet,
                fluid,
                DataComponentPatch.EMPTY,
                0,
                0,
                0,
                1,
                1,
                1,
                0,
                false,
                true
            ).submit(matrices, queue);
            return;
        }
        CachedBuffers.block(blockState).submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Fan";
    }

    @Override
    public Class<FanRenderState> getRenderStateClass() {
        return FanRenderState.class;
    }
}
