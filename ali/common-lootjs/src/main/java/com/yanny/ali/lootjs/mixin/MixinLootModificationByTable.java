package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.LootModificationByTable;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootModificationByTable.class)
public interface MixinLootModificationByTable {
    @Accessor
    ResourceLocationFilter[] getFilters();
}
