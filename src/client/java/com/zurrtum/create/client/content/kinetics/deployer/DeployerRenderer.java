package com.zurrtum.create.client.content.kinetics.deployer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.deployer.DeployerRenderer.DeployerRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.State;
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
import net.minecraft.tags.ItemTags;
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
import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerRenderer implements BlockEntityRenderer<DeployerBlockEntity, DeployerRenderState> {
    protected final ItemModelResolver itemModelManager;

    public DeployerRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public DeployerRenderState createRenderState() {
        return new DeployerRenderState();
    }

    @Override
    public void extractRenderState(
        DeployerBlockEntity be,
        DeployerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        updateItemRenderState(be, state, itemModelManager, level, tickProgress);
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            state.blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
        );
        updateComponentsRenderState(be, state, level, tickProgress);
    }

    public static void updateItemRenderState(
        DeployerBlockEntity be,
        DeployerRenderState state,
        ItemModelResolver itemModelManager,
        Level world,
        float tickProgress
    ) {
        ItemStack heldItem = be.heldItem;
        if (heldItem.isEmpty()) {
            return;
        }
        Direction facing = state.blockState.getValue(FACING);
        Vec3 offset = getHandOffset(state, be, tickProgress, facing).add(VecHelper.CENTER_OF_ORIGIN);
        state.item = DeployerItemRenderState.create(be, itemModelManager, world, heldItem, offset, facing);
    }

    public static void updateComponentsRenderState(
        DeployerBlockEntity be,
        DeployerRenderState state,
        Level level,
        float tickProgress
    ) {
        if (VisualizationManager.supportsVisualization(level)) {
            return;
        }
        Direction facing = state.blockState.getValue(FACING);
        ComponentsRenderState components = state.components = new ComponentsRenderState();
        Direction.Axis axis = getRotationAxisOf(state.blockState);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        components.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        components.pole = CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        components.hand = CachedBuffers.partial(getHandPose(be), state.blockState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        components.angle = getRotateAngleWithoutBeOffset(axis, be, state, level);
        components.offset = getHandOffset(state, be, tickProgress, facing);
        components.yRot = getUpRotateAngle(AngleHelper.horizontalAngle(facing));
        if (facing == Direction.UP) {
            components.xRot = new Quaternionf().setAngleAxis(RAD_270, 1, 0, 0);
        } else if (facing == Direction.DOWN) {
            components.xRot = new Quaternionf().setAngleAxis(RAD_90, 1, 0, 0);
        }
        if (state.blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) {
            components.zRot = new Quaternionf().setAngleAxis(RAD_90, 0, 0, 1);
        }
    }

    @Override
    public void submit(
        DeployerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightCoords);
        }
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.components != null) {
            state.components.submit(matrices, queue);
        }
    }

    public static PartialModel getHandPose(DeployerBlockEntity be) {
        return be.mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING :
            be.heldItem.isEmpty() ? AllPartialModels.DEPLOYER_HAND_POINTING : AllPartialModels.DEPLOYER_HAND_HOLDING;
    }

    public static Vec3 getHandOffset(
        DeployerRenderState state,
        DeployerBlockEntity be,
        float partialTicks,
        Direction facing
    ) {
        if (state.offset != null) {
            return state.offset;
        }
        return state.offset = getHandOffset(be, partialTicks, facing);
    }

    public static Vec3 getHandOffset(DeployerBlockEntity be, float partialTicks, Direction facing) {
        float distance = getHandOffset(be, partialTicks);
        return Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(distance);
    }

    public static float getHandOffset(DeployerBlockEntity be, float partialTicks) {
        if (be.isVirtual()) {
            return be.animatedOffset.getValue(partialTicks);
        }

        float progress = 0;
        int timerSpeed = be.getTimerSpeed();

        if (be.state == State.EXPANDING) {
            progress = 1 - (be.timer - partialTicks * timerSpeed) / 1000.0f;
            if (be.fistBump) {
                progress *= progress;
            }
        }
        if (be.state == State.RETRACTING) {
            progress = (be.timer - partialTicks * timerSpeed) / 1000.0f;
        }
        float handLength = be.mode == Mode.PUNCH ? 0.1875f : be.heldItem.isEmpty() ? 0 : 0.25f;
        return Math.min(Mth.clamp(progress, 0, 1) * (be.reach + handLength), 1.3125f);
    }

    static PartialModel getHandPose(Mode mode) {
        return mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING : AllPartialModels.DEPLOYER_HAND_POINTING;
    }

    public static class DeployerRenderState extends BlockEntityRenderState {
        public @Nullable Vec3 offset;
        public @Nullable DeployerItemRenderState item;
        public @Nullable FilterRenderState filter;
        public @Nullable ComponentsRenderState components;
    }

    public static abstract class DeployerItemRenderState {
        public Vec3 offset;
        public @Nullable Quaternionf yRot;
        public ItemStackRenderState item;

        public DeployerItemRenderState(
            ItemModelResolver itemModelManager,
            Level world,
            ItemStack heldItem,
            Vec3 offset,
            Direction facing
        ) {
            this.offset = offset;
            yRot = getYRotateAngle(AngleHelper.horizontalAngle(facing) + 180);
            item = new ItemStackRenderState();
            item.displayContext = getDisplayContext();
            itemModelManager.appendItemLayers(item, heldItem, item.displayContext, world, null, 0);
        }

        public static DeployerItemRenderState create(
            DeployerBlockEntity be,
            ItemModelResolver itemModelManager,
            Level world,
            ItemStack heldItem,
            Vec3 offset,
            Direction facing
        ) {
            if (be.mode == Mode.PUNCH) {
                return new DeployerItemPunchRenderState(itemModelManager, world, heldItem, offset, facing);
            }
            if (facing == Direction.UP && be.getSpeed() == 0) {
                return new DeployerItemDisplayRenderState(itemModelManager, world, heldItem, offset, Direction.UP);
            }
            return new DeployerItemUseRenderState(itemModelManager, world, heldItem, offset, facing);
        }

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.pushPose();
            matrices.translate(offset);
            if (yRot != null) {
                matrices.rotate(yRot);
            }
            transform(matrices);
            item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }

        public abstract ItemDisplayContext getDisplayContext();

        protected abstract void transform(PoseStack matrices);
    }

    public static class DeployerItemUseRenderState extends DeployerItemRenderState {
        public @Nullable Quaternionf xRot;
        public boolean isBlockItem;

        public DeployerItemUseRenderState(
            ItemModelResolver itemModelManager,
            Level world,
            ItemStack heldItem,
            Vec3 offset,
            Direction facing
        ) {
            super(itemModelManager, world, heldItem, offset, facing);
            if (facing == Direction.UP) {
                xRot = Axis.XP.rotation(RAD_90);
            } else if (facing == Direction.DOWN) {
                xRot = Axis.XP.rotation(RAD_270);
            }
            isBlockItem = heldItem.getItem() instanceof BlockItem && item.usesBlockLight();
        }

        @Override
        public ItemDisplayContext getDisplayContext() {
            return ItemDisplayContext.FIXED;
        }

        @Override
        protected void transform(PoseStack matrices) {
            if (xRot != null) {
                matrices.rotate(xRot);
            }
            matrices.translate(0, 0, -0.6875f);
            if (isBlockItem) {
                matrices.scale(0.734375f, 0.734375f, 0.734375f);
            } else {
                matrices.scale(0.5f, 0.5f, 0.5f);
            }
        }
    }

    public static class DeployerItemPunchRenderState extends DeployerItemRenderState {
        public @Nullable Quaternionf xRot;
        public boolean isSpears;

        public DeployerItemPunchRenderState(
            ItemModelResolver itemModelManager,
            Level world,
            ItemStack heldItem,
            Vec3 offset,
            Direction facing
        ) {
            super(itemModelManager, world, heldItem, offset, facing);
            isSpears = heldItem.is(ItemTags.SPEARS);
            if (isSpears) {
                if (facing == Direction.UP) {
                    xRot = Axis.XP.rotation(Mth.DEG_TO_RAD * 20);
                } else if (facing == Direction.DOWN) {
                    xRot = Axis.XP.rotation(Mth.DEG_TO_RAD * 200);
                } else {
                    xRot = Axis.XP.rotation(Mth.DEG_TO_RAD * -70);
                }
            } else {
                if (facing == Direction.UP) {
                    xRot = Axis.XP.rotation(RAD_90);
                } else if (facing == Direction.DOWN) {
                    xRot = Axis.XP.rotation(RAD_270);
                }
            }
        }

        @Override
        public ItemDisplayContext getDisplayContext() {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }

        @Override
        protected void transform(PoseStack matrices) {
            if (xRot != null) {
                matrices.rotate(xRot);
            }
            matrices.translate(0, 0.125f, -0.75f);
            if (isSpears) {
                matrices.translate(0, 0.6f, 0.6f);
            }
            matrices.scale(0.75f, 0.75f, 0.75f);
        }
    }

    public static class DeployerItemDisplayRenderState extends DeployerItemRenderState {
        public boolean isBlockItem;
        public Quaternionf yRot2;

        public DeployerItemDisplayRenderState(
            ItemModelResolver itemModelManager,
            Level world,
            ItemStack heldItem,
            Vec3 offset,
            Direction facing
        ) {
            super(itemModelManager, world, heldItem, offset, facing);
            isBlockItem = heldItem.getItem() instanceof BlockItem && item.usesBlockLight();
            yRot2 = Axis.YP.rotation(Mth.DEG_TO_RAD * AnimationTickHolder.getRenderTime(world));
        }

        @Override
        public ItemDisplayContext getDisplayContext() {
            return ItemDisplayContext.GROUND;
        }

        @Override
        protected void transform(PoseStack matrices) {
            if (isBlockItem) {
                matrices.translate(0, 0.5625f, 0);
                matrices.scale(1.25f, 1.25f, 1.25f);
            } else {
                matrices.translate(0, 0.6875f, 0);
            }
            matrices.rotate(yRot2);
        }
    }

    public static class ComponentsRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @UnknownNullability SuperByteBufferRenderState pole;
        public @UnknownNullability SuperByteBufferRenderState hand;
        public @Nullable Quaternionf angle;
        public @UnknownNullability Vec3 offset;
        public @Nullable Quaternionf yRot;
        public @Nullable Quaternionf xRot;
        public @Nullable Quaternionf zRot;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            if (angle != null) {
                matrices.pushPose();
                matrices.rotateAround(angle, 0.5f, 0.5f, 0.5f);
                shaft.submit(matrices, queue);
                matrices.popPose();
            } else {
                shaft.submit(matrices, queue);
            }
            matrices.translate(offset);
            if (yRot != null) {
                matrices.rotateAround(yRot, 0.5f, 0.5f, 0.5f);
            }
            if (xRot != null) {
                matrices.rotateAround(xRot, 0.5f, 0.5f, 0.5f);
            }
            hand.submit(matrices, queue);
            if (zRot != null) {
                matrices.rotateAround(zRot, 0.5f, 0.5f, 0.5f);
            }
            pole.submit(matrices, queue);
        }
    }
}
