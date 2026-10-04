package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmRenderer.ArmRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class ArmRenderer implements BlockEntityRenderer<ArmBlockEntity, ArmRenderState> {
    protected ItemModelResolver itemModelManager;

    public ArmRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public ArmRenderState createRenderState() {
        return new ArmRenderState();
    }

    @Override
    public void extractRenderState(
        ArmBlockEntity be,
        ArmRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        ItemStack heldItem = be.heldItem;
        if (VisualizationManager.supportsVisualization(level)) {
            if (heldItem.isEmpty()) {
                return;
            }
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            ArmItemData item = state.item = new ArmItemData();
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, heldItem, renderState.displayContext, level, null, 0);
            item.state = renderState;
            item.xRot = Axis.XP.rotation(RAD_90);
            if (heldItem.getItem() instanceof BlockItem && renderState.usesBlockLight()) {
                item.offset = -0.5625f;
                item.scale = 0.5f;
            } else {
                item.offset = -0.625f;
                item.scale = 0.625f;
            }
            if (state.blockState.getValue(ArmBlock.CEILING)) {
                state.rotate = Axis.XP.rotation(RAD_180);
            }
            if (be.phase != Phase.DANCING || be.getSpeed() == 0) {
                state.baseAngle = getYRotateAngle(be.baseAngle.getValue(tickProgress));
                state.lowerArmAngle = getXRotateAngle(be.lowerArmAngle.getValue(tickProgress));
                state.upperArmAngle = getXRotateAngle(be.upperArmAngle.getValue(tickProgress) - 180);
                state.headAngle = getXRotateAngle(be.headAngle.getValue(tickProgress) - 45);
            } else {
                float renderTick = AnimationTickHolder.getRenderTime(level) + be.hashCode() % 64;
                state.baseAngle = getYRotateAngle(renderTick * 10 % 360);
                float lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
                state.lowerArmAngle = getXRotateAngle(lowerArmAngle + 135);
                state.upperArmAngle = getXRotateAngle(Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95) - 90);
                state.headAngle = getXRotateAngle(-lowerArmAngle - 45);
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.cog = CachedBuffers.partial(AllPartialModels.ARM_COG, state.blockState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        float time = AnimationTickHolder.getRenderTime(level);
        Direction.Axis axis = getRotationAxisOf(state.blockState);
        float offset = KineticBlockEntityVisual.rotationOffset(state.blockState, axis, state.blockPos);
        float speed = be.getSpeed();
        float progress = getProgress(speed, time);
        state.angle = getRotateAngle(progress, offset, axis);
        ArmRenderData arm = state.arm = new ArmRenderData();
        if (heldItem.isEmpty()) {
            arm.clawOffset = 0.0625f;
        } else {
            ArmItemData item = state.item = new ArmItemData();
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, heldItem, renderState.displayContext, level, null, 0);
            item.state = renderState;
            item.xRot = Axis.XP.rotation(RAD_90);
            if (heldItem.getItem() instanceof BlockItem && renderState.usesBlockLight()) {
                item.offset = -0.5625f;
                item.scale = 0.5f;
                arm.clawOffset = 0.1875f;
            } else {
                item.offset = -0.625f;
                item.scale = 0.625f;
                arm.clawOffset = 0.078125f;
            }
        }
        if (state.blockState.getValue(ArmBlock.CEILING)) {
            state.rotate = Axis.XP.rotation(RAD_180);
            if (be.goggles) {
                arm.inverted = Axis.ZP.rotation(RAD_180);
            }
        }
        SuperByteBuffer lower = CachedBuffers.partial(AllPartialModels.ARM_LOWER_BODY, state.blockState);
        SuperByteBuffer upper = CachedBuffers.partial(AllPartialModels.ARM_UPPER_BODY, state.blockState);
        if (be.phase != Phase.DANCING || speed == 0) {
            state.baseAngle = getYRotateAngle(be.baseAngle.getValue(tickProgress));
            state.lowerArmAngle = getXRotateAngle(be.lowerArmAngle.getValue(tickProgress));
            state.upperArmAngle = getXRotateAngle(be.upperArmAngle.getValue(tickProgress) - 180);
            state.headAngle = getXRotateAngle(be.headAngle.getValue(tickProgress) - 45);
        } else {
            Color color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100);
            lower.color(color);
            upper.color(color);
            float renderTick = time + be.hashCode() % 64;
            state.baseAngle = getYRotateAngle(renderTick * 10 % 360);
            float lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
            state.lowerArmAngle = getXRotateAngle(lowerArmAngle + 135);
            state.upperArmAngle = getXRotateAngle(Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95) - 90);
            state.headAngle = getXRotateAngle(-lowerArmAngle - 45);
        }
        arm.base = CachedBuffers.partial(AllPartialModels.ARM_BASE, state.blockState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        arm.lower = lower.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.upper = upper.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.claw = CachedBuffers.partial(
            be.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE,
            state.blockState
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.clawUpper = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.clawLower = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        ArmRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.cog == null) {
            matrices.translate(0.5f, 0.5f, 0.5f);
            if (state.rotate != null) {
                matrices.rotate(state.rotate);
            }
            matrices.translate(0, 0.25f, 0);
            if (state.baseAngle != null) {
                matrices.rotate(state.baseAngle);
            }
            matrices.translate(0, 0.125f, 0);
            if (state.lowerArmAngle != null) {
                matrices.rotate(state.lowerArmAngle);
            }
            matrices.translate(0, 0, -0.875f);
            if (state.upperArmAngle != null) {
                matrices.rotate(state.upperArmAngle);
            }
            matrices.translate(0, 0, -0.9375f);
            if (state.headAngle != null) {
                matrices.rotate(state.headAngle);
            }
            state.item.render(matrices, queue, state.lightCoords);
            return;
        }
        if (state.angle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
            state.cog.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.cog.submit(matrices, queue);
        }
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (state.rotate != null) {
            matrices.rotate(state.rotate);
        }
        matrices.translate(0, 0.25f, 0);
        if (state.baseAngle != null) {
            matrices.rotate(state.baseAngle);
        }
        ArmRenderData arm = state.arm;
        arm.base.submit(matrices, queue);
        matrices.translate(0, 0.125f, 0);
        if (state.lowerArmAngle != null) {
            matrices.rotate(state.lowerArmAngle);
        }
        arm.lower.submit(matrices, queue);
        matrices.translate(0, 0, -0.875f);
        if (state.upperArmAngle != null) {
            matrices.rotate(state.upperArmAngle);
        }
        arm.upper.submit(matrices, queue);
        matrices.translate(0, 0, -0.9375f);
        if (state.headAngle != null) {
            matrices.rotate(state.headAngle);
        }
        if (arm.inverted != null) {
            matrices.rotate(arm.inverted);
            arm.claw.submit(matrices, queue);
            matrices.rotate(arm.inverted);
        } else {
            arm.claw.submit(matrices, queue);
        }
        matrices.pushPose();
        matrices.translate(0, -state.arm.clawOffset, -0.375f);
        arm.clawLower.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, state.arm.clawOffset, -0.375f);
        arm.clawUpper.submit(matrices, queue);
        matrices.popPose();
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightCoords);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class ArmRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState cog;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf rotate;
        public @Nullable Quaternionf baseAngle;
        public @Nullable Quaternionf lowerArmAngle;
        public @Nullable Quaternionf upperArmAngle;
        public @Nullable Quaternionf headAngle;
        public @UnknownNullability ArmRenderData arm;
        public @Nullable ArmItemData item;
    }

    public static class ArmItemData {
        public @UnknownNullability ItemStackRenderState state;
        public @UnknownNullability Quaternionf xRot;
        public float offset;
        public float scale;

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.rotate(xRot);
            matrices.translate(0, offset, 0);
            matrices.scale(scale, scale, scale);
            state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static class ArmRenderData {
        public @UnknownNullability SuperByteBufferRenderState base;
        public @UnknownNullability SuperByteBufferRenderState lower;
        public @UnknownNullability SuperByteBufferRenderState upper;
        public @UnknownNullability SuperByteBufferRenderState claw;
        public @UnknownNullability SuperByteBufferRenderState clawUpper;
        public @UnknownNullability SuperByteBufferRenderState clawLower;
        public @Nullable Quaternionf inverted;
        public float clawOffset;
    }
}
