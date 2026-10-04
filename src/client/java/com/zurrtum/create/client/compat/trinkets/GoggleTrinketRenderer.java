package com.zurrtum.create.client.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GoggleTrinketRenderer implements TrinketRenderer {
    public static void register() {
        GogglesItem.addIsWearingPredicate(player -> TrinketsApi.getAttachment(player).isEquipped(AllItems.GOGGLES));
        TrinketRendererRegistry.registerRenderer(AllItems.GOGGLES, new GoggleTrinketRenderer());
    }

    @Override
    public void submit(
        ItemStack stack,
        TrinketSlotAccess slotReference,
        EntityModel<? extends LivingEntityRenderState> contextModel,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        LivingEntityRenderState state,
        float limbAngle,
        float limbDistance
    ) {
        if (stack.is(AllItems.GOGGLES) && contextModel instanceof PlayerModel entityModel) {
            matrices.pushPose();
            entityModel.root().translateAndRotate(matrices);
            entityModel.getHead().translateAndRotate(matrices);
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.rotate(Axis.YP.rotationDegrees(180.0F));
            matrices.scale(0.625F, -0.625F, -0.625F);
            if (!((AvatarRenderState) state).headEquipment.isEmpty()) {
                matrices.rotate(Axis.ZP.rotationDegrees(180.0F));
                matrices.translate(0.0F, -0.25F, 0.0F);
            }
            ItemStackRenderState item = new ItemStackRenderState();
            item.displayContext = ItemDisplayContext.HEAD;
            Minecraft mc = Minecraft.getInstance();
            mc.getItemModelResolver().appendItemLayers(item, stack, item.displayContext, mc.level, null, 0);
            item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }
}
