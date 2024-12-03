package com.yanny.emi_loot_addon.network.function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class CopyNameFunction extends LootConditionalFunction {
    public CopyNameFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
    }

    public CopyNameFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }
}
