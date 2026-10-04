package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricEmitterSupplier;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferEmitter.class)
public interface BufferEmitterMixin extends FabricEmitterSupplier {
    @Shadow(remap = false)
    VertexConsumer getBuffer(boolean shade, ChunkSectionLayer layer);

    @Override
    default @NonNull QuadEmitter quadEmitter() {
        return Renderer.get().quadEmitter(quad -> quad.buffer(
            OverlayTexture.NO_OVERLAY,
            getBuffer(quad.shadeDirectionOverride() == null, quad.chunkLayer())
        ));
    }
}
