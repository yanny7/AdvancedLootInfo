package com.yanny.ali.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

@Mixin(BeekeeperTradeOffers.EnchantedShearsForEmeralds.class)
public interface MixinEnchantedShearsForEmeralds {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseEmeralds();

    @Accessor
    int getExtraEmeralds();

    @Accessor
    int getMaxUses();
}
