package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class BasinBlazeBurnerRenderer extends GuiBlockRenderer<BasinBlazeBurnerRenderState> {
    @Override
    protected void renderToTexture(BasinBlazeBurnerRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Minecraft mc = Minecraft.getInstance();
        matrices.scale(1, 1, -1);
        matrices.rotate(Axis.XP.rotationDegrees(-15.5f));
        matrices.rotate(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        matrices.scale(1, -1, 1);
        float offset = -(Mth.sin(AnimationTickHolder.getRenderTime() / 16.0f) + 0.5f) / 16.0f;
        CachedBuffers.block(AllBlocks.BLAZE_BURNER.defaultBlockState()).submit(matrices, queue);

        matrices.pushPose();
        matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        boolean seething = state.heat() == HeatLevel.SEETHING;
        CachedBuffers.partial(
            seething ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE,
            Blocks.AIR.defaultBlockState()
        ).submit(matrices, queue);
        matrices.translate(0, offset, 0);
        CachedBuffers.partial(
            seething ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2,
            Blocks.AIR.defaultBlockState()
        ).submit(matrices, queue);
        matrices.popPose();

        SpriteShiftEntry spriteShift = seething ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();

        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

        float time = AnimationTickHolder.getRenderTime(mc.level);
        float speed = 1 / 32.0f + 1 / 64.0f * state.heat().ordinal();

        float progress = speed * time;
        double vScroll = progress - Math.floor(progress);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = progress / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, Blocks.AIR.defaultBlockState())
            .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll).submit(matrices, queue);
    }

    @Override
    protected String getTextureLabel() {
        return "Blaze Burner";
    }

    @Override
    public Class<BasinBlazeBurnerRenderState> getRenderStateClass() {
        return BasinBlazeBurnerRenderState.class;
    }
}
