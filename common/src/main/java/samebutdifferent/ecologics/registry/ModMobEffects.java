package samebutdifferent.ecologics.registry;

import java.util.ArrayList;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import oshi.util.tuples.Pair;
import samebutdifferent.ecologics.Ecologics;
import samebutdifferent.ecologics.effect.SlipperyMobEffect;

public class ModMobEffects 
{
    public static void init() {
    	for (Pair<ResourceLocation, MobEffect> registry : MOB_EFFECTS) {
    		Registry.register(BuiltInRegistries.MOB_EFFECT, registry.getA(), registry.getB());
    	}
    }
    
    public static MobEffect registerMobEffect(String name, MobEffect effect) {
    	MOB_EFFECTS.add(new Pair(ResourceLocation.fromNamespaceAndPath(Ecologics.MOD_ID, name), effect));
    	return effect;
    }

    public static final ArrayList<Pair<ResourceLocation, MobEffect>> MOB_EFFECTS = new ArrayList();
    
    public static final Holder<MobEffect> SLIPPERY = Holder.direct(registerMobEffect("slippery", new SlipperyMobEffect()));
}
