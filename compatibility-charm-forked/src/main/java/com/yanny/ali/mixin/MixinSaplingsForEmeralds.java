package com.yanny.ali.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;

import java.util.List;

@Mixin(LumberjackTradeOffers.SaplingsForEmeralds.class)
public interface MixinSaplingsForEmeralds {
    @Accessor
    int getVillagerXp();

    @Accessor
    int getBaseEmeralds();

    @Accessor
    int getExtraEmeralds();

    @Accessor
    int getMaxUses();

    @Accessor
    List<Item> getSaplings();
}
