package com.yanny.ali.lootjs.modifier;

import com.mojang.serialization.MapCodec;
import com.yanny.ali.plugin.server.LootFunctionTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public abstract class BaseLootItemFunction implements LootItemFunction {
    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return itemStack;
    }

    @NotNull
    @Override
    public MapCodec<? extends LootItemFunction> codec() {
        return LootFunctionTypes.UNUSED;
    }
}
