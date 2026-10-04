package com.zurrtum.create.client.content.redstone.deskBell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.redstone.deskBell.DeskBellRenderer.DeskBellRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public class DeskBellRenderer implements BlockEntityRenderer<DeskBellBlockEntity, DeskBellRenderState> {
    public DeskBellRenderer(Context context) {
    }

    @Override
    public DeskBellRenderState createRenderState() {
        return new DeskBellRenderState();
    }

    @Override
    public void extractRenderState(
        DeskBellBlockEntity be,
        DeskBellRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        float p = be.animation.getValue(tickProgress);
        BlockState blockState = be.getBlockState();
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        float f = (float) (1 - 4 * Math.pow(Math.max(p - 0.5, 0) - 0.5, 2));
        float f2 = (float) Math.pow(p, 1.25f);
        Direction facing = blockState.getValue(DeskBellBlock.FACING);
        state.yRot = KineticBlockEntityRenderer.getYRotateAngle(AngleHelper.horizontalAngle(facing));
        state.xRot = KineticBlockEntityRenderer.getXRotateAngle(AngleHelper.verticalAngle(facing) + 90);
        state.plunger = CachedBuffers.partial(AllPartialModels.DESK_BELL_PLUNGER, blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.plungerOffset = f * -0.046875f;
        state.bell = CachedBuffers.partial(AllPartialModels.DESK_BELL_BELL, blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.bellOffset = -1 / 16;
        float offset = p * Mth.PI * 4 + be.animationOffset;
        state.bellXRot = Axis.XP.rotation(Mth.DEG_TO_RAD * (f2 * 8 * Mth.sin(offset)));
        state.bellZRot = Axis.ZP.rotation(Mth.DEG_TO_RAD * (f2 * 8 * Mth.cos(offset)));
    }

    @Override
    public void submit(
        DeskBellRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (state.yRot != null) {
            matrices.rotate(state.yRot);
        }
        if (state.xRot != null) {
            matrices.rotate(state.xRot);
        }
        matrices.pushPose();
        matrices.translate(-0.5f, state.plungerOffset - 0.5f, -0.5f);
        state.plunger.submit(matrices, queue);
        matrices.popPose();
        matrices.translate(0, state.bellOffset, 0);
        matrices.rotate(state.bellXRot);
        matrices.rotate(state.bellZRot);
        matrices.translate(-0.5f, -state.bellOffset - 0.5f, -0.5f);
        state.bell.submit(matrices, queue);
    }

    public static class DeskBellRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState plunger;
        public @UnknownNullability SuperByteBufferRenderState bell;
        public @Nullable Quaternionfc yRot;
        public @Nullable Quaternionfc xRot;
        public @UnknownNullability Quaternionfc bellXRot;
        public @UnknownNullability Quaternionfc bellZRot;
        public float plungerOffset;
        public int bellOffset;
    }
}
