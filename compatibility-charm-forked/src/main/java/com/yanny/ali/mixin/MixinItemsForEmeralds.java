package com.yanny.ali.mixin;

import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charmony.helper.GenericTradeOffers;

@Mixin(GenericTradeOffers.ItemsForEmeralds.class)
public interface MixinItemsForEmeralds {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseItems();

    @Accessor
    int getExtraItems();

    @Accessor
    int getBaseEmeralds();

    @Accessor
    int getExtraEmeralds();

    @Accessor
    int getMaxUses();

    @Accessor
    ItemLike getItemLike();
}
