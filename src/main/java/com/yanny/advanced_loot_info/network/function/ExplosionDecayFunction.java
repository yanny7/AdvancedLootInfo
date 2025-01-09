package com.yanny.advanced_loot_info.network.function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class ExplosionDecayFunction extends LootConditionalFunction {
    public ExplosionDecayFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
    }

    public ExplosionDecayFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }
}
