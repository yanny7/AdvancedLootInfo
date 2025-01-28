package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.mixin.MixinLootItemConditionalFunction;
import com.yanny.advanced_loot_info.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public abstract class LootConditionalFunction implements ILootFunction {
    public final List<ILootCondition> conditions;

    public LootConditionalFunction(LootContext context, LootItemFunction function) {
        conditions = LootUtils.ofCondition(context, ((MixinLootItemConditionalFunction) function).getPredicates());
    }

    public LootConditionalFunction(FriendlyByteBuf buf) {
        conditions = LootUtils.decodeCondition(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        LootUtils.encodeCondition(buf, conditions);
    }
}
