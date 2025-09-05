package com.yanny.ali.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

@Mixin(BeekeeperTradeOffers.EmeraldsForFlowers.class)
public interface MixinEmeraldForFlowers {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseCost();

    @Accessor
    int getExtraCost();

    @Accessor
    int getBaseEmeralds();

    @Accessor
    int getExtraEmeralds();

    @Accessor
    int getMaxUses();
}
