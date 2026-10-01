package com.zurrtum.create.content.kinetics.gearbox;

import net.minecraft.world.item.context.BlockPlaceContext;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.IRotate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class VerticalGearboxItem extends BlockItem {

    public VerticalGearboxItem(Properties settings) {
        super(AllBlocks.GEARBOX, settings);
    }

    @Override
    public void registerBlocks(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
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
        ItemStack stack,
        BlockState state
    ) {
        Axis prefferedAxis = null;
        for (Direction side : Iterate.horizontalDirections) {
            BlockState blockState = world.getBlockState(pos.relative(side));
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(
                    world,
                    pos.relative(side),
                    blockState,
                    side.getOpposite()
                )) {
                    if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                        prefferedAxis = null;
                        break;
                    }
                    prefferedAxis = side.getAxis();
                }
            }
        }

        Axis axis = prefferedAxis == null ? player.getDirection().getClockWise().getAxis() :
            prefferedAxis == Axis.X ? Axis.Z : Axis.X;
        world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.AXIS, axis));
        return BlockItem.updateCustomBlockEntityTag(world, player, pos, stack);
    }

}
