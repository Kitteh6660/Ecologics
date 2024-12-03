package samebutdifferent.ecologics.neoforge;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import samebutdifferent.ecologics.Ecologics;
import samebutdifferent.ecologics.block.FloweringAzaleaLogBlock;
import samebutdifferent.ecologics.block.PotBlock;
import samebutdifferent.ecologics.platform.neoforge.CommonPlatformHelperImpl;
import samebutdifferent.ecologics.registry.ModBlockEntityTypes;
import samebutdifferent.ecologics.registry.ModBlocks;
import samebutdifferent.ecologics.registry.ModEntityTypes;
import samebutdifferent.ecologics.registry.ModFeatures;
import samebutdifferent.ecologics.registry.ModFoliagePlacerTypes;
import samebutdifferent.ecologics.registry.ModItems;
import samebutdifferent.ecologics.registry.ModMobEffects;
import samebutdifferent.ecologics.registry.ModPotions;
import samebutdifferent.ecologics.registry.ModSoundEvents;
import samebutdifferent.ecologics.registry.ModTrunkPlacerTypes;
import samebutdifferent.ecologics.registry.neoforge.ModConfigNeoForge;
import samebutdifferent.ecologics.registry.neoforge.ModGlobalLootModifiers;

import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.item.CreativeModeTabs;

@Mod(Ecologics.MOD_ID)
@EventBusSubscriber(modid = Ecologics.MOD_ID)
public class EcologicsNeoForge 
{
	private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "tab"));
		
    public EcologicsNeoForge(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ModConfigNeoForge.COMMON_CONFIG);

        //CommonPlatformHelperImpl.BLOCKS.register(bus);
        //CommonPlatformHelperImpl.ITEMS.register(bus);
        //CommonPlatformHelperImpl.SOUND_EVENTS.register(bus);
        //CommonPlatformHelperImpl.ENTITY_TYPES.register(bus);
        //CommonPlatformHelperImpl.BLOCK_ENTITY_TYPES.register(bus);
        //CommonPlatformHelperImpl.FEATURES.register(bus);
        //CommonPlatformHelperImpl.TRUNK_PLACER_TYPES.register(bus);
        //CommonPlatformHelperImpl.FOLIAGE_PLACER_TYPES.register(bus);
        //CommonPlatformHelperImpl.MOB_EFFECTS.register(bus);
        //CommonPlatformHelperImpl.POTIONS.register(bus);
        ModGlobalLootModifiers.GLM.register(bus);

        bus.addListener(this::registerEntityAttributes);
        bus.addListener(this::registerModContent);
        bus.addListener(this::setup);
        bus.addListener(this::assignItemsToTab);
    }

    public void registerEntityAttributes(EntityAttributeCreationEvent event) {
        Map<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> attributes = new HashMap<>();
        Ecologics.registerEntityAttributes(attributes);
        attributes.forEach((entity, builder) -> event.put(entity, builder.build()));
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Ecologics.commonSetup();
        });
    }

    private void registerModContent(RegisterEvent event) {
    	event.register(Registries.SOUND_EVENT, helper -> { ModSoundEvents.init(); });
    	event.register(Registries.BLOCK, helper -> { ModBlocks.init(); });
    	event.register(Registries.ITEM, helper -> { ModItems.init(); });
    	event.register(Registries.ENTITY_TYPE, helper -> { ModEntityTypes.init(); });
    	event.register(Registries.BLOCK_ENTITY_TYPE, helper -> { ModBlockEntityTypes.init(); });
    	event.register(Registries.FEATURE, helper -> { ModFeatures.init(); });
    	event.register(Registries.TRUNK_PLACER_TYPE, helper -> { ModTrunkPlacerTypes.init(); });
    	event.register(Registries.FOLIAGE_PLACER_TYPE, helper -> { ModFoliagePlacerTypes.init(); });
    	event.register(Registries.MOB_EFFECT, helper -> { ModMobEffects.init(); });
    	event.register(Registries.POTION, helper -> { ModPotions.init(); });
    	event.register(Registries.CREATIVE_MODE_TAB, helper -> {
    		helper.register(TAB, CreativeModeTab.builder().title(Component.translatable("itemGroup.ecologics.tab")).withTabsBefore(CreativeModeTabs.SPAWN_EGGS).icon(() -> { return new ItemStack(ModBlocks.COCONUT_LOG); }).build());
    	});
    }

    private void assignItemsToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TAB) {
	        event.accept(ModBlocks.COCONUT_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.STRIPPED_COCONUT_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.STRIPPED_COCONUT_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_PLANKS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_FENCE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_FENCE_GATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_DOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_TRAPDOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_BUTTON, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_PRESSURE_PLATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModBlocks.WALNUT_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.STRIPPED_WALNUT_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.STRIPPED_WALNUT_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_PLANKS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_FENCE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_FENCE_GATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_DOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_TRAPDOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_BUTTON, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_PRESSURE_PLATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModBlocks.AZALEA_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.STRIPPED_AZALEA_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.STRIPPED_AZALEA_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_PLANKS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_FENCE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_FENCE_GATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_DOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_TRAPDOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_BUTTON, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.AZALEA_PRESSURE_PLATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModBlocks.FLOWERING_AZALEA_LOG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_WOOD, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_PLANKS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_FENCE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_FENCE_GATE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_DOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.FLOWERING_AZALEA_TRAPDOOR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModItems.COCONUT_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.COCONUT_HANGING_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.WALNUT_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.WALNUT_HANGING_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.AZALEA_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.AZALEA_HANGING_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.FLOWERING_AZALEA_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.FLOWERING_AZALEA_HANGING_SIGN, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

	        event.accept(ModItems.COCONUT_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.COCONUT_CHEST_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.WALNUT_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.WALNUT_CHEST_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.AZALEA_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.AZALEA_CHEST_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.FLOWERING_AZALEA_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.FLOWERING_AZALEA_CHEST_BOAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModBlocks.COCONUT_LEAVES, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_LEAVES, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.COCONUT_SEEDLING, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.WALNUT_SAPLING, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModBlocks.COCONUT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SEASHELL, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SEASHELL_BLOCK, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SEASHELL_TILES, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SEASHELL_TILE_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SEASHELL_TILE_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SEASHELL_TILE_WALL, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.POT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.THIN_ICE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.ICE_BRICKS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.ICE_BRICK_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.ICE_BRICK_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.ICE_BRICK_WALL, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SNOW_BRICKS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SNOW_BRICK_STAIRS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SNOW_BRICK_SLAB, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SNOW_BRICK_WALL, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	
	        event.accept(ModBlocks.AZALEA_FLOWER, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModBlocks.SURFACE_MOSS, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.COCONUT_SLICE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.COCONUT_HUSK, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.CRAB_CLAW, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.CRAB_MEAT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.TROPICAL_STEW, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.COCONUT_CRAB_SPAWN_EGG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.PENGUIN_SPAWN_EGG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.SQUIRREL_SPAWN_EGG, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.SANDCASTLE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.MUSIC_DISC_COCONUT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.PRICKLY_PEAR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.COOKED_PRICKLY_PEAR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.PENGUIN_FEATHER, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	        event.accept(ModItems.WALNUT, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

    }

    @SubscribeEvent
    public static void onCropGrow(CropGrowEvent.Post event) {
        BlockPos pos = event.getPos();
        LevelAccessor level = event.getLevel();
        BlockState state = event.getState();
        if (state.is(Blocks.CACTUS)) {
            if (level.getBlockState(pos.above()).is(Blocks.CACTUS) && level.getBlockState(pos.below()).is(Blocks.CACTUS)) {
                if (level.isEmptyBlock(pos.above(2)) && level.getRandom().nextFloat() <= ModConfigNeoForge.PRICKLY_PEAR_GROWTH_CHANCE.get()) {
                    level.setBlock(pos.above(2), ModBlocks.PRICKLY_PEAR.defaultBlockState(), 2);
                    level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        InteractionHand hand = event.getHand();
        if (state.is(ModBlocks.POT) && player.isCrouching()) {
            if (player.getMainHandItem().getItem() instanceof PickaxeItem && hand.equals(InteractionHand.MAIN_HAND)){
                level.setBlockAndUpdate(pos, state.cycle(PotBlock.CHISEL));
                level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                player.swing(InteractionHand.MAIN_HAND);
                player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
            if (player.getOffhandItem().getItem() instanceof PickaxeItem && !(player.getMainHandItem().getItem() instanceof PickaxeItem) && hand.equals(InteractionHand.OFF_HAND)){
                level.setBlockAndUpdate(pos, state.cycle(PotBlock.CHISEL));
                level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                player.swing(InteractionHand.OFF_HAND);
                player.getOffhandItem().hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
            }
        }
        if (!event.getLevel().isClientSide()) {
            ItemStack stack = event.getItemStack();
            Direction direction = event.getHitVec().getDirection().getAxis() == Direction.Axis.Y ? event.getHitVec().getDirection().getOpposite() : event.getHitVec().getDirection();
            if (stack.is(Items.SHEARS)) {
                if (state.is(Blocks.FLOWERING_AZALEA)) {
                    FloweringAzaleaLogBlock.shearAzalea(level, player, pos, stack, hand, direction, Blocks.AZALEA.defaultBlockState());
                    player.swing(hand, true);
                }
                if (state.is(Blocks.FLOWERING_AZALEA_LEAVES)) {
                    FloweringAzaleaLogBlock.shearAzalea(level, player, pos, stack, hand, direction, Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, state.getValue(LeavesBlock.PERSISTENT)).setValue(LeavesBlock.DISTANCE, state.getValue(LeavesBlock.DISTANCE)));
                    player.swing(hand, true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        // builder.addMix(Potions.AWKWARD, ModItems.PENGUIN_FEATHER, ModPotions.SLIDING);
        // builder.addMix(ModPotions.SLIDING, Items.REDSTONE, ModPotions.LONG_SLIDING);
    }
    
    /*@SubscribeEvent
    public static void onMissingBlockMappings(MissingMappingsEvent event) {
        for (var mapping : event.getAllMappings(BuiltInRegistries.BLOCK.getRegistryKey())) {
            if (mapping.getKey().equals(ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "coconut_husk"))) {
                ResourceLocation remapped = ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "coconut_seedling");
                if (BuiltInRegistries.BLOCK.containsKey(remapped)) {
                    mapping.remap(BuiltInRegistries.BLOCK.get(remapped));
                } else {
                    mapping.warn();
                }
            }
            if (mapping.getKey().equals(ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "potted_coconut_husk"))) {
                ResourceLocation remapped = ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "potted_coconut_seedling");
                if (BuiltInRegistries.BLOCK.containsKey(remapped)) {
                    mapping.remap(BuiltInRegistries.BLOCK.get(remapped));
                } else {
                    mapping.warn();
                }
            }
        }
    }*/
}