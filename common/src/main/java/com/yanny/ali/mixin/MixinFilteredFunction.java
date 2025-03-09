package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FilteredFunction.class)
public interface MixinFilteredFunction {
    @Accessor
    ItemPredicate getFilter();

    @Accessor
    LootItemFunction getModifier();
}
