package com.yanny.ali.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;

@Mixin(LumberjackTradeOffers.BarkForLogs.class)
public interface MixinBarkForLogs {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseCost();

    @Accessor
    int getExtraCost();

    @Accessor
    int getMaxUses();
}
