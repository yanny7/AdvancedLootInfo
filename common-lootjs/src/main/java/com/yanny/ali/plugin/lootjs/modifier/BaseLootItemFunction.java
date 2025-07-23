package com.yanny.ali.plugin.lootjs.modifier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;

public abstract class BaseLootItemFunction implements LootItemFunction {
    @NotNull
    @Override
    public LootItemFunctionType<?> getType() {
        return LootFunctionTypes.UNUSED;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return itemStack;
    }
}
