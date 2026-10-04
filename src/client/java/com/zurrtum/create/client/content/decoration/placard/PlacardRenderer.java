package com.zurrtum.create.client.content.decoration.placard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.content.decoration.placard.PlacardRenderer.PlacardRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.decoration.placard.PlacardBlockEntity;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRadiansRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;

public class PlacardRenderer implements BlockEntityRenderer<PlacardBlockEntity, PlacardRenderState> {
    protected final ItemModelResolver itemModelManager;

    public PlacardRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public boolean shouldRender(PlacardBlockEntity be, Vec3 cameraPosition) {
        return BlockEntityRenderer.super.shouldRender(be, cameraPosition) && !be.getHeldItem().isEmpty();
    }

    @Override
    public PlacardRenderState createRenderState() {
        return new PlacardRenderState();
    }

    @Override
    public void extractRenderState(
        PlacardBlockEntity be,
        PlacardRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        AttachFace face = state.blockState.getValue(PlacardBlock.FACE);
        ItemStackRenderState item = state.item = new ItemStackRenderState();
        item.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.appendItemLayers(item, be.getHeldItem(), item.displayContext, level, null, 0);
        boolean isCeiling = face == AttachFace.CEILING;
        Direction facing = state.blockState.getValue(PlacardBlock.FACING);
        if (isCeiling) {
            state.upAngle = getUpRadiansRotateAngle(Mth.PI + AngleHelper.rad(180 + AngleHelper.horizontalAngle(facing)));
            state.eastAngle = new Quaternionf().setAngleAxis(-Mth.HALF_PI, 1, 0, 0);
        } else {
            state.upAngle = getUpRotateAngle(180 + AngleHelper.horizontalAngle(facing));
            if (face == AttachFace.FLOOR) {
                state.eastAngle = new Quaternionf().setAngleAxis(Mth.HALF_PI, 1, 0, 0);
            }
        }
        state.scale = item.usesBlockLight() ? 0.5f : 0.375f;
    }

    @Override
    public void submit(
        PlacardRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (state.upAngle != null) {
            matrices.rotate(state.upAngle);
        }
        if (state.eastAngle != null) {
            matrices.rotate(state.eastAngle);
        }
        matrices.translate(0, 0, 0.28125f);
        float scale = state.scale;
        matrices.scale(scale, scale, scale);
        state.item.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    public static class PlacardRenderState extends BlockEntityRenderState {
        public @UnknownNullability ItemStackRenderState item;
        public @Nullable Quaternionf upAngle;
        public @Nullable Quaternionf eastAngle;
        public float scale;
    }
}
