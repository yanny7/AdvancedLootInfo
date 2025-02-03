package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.mixin.MixinLootItemConditionalFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public abstract class LootConditionalFunction implements ILootFunction {
    public final List<ILootCondition> conditions;

    public LootConditionalFunction(IContext context, LootItemFunction function) {
        conditions = context.utils().convertConditions(context, ((MixinLootItemConditionalFunction) function).getPredicates());
    }

    public LootConditionalFunction(IContext context, FriendlyByteBuf buf) {
        conditions = context.utils().decodeConditions(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeConditions(context, buf, conditions);
    }
}
