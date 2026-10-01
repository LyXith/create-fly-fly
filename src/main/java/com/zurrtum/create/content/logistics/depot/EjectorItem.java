package com.zurrtum.create.content.logistics.depot;

import net.minecraft.world.item.context.BlockPlaceContext;
import com.zurrtum.create.infrastructure.packet.s2c.EjectorPlacementRequestPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class EjectorItem extends BlockItem {

    public EjectorItem(Block p_i48527_1_, Properties p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(ctx);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!super.placeBlock(context, state)) {
            return false;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        onBlockPlaced(pos, level, context.getPlayer(), context.getItemInHand(), level.getBlockState(pos));
        return true;
    }

    protected boolean onBlockPlaced(
        BlockPos pos,
        Level world,
        @Nullable Player player,
        ItemStack p_195943_4_,
        BlockState p_195943_5_
    ) {
        if (!world.isClientSide() && player instanceof ServerPlayer sp) {
            sp.connection.send(new EjectorPlacementRequestPacket(pos));
        }
        return BlockItem.updateCustomBlockEntityTag(world, player, pos, p_195943_4_);
    }

    @Override
    public boolean canDestroyBlock(
        ItemStack stack,
        BlockState state,
        Level world,
        BlockPos pos,
        LivingEntity p_195938_4_
    ) {
        return !p_195938_4_.isShiftKeyDown();
    }

}
