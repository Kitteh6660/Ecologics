package samebutdifferent.ecologics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import samebutdifferent.ecologics.registry.ModBlocks;
import samebutdifferent.ecologics.registry.ModEntityTypes;
import samebutdifferent.ecologics.registry.ModSoundEvents;

public class ThinIceBlock extends IceBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    
    public ThinIceBlock() {
        super(Properties.of().friction(0.98F).strength(0.5F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, blockGetter, pos, entityType) -> entityType.equals(EntityType.POLAR_BEAR) || entityType.equals(ModEntityTypes.PENGUIN)));
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
    	Holder<Enchantment> ffholder = pLevel.registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.FEATHER_FALLING);
        if (pEntity instanceof Player player && EnchantmentHelper.getEnchantmentLevel(ffholder, player) == 0) {
            pLevel.playSound(null, pPos, ModSoundEvents.THIN_ICE_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + pLevel.random.nextFloat() * 0.2F);
            replaceIfThinIce(pPos, 3, pLevel);
            replaceIfThinIce(pPos.north(), 2, pLevel);
            replaceIfThinIce(pPos.east(), 2, pLevel);
            replaceIfThinIce(pPos.south(), 2, pLevel);
            replaceIfThinIce(pPos.west(), 2, pLevel);
            replaceIfThinIce(pPos.north().west(), 1, pLevel);
            replaceIfThinIce(pPos.north().east(), 1, pLevel);
            replaceIfThinIce(pPos.south().west(), 1, pLevel);
            replaceIfThinIce(pPos.south().east(), 1, pLevel);
        }
    }

    private void replaceIfThinIce(BlockPos pPos, int age, Level pLevel) {
        BlockState state = pLevel.getBlockState(pPos);
        if (state.is(ModBlocks.THIN_ICE)) {
            pLevel.setBlock(pPos, ModBlocks.THIN_ICE.defaultBlockState().setValue(AGE, Math.min(state.getValue(AGE) + age, 3)), 2);
        }
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (pEntity instanceof Player && pState.getValue(AGE) > 0) {
            this.crack(pState, pLevel, pPos);
        }
    }

    private boolean crack(BlockState pState, Level pLevel, BlockPos pPos) {
        int age = pState.getValue(AGE);
        if (age < 3) {
            pLevel.setBlock(pPos, pState.setValue(AGE, age + 1), 2);
            pLevel.playSound(null, pPos, ModSoundEvents.THIN_ICE_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + pLevel.random.nextFloat() * 0.2F);
            return false;
        } else {
            pLevel.removeBlock(pPos, false);
            if (!pLevel.dimensionType().ultraWarm()) {
            	//TODO: Replace this check.
                //Material material = pLevel.getBlockState(pPos.below()).getMaterial();
                if (pState.blocksMotion() || pState.getFluidState().getType() != Fluids.EMPTY) {
                    pLevel.setBlockAndUpdate(pPos, Blocks.WATER.defaultBlockState());
                }
            }
            pLevel.playSound(null, pPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(AGE);
    }
}
