package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.modifier.handler.ModifyLootAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModifyLootAction.class)
public interface MixinModifyLootAction {
    @Accessor
    ItemFilter getPredicate();

    @Accessor
    ModifyLootAction.Callback getCallback();
}
