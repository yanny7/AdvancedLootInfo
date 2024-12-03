package com.yanny.emi_loot_addon.network.function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SetInstrumentFunction extends LootConditionalFunction {
    public SetInstrumentFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
    }

    public SetInstrumentFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }
}
