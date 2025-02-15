package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinLootItemConditionalFunction;
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
