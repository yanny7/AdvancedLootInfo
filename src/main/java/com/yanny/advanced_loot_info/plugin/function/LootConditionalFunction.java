package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.mixin.MixinLootItemConditionalFunction;
import com.yanny.advanced_loot_info.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public abstract class LootConditionalFunction implements ILootFunction {
    public final List<LootCondition> conditions;

    public LootConditionalFunction(LootContext context, LootItemFunction function) {
        conditions = LootCondition.of(context, ((MixinLootItemConditionalFunction) function).getPredicates());
    }

    public LootConditionalFunction(FriendlyByteBuf buf) {
        conditions = LootCondition.decode(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        LootCondition.encode(buf, conditions);
    }
}
