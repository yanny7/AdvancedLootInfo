package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.modifier.handler.RemoveLootAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RemoveLootAction.class)
public interface MixinRemoveLootAction {
    @Accessor
    ItemFilter getFilter();
}
