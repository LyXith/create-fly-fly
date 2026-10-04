package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class LargeBogeyRenderState extends StandardBogeyRenderState {
    public @UnknownNullability SuperByteBufferRenderState secondaryShaft;
    public @UnknownNullability SuperByteBufferRenderState drive;
    public @UnknownNullability SuperByteBufferRenderState belt;
    public @UnknownNullability SuperByteBufferRenderState piston;
    public float pistonOffset;
    public @UnknownNullability SuperByteBufferRenderState wheels;
    public @UnknownNullability SuperByteBufferRenderState pin;
    public @Nullable Quaternionf wheelAngle;
    public @UnknownNullability Quaternionf wheelAngleInvert;

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        super.submit(matrices, queue);
        matrices.pushPose();
        if (wheelAngle != null) {
            matrices.last().normal().rotate(wheelAngle);
            matrices.pushPose();
            matrices.translate(-0.5f, 0.25f, 0.5f);
            matrices.last().pose().rotateAround(wheelAngle, 0.5f, 0.5f, 0.5f);
            secondaryShaft.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(-0.5f, 0.25f, -1.5f);
            matrices.last().pose().rotateAround(wheelAngle, 0.5f, 0.5f, 0.5f);
            secondaryShaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            matrices.translate(-0.5f, 0.25f, 0.5f);
            secondaryShaft.submit(matrices, queue);
            matrices.translate(0, 0, -2);
            secondaryShaft.submit(matrices, queue);
        }
        matrices.popPose();
        matrices.pushPose();
        matrices.scale(0.998046875f, 0.998046875f, 0.998046875f);
        drive.submit(matrices, queue);
        belt.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, 0, pistonOffset);
        piston.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, 1, 0);
        if (wheelAngle != null) {
            matrices.rotate(wheelAngle);
            wheels.submit(matrices, queue);
            matrices.translate(0, 0.25f, 0);
            matrices.rotate(wheelAngleInvert);
        } else {
            wheels.submit(matrices, queue);
            matrices.translate(0, 0.25f, 0);
        }
        pin.submit(matrices, queue);
        matrices.popPose();
    }
}
