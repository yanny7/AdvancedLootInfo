package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.entry.ItemLootEntry;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.modifier.handler.ReplaceLootAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReplaceLootAction.class)
public interface MixinReplaceLootAction {
    @Accessor
    ItemFilter getFilter();

    @Accessor
    ItemLootEntry getItemLootEntry();

    @Accessor
    boolean getPreserveCount();
}
