package samebutdifferent.ecologics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class MossLayerBlock extends SnowLayerBlock {
    public MossLayerBlock() {
        super(Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.1F).sound(SoundType.MOSS_CARPET));
        this.registerDefaultState(this.getStateDefinition().any().setValue(LAYERS, 2));
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack item, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if ((pState.getValue(LAYERS) < 8) && ItemStack.isSameItem(Blocks.MOSS_CARPET.asItem().getDefaultInstance(), item) && (pPlayer.getInBlockState() != pState)) {
            if (pState.is(this) && !pLevel.isClientSide()) {
                if (pState.getValue(LAYERS) < 7) {
                    pLevel.setBlockAndUpdate(pPos, this.defaultBlockState().setValue(LAYERS, pState.getValue(LAYERS) + 1));
                } else {
                    pLevel.setBlockAndUpdate(pPos, Blocks.MOSS_BLOCK.defaultBlockState());
                }
                if (!pPlayer.isCreative()) {
                    item.shrink(1);
                }
            }
            pLevel.playSound(pPlayer, pPos, SoundEvents.MOSS_CARPET_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState p_56630_) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
        return !reader.isEmptyBlock(pos.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, world, pos, facingPos);
    }

    @Override
    public void randomTick(BlockState p_56615_, ServerLevel p_56616_, BlockPos p_56617_, RandomSource p_56618_) {

    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        int layers = state.getValue(LAYERS);
        if (useContext.getItemInHand().is(this.asItem()) && layers < 8) {
            return useContext.getClickedFace() == Direction.UP;
        } else {
            return layers == 1;
        }
    }
}
