package samebutdifferent.ecologics.platform;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

//TODO: Determine if this should be removed and replaced.
public class CommonPlatformHelper 
{
    /*@ExpectPlatform
    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Item, M extends Mob> Supplier<T> registerSpawnEggItem(String name, Supplier<EntityType<M>> entityType, int backgroundColor, int highlightColor) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends SoundEvent> Supplier<T> registerSoundEvent(String name, Supplier<T> soundEvent) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Entity> Supplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> registerBlockEntityType(String name, Supplier<T> blockEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        @NotNull T create(BlockPos pos, BlockState state);
    }

    @ExpectPlatform
    public static <T extends Potion> Supplier<T> registerPotion(String name, Supplier<T> potion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerBrewingRecipe(Holder<Potion> awkward, Item ingredient, Holder<Potion> output) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends FoliagePlacer> Supplier<FoliagePlacerType<T>> registerFoliagePlacerType(String name, Supplier<FoliagePlacerType<T>> foliagePlacerType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends TrunkPlacer> Supplier<TrunkPlacerType<T>> registerTrunkPlacerType(String name, Supplier<TrunkPlacerType<T>> trunkPlacerType) {
        throw new AssertionError();
    }*/

    public static <T extends Block> void setFlammable(Block block, int encouragement, int flammability) {
        setFlammable(Blocks.FIRE, block, encouragement, flammability);
    }

    @ExpectPlatform
    public static void setFlammable(Block fireBlock, Block block, int encouragement, int flammability) {
        throw new AssertionError();
    }

    /*@ExpectPlatform
    public static <T extends MobEffect> Supplier<T> registerMobEffect(String name, Supplier<T> mobEffect) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Feature<?>> Supplier<T> registerFeature(String name, Supplier<T> feature) {
        throw new AssertionError();
    }*/

    @ExpectPlatform
    public static <T extends Mob> void registerSpawnPlacement(EntityType<T> entityType, SpawnPlacementType decoratorType, Heightmap.Types heightMapType, SpawnPlacements.SpawnPredicate<T> decoratorPredicate) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static WoodType createWoodType(String name, BlockSetType setType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static WoodType registerWoodType(WoodType woodType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerCompostable(float chance, ItemLike item) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerStrippables(Map<Block, Block> blockMap) {
        throw new AssertionError();
    }

    //TODO: Remove this.
    /*@ExpectPlatform
    public static Supplier<JukeboxPlayable> registerRecordItem(String name, int comparatorValue, Supplier<SoundEvent> soundSupplier, Item.Properties properties) {
        throw new AssertionError();
    }*/
}
