package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.math.MatrixMath;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TransformingVertexConsumer implements VertexConsumer {
    private @UnknownNullability VertexConsumer delegate;
    private @UnknownNullability PoseStack poseStack;

    public void setPoseStack(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public VertexConsumer wrap(VertexConsumer delegate) {
        this.delegate = delegate;
        return this;
    }

    public void clear() {
        delegate = null;
        poseStack = null;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        Matrix4f matrix = poseStack.last().pose();
        delegate.addVertex(
            MatrixMath.transformPositionX(matrix, x, y, z),
            MatrixMath.transformPositionY(matrix, x, y, z),
            MatrixMath.transformPositionZ(matrix, x, y, z)
        );
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        delegate.setUv3(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        Matrix3f matrix = poseStack.last().normal();
        delegate.setNormal(
            MatrixMath.transformNormalX(matrix, x, y, z),
            MatrixMath.transformNormalY(matrix, x, y, z),
            MatrixMath.transformNormalZ(matrix, x, y, z)
        );
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
