package com.zurrtum.create.content.logistics.packagePort;

import net.minecraft.world.item.context.BlockPlaceContext;
import com.zurrtum.create.infrastructure.packet.s2c.PackagePortPlacementRequestPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class PackagePortItem extends BlockItem {

    public PackagePortItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
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
            sp.connection.send(new PackagePortPlacementRequestPacket(pos));
        }
        return BlockItem.updateCustomBlockEntityTag(world, player, pos, p_195943_4_);
    }

}
