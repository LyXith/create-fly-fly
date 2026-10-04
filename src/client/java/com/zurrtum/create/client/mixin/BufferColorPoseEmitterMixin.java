package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferColorPoseEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferPoseEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricEmitterSupplier;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferColorPoseEmitter.class)
public interface BufferColorPoseEmitterMixin extends BufferPoseEmitter, FabricEmitterSupplier {
    @Shadow(remap = false)
    int getColor();

    @Override
    default @NonNull QuadEmitter quadEmitter() {
        int color = getColor();
        Pose pose = getPose();
        return Renderer.get().quadEmitter(quad -> {
            quad.multiplyColor(color);
            quad.buffer(OverlayTexture.NO_OVERLAY, pose, getBuffer(quad.shadeDirectionOverride() == null, quad.chunkLayer()));
        });
    }
}
