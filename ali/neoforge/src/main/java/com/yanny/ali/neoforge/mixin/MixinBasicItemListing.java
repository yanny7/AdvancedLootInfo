package com.yanny.ali.neoforge.mixin;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BasicItemListing.class)
public interface MixinBasicItemListing {
    @Accessor
    ItemStack getPrice();

    @Accessor
    ItemStack getPrice2();

    @Accessor
    ItemStack getForSale();

    @Accessor
    int getMaxTrades();

    @Accessor
    int getXp();

    @Accessor
    float getPriceMult();
}
