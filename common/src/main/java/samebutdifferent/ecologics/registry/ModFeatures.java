package samebutdifferent.ecologics.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import samebutdifferent.ecologics.Ecologics;
import samebutdifferent.ecologics.platform.CommonPlatformHelper;
import samebutdifferent.ecologics.worldgen.feature.CoastalFeature;
import samebutdifferent.ecologics.worldgen.feature.DesertRuinFeature;
import samebutdifferent.ecologics.worldgen.feature.ThinIceFeature;

public class ModFeatures 
{
    public static void init() {}

    public static final Supplier<CoastalFeature> COASTAL = CommonPlatformHelper.registerFeature("coastal", () -> new CoastalFeature(SimpleBlockConfiguration.CODEC));
    public static final Supplier<ThinIceFeature> THIN_ICE = CommonPlatformHelper.registerFeature("thin_ice", () -> new ThinIceFeature(DiskConfiguration.CODEC));
    public static final Supplier<DesertRuinFeature> DESERT_RUIN = CommonPlatformHelper.registerFeature("desert_ruin", () -> new DesertRuinFeature(NoneFeatureConfiguration.CODEC));
    
    public static final Supplier<TreeFeature> AZALEA_TREE = CommonPlatformHelper.registerFeature("azalea_tree", () -> new TreeFeature(TreeConfiguration.CODEC));
    public static final Supplier<TreeFeature> COCONUT_TREE = CommonPlatformHelper.registerFeature("coconut_tree", () -> new TreeFeature(TreeConfiguration.CODEC));
    public static final Supplier<TreeFeature> WALNUT_TREE = CommonPlatformHelper.registerFeature("walnut_tree", () -> new TreeFeature(TreeConfiguration.CODEC));
    
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROOTED_AZALEA_TREE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "rooted_azalea_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> COCONUT_REGULAR = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "coconut"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> WALNUT_REGULAR = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, "walnut"));
}
