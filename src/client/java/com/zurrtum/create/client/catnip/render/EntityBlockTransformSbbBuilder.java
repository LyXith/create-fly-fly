package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferPoseEmitter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;

public class EntityBlockTransformSbbBuilder extends EntityBlockSbbBuilder implements BufferPoseEmitter {
    private final Pose origin = new Pose();
    private final Pose target = new Pose();

    public EntityBlockTransformSbbBuilder wrap(Pose pose) {
        origin.set(pose);
        return this;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        VertexConsumer buffer = getBuffer(info.shadeDirectionOverride() == null, info.layer());
        if (x != 0 || y != 0 || z != 0) {
            target.set(origin);
            target.translate(x, y, z);
            buffer.putBakedQuad(target, quad, instance);
        } else {
            buffer.putBakedQuad(origin, quad, instance);
        }
    }

    @Override
    public Pose getPose() {
        return origin;
    }
}
