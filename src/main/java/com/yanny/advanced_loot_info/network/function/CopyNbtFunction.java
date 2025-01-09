package com.yanny.advanced_loot_info.network.function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class CopyNbtFunction extends LootConditionalFunction {
    public CopyNbtFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
    }

    public CopyNbtFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }
}
