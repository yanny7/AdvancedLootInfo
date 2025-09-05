package com.yanny.ali.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charmony.helper.GenericTradeOffers;

@Mixin(GenericTradeOffers.TagForEmeralds.class)
public interface MixinTagForEmeralds<T extends ItemLike> {
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
    TagKey<T> getTag();
}
