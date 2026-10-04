package com.zurrtum.create.client.content.contraptions.chassis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.chassis.StickerRenderer.StickerRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class StickerRenderer implements BlockEntityRenderer<StickerBlockEntity, StickerRenderState> {
    public StickerRenderer(Context context) {
    }

    @Override
    public StickerRenderState createRenderState() {
        return new StickerRenderState();
    }

    @Override
    public void extractRenderState(
        StickerBlockEntity be,
        StickerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.head = CachedBuffers.partial(AllPartialModels.STICKER_HEAD, state.blockState).cardinalLighting(level)
            .light(state.lightCoords).extractRenderState();
        Direction facing = state.blockState.getValue(StickerBlock.FACING);
        state.yRot = KineticBlockEntityRenderer.getYRotateAngle(AngleHelper.horizontalAngle(facing));
        state.xRot = KineticBlockEntityRenderer.getXRotateAngle(AngleHelper.verticalAngle(facing) + 90);
        float offset;
        if (!be.isVirtual() && level != Minecraft.getInstance().level) {
            offset = state.blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;
        } else {
            offset = be.piston.getValue(AnimationTickHolder.getPartialTicks(level));
        }
        state.offset = offset * offset * 0.25f;
        state.nudge = SmartBlockEntityRenderer.createNudge(be.hashCode());
    }

    @Override
    public void submit(
        StickerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        matrices.translate(state.nudge);
        if (state.yRot != null || state.xRot != null) {
            matrices.translate(0.5f, 0.5f, 0.5f);
            if (state.yRot != null) {
                matrices.rotate(state.yRot);
            }
            if (state.xRot != null) {
                matrices.rotate(state.xRot);
            }
            matrices.translate(-0.5f, -0.5f, -0.5f);
        }
        matrices.translate(0, state.offset, 0);
        state.head.submit(matrices, queue);
    }

    public static class StickerRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState head;
        public @UnknownNullability Vec3 nudge;
        public @Nullable Quaternionf yRot;
        public @Nullable Quaternionf xRot;
        public float offset;
    }
}
