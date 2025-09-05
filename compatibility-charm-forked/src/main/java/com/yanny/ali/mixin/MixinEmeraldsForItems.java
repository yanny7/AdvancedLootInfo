package com.yanny.ali.mixin;

import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charmony.helper.GenericTradeOffers;

@Mixin(GenericTradeOffers.EmeraldsForItems.class)
public interface MixinEmeraldsForItems {
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

    @Accessor
    ItemLike getItemLike();
}
