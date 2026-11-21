package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BakedItemModelBufferer {
    static final List<RenderType> CHUNK_LAYERS = List.of(
        Sheets.cutoutBlockItemSheet(),
        Sheets.translucentItemSheet()
    );

    /**
     * Bakes the item into a set of meshes.
     *
     * @return whether the item only uses regular item quads. Items rendered through special model
     * renderers (models, custom geometry, block models, ...) return false and must keep using
     * vanilla rendering.
     */
    public static boolean bufferItemStack(
        ItemStack stack,
        BlockAndTintGetter level,
        ItemDisplayContext displayContext,
        ResultConsumer resultConsumer,
        MeshResultConsumer meshResultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        PoseStack poseStack = objects.identityPoseStack;
        ClientLevel world = level instanceof ClientLevel clientWorld ? clientWorld : null;
        ItemStackRenderState state = objects.state;
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, displayContext, world, null, 0);
        MeshSubmitNodeCollector collector = objects.collector;
        collector.begin(resultConsumer);
        state.submit(poseStack, collector, 0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);
        collector.end();
        return collector.isSupported();
    }

    public interface ResultConsumer {
        void accept(RenderType renderType, boolean shaded, com.mojang.blaze3d.vertex.MeshData data);
    }

    public interface MeshResultConsumer {
        void accept(RenderType renderType, com.zurrtum.create.client.flywheel.api.material.Material material, Mesh mesh, boolean translucent);
    }

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    public static Map<RenderType, Integer> getChunkLayers() {
        return THREAD_LOCAL_OBJECTS.get().chunkLayers;
    }

    private static int getLayerColorSafe(int[] tintLayers, MaterialInfo info) {
        if (!info.isTinted()) {
            return -1;
        }
        int tintIndex = info.tintIndex();
        if (tintIndex < 0 || tintIndex >= tintLayers.length) {
            return -1;
        }
        return tintLayers[tintIndex];
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();
        public final ItemStackRenderState state = new ItemStackRenderState();
        public final MeshSubmitNodeCollector collector = new MeshSubmitNodeCollector();
        public final Map<RenderType, Integer> chunkLayers = new HashMap<>();

        {
            for (int i = 0, size = CHUNK_LAYERS.size(); i < size; i++) {
                chunkLayers.put(CHUNK_LAYERS.get(i), i);
            }
        }
    }

    /**
     * Collects item quads into Flywheel meshes. Every other submit method is treated as
     * unsupported geometry, which makes the whole item fall back to vanilla rendering.
     */
    private static class MeshSubmitNodeCollector implements SubmitNodeCollector {
        private final Map<RenderType, ItemMeshEmitter> emitters = new HashMap<>();
        private final QuadInstance quadInstance = new QuadInstance();
        private ResultConsumer resultConsumer;
        private boolean supported;

        private void begin(ResultConsumer resultConsumer) {
            this.resultConsumer = resultConsumer;
            supported = true;
            emitters.clear();
        }

        private void end() {
            for (ItemMeshEmitter emitter : emitters.values()) {
                emitter.end();
            }
            emitters.clear();
            resultConsumer = null;
        }

        private boolean isSupported() {
            return supported;
        }

        private void unsupported() {
            supported = false;
        }

        private ItemMeshEmitter emitter(RenderType renderType) {
            ItemMeshEmitter emitter = emitters.get(renderType);
            if (emitter == null) {
                emitter = new ItemMeshEmitter(renderType);
                emitter.prepare(resultConsumer, (type, mesh, translucent) -> {
                });
                emitters.put(renderType, emitter);
            }
            return emitter;
        }

        @Override
        public void submitItem(
            PoseStack poseStack,
            ItemDisplayContext displayContext,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            int[] tintLayers,
            ItemQuads quads,
            FoilType foilType
        ) {
            if (quads.isEmpty()) {
                return;
            }
            PoseStack.Pose pose = poseStack.last();
            for (BakedQuad quad : quads.all()) {
                MaterialInfo info = quad.materialInfo();
                RenderType renderType = switch (foilType) {
                    case NONE -> info.itemRenderType();
                    case STANDARD -> info.itemGlintRenderType();
                    case SPECIAL -> info.itemGlintSpecialRenderType();
                };
                quadInstance.setLightCoords(lightCoords);
                quadInstance.setOverlayCoords(overlayCoords);
                quadInstance.setColor(getLayerColorSafe(tintLayers, info));
                emitter(renderType).putBakedQuad(pose, quad, quadInstance);
            }
        }

        @Override
        public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer renderer) {
            unsupported();
        }

        @Override
        public void submitBlockModel(
            PoseStack poseStack,
            RenderType renderType,
            List<BlockStateModelPart> parts,
            int[] tintLayers,
            int lightCoords,
            int overlayCoords,
            int outlineColor
        ) {
            unsupported();
        }

        @Override
        public void submitBreakingBlockModel(
            PoseStack poseStack,
            List<BlockStateModelPart> parts,
            int progress,
            boolean allLayers
        ) {
            unsupported();
        }

        @Override
        public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState state, int overlayCoords) {
            unsupported();
        }

        @Override
        public <S> void submitModel(
            net.minecraft.client.model.Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            UvMapping uvMapping,
            int color
        ) {
            unsupported();
        }

        @Override
        public <S> void submitCrumblingOverlay(
            net.minecraft.client.model.Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            CrumblingOverlay overlay
        ) {
            unsupported();
        }

        @Override
        public void submitShadow(
            PoseStack poseStack,
            float radius,
            List<net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece> pieces
        ) {
            unsupported();
        }

        @Override
        public void submitNameTag(
            PoseStack poseStack,
            Vec3 pos,
            int color,
            Component text,
            boolean seeThrough,
            int lightCoords,
            CameraRenderState cameraState
        ) {
            unsupported();
        }

        @Override
        public void submitText(
            PoseStack poseStack,
            float x,
            float y,
            FormattedCharSequence text,
            boolean shadow,
            net.minecraft.client.gui.Font.DisplayMode displayMode,
            int lightCoords,
            int color,
            int backgroundColor,
            int outlineColor
        ) {
            unsupported();
        }

        @Override
        public void submitTextBackground(
            PoseStack poseStack,
            float x,
            float y,
            float width,
            float height,
            int color,
            net.minecraft.client.gui.Font.DisplayMode displayMode,
            int lightCoords
        ) {
            unsupported();
        }

        @Override
        public void submitFlame(PoseStack poseStack, net.minecraft.client.renderer.entity.state.EntityRenderState state, Quaternionf rotation) {
            unsupported();
        }

        @Override
        public void submitLeash(PoseStack poseStack, net.minecraft.client.renderer.entity.state.EntityRenderState.LeashState leash) {
            unsupported();
        }

        @Override
        public void submitShapeOutline(
            PoseStack poseStack,
            VoxelShape shape,
            RenderType renderType,
            int color,
            float lineWidth,
            boolean throughBlocks
        ) {
            unsupported();
        }

        @Override
        public void submitQuadParticleGroup(QuadParticleRenderState particles) {
            unsupported();
        }

        @Override
        public void submitGizmoPrimitives(
            DrawableGizmoPrimitives.Group group,
            CameraRenderState cameraState,
            boolean throughBlocks
        ) {
            unsupported();
        }

        @Override
        public OrderedSubmitNodeCollector order(int order) {
            return this;
        }
    }
}
