package com.yanny.ali.mixin;

import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charmony.helper.GenericTradeOffers;

@Mixin(GenericTradeOffers.ItemsForItems.class)
public interface MixinItemsForItems {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseInput();

    @Accessor
    int getExtraInput();

    @Accessor
    int getBaseOutput();

    @Accessor
    int getExtraOutput();

    @Accessor
    int getMaxUses();

    @Accessor
    ItemLike getInputItem();

    @Accessor
    ItemLike getOutputItem();
}
