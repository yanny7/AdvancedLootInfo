package com.yanny.ali.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

@Mixin(BeekeeperTradeOffers.TallFlowerForEmeralds.class)
public interface MixinTallFlowerForEmeralds {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseEmeralds();

    @Accessor
    int getExtraEmeralds();

    @Accessor
    int getMaxUses();
}
