package samebutdifferent.ecologics.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import samebutdifferent.ecologics.block.entity.ModSignBlockEntity;
import samebutdifferent.ecologics.block.entity.PotBlockEntity;
import samebutdifferent.ecologics.platform.CommonPlatformHelper;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final Supplier<BlockEntityType<ModSignBlockEntity>> SIGN = CommonPlatformHelper.registerBlockEntityType("sign", () -> CommonPlatformHelper.createBlockEntityType(ModSignBlockEntity::new,
            ModBlocks.COCONUT_SIGN.get(), ModBlocks.COCONUT_WALL_SIGN.get(),
            ModBlocks.WALNUT_SIGN.get(), ModBlocks.WALNUT_WALL_SIGN.get(),
            ModBlocks.AZALEA_SIGN.get(), ModBlocks.AZALEA_WALL_SIGN.get(),
            ModBlocks.FLOWERING_AZALEA_SIGN.get(), ModBlocks.FLOWERING_AZALEA_WALL_SIGN.get()
    ));
    public static final Supplier<BlockEntityType<PotBlockEntity>> POT = CommonPlatformHelper.registerBlockEntityType("pot", () -> CommonPlatformHelper.createBlockEntityType(PotBlockEntity::new, ModBlocks.POT.get()));
}
