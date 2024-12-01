package samebutdifferent.ecologics.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import samebutdifferent.ecologics.Ecologics;

public class ModPotions {
    public static void init() {}

    public static final Holder<Potion> SLIDING = Holder.direct(Registry.register(BuiltInRegistries.POTION, Ecologics.MOD_ID + ":sliding", new Potion(new MobEffectInstance(ModMobEffects.SLIPPERY, 3600))));  //CommonPlatformHelper.registerPotion("sliding", () -> Holder.direct(new Potion(new MobEffectInstance(ModMobEffects.SLIPPERY, 3600))));
    public static final Holder<Potion> LONG_SLIDING = Holder.direct(Registry.register(BuiltInRegistries.POTION, Ecologics.MOD_ID + ":long_sliding", new Potion(new MobEffectInstance(ModMobEffects.SLIPPERY, 3600))));  // CommonPlatformHelper.registerPotion("long_sliding", () -> Holder.direct(new Potion("sliding", new MobEffectInstance(ModMobEffects.SLIPPERY, 9600))));
}
