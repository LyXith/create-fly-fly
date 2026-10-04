package com.zurrtum.create.client.content.kinetics.steamEngine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineRenderer.SteamEngineRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity, SteamEngineRenderState> {
    public SteamEngineRenderer(Context context) {
    }

    @Override
    public boolean shouldRender(SteamEngineBlockEntity blockEntity, Vec3 cameraPosition) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPosition) && hasTargetAngle(blockEntity);
    }

    @Override
    public SteamEngineRenderState createRenderState() {
        return new SteamEngineRenderState();
    }

    @Override
    public void extractRenderState(
        SteamEngineBlockEntity be,
        SteamEngineRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = SteamEngineBlock.getFacing(state.blockState);
        Direction.Axis facingAxis = facing.getAxis();
        PoweredShaftBlockEntity shaft = be.getShaft();
        Direction.Axis axis = getRotationAxisOf(shaft);
        if (facingAxis.isHorizontal() && axis == Direction.Axis.Y || facingAxis.isVertical() && axis == Direction.Axis.Z) {
            state.rollAngle = Axis.YP.rotation(Mth.DEG_TO_RAD * -90);
        }
        float angle = getAngleForBe(shaft, shaft.getBlockPos(), axis);
        if (axis.isHorizontal() && facingAxis == Direction.Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE) {
            angle *= -1;
        }
        if (axis == Direction.Axis.X && facing == Direction.DOWN) {
            angle *= -1;
        }
        float sinAngle = Mth.sin(angle);
        float cosAngle = Mth.cos(angle);
        float piston = 0.375f * sinAngle - Mth.sqrt(Mth.square(0.875f) - Mth.square(0.375f) * Mth.square(cosAngle));
        float distance = Mth.sqrt(Mth.square(piston - 0.375f * sinAngle)) / 0.875f;
        state.piston = CachedBuffers.partial(AllPartialModels.ENGINE_PISTON, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.linkage = CachedBuffers.partial(AllPartialModels.ENGINE_LINKAGE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.connector = CachedBuffers.partial(AllPartialModels.ENGINE_CONNECTOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.yRot = getYRotateAngle(AngleHelper.horizontalAngle(facing));
        state.xRot = getXRotateAngle(AngleHelper.verticalAngle(facing) + 90);
        state.linkageRotate = Axis.XP.rotation((float) (cosAngle >= 0 ? Math.acos(distance) : -Math.acos(distance)));
        state.pistonTranslate = piston + 0.75f;
        state.connectorRotate = Axis.XP.rotation(-(angle + Mth.HALF_PI));
    }

    @Override
    public void submit(
        SteamEngineRenderState state,
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
        if (state.rollAngle != null) {
            matrices.rotate(state.rollAngle);
        }
        matrices.pushPose();
        matrices.translate(-0.5f, state.pistonTranslate, -0.5f);
        state.piston.submit(matrices, queue);
        matrices.translate(0, 1.0f, 0);
        matrices.rotateAround(state.linkageRotate, 0, 0.25f, 0.5f);
        state.linkage.submit(matrices, queue);
        matrices.popPose();
        matrices.translate(-0.5f, 1.5f, -0.5f);
        matrices.rotateAround(state.connectorRotate, 0.5f, 0.5f, 0.5f);
        state.connector.submit(matrices, queue);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static boolean hasTargetAngle(SteamEngineBlockEntity be) {
        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft == null) {
            return false;
        }
        Direction.Axis axis = getRotationAxisOf(shaft);
        return axis != SteamEngineBlock.getFacing(be.getBlockState()).getAxis();
    }

    @Nullable
    public static Float getTargetAngle(SteamEngineBlockEntity be) {
        BlockState blockState = be.getBlockState();
        if (!blockState.is(AllBlocks.STEAM_ENGINE)) {
            return null;
        }
        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft == null) {
            return null;
        }
        Direction facing = SteamEngineBlock.getFacing(blockState);
        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis axis = getRotationAxisOf(shaft);
        if (axis == facingAxis) {
            return null;
        }
        float angle = getAngleForBe(shaft, shaft.getBlockPos(), axis);
        if (axis.isHorizontal() && facingAxis == Direction.Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE) {
            angle *= -1;
        }
        if (axis == Direction.Axis.X && facing == Direction.DOWN) {
            angle *= -1;
        }
        return angle;
    }

    public static class SteamEngineRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState piston;
        public @UnknownNullability SuperByteBufferRenderState linkage;
        public @UnknownNullability SuperByteBufferRenderState connector;
        public @Nullable Quaternionf yRot;
        public @Nullable Quaternionf xRot;
        public @Nullable Quaternionf rollAngle;
        public @UnknownNullability Quaternionf linkageRotate;
        public float pistonTranslate;
        public @UnknownNullability Quaternionf connectorRotate;
    }
}
