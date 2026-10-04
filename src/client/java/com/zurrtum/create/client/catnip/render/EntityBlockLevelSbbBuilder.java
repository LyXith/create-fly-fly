package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;

public class EntityBlockLevelSbbBuilder extends EntityBlockSbbBuilder implements FluidRenderer.Output {
    private int fluidX, fluidY, fluidZ;
    private final FluidTemplateMeshBuffer[] fluidBuffers = new FluidTemplateMeshBuffer[]{
        new FluidTemplateMeshBuffer(buffers[0]),
        new FluidTemplateMeshBuffer(buffers[1]),
        new FluidTemplateMeshBuffer(buffers[2]),
    };

    public void prepareForFluid(BlockPos pos) {
        fluidX = pos.getX() & 0xFFFFFFF0;
        fluidY = pos.getY() & 0xFFFFFFF0;
        fluidZ = pos.getZ() & 0xFFFFFFF0;
    }

    @Override
    public VertexConsumer getBuilder(ChunkSectionLayer layer) {
        return fluidBuffers[layer.ordinal()];
    }

    private class FluidTemplateMeshBuffer implements VertexConsumer {
        private final TemplateMeshBuffer buffer;

        public FluidTemplateMeshBuffer(TemplateMeshBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlayCoords,
            int lightCoords,
            float nx,
            float ny,
            float nz
        ) {
            buffer.addVertex(fluidX + x, fluidY + y, fluidZ + z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setColor(int color) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            throw new UnsupportedOperationException("FluidTemplateMeshBuffer only supports addVertex!");
        }
    }
}
