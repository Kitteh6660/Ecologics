package samebutdifferent.ecologics.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import samebutdifferent.ecologics.effect.SlipperyMobEffect;
import samebutdifferent.ecologics.platform.CommonPlatformHelper;

public class ModMobEffects {
    public static void init() {}

    public static final Holder<MobEffect> SLIPPERY = Holder.direct(CommonPlatformHelper.registerMobEffect("slippery", () -> new SlipperyMobEffect()).get());
}
